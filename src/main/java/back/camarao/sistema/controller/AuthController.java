package back.camarao.sistema.controller;

import java.net.URI;

import back.camarao.sistema.dto.AuthDTO;
import back.camarao.sistema.dto.UserDTO;
import back.camarao.sistema.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping({"/api/v1/auth/register", "/register"})
    public ResponseEntity<UserDTO.Response> register(@Valid @RequestBody UserDTO.CreateRequest dto) {
        UserDTO.Response criado = userService.cadastrar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/auth/me")
                .build().toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PostMapping({"/api/v1/auth/login", "/login"})
    public ResponseEntity<AuthDTO.LoginResponse> login(@Valid @RequestBody AuthDTO.LoginRequest dto) {
        UserService.LoginResult result = userService.login(dto);
        ResponseCookie cookie = ResponseCookie.from("token", result.token())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 dias
                .sameSite("None")
                .build();
        return ResponseEntity.ok().header("Set-Cookie", cookie.toString()).body(result.response());
    }

    @GetMapping({"/api/v1/auth/me", "/me"})
    public ResponseEntity<UserDTO.Response> me(@AuthenticationPrincipal(expression = "username") String email) {
        return ResponseEntity.ok(userService.buscarPorEmail(email));
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
        return ResponseEntity.noContent().header("Set-Cookie", cookie.toString()).build();
    }

    @GetMapping({"/api/v1/auth/me-nome", "/me-nome"})
    public ResponseEntity<String> meNome(@AuthenticationPrincipal(expression = "username") String email) {
        return ResponseEntity.ok(userService.buscarPorEmail(email).nome());
    }
}
