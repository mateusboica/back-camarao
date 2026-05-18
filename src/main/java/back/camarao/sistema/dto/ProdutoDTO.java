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

    private ProdutoDTO() {
    }

    public record Request(
            @NotBlank(message = "O nome e obrigatorio")
            @Size(min = 3, max = 120, message = "Nome deve ter entre 3 e 120 caracteres")
            String nome,

            @NotNull(message = "O preco e obrigatorio")
            @DecimalMin(value = "0.01", message = "Preco deve ser maior que zero")
            BigDecimal preco,

            @NotBlank(message = "A descricao e obrigatoria")
            @Size(max = 1000, message = "Descricao deve ter no maximo 1000 caracteres")
            String descricao,

            boolean isDisponivel,

            @NotBlank(message = "A URL da imagem e obrigatoria")
            String img,

            @NotNull(message = "A categoria e obrigatoria")
            Categoria categoria,

            List<String> tags
    ) {
    }

    public record Response(
            String id,
            String nome,
            BigDecimal preco,
            String descricao,
            boolean isDisponivel,
            String img,
            Categoria categoria,
            List<String> tags,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static Response from(Produto produto) {
            return new Response(
                    produto.getId(),
                    produto.getNome(),
                    produto.getPreco(),
                    produto.getDescricao(),
                    produto.isDisponivel(),
                    produto.getImg(),
                    produto.getCategoria(),
                    produto.getTags(),
                    produto.getCreatedAt(),
                    produto.getUpdatedAt());
        }
    }

    public record PatchDisponibilidade(
            @NotNull boolean isDisponivel
    ) {
    }
}
