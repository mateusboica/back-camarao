import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HorarioFunncionamento{

    private DayOfWeek diaSemana;
    private LocalTime horaAbertura;
    private LocalTime horaFechamento;

}