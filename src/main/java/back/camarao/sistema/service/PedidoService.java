package back.camarao.sistema.service;

import back.camarao.sistema.dto.PedidoDTO;
import back.camarao.sistema.enums.StatusPedido;
import back.camarao.sistema.exception.BusinessRuleException;
import back.camarao.sistema.exception.ResourceNotFoundException;
import back.camarao.sistema.integration.maps.GoogleDistanceMatrixService;
import back.camarao.sistema.model.HorarioFuncionamento;
import back.camarao.sistema.integration.cep.CepService;
import back.camarao.sistema.model.ItemPedido;
import back.camarao.sistema.model.Loja;
import back.camarao.sistema.model.Pedido;
import back.camarao.sistema.model.Produto;
import back.camarao.sistema.model.StatusHistoricoPedido;
import back.camarao.sistema.repository.PedidoRepository;
import back.camarao.sistema.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoService {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Map<StatusPedido, List<StatusPedido>> TRANSICOES_PERMITIDAS = Map.of(
            StatusPedido.RECEBIDO, List.of(StatusPedido.EM_PREPARO, StatusPedido.CANCELADO),
            StatusPedido.EM_PREPARO, List.of(StatusPedido.SAIU_PARA_ENTREGA, StatusPedido.CANCELADO),
            StatusPedido.SAIU_PARA_ENTREGA, List.of(StatusPedido.ENTREGUE, StatusPedido.CANCELADO),
            StatusPedido.ENTREGUE, List.of(),
            StatusPedido.CANCELADO, List.of()
    );

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final LojaService lojaService;
    private final CepService cepService;
    private final GoogleDistanceMatrixService distanceMatrixService;

    public Page<PedidoDTO.Response> listarTodos(Pageable pageable) {
        return pedidoRepository.findAll(pageable).map(PedidoDTO.Response::from);
    }

    public Page<PedidoDTO.Response> listarPorLoja(String lojaId, Pageable pageable) {
        lojaService.encontrarOuLancar(lojaId);
        return pedidoRepository.findByLojaId(lojaId, pageable).map(PedidoDTO.Response::from);
    }

    public Page<PedidoDTO.Response> listarPorStatus(StatusPedido status, Pageable pageable) {
        return pedidoRepository.findByStatus(status, pageable).map(PedidoDTO.Response::from);
    }

    public PedidoDTO.Response buscarPorId(String id) {
        return PedidoDTO.Response.from(encontrarOuLancar(id));
    }

    public PedidoDTO.Response acompanharPorCodigo(String codigo, String telefone) {
        Pedido pedido = pedidoRepository.findByCodigo(normalizarCodigo(codigo))
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", codigo));

        String telefonePedido = somenteDigitos(pedido.getTelefoneCliente());
        String telefoneInformado = somenteDigitos(telefone);

        if (telefoneInformado.isBlank() || !telefonePedido.endsWith(telefoneInformado)) {
            throw new BusinessRuleException("Telefone nao confere com o pedido informado");
        }

        return PedidoDTO.Response.from(pedido);
    }

    public PedidoDTO.Response criar(PedidoDTO.Request dto) {
        Loja loja = lojaService.encontrarOuLancar(dto.lojaId());
        if (!lojaEstaAberta(loja)) {
            throw new BusinessRuleException("A loja esta fechada no momento e nao pode receber pedidos");
        }

        Map<String, Produto> produtosPorId = produtoRepository.findAllById(
                        dto.itens().stream().map(PedidoDTO.ItemRequest::produtoId).toList())
                .stream()
                .collect(Collectors.toMap(Produto::getId, Function.identity()));

        List<ItemPedido> itens = dto.itens().stream()
                .map(item -> criarItem(item, produtosPorId))
                .toList();

        BigDecimal subtotal = itens.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        EnderecoPedido enderecoPedido = resolverEnderecoPedido(dto);
        BigDecimal taxaServico = valorOuZero(loja.getTaxaServico());
        BigDecimal taxaEntrega = calcularTaxaEntrega(loja, enderecoPedido);
        BigDecimal total = subtotal.add(taxaServico).add(taxaEntrega);
        Instant agora = Instant.now();

        Pedido pedido = Pedido.builder()
                .lojaId(loja.getId())
                .codigo(gerarCodigoPedido())
                .nomeCliente(dto.nomeCliente().trim())
                .telefoneCliente(dto.telefoneCliente().trim())
                .enderecoEntrega(enderecoPedido.resumo())
                .cepEntrega(enderecoPedido.cep())
                .ruaEntrega(enderecoPedido.rua())
                .numeroEntrega(enderecoPedido.numero())
                .bairroEntrega(enderecoPedido.bairro())
                .complementoEntrega(enderecoPedido.complemento())
                .referenciaEntrega(enderecoPedido.referencia())
                .metodoPagamento(dto.pagamento() == null ? null : dto.pagamento().metodo())
                .trocoPara(dto.pagamento() == null ? null : dto.pagamento().trocoPara())
                .observacao(dto.observacao() == null ? null : dto.observacao().trim())
                .itens(itens)
                .subtotal(subtotal)
                .taxaServico(taxaServico)
                .taxaEntrega(taxaEntrega)
                .total(total)
                .status(StatusPedido.RECEBIDO)
                .statusAtualizadoEm(agora)
                .historicoStatus(new ArrayList<>(List.of(StatusHistoricoPedido.builder()
                        .status(StatusPedido.RECEBIDO)
                        .alteradoEm(agora)
                        .observacao("Pedido criado pelo cliente")
                        .build())))
                .build();

        Pedido salvo = pedidoRepository.save(pedido);
        log.info("Pedido criado: id={}, lojaId={}, total={}", salvo.getId(), salvo.getLojaId(), salvo.getTotal());
        return PedidoDTO.Response.from(salvo);
    }

    public PedidoDTO.CepResponse buscarEnderecoPorCep(String cep) {
        return PedidoDTO.CepResponse.from(cepService.obterEnderecoPorCep(cep));
    }

    public PedidoDTO.FreteResponse calcularFrete(String lojaId, String cep) {
        Loja loja = lojaService.encontrarOuLancar(lojaId);
        String enderecoEntrega = resolverEnderecoEntrega(cep);
        BigDecimal valorPorKm = valorOuZero(loja.getValorEntregaPorKm());
        BigDecimal distanciaKm = distanceMatrixService.obterDistanciaKm(loja.getEndereco(), enderecoEntrega);
        BigDecimal taxaEntrega = distanceMatrixService.calcularFrete(distanciaKm, valorPorKm);

        return new PedidoDTO.FreteResponse(cep, enderecoEntrega, distanciaKm, valorPorKm, taxaEntrega);
    }

    public PedidoDTO.Response alterarStatus(String id, StatusPedido status) {
        return alterarStatus(id, status, null);
    }

    public PedidoDTO.Response alterarStatus(String id, StatusPedido status, String observacao) {
        Pedido pedido = encontrarOuLancar(id);
        validarTransicaoStatus(pedido.getStatus(), status);

        if (pedido.getStatus() == status) {
            return PedidoDTO.Response.from(pedido);
        }

        Instant agora = Instant.now();
        pedido.setStatus(status);
        pedido.setStatusAtualizadoEm(agora);

        List<StatusHistoricoPedido> historico = pedido.getHistoricoStatus() == null
                ? new ArrayList<>()
                : new ArrayList<>(pedido.getHistoricoStatus());
        historico.add(StatusHistoricoPedido.builder()
                .status(status)
                .alteradoEm(agora)
                .observacao(textoOuNulo(observacao))
                .build());
        pedido.setHistoricoStatus(historico);

        return PedidoDTO.Response.from(pedidoRepository.save(pedido));
    }

    public void deletar(String id) {
        encontrarOuLancar(id);
        pedidoRepository.deleteById(id);
        log.info("Pedido deletado: id={}", id);
    }

    public Pedido encontrarOuLancar(String id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", id));
    }

    private ItemPedido criarItem(PedidoDTO.ItemRequest item, Map<String, Produto> produtosPorId) {
        Produto produto = produtosPorId.get(item.produtoId());
        if (produto == null) {
            throw new ResourceNotFoundException("Produto", item.produtoId());
        }
        if (!produto.isDisponivel()) {
            throw new BusinessRuleException("O produto '%s' nao esta disponivel para pedido".formatted(produto.getNome()));
        }

        BigDecimal quantidade = BigDecimal.valueOf(item.quantidade());
        BigDecimal subtotal = produto.getPreco().multiply(quantidade);

        return ItemPedido.builder()
                .produtoId(produto.getId())
                .nomeProduto(produto.getNome())
                .precoUnitario(produto.getPreco())
                .quantidade(item.quantidade())
                .subtotal(subtotal)
                .build();
    }

    private String resolverEnderecoEntrega(String cep) {
        CepService.Endereco endereco = cepService.obterEnderecoPorCep(cep);
        return cepService.formatarEndereco(endereco);
    }

    private EnderecoPedido resolverEnderecoPedido(PedidoDTO.Request dto) {
        PedidoDTO.EnderecoEntregaRequest endereco = dto.endereco();
        String cep = endereco == null ? dto.enderecoEntrega() : endereco.cep();
        boolean semCep = endereco != null && Boolean.TRUE.equals(endereco.semCep());

        if (!semCep && cep != null && !cep.isBlank()) {
            CepService.Endereco enderecoCep = cepService.obterEnderecoPorCep(cep);
            String rua = primeiroTexto(endereco == null ? null : endereco.rua(), enderecoCep.logradouro());
            String bairro = primeiroTexto(endereco == null ? null : endereco.bairro(), enderecoCep.bairro());
            String numero = endereco == null ? null : textoOuNulo(endereco.numero());
            String complemento = primeiroTexto(endereco == null ? null : endereco.complemento(), enderecoCep.complemento());
            String referencia = endereco == null ? null : textoOuNulo(endereco.referencia());

            return new EnderecoPedido(
                    enderecoCep.cep(),
                    rua,
                    numero,
                    bairro,
                    complemento,
                    referencia,
                    formatarEnderecoPedido(rua, numero, bairro, enderecoCep.localidade(), enderecoCep.uf(), enderecoCep.cep()));
        }

        if (endereco == null
                || textoOuNulo(endereco.rua()) == null
                || textoOuNulo(endereco.numero()) == null
                || textoOuNulo(endereco.bairro()) == null) {
            throw new BusinessRuleException("Informe rua, numero e bairro para entrega");
        }

        return new EnderecoPedido(
                null,
                endereco.rua().trim(),
                endereco.numero().trim(),
                endereco.bairro().trim(),
                textoOuNulo(endereco.complemento()),
                textoOuNulo(endereco.referencia()),
                formatarEnderecoManual(endereco));
    }

    private BigDecimal calcularTaxaEntrega(Loja loja, EnderecoPedido enderecoPedido) {
        BigDecimal valorPorKm = valorOuZero(loja.getValorEntregaPorKm());
        if (valorPorKm.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        if (enderecoPedido.cep() == null || enderecoPedido.cep().isBlank()) {
            return valorPorKm;
        }

        return distanceMatrixService.calcularFrete(loja.getEndereco(), enderecoPedido.resumo(), valorPorKm);
    }

    private String formatarEnderecoPedido(String rua, String numero, String bairro, String cidade, String estado, String cep) {
        String numeroFormatado = numero == null ? "S/N" : numero;
        return "%s, %s, %s - %s/%s, CEP %s".formatted(rua, numeroFormatado, bairro, cidade, estado, cep);
    }

    private String formatarEnderecoManual(PedidoDTO.EnderecoEntregaRequest endereco) {
        return "%s, %s, %s".formatted(
                endereco.rua().trim(),
                endereco.numero().trim(),
                endereco.bairro().trim());
    }

    private String primeiroTexto(String preferencial, String fallback) {
        String texto = textoOuNulo(preferencial);
        return texto == null ? textoOuNulo(fallback) : texto;
    }

    private String textoOuNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private boolean lojaEstaAberta(Loja loja) {
        if (!Boolean.TRUE.equals(loja.getAberto()) || loja.getHorarioFuncionamento() == null) {
            return false;
        }

        LocalDateTime agora = LocalDateTime.now(ZONE_ID);
        return loja.getHorarioFuncionamento().stream()
                .anyMatch(horario -> horarioAbertoAgora(horario, agora));
    }

    private boolean horarioAbertoAgora(HorarioFuncionamento horario, LocalDateTime agora) {
        if (horario.getDiaSemana() == null
                || horario.getHoraAbertura() == null
                || horario.getHoraFechamento() == null
                || !horario.getDiaSemana().equals(agora.getDayOfWeek())) {
            return false;
        }

        return !agora.toLocalTime().isBefore(horario.getHoraAbertura())
                && agora.toLocalTime().isBefore(horario.getHoraFechamento());
    }

    private BigDecimal valorOuZero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private void validarTransicaoStatus(StatusPedido atual, StatusPedido novo) {
        if (atual == null || novo == null || atual == novo) {
            return;
        }

        List<StatusPedido> permitidos = TRANSICOES_PERMITIDAS.getOrDefault(atual, List.of());
        if (!permitidos.contains(novo)) {
            throw new BusinessRuleException("Transicao de status invalida: %s para %s".formatted(atual, novo));
        }
    }

    private String gerarCodigoPedido() {
        String data = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZONE_ID).format(Instant.now());

        for (int tentativa = 0; tentativa < 10; tentativa++) {
            String sufixo = String.valueOf(1000 + RANDOM.nextInt(9000));
            String codigo = "DP-%s-%s".formatted(data, sufixo);

            if (!pedidoRepository.existsByCodigo(codigo)) {
                return codigo;
            }
        }

        throw new BusinessRuleException("Nao foi possivel gerar o codigo do pedido");
    }

    private String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }

    private String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    private record EnderecoPedido(
            String cep,
            String rua,
            String numero,
            String bairro,
            String complemento,
            String referencia,
            String resumo
    ) {
    }
}
