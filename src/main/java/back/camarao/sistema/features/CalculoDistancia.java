package back.camarao.sistema.features;

import back.camarao.sistema.exception.BusinessRuleException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculoDistancia {

    private final TransformadorCEP transformadorCEP;
    private final RestClient restClient = RestClient.create();

    @Value("${api-key}")
    private String apiKey;

    public String obterEndereco(String cep) {
        return transformadorCEP.formatarEndereco(transformadorCEP.obterEnderecoPorCep(cep));
    }

    public String obterDistancia(String cep) {
        return obterDistancia("Candangolandia, Brasilia, DF", obterEndereco(cep));
    }

    public BigDecimal calcularFrete(String origem, String destino, BigDecimal valorPorKm) {
        BigDecimal distanciaKm = obterDistanciaKm(origem, destino);
        return calcularFrete(distanciaKm, valorPorKm);
    }

    public BigDecimal calcularFrete(BigDecimal distanciaKm, BigDecimal valorPorKm) {
        return distanciaKm.multiply(valorPorKm).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal obterDistanciaKm(String origem, String destino) {
        DistanceMatrixResponse response = consultarDistancia(origem, destino);
        Element element = primeiroElementoValido(response);

        return BigDecimal.valueOf(element.distance().value())
                .divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
    }

    private String obterDistancia(String origem, String destino) {
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
                .body(String.class);
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
