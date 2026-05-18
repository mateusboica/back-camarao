package back.camarao.sistema.dto;

import back.camarao.sistema.model.HorarioFuncionamento;
import back.camarao.sistema.model.Loja;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

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
            @Size(max = 300, message = "Endereco deve ter no maximo 300 caracteres")
            String endereco,

            @NotBlank(message = "O telefone e obrigatorio")
            @Pattern(regexp = "^\\+?[0-9 ()-]{10,20}$", message = "Telefone deve conter entre 10 e 20 caracteres validos")
            String telefone,

            @NotNull(message = "Informe se a loja esta aberta")
            Boolean aberto,

            @NotBlank(message = "A URL do logo e obrigatoria")
            @Pattern(regexp = "^(https?://).+", message = "Logo deve ser uma URL http ou https")
            String logoUrl,

            @NotNull(message = "A taxa de servico e obrigatoria")
            @DecimalMin(value = "0.00", message = "Taxa de servico nao pode ser negativa")
            @Digits(integer = 8, fraction = 2, message = "Taxa de servico deve ter no maximo 8 digitos inteiros e 2 decimais")
            BigDecimal taxaServico,

            @NotNull(message = "O valor de entrega por km e obrigatorio")
            @DecimalMin(value = "0.00", message = "Valor de entrega por km nao pode ser negativo")
            @Digits(integer = 8, fraction = 2, message = "Valor de entrega por km deve ter no maximo 8 digitos inteiros e 2 decimais")
            @JsonAlias({"taxaEntrega", "valorPorKm"})
            BigDecimal valorEntregaPorKm,

            @Valid
            @NotEmpty(message = "O horario de funcionamento e obrigatorio")
            List<HorarioFuncionamento> horarioFuncionamento
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
            BigDecimal taxaServico,
            BigDecimal taxaEntrega,
            BigDecimal valorEntregaPorKm,
            List<HorarioFuncionamento> horarioFuncionamento,
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
                    loja.getValorEntregaPorKm(),
                    loja.getValorEntregaPorKm(),
                    loja.getHorarioFuncionamento(),
                    loja.getCreatedAt(),
                    loja.getUpdatedAt());
        }
    }
}
