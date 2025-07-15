package es.ubu.reservapp.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import es.ubu.reservapp.exception.UserNotFoundException;
import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.Usuario;

/**
 * Interface que representa el servicio de la entidad Usuario.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
public interface UsuarioService {

	/**
	 * Guarda o actualiza un usuario.
	 * 
	 * @param user el usuario a guardar o actualizar.
	 */
	@Transactional
	void save(Usuario user);

	/**
	 * Busca un usuario por su ID.
	 * 
	 * @param id el ID del usuario a buscar.
	 * @return el usuario encontrado, o null si no se encuentra.
	 */
	@Transactional(readOnly = true)
	Usuario findUsuarioById(String id);

	/**
	 * Valida la autenticación de un usuario.
	 * 
	 * @param id       el ID del usuario.
	 * @param password la contraseña del usuario.
	 * @return el usuario autenticado si las credenciales son válidas, o null si no lo son.
	 */
    @Transactional(readOnly=true)
	Usuario validateAuthentication(String id, String password);

	/**
	 * Comprueba si existe un usuario con el correo electrónico proporcionado.
	 * 
	 * @param correoUsuario el correo electrónico del usuario a comprobar.
	 * @return true si existe un usuario con ese correo, false en caso contrario.
	 */
    @Transactional(readOnly=true)
	boolean existeEmail(String correoUsuario);

	/**
	 * Comprueba si existe un usuario con el ID proporcionado.
	 * 
	 * @param id el ID del usuario a comprobar.
	 * @return true si existe un usuario con ese ID, false en caso contrario.
	 */
    @Transactional(readOnly=true)
	boolean existeId(String id);

	/**
	 * Recupera todos los usuarios.
	 * 
	 * @return una lista de todos los usuarios.
	 */
    @Transactional(readOnly=true)
	List<Usuario> findAll();

	/**
	 * Busca un usuario por su correo electrónico.
	 * 
	 * @param correo el correo electrónico del usuario a buscar.
	 * @return el usuario encontrado, o null si no se encuentra.
	 */
    @Transactional(readOnly=true)
	Usuario findUsuarioByCorreo(String correo);

	/**
	 * Bloquea un usuario por su ID.
	 * 
	 * @param id el ID del usuario a bloquear.
	 * @throws UserNotFoundException si no se encuentra el usuario o ya está
	 *                               bloqueado.
	 */
	@Transactional
	void blockUser(String id) throws UserNotFoundException;

	/**
	 * Desbloquea un usuario por su ID.
	 * 
	 * @param id el ID del usuario a desbloquear.
	 * @throws UserNotFoundException si no se encuentra el usuario o ya está
	 *                               desbloqueado.
	 */
	@Transactional
	void unblockUser(String id) throws UserNotFoundException;
	
	/**
	 * Elimina un usuario por su ID.
	 * 
	 * @param id el ID del usuario a eliminar.
	 */
    @Transactional
	void deleteById(String id) throws UserNotFoundException;
	
    /**
     * Método común para mostrar la asignación de establecimientos.
     * 
     * @param userId ID del usuario
     * @param model Modelo de la vista
     * @return model Modelo con los objetos a mostrar
     */
    @Transactional(readOnly=true)
	Model recuperarEstablecimientosUsuario(String userId, Model model);

    /**
     * Método común para mostrar la asignación de establecimientos.
     * 
     * @param usuario Usuario al que se le obtienen los establecimientos
     * @param model Modelo de la vista
     * @return model Modelo con los objetos a mostrar
     */
    @Transactional(readOnly=true)
	Model obtenerEstablecimientosUsuario(Usuario usuario, Model model);

    /**
     * Asgigna los establecimientos al usuario.
     * 
     * @param usuario Usuario al que se le asignan los establecimientos.
     * @param establecimientos Establecimientos a asignar.
     * @return usuario con los establecimientos asignados.
     */
    @Transactional
	Usuario asignarEstablecimientos(Usuario usuario, List<Establecimiento> establecimientos);

    /**
     * Comprueba si el establecimiento está asignado al usuario.
     * 
     * @param usuario
     * @param establecimiento
     * @return
     */
    @Transactional(readOnly=true)
	boolean establecimientoAsignado(Usuario usuario, Establecimiento establecimiento);

    /**
	 * Busca usuarios por un criterio de búsqueda.
	 * 
	 * @param query el criterio de búsqueda.
	 * @return una lista de usuarios que coinciden con el criterio de búsqueda.
	 */
    @Transactional(readOnly=true)
    List<Usuario> buscarUsuarioSegunQuery(String query);
}
