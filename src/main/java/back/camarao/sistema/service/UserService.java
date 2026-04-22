package back.camarao.sistema.service;

import back.camarao.sistema.dto.AuthDTO;
import back.camarao.sistema.dto.UserDTO;
import back.camarao.sistema.enums.Roles;
import back.camarao.sistema.exception.ResourceAlreadyExistsException;
import back.camarao.sistema.exception.ResourceNotFoundException;
import back.camarao.sistema.model.User;
import back.camarao.sistema.repository.UserRepository;
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
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public UserDTO.Response cadastrar(UserDTO.CreateRequest dto) {
        String nome = dto.nome().trim();
        String email = dto.email().trim().toLowerCase();
        Roles acesso = definirAcessoCadastro();

        if (userRepository.existsByNomeIgnoreCase(nome)) {
            throw new ResourceAlreadyExistsException(
                    "Ja existe um usuario com o nome '%s'".formatted(nome));
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResourceAlreadyExistsException(
                    "Ja existe um usuario com o email '%s'".formatted(email));
        }

        User user = User.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode(dto.senha()))
                .acesso(acesso.name())
                .build();

        User salvo = userRepository.save(user);
        log.info("Usuario criado: id={}, email={}", salvo.getId(), salvo.getEmail());
        return UserDTO.Response.from(salvo);
    }

    public LoginResult login(AuthDTO.LoginRequest dto) {
        String email = dto.email().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        email,
                        dto.senha()));

        User user = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new BadCredentialsException("Email ou senha invalidos"));

        String token = tokenService.generateToken(user);
        AuthDTO.LoginResponse response = AuthDTO.LoginResponse.from(user, token);

        return new LoginResult(response, token);
    }

    public record LoginResult(AuthDTO.LoginResponse response, String token) {}

    public LoginResult atualizarPerfil(String emailAtual, UserDTO.UpdateProfileRequest dto) {
        User user = buscarUsuarioPorEmail(emailAtual);
        String novoNome = dto.nome().trim();
        String novoEmail = dto.email().trim().toLowerCase();

        validarNomeDisponivelParaUsuario(novoNome, user.getId());
        validarEmailDisponivelParaUsuario(novoEmail, user.getId());

        user.setNome(novoNome);
        user.setEmail(novoEmail);

        User salvo = userRepository.save(user);
        String token = tokenService.generateToken(salvo);
        AuthDTO.LoginResponse response = AuthDTO.LoginResponse.from(salvo, token);

        return new LoginResult(response, token);
    }

    public void alterarSenha(String emailAtual, UserDTO.UpdatePasswordRequest dto) {
        User user = buscarUsuarioPorEmail(emailAtual);

        if (!passwordEncoder.matches(dto.senhaAtual(), user.getSenha())) {
            throw new BadCredentialsException("Senha atual invalida");
        }

        user.setSenha(passwordEncoder.encode(dto.novaSenha()));
        userRepository.save(user);
    }

    public UserDTO.Response buscarPorEmail(String email) {
        User user = buscarUsuarioPorEmail(email);
        return UserDTO.Response.from(user);
    }

    private User buscarUsuarioPorEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", email));
    }

    private void validarNomeDisponivelParaUsuario(String nome, String userId) {
        userRepository.findByNomeIgnoreCase(nome)
                .filter(user -> !user.getId().equals(userId))
                .ifPresent(user -> {
                    throw new ResourceAlreadyExistsException(
                            "Ja existe um usuario com o nome '%s'".formatted(nome));
                });
    }

    private void validarEmailDisponivelParaUsuario(String email, String userId) {
        userRepository.findByEmailIgnoreCase(email)
                .filter(user -> !user.getId().equals(userId))
                .ifPresent(user -> {
                    throw new ResourceAlreadyExistsException(
                            "Ja existe um usuario com o email '%s'".formatted(email));
                });
    }
    
    private Roles definirAcessoCadastro() {
        return Roles.USER;
    }

}
