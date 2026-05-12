package back.camarao.sistema.service;

import back.camarao.sistema.dto.PedidoDTO;
import back.camarao.sistema.enums.StatusPedido;
import back.camarao.sistema.exception.BusinessRuleException;
import back.camarao.sistema.exception.ResourceNotFoundException;
import back.camarao.sistema.features.HorarioFuncionamento;
import back.camarao.sistema.model.ItemPedido;
import back.camarao.sistema.model.Loja;
import back.camarao.sistema.model.Pedido;
import back.camarao.sistema.model.Produto;
import back.camarao.sistema.repository.PedidoRepository;
import back.camarao.sistema.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoService {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final LojaService lojaService;

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
        BigDecimal taxaServico = valorOuZero(loja.getTaxaServico());
        BigDecimal taxaEntrega = valorOuZero(loja.getTaxaEntrega());
        BigDecimal total = subtotal.add(taxaServico).add(taxaEntrega);

        Pedido pedido = Pedido.builder()
                .lojaId(loja.getId())
                .nomeCliente(dto.nomeCliente().trim())
                .telefoneCliente(dto.telefoneCliente().trim())
                .enderecoEntrega(dto.enderecoEntrega().trim())
                .observacao(dto.observacao() == null ? null : dto.observacao().trim())
                .itens(itens)
                .subtotal(subtotal)
                .taxaServico(taxaServico)
                .taxaEntrega(taxaEntrega)
                .total(total)
                .status(StatusPedido.RECEBIDO)
                .build();

        Pedido salvo = pedidoRepository.save(pedido);
        log.info("Pedido criado: id={}, lojaId={}, total={}", salvo.getId(), salvo.getLojaId(), salvo.getTotal());
        return PedidoDTO.Response.from(salvo);
    }

    public PedidoDTO.Response alterarStatus(String id, StatusPedido status) {
        Pedido pedido = encontrarOuLancar(id);
        pedido.setStatus(status);
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
}
