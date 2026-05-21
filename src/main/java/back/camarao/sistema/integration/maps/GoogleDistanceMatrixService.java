package back.camarao.sistema.integration.maps;

import back.camarao.sistema.exception.BusinessRuleException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class GoogleDistanceMatrixService {

    private final RestClient restClient = RestClient.create();

    @Value("${google.maps.api-key}")
    private String apiKey;

    public BigDecimal calcularFrete(String origem, String destino, BigDecimal valorPorKm) {
        BigDecimal distanciaKm = obterDistanciaKm(origem, destino);
        return calcularFrete(distanciaKm, valorPorKm);
    }

    public BigDecimal calcularFrete(BigDecimal distanciaKm, BigDecimal valorPorKm) {
        return distanciaKm.multiply(valorPorKm).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal obterDistanciaKm(String origem, String destino) {
        validarConfiguracao();
        DistanceMatrixResponse response = consultarDistancia(origem, destino);
        Element element = primeiroElementoValido(response);

        return BigDecimal.valueOf(element.distance().value())
                .divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
    }

    private DistanceMatrixResponse consultarDistancia(String origem, String destino) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("maps.googleapis.com")
                        .path("/maps/api/distancematrix/json")
                        .queryParam("destinations", destino)
                        .queryParam("origins", origem)
                        .queryParam("units", "metric")
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .body(DistanceMatrixResponse.class);
    }

    private void validarConfiguracao() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessRuleException("Calculo de frete por distancia nao configurado");
        }
    }

    private Element primeiroElementoValido(DistanceMatrixResponse response) {
        if (response == null || response.rows() == null || response.rows().isEmpty()) {
            throw new BusinessRuleException("Nao foi possivel calcular o frete");
        }

        Row row = response.rows().get(0);
        if (row.elements() == null || row.elements().isEmpty()) {
            throw new BusinessRuleException("Nao foi possivel calcular o frete");
        }

        Element element = row.elements().get(0);
        if (!"OK".equalsIgnoreCase(element.status())
                || element.distance() == null
                || element.distance().value() == null) {
            throw new BusinessRuleException("Nao foi possivel calcular o frete para este CEP");
        }

        return element;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DistanceMatrixResponse(List<Row> rows, String status) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Row(List<Element> elements) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Element(String status, Distance distance) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Distance(String text, Long value) {
    }
}
