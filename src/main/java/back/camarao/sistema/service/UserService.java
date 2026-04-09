package back.camarao.sistema.service;

import back.camarao.sistema.dto.AuthDTO;
import back.camarao.sistema.dto.UserDTO;
import back.camarao.sistema.exception.ResourceAlreadyExistsException;
import back.camarao.sistema.exception.ResourceNotFoundException;
import back.camarao.sistema.model.User;
import back.camarao.sistema.repository.UserRepository;
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

    public UserDTO.Response cadastrar(UserDTO.CreateRequest dto) {
        String nome = dto.nome().trim();
        String email = dto.email().trim().toLowerCase();
        String acesso = normalizarAcesso(dto.acesso());

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
                .acesso(acesso)
                .build();

        User salvo = userRepository.save(user);
        log.info("Usuario criado: id={}, email={}", salvo.getId(), salvo.getEmail());
        return UserDTO.Response.from(salvo);
    }

    public AuthDTO.LoginResponse login(AuthDTO.LoginRequest dto) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        dto.email().trim().toLowerCase(),
                        dto.senha()));

        User user = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new BadCredentialsException("Email ou senha invalidos"));

        return AuthDTO.LoginResponse.from(user);
    }

    public UserDTO.Response buscarPorEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", email));
        return UserDTO.Response.from(user);
    }

    private String normalizarAcesso(String acesso) {
        String acessoNormalizado = acesso.trim().toUpperCase();
        if (acessoNormalizado.startsWith("ROLE_")) {
            return acessoNormalizado.substring(5);
        }
        return acessoNormalizado;
    }
}
