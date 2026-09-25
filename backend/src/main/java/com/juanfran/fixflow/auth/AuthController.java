package com.juanfran.fixflow.auth;

import com.juanfran.fixflow.usuario.Rol;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** Devuelve quién está autenticado según el token. El frontend lo usa al recargar la página. */
    @GetMapping("/me")
    public UsuarioActual me(@AuthenticationPrincipal Jwt jwt) {
        return UsuarioActual.desde(jwt);
    }
}
