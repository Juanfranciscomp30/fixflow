package com.juanfran.fixflow.usuario;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    /** Lo justo para pintar un desplegable: nunca devolvemos email ni hash de contraseña. */
    public record TecnicoDto(Long id, String nombre) {
    }

    private final UsuarioRepository usuarios;

    public UsuarioController(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping("/tecnicos")
    public List<TecnicoDto> tecnicos() {
        return usuarios.findByRolAndActivoTrueOrderByNombre(Rol.TECNICO).stream()
                .map(u -> new TecnicoDto(u.getId(), u.getNombre()))
                .toList();
    }
}
