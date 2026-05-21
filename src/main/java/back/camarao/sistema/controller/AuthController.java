package back.camarao.sistema.controller;

import back.camarao.sistema.dto.AuthDTO;
import back.camarao.sistema.dto.UsuarioDTO;
import back.camarao.sistema.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    @PostMapping({"/api/v1/auth/register", "/register"})
    public ResponseEntity<UsuarioDTO.Response> register(@Valid @RequestBody UsuarioDTO.CreateRequest dto) {
        UsuarioDTO.Response criado = usuarioService.cadastrar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/auth/me")
                .build()
                .toUri();

        return ResponseEntity.created(location).body(criado);
    }

    @PostMapping({"/api/v1/auth/login", "/login"})
    public ResponseEntity<AuthDTO.LoginResponse> login(@Valid @RequestBody AuthDTO.LoginRequest dto) {
        UsuarioService.LoginResult result = usuarioService.login(dto);

        return ResponseEntity.ok()
                .header("Set-Cookie", buildAuthCookie(result.token()).toString())
                .body(result.response());
    }

    @GetMapping({"/api/v1/auth/me", "/me"})
    public ResponseEntity<UsuarioDTO.Response> me(
            @AuthenticationPrincipal(expression = "username") String email) {
        return ResponseEntity.ok(usuarioService.buscarPorEmail(email));
    }

    @PatchMapping({"/api/v1/auth/me", "/me"})
    public ResponseEntity<AuthDTO.LoginResponse> atualizarPerfil(
            @AuthenticationPrincipal(expression = "username") String email,
            @Valid @RequestBody UsuarioDTO.UpdateProfileRequest dto) {
        UsuarioService.LoginResult result = usuarioService.atualizarPerfil(email, dto);

        return ResponseEntity.ok()
                .header("Set-Cookie", buildAuthCookie(result.token()).toString())
                .body(result.response());
    }

    @PatchMapping({"/api/v1/auth/me/senha", "/me/senha"})
    public ResponseEntity<Void> alterarSenha(
            @AuthenticationPrincipal(expression = "username") String email,
            @Valid @RequestBody UsuarioDTO.UpdatePasswordRequest dto) {
        usuarioService.alterarSenha(email, dto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping({"/api/v1/auth/logout", "/logout"})
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        return ResponseEntity.noContent()
                .header("Set-Cookie", cookie.toString())
                .build();
    }

    @GetMapping({"/api/v1/auth/me-nome", "/me-nome"})
    public ResponseEntity<String> meNome(
            @AuthenticationPrincipal(expression = "username") String email) {
        return ResponseEntity.ok(usuarioService.buscarPorEmail(email).nome());
    }

    private ResponseCookie buildAuthCookie(String token) {
        return ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("None")
                .build();
    }
}
