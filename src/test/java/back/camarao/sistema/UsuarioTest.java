package back.camarao.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import back.camarao.sistema.dto.UsuarioDTO;
import back.camarao.sistema.exception.ResourceAlreadyExistsException;
import back.camarao.sistema.model.Usuario;
import back.camarao.sistema.repository.UsuarioRepository;
import back.camarao.sistema.security.TokenService;
import back.camarao.sistema.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void deveCadastrarUsuarioComDadosNormalizadosESenhaCriptografada() {
        UsuarioDTO.CreateRequest request = new UsuarioDTO.CreateRequest(
                "  Test User  ",
                "  TEST@EXAMPLE.COM  ",
                "password123");

        when(usuarioRepository.existsByNomeIgnoreCase("Test User")).thenReturn(false);
        when(usuarioRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("senha-criptografada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId("usuario-1");
            return usuario;
        });

        UsuarioDTO.Response response = usuarioService.cadastrar(request);

        assertThat(response.id()).isEqualTo("usuario-1");
        assertThat(response.nome()).isEqualTo("Test User");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.acesso()).isEqualTo("USER");

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());

        Usuario usuarioSalvo = usuarioCaptor.getValue();
        assertThat(usuarioSalvo.getNome()).isEqualTo("Test User");
        assertThat(usuarioSalvo.getEmail()).isEqualTo("test@example.com");
        assertThat(usuarioSalvo.getSenha()).isEqualTo("senha-criptografada");
        assertThat(usuarioSalvo.getAcesso()).isEqualTo("USER");
    }

    @Test
    void naoDeveCadastrarUsuarioQuandoEmailJaExiste() {
        UsuarioDTO.CreateRequest request = new UsuarioDTO.CreateRequest(
                "Test User",
                "test@example.com",
                "password123");

        when(usuarioRepository.existsByNomeIgnoreCase("Test User")).thenReturn(false);
        when(usuarioRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cadastrar(request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("test@example.com");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
