package back.camarao.sistema.integration.cep;

import back.camarao.sistema.exception.BusinessRuleException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CepService {

    private final RestClient restClient;

    public CepService() {
        this.restClient = RestClient.create();
    }

    public Endereco obterEnderecoPorCep(String cep) {
        String cepNormalizado = normalizarCep(cep);
        Endereco endereco = restClient.get()
                .uri("https://viacep.com.br/ws/{cep}/json/", cepNormalizado)
                .retrieve()
                .body(Endereco.class);

        if (endereco == null || Boolean.TRUE.equals(endereco.erro())) {
            throw new BusinessRuleException("CEP nao encontrado");
        }

        return endereco;
    }

    public String formatarEndereco(Endereco endereco) {
        String complemento = endereco.complemento() == null || endereco.complemento().isBlank()
                ? ""
                : ", " + endereco.complemento().trim();

        return "%s%s, %s, %s - %s, CEP %s".formatted(
                endereco.logradouro(),
                complemento,
                endereco.bairro(),
                endereco.localidade(),
                endereco.uf(),
                endereco.cep());
    }

    private String normalizarCep(String cep) {
        if (cep == null) {
            throw new BusinessRuleException("CEP e obrigatorio");
        }

        String cepNormalizado = cep.replaceAll("\\D", "");
        if (cepNormalizado.length() != 8) {
            throw new BusinessRuleException("CEP deve conter 8 digitos");
        }

        return cepNormalizado;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Endereco(
            String cep,
            String logradouro,
            String complemento,
            String bairro,
            String localidade,
            String uf,
            Boolean erro
    ) {
    }
}
