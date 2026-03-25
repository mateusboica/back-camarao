package back.camarao.sistema.dto;

import back.camarao.sistema.enums.Categoria;
import back.camarao.sistema.model.Produto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ProdutoDTO {

    private ProdutoDTO() {}

    // ── REQUEST (criação / atualização) ──────────────────────────────────────

    public record Request(

        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 3, max = 120, message = "Nome deve ter entre 3 e 120 caracteres")
        String nome,

        @NotNull(message = "O preço é obrigatório")
        @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
        BigDecimal preco,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
        String descricao,

        boolean isDisponivel,

        @NotBlank(message = "A URL da imagem é obrigatória")
        String img,

        @NotNull(message = "A categoria é obrigatória")
        Categoria categoria,

        List<String> tags
    ) {}

    // ── RESPONSE (retorno para o cliente) ────────────────────────────────────

    public record Response(
        String id,
        String nome,
        BigDecimal preco,
        String descricao,
        boolean isDisponivel,
        String img,
        Categoria categoria,
        List<String> tags,
        Double notaMedia,
        Integer totalAvaliacoes,
        Instant createdAt,
        Instant updatedAt
    ) {
        /** Factory: converte entidade → DTO de resposta. */
        public static Response from(Produto p) {
            return new Response(
                p.getId(),
                p.getNome(),
                p.getPreco(),
                p.getDescricao(),
                p.isDisponivel(),
                p.getImg(),
                p.getCategoria(),
                p.getTags(),
                p.getNotaMedia(),
                p.getTotalAvaliacoes(),
                p.getCreatedAt(),
                p.getUpdatedAt()
            );
        }
    }

    // ── PATCH (atualização parcial) ───────────────────────────────────────────

    public record PatchDisponibilidade(
        @NotNull boolean isDisponivel
    ) {}
}
