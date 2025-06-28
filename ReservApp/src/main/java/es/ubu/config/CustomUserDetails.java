package es.ubu.config;

import java.util.Collection;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import es.ubu.reservapp.model.entities.Usuario;
import lombok.Getter;

/**
 * Clase que extiende User para incluir información adicional del usuario.
 * Implementa UserDetails para ser usado en el contexto de Spring Security.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Getter
public class CustomUserDetails extends User {

    private static final long serialVersionUID = 1L;
    
    private final String nombre;
    private final String apellidos;
    private final String correo;
    private final String telefono;

	/**
	 * Constructor que inicializa los detalles del usuario personalizado.
	 *
	 * @param usuario     Usuario del que se obtienen los detalles.
	 * @param authorities Colección de autoridades concedidas al usuario.
	 */
    public CustomUserDetails(Usuario usuario, Collection<? extends GrantedAuthority> authorities) {
        super(usuario.getId(), usuario.getPassword(), !usuario.isBloqueado(), true, true, true, authorities);
        this.nombre = usuario.getNombre();
        this.apellidos = usuario.getApellidos();
        this.correo = usuario.getCorreo();
        this.telefono = usuario.getTelefono();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(apellidos, correo, nombre, telefono);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CustomUserDetails other = (CustomUserDetails) obj;
		return Objects.equals(apellidos, other.apellidos) && Objects.equals(correo, other.correo)
				&& Objects.equals(nombre, other.nombre) && Objects.equals(telefono, other.telefono);
	}
    
}