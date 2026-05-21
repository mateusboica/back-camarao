package back.camarao.sistema.dto;

import java.time.Instant;

import back.camarao.sistema.model.Usuario;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class UsuarioDTO {
    private UsuarioDTO() {
    }

    public record CreateRequest(
            @NotBlank(message = "O nome e obrigatorio")
            @Size(min = 3, max = 120, message = "Nome deve ter entre 3 e 120 caracteres")
            String nome,

            @NotBlank(message = "O email e obrigatorio")
            @Email(message = "Informe um email valido")
            String email,

            @NotBlank(message = "A senha e obrigatoria")
            @Size(min = 6, max = 120, message = "Senha deve ter entre 6 e 120 caracteres")
            @JsonAlias("password")
            String senha) {
    }

    public record UpdateProfileRequest(
            @NotBlank(message = "O nome e obrigatorio")
            @Size(min = 3, max = 120, message = "Nome deve ter entre 3 e 120 caracteres")
            String nome,

            @NotBlank(message = "O email e obrigatorio")
            @Email(message = "Informe um email valido")
            String email) {
    }

    public record UpdatePasswordRequest(
            @NotBlank(message = "A senha atual e obrigatoria")
            @JsonAlias({"currentPassword", "senha_atual"})
            String senhaAtual,

            @NotBlank(message = "A nova senha e obrigatoria")
            @Size(min = 6, max = 120, message = "Senha deve ter entre 6 e 120 caracteres")
            @JsonAlias({"newPassword", "nova_senha"})
            String novaSenha) {
    }

    public record Response(
            String id,
            String nome,
            String email,
            String acesso,
            Instant createdAt,
            Instant updatedAt) {
        public static Response from(Usuario usuario) {
            return new Response(
                    usuario.getId(),
                    usuario.getNome(),
                    usuario.getEmail(),
                    usuario.getAcesso(),
                    usuario.getCreatedAt(),
                    usuario.getUpdatedAt());
        }
    }
}
