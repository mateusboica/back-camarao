package back.camarao.sistema.dto;

import back.camarao.sistema.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class AuthDTO {
    private AuthDTO() {
    }

    public record LoginRequest(
            @NotBlank(message = "O email e obrigatorio")
            @Email(message = "Informe um email valido")
            String email,

            @NotBlank(message = "A senha e obrigatoria")
            String senha) {
    }

    public record LoginResponse(
            String id,
            String nome,
            String email,
            String acesso,
            String token,
            String tipo,
            String mensagem) {
        public static LoginResponse from(User user, String token) {
            return new LoginResponse(
                    user.getId(),
                    user.getNome(),
                    user.getEmail(),
                    user.getAcesso(),
                    token,
                    "Bearer",
                    "Login realizado com sucesso");
        }
    }
}
