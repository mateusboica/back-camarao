package back.camarao.sistema;

import back.camarao.sistema.dto.LojaDTO;
import back.camarao.sistema.features.HorarioFuncionamento;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LojaTeste {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void deveAceitarRequestComTiposValidos() {
        LojaDTO.Request request = new LojaDTO.Request(
                "Camarao do Chef",
                "Restaurante especializado em frutos do mar",
                "Rua Principal, 123",
                "+55 11 99999-9999",
                true,
                "https://cdn.example.com/logo.png",
                new BigDecimal("2.50"),
                new BigDecimal("8.90"),
                List.of(new HorarioFuncionamento(
                        DayOfWeek.MONDAY,
                        LocalTime.of(10, 0),
                        LocalTime.of(22, 0))));

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void deveRejeitarTaxaNegativaEHorarioInvalido() {
        LojaDTO.Request request = new LojaDTO.Request(
                "Camarao do Chef",
                "Restaurante especializado em frutos do mar",
                "Rua Principal, 123",
                "+55 11 99999-9999",
                true,
                "https://cdn.example.com/logo.png",
                new BigDecimal("-1.00"),
                new BigDecimal("8.90"),
                List.of(new HorarioFuncionamento(
                        DayOfWeek.MONDAY,
                        LocalTime.of(22, 0),
                        LocalTime.of(10, 0))));

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("taxaServico", "horarioFuncionamento[0].horarioValido");
    }
}
