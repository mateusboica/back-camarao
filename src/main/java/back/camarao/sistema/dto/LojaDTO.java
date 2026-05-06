package back.camarao.sistema.dto;

import back.camarao.sistema.model.Loja;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class LojaDTO {

    private LojaDTO() {
    }

    public record Request(
            @NotBlank(message = "O nome da loja e obrigatorio")
            @Size(min = 3, max = 120, message = "Nome deve ter entre 3 e 120 caracteres")
            String nome,

            @NotBlank(message = "A descricao e obrigatoria")
            @Size(max = 1000, message = "Descricao deve ter no maximo 1000 caracteres")
            String descricao,

            @NotBlank(message = "O endereco e obrigatorio")
            String endereco,

            @NotBlank(message = "O telefone e obrigatorio")
            String telefone,

            @NotNull(message = "Informe se a loja esta aberta")
            Boolean aberto,

            @NotBlank(message = "A URL do logo e obrigatoria")
            String logoUrl,

            @NotBlank(message = "A taxa de servico e obrigatoria")
            String taxaServico,

            @NotBlank(message = "A taxa de entrega e obrigatoria")
            String taxaEntrega,

            @NotBlank(message = "O horario de funcionamento e obrigatorio")
            String horarioFuncionamento
    ) {
    }

    public record PatchStatus(
            @NotNull(message = "Informe se a loja esta aberta")
            Boolean aberto
    ) {
    }

    public record LojaResponse(
            String id,
            String nome,
            String descricao,
            String endereco,
            String telefone,
            Boolean aberto,
            String logoUrl,
            String taxaServico,
            String taxaEntrega,
            String horarioFuncionamento,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static LojaResponse from(Loja loja) {
            return new LojaResponse(
                    loja.getId(),
                    loja.getNome(),
                    loja.getDescricao(),
                    loja.getEndereco(),
                    loja.getTelefone(),
                    loja.getAberto(),
                    loja.getLogoUrl(),
                    loja.getTaxaServico(),
                    loja.getTaxaEntrega(),
                    loja.getHorarioFuncionamento(),
                    loja.getCreatedAt(),
                    loja.getUpdatedAt());
        }
    }
}
