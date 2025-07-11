package es.ubu.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.repositories.UsuarioRepo;

/**
 * Servicio que implementa UserDetailsService para cargar detalles de usuario
 * desde un repositorio.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

	/**
	 * Repositorio para acceder a los datos de usuario.
	 */
    private UsuarioRepo usuarioRepo;
    
	/**
	 * Constructor que inyecta el repositorio de usuarios.
	 *
	 * @param usuarioRepo Repositorio de usuarios.
	 */
	public CustomUserDetailsService(UsuarioRepo usuarioRepo) {
		this.usuarioRepo = usuarioRepo;
	}

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepo.findById(username).orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // Añadir rol de administrador si corresponde
        if (usuario.isAdministrador()) {
            authorities.add(new SimpleGrantedAuthority("ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("USER"));
        }
        
        // Añadir roles adicionales desde perfiles si existen
        if (usuario.getLstPerfiles() != null) {
            usuario.getLstPerfiles().forEach(perfil -> 
                authorities.add(new SimpleGrantedAuthority(perfil.getNombre()))
            );
        }

        // Crear un UserDetails personalizado que incluya los campos adicionales del usuario
        return new CustomUserDetails(usuario, authorities);
    }
}