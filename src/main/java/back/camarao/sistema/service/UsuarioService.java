package back.camarao.sistema.service;

import back.camarao.sistema.dto.AuthDTO;
import back.camarao.sistema.dto.UsuarioDTO;
import back.camarao.sistema.enums.PerfilAcesso;
import back.camarao.sistema.exception.ResourceAlreadyExistsException;
import back.camarao.sistema.exception.ResourceNotFoundException;
import back.camarao.sistema.model.Usuario;
import back.camarao.sistema.repository.UsuarioRepository;
import back.camarao.sistema.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public UsuarioDTO.Response cadastrar(UsuarioDTO.CreateRequest dto) {
        String nome = dto.nome().trim();
        String email = dto.email().trim().toLowerCase();

        validarNovoUsuario(nome, email);

        Usuario usuario = Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode(dto.senha()))
                .acesso(PerfilAcesso.USER.name())
                .build();

        Usuario salvo = usuarioRepository.save(usuario);
        log.info("Usuario criado: id={}, email={}", salvo.getId(), salvo.getEmail());
        return UsuarioDTO.Response.from(salvo);
    }

    public LoginResult login(AuthDTO.LoginRequest dto) {
        String email = dto.email().trim().toLowerCase();
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, dto.senha()));

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new BadCredentialsException("Email ou senha invalidos"));

        String token = tokenService.generateToken(usuario);
        return new LoginResult(AuthDTO.LoginResponse.from(usuario, token), token);
    }

    public LoginResult atualizarPerfil(String emailAtual, UsuarioDTO.UpdateProfileRequest dto) {
        Usuario usuario = buscarUsuarioPorEmail(emailAtual);
        String novoNome = dto.nome().trim();
        String novoEmail = dto.email().trim().toLowerCase();

        validarNomeDisponivelParaUsuario(novoNome, usuario.getId());
        validarEmailDisponivelParaUsuario(novoEmail, usuario.getId());

        usuario.setNome(novoNome);
        usuario.setEmail(novoEmail);

        Usuario salvo = usuarioRepository.save(usuario);
        String token = tokenService.generateToken(salvo);
        return new LoginResult(AuthDTO.LoginResponse.from(salvo, token), token);
    }

    public void alterarSenha(String emailAtual, UsuarioDTO.UpdatePasswordRequest dto) {
        Usuario usuario = buscarUsuarioPorEmail(emailAtual);

        if (!passwordEncoder.matches(dto.senhaAtual(), usuario.getSenha())) {
            throw new BadCredentialsException("Senha atual invalida");
        }

        usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        usuarioRepository.save(usuario);
    }

    public UsuarioDTO.Response buscarPorEmail(String email) {
        return UsuarioDTO.Response.from(buscarUsuarioPorEmail(email));
    }

    private void validarNovoUsuario(String nome, String email) {
        if (usuarioRepository.existsByNomeIgnoreCase(nome)) {
            throw new ResourceAlreadyExistsException(
                    "Ja existe um usuario com o nome '%s'".formatted(nome));
        }

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResourceAlreadyExistsException(
                    "Ja existe um usuario com o email '%s'".formatted(email));
        }
    }

    private Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", email));
    }

    private void validarNomeDisponivelParaUsuario(String nome, String usuarioId) {
        usuarioRepository.findByNomeIgnoreCase(nome)
                .filter(usuario -> !usuario.getId().equals(usuarioId))
                .ifPresent(usuario -> {
                    throw new ResourceAlreadyExistsException(
                            "Ja existe um usuario com o nome '%s'".formatted(nome));
                });
    }

    private void validarEmailDisponivelParaUsuario(String email, String usuarioId) {
        usuarioRepository.findByEmailIgnoreCase(email)
                .filter(usuario -> !usuario.getId().equals(usuarioId))
                .ifPresent(usuario -> {
                    throw new ResourceAlreadyExistsException(
                            "Ja existe um usuario com o email '%s'".formatted(email));
                });
    }

    public record LoginResult(AuthDTO.LoginResponse response, String token) {
    }
}
