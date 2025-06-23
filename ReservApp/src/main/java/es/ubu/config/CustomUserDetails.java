package es.ubu.config;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import es.ubu.reservapp.model.entities.Usuario;
import lombok.Getter;

@Getter
public class CustomUserDetails extends User {

    private static final long serialVersionUID = 1L;
    
    private final String nombre;
    private final String apellidos;
    private final String correo;
    private final String telefono;

    public CustomUserDetails(Usuario usuario, Collection<? extends GrantedAuthority> authorities) {
        super(usuario.getId(), usuario.getPassword(), !usuario.isBloqueado(), true, true, true, authorities);
        this.nombre = usuario.getNombre();
        this.apellidos = usuario.getApellidos();
        this.correo = usuario.getCorreo();
        this.telefono = usuario.getTelefono();
    }

}