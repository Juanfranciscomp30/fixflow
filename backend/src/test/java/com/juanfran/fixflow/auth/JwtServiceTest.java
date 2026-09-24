package com.juanfran.fixflow.auth;

import com.juanfran.fixflow.usuario.Rol;
import com.juanfran.fixflow.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Prueba real de firma y verificación, sin arrancar Spring. */
class JwtServiceTest {

    private final JwtConfig config = new JwtConfig();
    private JwtService jwtService;
    private JwtDecoder decoder;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties("clave-de-test-de-al-menos-32-caracteres!!", Duration.ofHours(8));
        SecretKey key = config.jwtSecretKey(props);
        jwtService = new JwtService(config.jwtEncoder(key), props);
        decoder = config.jwtDecoder(key);
    }

    @Test
    void elTokenLlevaIdNombreYRolYCaducaEn8Horas() {
        Usuario admin = new Usuario("Carmen Ortiz", "admin@fixflow.demo", "hash", Rol.ADMIN);
        ReflectionTestUtils.setField(admin, "id", 1L);

        Jwt jwt = decoder.decode(jwtService.generarToken(admin));

        assertThat(jwt.getSubject()).isEqualTo("1");
        assertThat(jwt.getClaimAsString("nombre")).isEqualTo("Carmen Ortiz");
        assertThat(jwt.getClaimAsString("rol")).isEqualTo("ADMIN");
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofHours(8));
    }

    @Test
    void rechazaTokensFirmadosConOtraClave() {
        JwtProperties otra = new JwtProperties("otra-clave-distinta-tambien-de-32-caracteres", Duration.ofHours(8));
        JwtService otroServicio = new JwtService(config.jwtEncoder(config.jwtSecretKey(otra)), otra);
        Usuario usuario = new Usuario("Antonio Ruiz", "antonio@fixflow.demo", "hash", Rol.TECNICO);
        ReflectionTestUtils.setField(usuario, "id", 2L);

        String tokenFalso = otroServicio.generarToken(usuario);

        assertThatThrownBy(() -> decoder.decode(tokenFalso)).isInstanceOf(JwtException.class);
    }

    @Test
    void noArrancaConUnSecretoDemasiadoCorto() {
        assertThatThrownBy(() -> new JwtProperties("corto", Duration.ofHours(8)))
                .isInstanceOf(IllegalStateException.class);
    }
}
