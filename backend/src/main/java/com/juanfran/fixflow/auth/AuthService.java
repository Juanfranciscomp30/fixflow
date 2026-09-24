package com.juanfran.fixflow.auth;

import com.juanfran.fixflow.usuario.Usuario;
import com.juanfran.fixflow.usuario.UsuarioRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    // Mismo mensaje para email inexistente, contraseña incorrecta o usuario desactivado:
    // así no se puede averiguar qué emails existen en el sistema.
    static final String CREDENCIALES_INVALIDAS = "Email o contraseña incorrectos";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarios.findByEmail(request.email().trim().toLowerCase())
                .filter(Usuario::isActivo)
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException(CREDENCIALES_INVALIDAS));

        return new LoginResponse(
                jwtService.generarToken(usuario),
                "Bearer",
                jwtService.expiracionEnSegundos(),
                usuario.getNombre(),
                usuario.getRol());
    }
}
