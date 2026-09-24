package com.juanfran.fixflow.auth;

import com.juanfran.fixflow.usuario.Rol;
import com.juanfran.fixflow.usuario.Usuario;
import com.juanfran.fixflow.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UsuarioRepository usuarios;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @InjectMocks AuthService authService;

    private Usuario marta;

    @BeforeEach
    void setUp() {
        marta = new Usuario("Marta Sánchez", "marta@fixflow.demo", "hash", Rol.TECNICO);
    }

    @Test
    void loginCorrectoDevuelveTokenYDatosDelUsuario() {
        when(usuarios.findByEmail("marta@fixflow.demo")).thenReturn(Optional.of(marta));
        when(passwordEncoder.matches("Demo1234!", "hash")).thenReturn(true);
        when(jwtService.generarToken(marta)).thenReturn("token-de-prueba");
        when(jwtService.expiracionEnSegundos()).thenReturn(28800L);

        LoginResponse respuesta = authService.login(new LoginRequest("marta@fixflow.demo", "Demo1234!"));

        assertThat(respuesta.token()).isEqualTo("token-de-prueba");
        assertThat(respuesta.tipo()).isEqualTo("Bearer");
        assertThat(respuesta.rol()).isEqualTo(Rol.TECNICO);
        assertThat(respuesta.nombre()).isEqualTo("Marta Sánchez");
    }

    @Test
    void elEmailNoDistingueMayusculasNiEspacios() {
        when(usuarios.findByEmail("marta@fixflow.demo")).thenReturn(Optional.of(marta));
        when(passwordEncoder.matches("Demo1234!", "hash")).thenReturn(true);
        when(jwtService.generarToken(marta)).thenReturn("token");

        LoginResponse respuesta = authService.login(new LoginRequest("  Marta@FixFlow.demo ", "Demo1234!"));

        assertThat(respuesta.token()).isEqualTo("token");
    }

    @Test
    void contrasenaIncorrectaDevuelveErrorGenerico() {
        when(usuarios.findByEmail("marta@fixflow.demo")).thenReturn(Optional.of(marta));
        when(passwordEncoder.matches("mala", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("marta@fixflow.demo", "mala")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage(AuthService.CREDENCIALES_INVALIDAS);
        verify(jwtService, never()).generarToken(any());
    }

    @Test
    void emailInexistenteDevuelveElMismoErrorGenerico() {
        when(usuarios.findByEmail("nadie@fixflow.demo")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nadie@fixflow.demo", "x")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage(AuthService.CREDENCIALES_INVALIDAS);
    }

    @Test
    void usuarioDesactivadoNoPuedeEntrar() {
        marta.desactivar();
        when(usuarios.findByEmail("marta@fixflow.demo")).thenReturn(Optional.of(marta));

        assertThatThrownBy(() -> authService.login(new LoginRequest("marta@fixflow.demo", "Demo1234!")))
                .isInstanceOf(BadCredentialsException.class);
        verify(passwordEncoder, never()).matches(any(), any());
    }
}
