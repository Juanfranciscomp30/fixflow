package com.juanfran.fixflow.auth;

import com.juanfran.fixflow.usuario.Usuario;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final JwtProperties props;

    public JwtService(JwtEncoder encoder, JwtProperties props) {
        this.encoder = encoder;
        this.props = props;
    }

    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConfig.ISSUER)
                .issuedAt(ahora)
                .expiresAt(ahora.plus(props.expiracion()))
                .subject(String.valueOf(usuario.getId()))
                .claim("nombre", usuario.getNombre())
                .claim("rol", usuario.getRol().name())
                .build();
        // Hay que indicar HS256 explícitamente; si no, Nimbus busca una clave RSA
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long expiracionEnSegundos() {
        return props.expiracion().toSeconds();
    }
}
