package back.camarao.sistema;

import back.camarao.sistema.dto.PedidoDTO;
import back.camarao.sistema.enums.Categoria;
import back.camarao.sistema.exception.BusinessRuleException;
import back.camarao.sistema.integration.cep.CepService;
import back.camarao.sistema.integration.maps.GoogleDistanceMatrixService;
import back.camarao.sistema.model.HorarioFuncionamento;
import back.camarao.sistema.model.Loja;
import back.camarao.sistema.model.Pedido;
import back.camarao.sistema.model.Produto;
import back.camarao.sistema.repository.PedidoRepository;
import back.camarao.sistema.repository.ProdutoRepository;
import back.camarao.sistema.service.LojaService;
import back.camarao.sistema.service.PedidoService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoTest {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private LojaService lojaService;

    @Mock
    private CepService cepService;

    @Mock
    private GoogleDistanceMatrixService distanceMatrixService;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void deveValidarRequestComItens() {
        PedidoDTO.Request request = requestValido();

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void deveRejeitarPedidoQuandoLojaEstaFechada() {
        PedidoDTO.Request request = requestValido();
        when(lojaService.encontrarOuLancar("loja-1")).thenReturn(lojaFechada());

        assertThatThrownBy(() -> pedidoService.criar(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("A loja esta fechada no momento e nao pode receber pedidos");

        verifyNoInteractions(produtoRepository, pedidoRepository);
    }

    @Test
    void deveCriarPedidoQuandoLojaEstaAbertaNoHorarioAtual() {
        PedidoDTO.Request request = requestValido();
        Produto produto = Produto.builder()
                .id("produto-1")
                .nome("Camarao alho e oleo")
                .preco(new BigDecimal("40.00"))
                .disponivel(true)
                .categoria(Categoria.FRUTOS_DO_MAR)
                .build();

        when(lojaService.encontrarOuLancar("loja-1")).thenReturn(lojaAbertaAgora());
        when(produtoRepository.findAllById(List.of("produto-1"))).thenReturn(List.of(produto));
        CepService.Endereco endereco = new CepService.Endereco(
                "59000-000",
                "Rua Principal",
                null,
                "Centro",
                "Natal",
                "RN",
                false);
        when(cepService.obterEnderecoPorCep("59000000")).thenReturn(endereco);
        when(cepService.formatarEndereco(endereco)).thenReturn("Rua Principal, Centro, Natal - RN, CEP 59000-000");
        when(distanceMatrixService.calcularFrete(
                "Endereco da loja",
                "Rua Principal, Centro, Natal - RN, CEP 59000-000",
                new BigDecimal("8.90"))).thenReturn(new BigDecimal("8.90"));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PedidoDTO.Response response = pedidoService.criar(request);

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(captor.capture());
        Pedido salvo = captor.getValue();

        assertThat(salvo.getSubtotal()).isEqualByComparingTo("80.00");
        assertThat(salvo.getTaxaServico()).isEqualByComparingTo("2.50");
        assertThat(salvo.getTaxaEntrega()).isEqualByComparingTo("8.90");
        assertThat(salvo.getTotal()).isEqualByComparingTo("91.40");
        assertThat(response.total()).isEqualByComparingTo("91.40");
    }

    private PedidoDTO.Request requestValido() {
        return new PedidoDTO.Request(
                "loja-1",
                "Maria Silva",
                "+55 11 99999-9999",
                "59000000",
                new PedidoDTO.EnderecoEntregaRequest(
                        "59000000",
                        null,
                        "123",
                        null,
                        null,
                        null,
                        false),
                new PedidoDTO.PagamentoRequest("pix", null),
                "Sem cebola",
                List.of(new PedidoDTO.ItemRequest("produto-1", 2)));
    }

    private Loja lojaFechada() {
        return Loja.builder()
                .id("loja-1")
                .endereco("Endereco da loja")
                .aberto(false)
                .horarioFuncionamento(List.of())
                .taxaServico(new BigDecimal("2.50"))
                .valorEntregaPorKm(new BigDecimal("8.90"))
                .build();
    }

    private Loja lojaAbertaAgora() {
        LocalDateTime agora = LocalDateTime.now(ZONE_ID);
        return Loja.builder()
                .id("loja-1")
                .endereco("Endereco da loja")
                .aberto(true)
                .horarioFuncionamento(List.of(HorarioFuncionamento.builder()
                        .diaSemana(agora.getDayOfWeek())
                        .horaAbertura(LocalTime.MIN)
                        .horaFechamento(LocalTime.MAX)
                        .build()))
                .taxaServico(new BigDecimal("2.50"))
                .valorEntregaPorKm(new BigDecimal("8.90"))
                .build();
    }
}
