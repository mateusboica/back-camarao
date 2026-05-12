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
        String apiKey;

    public String obterDistancia(){
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?destinations=New%20York%20City%2C%20NY&origins=Washington%2C%20DC&units=imperial&key=" + apiKey;
        String response = restClient.get()
        .uri(url)
        .retrieve()
        .body(String.class);

        return response;
        
    }


    
}
