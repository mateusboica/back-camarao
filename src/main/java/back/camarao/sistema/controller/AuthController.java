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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDTO.Response> register(@Valid @RequestBody UserDTO.CreateRequest dto) {
        UserDTO.Response criado = userService.cadastrar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/auth/me")
                .build().toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDTO.LoginResponse> login(@Valid @RequestBody AuthDTO.LoginRequest dto) {
        AuthDTO.LoginResponse response = userService.login(dto);
        ResponseCookie cookie = ResponseCookie.from("token", response.token())
                .httpOnly(true)
                .secure(false) // false para desenvolvimento local (HTTP)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 dias
                .sameSite("Lax") // Lax permite requests de terceiros
                .build();
        return ResponseEntity.ok().header("Set-Cookie", cookie.toString()).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO.Response> me(@AuthenticationPrincipal(expression = "username") String email) {
        return ResponseEntity.ok(userService.buscarPorEmail(email));
    }
}
