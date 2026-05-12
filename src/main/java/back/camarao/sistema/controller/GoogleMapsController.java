package back.camarao.sistema.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import back.camarao.sistema.features.CalculoDistancia;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1/maps")
@CrossOrigin(origins = "http://localhost:5173")
public class GoogleMapsController {

    private final CalculoDistancia calculoDistancia;

    public GoogleMapsController(CalculoDistancia calculoDistancia) {
        this.calculoDistancia = calculoDistancia;
    }

    @GetMapping("/distancia")
    public String calcularDistancia() {
        return calculoDistancia.obterDistancia();
    }
}
