package back.camarao.sistema.dto;

import back.camarao.sistema.enums.StatusPedido;
import back.camarao.sistema.integration.cep.CepService;
import back.camarao.sistema.model.ItemPedido;
import back.camarao.sistema.model.Pedido;
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
            StatusPedido status
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
            Instant createdAt,
            Instant updatedAt
    ) {
        public static Response from(Pedido pedido) {
            return new Response(
                    pedido.getId(),
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
                    pedido.getCreatedAt(),
                    pedido.getUpdatedAt());
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
}
