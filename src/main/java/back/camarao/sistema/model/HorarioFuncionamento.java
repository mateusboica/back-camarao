package back.camarao.sistema.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HorarioFuncionamento {

    @NotNull(message = "O dia da semana e obrigatorio")
    private DayOfWeek diaSemana;

    @NotNull(message = "A hora de abertura e obrigatoria")
    private LocalTime horaAbertura;

    @NotNull(message = "A hora de fechamento e obrigatoria")
    private LocalTime horaFechamento;

    @AssertTrue(message = "A hora de fechamento deve ser depois da hora de abertura")
    public boolean isHorarioValido() {
        if (horaAbertura == null || horaFechamento == null) {
            return true;
        }
        return horaFechamento.isAfter(horaAbertura);
    }
}
