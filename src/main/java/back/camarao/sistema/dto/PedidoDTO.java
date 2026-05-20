package back.camarao.sistema.dto;

import back.camarao.sistema.enums.StatusPedido;
import back.camarao.sistema.integration.cep.CepService;
import back.camarao.sistema.model.ItemPedido;
import back.camarao.sistema.model.Pedido;
import back.camarao.sistema.model.StatusHistoricoPedido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class PedidoDTO {

    private PedidoDTO() {
    }

    public record ItemRequest(
            @NotBlank(message = "O produto e obrigatorio")
            String produtoId,

            @NotNull(message = "A quantidade e obrigatoria")
            @Min(value = 1, message = "A quantidade deve ser maior que zero")
            Integer quantidade
    ) {
    }

    public record Request(
            @NotBlank(message = "A loja e obrigatoria")
            String lojaId,

            @NotBlank(message = "O nome do cliente e obrigatorio")
            @Size(min = 3, max = 120, message = "Nome do cliente deve ter entre 3 e 120 caracteres")
            String nomeCliente,

            @NotBlank(message = "O telefone do cliente e obrigatorio")
            @Pattern(regexp = "^\\+?[0-9 ()-]{10,20}$", message = "Telefone deve conter entre 10 e 20 caracteres validos")
            String telefoneCliente,

            String enderecoEntrega,

            EnderecoEntregaRequest endereco,

            PagamentoRequest pagamento,

            @Size(max = 500, message = "Observacao deve ter no maximo 500 caracteres")
            String observacao,

            @Valid
            @NotEmpty(message = "Informe ao menos um item no pedido")
            List<ItemRequest> itens
    ) {
    }

    public record EnderecoEntregaRequest(
            @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "CEP deve conter 8 digitos")
            String cep,

            String rua,
            String numero,
            String bairro,
            String complemento,
            String referencia,
            Boolean semCep
    ) {
    }

    public record PagamentoRequest(
            @NotBlank(message = "A forma de pagamento e obrigatoria")
            String metodo,

            BigDecimal trocoPara
    ) {
    }

    public record PatchStatus(
            @NotNull(message = "O status do pedido e obrigatorio")
            StatusPedido status,

            @Size(max = 300, message = "Observacao de status deve ter no maximo 300 caracteres")
            String observacao
    ) {
    }

    public record CepResponse(
            String cep,
            String logradouro,
            String complemento,
            String bairro,
            String cidade,
            String estado
    ) {
        public static CepResponse from(CepService.Endereco endereco) {
            return new CepResponse(
                    endereco.cep(),
                    endereco.logradouro(),
                    endereco.complemento(),
                    endereco.bairro(),
                    endereco.localidade(),
                    endereco.uf());
        }
    }

    public record FreteResponse(
            String cep,
            String enderecoEntrega,
            BigDecimal distanciaKm,
            BigDecimal valorPorKm,
            BigDecimal taxaEntrega
    ) {
    }

    public record ItemResponse(
            String produtoId,
            String nomeProduto,
            BigDecimal precoUnitario,
            Integer quantidade,
            BigDecimal subtotal
    ) {
        public static ItemResponse from(ItemPedido item) {
            return new ItemResponse(
                    item.getProdutoId(),
                    item.getNomeProduto(),
                    item.getPrecoUnitario(),
                    item.getQuantidade(),
                    item.getSubtotal());
        }
    }

    public record Response(
            String id,
            String codigo,
            String lojaId,
            String nomeCliente,
            String telefoneCliente,
            String enderecoEntrega,
            EnderecoEntregaResponse endereco,
            String metodoPagamento,
            BigDecimal trocoPara,
            String observacao,
            List<ItemResponse> itens,
            BigDecimal subtotal,
            BigDecimal taxaServico,
            BigDecimal taxaEntrega,
            BigDecimal total,
            StatusPedido status,
            String statusLabel,
            Instant statusAtualizadoEm,
            List<StatusHistoricoResponse> historicoStatus,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static Response from(Pedido pedido) {
            return new Response(
                    pedido.getId(),
                    pedido.getCodigo(),
                    pedido.getLojaId(),
                    pedido.getNomeCliente(),
                    pedido.getTelefoneCliente(),
                    pedido.getEnderecoEntrega(),
                    EnderecoEntregaResponse.from(pedido),
                    pedido.getMetodoPagamento(),
                    pedido.getTrocoPara(),
                    pedido.getObservacao(),
                    pedido.getItens().stream().map(ItemResponse::from).toList(),
                    pedido.getSubtotal(),
                    pedido.getTaxaServico(),
                    pedido.getTaxaEntrega(),
                    pedido.getTotal(),
                    pedido.getStatus(),
                    formatStatusLabel(pedido.getStatus()),
                    pedido.getStatusAtualizadoEm(),
                    pedido.getHistoricoStatus() == null
                            ? List.of()
                            : pedido.getHistoricoStatus().stream().map(StatusHistoricoResponse::from).toList(),
                    pedido.getCreatedAt(),
                    pedido.getUpdatedAt());
        }
    }

    public record StatusHistoricoResponse(
            StatusPedido status,
            String statusLabel,
            Instant alteradoEm,
            String observacao
    ) {
        public static StatusHistoricoResponse from(StatusHistoricoPedido historico) {
            return new StatusHistoricoResponse(
                    historico.getStatus(),
                    formatStatusLabel(historico.getStatus()),
                    historico.getAlteradoEm(),
                    historico.getObservacao());
        }
    }

    public record EnderecoEntregaResponse(
            String cep,
            String rua,
            String numero,
            String bairro,
            String complemento,
            String referencia
    ) {
        public static EnderecoEntregaResponse from(Pedido pedido) {
            return new EnderecoEntregaResponse(
                    pedido.getCepEntrega(),
                    pedido.getRuaEntrega(),
                    pedido.getNumeroEntrega(),
                    pedido.getBairroEntrega(),
                    pedido.getComplementoEntrega(),
                    pedido.getReferenciaEntrega());
        }
    }

    private static String formatStatusLabel(StatusPedido status) {
        if (status == null) {
            return "Status nao informado";
        }

        return switch (status) {
            case RECEBIDO -> "Pedido recebido";
            case EM_PREPARO -> "Em preparo";
            case SAIU_PARA_ENTREGA -> "Saiu para entrega";
            case ENTREGUE -> "Entregue";
            case CANCELADO -> "Cancelado";
        };
    }
}
