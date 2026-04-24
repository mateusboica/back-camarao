package back.camarao.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import back.camarao.sistema.dto.UserDTO;
import back.camarao.sistema.exception.ResourceAlreadyExistsException;
import back.camarao.sistema.model.User;
import back.camarao.sistema.repository.UserRepository;
import back.camarao.sistema.security.TokenService;
import back.camarao.sistema.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UserService userService;

    @Test
    void deveCadastrarUsuarioComDadosNormalizadosESenhaCriptografada() {
        UserDTO.CreateRequest request = new UserDTO.CreateRequest(
                "  Test User  ",
                "  TEST@EXAMPLE.COM  ",
                "password123");

        when(userRepository.existsByNomeIgnoreCase("Test User")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("senha-criptografada");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("user-1");
            return user;
        });

        UserDTO.Response response = userService.cadastrar(request);

        assertThat(response.id()).isEqualTo("user-1");
        assertThat(response.nome()).isEqualTo("Test User");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.acesso()).isEqualTo("USER");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User userSalvo = userCaptor.getValue();
        assertThat(userSalvo.getNome()).isEqualTo("Test User");
        assertThat(userSalvo.getEmail()).isEqualTo("test@example.com");
        assertThat(userSalvo.getSenha()).isEqualTo("senha-criptografada");
        assertThat(userSalvo.getAcesso()).isEqualTo("USER");
    }

    @Test
    void naoDeveCadastrarUsuarioQuandoEmailJaExiste() {
        UserDTO.CreateRequest request = new UserDTO.CreateRequest(
                "Test User",
                "test@example.com",
                "password123");

        when(userRepository.existsByNomeIgnoreCase("Test User")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.cadastrar(request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("test@example.com");

        verify(userRepository, never()).save(any(User.class));
    }
}
