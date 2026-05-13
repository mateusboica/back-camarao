package back.camarao.sistema.features;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;

@Service
public class CalculoDistancia {

    private final RestClient restClient;

    public CalculoDistancia() {
        this.restClient = RestClient.create();
    }

    @Value("${api-key}")
      private String apiKey;

   public String obterDistancia() {
        String response = restClient.get()
            .uri(uriBuilder -> uriBuilder
                .scheme("https")
                .host("maps.googleapis.com")
                .path("/maps/api/distancematrix/json")
                .queryParam("destinations", "New York City, NY") 
                .queryParam("origins", "Washington, DC")
                .queryParam("units", "imperial")
                .queryParam("key", apiKey)
                .build()
            )
            .retrieve()
            .body(String.class);
        return response;
        
    }


    
}
