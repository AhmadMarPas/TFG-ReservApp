package es.ubu.reservapp.service;

import java.util.List;
import java.util.Optional;

import es.ubu.reservapp.model.entities.Convocatoria;
import es.ubu.reservapp.model.entities.ConvocatoriaPK;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;

/**
 * Interface que representa el servicio de la entidad Convocatoria.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
public interface ConvocatoriaService {

	/**
	 *  busca la convocatoria por id.
	 *  
	 * @param id Id de la convocatoria.
	 * @return en caso de encontrarlo, devuelve la convocatoria.
	 */
	Optional<Convocatoria> findById(ConvocatoriaPK id);
	
	/**
	 * Guarda o actualiza una convocatoria.
	 * 
	 * @param convocatoria la convocatoria a guardar o actualizar.
	 * @return 
	 */
	Convocatoria save(Convocatoria convocatoria);

	/**
	 * Busca las convocatorias de un usuario.
	 * 
	 * @param usuario usuario al que se buscan las convocatorias.
	 * @return Lista de convocatorias del usuario.
	 */
	List<Convocatoria> findConvocatoriaByUsuario(Usuario usuario);

	/**
	 * Busca las convocatorias de una reserva.
	 * 
	 * @param reserva Reserva a la que se busca las convocatorias.
	 * @return Lista de convocatorias de la reserva.
	 */
	List<Convocatoria> findConvocatoriaByReserva(Reserva reserva);

	/**
	 * Recupera todas las convocatorias.
	 * 
	 * @return una lista de todas las convocatorias.
	 */
	List<Convocatoria> findAll();
    
    /**
     * Elimina todas las convocatorias asociadas a una reserva.
     * 
     * @param reserva Reserva cuyas convocatorias se van a eliminar.
     */
    void deleteByReserva(Reserva reserva);

    /**
     * Busca una convocatoria por su ID incluyendo las marcadas como inválidas (soft delete).
     * 
     * @param idReserva ID de la reserva
     * @param idUsuario ID del usuario
     * @return Optional con la convocatoria si existe, incluso si está marcada como inválida
     */
    Optional<Convocatoria> findByIdIgnoringValido(Integer idReserva, String idUsuario);

    
    /**
     * Fusiona una convocatoria existente con el contexto de persistencia.
     * Útil para entidades recuperadas con consultas nativas.
     * 
     * @param convocatoria la convocatoria a fusionar
     * @return la convocatoria fusionada
     */
    Convocatoria merge(Convocatoria convocatoria);

}
