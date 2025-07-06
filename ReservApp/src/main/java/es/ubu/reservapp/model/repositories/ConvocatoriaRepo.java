package es.ubu.reservapp.model.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.ubu.reservapp.model.entities.Convocatoria;
import es.ubu.reservapp.model.entities.ConvocatoriaPK;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;

/**
 * Repositorio de Convocatoria.
 * <p>
 * Extiende de JpaRepository<Convocatoria, ConvocatoriaPK>
 * <p>
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface ConvocatoriaRepo extends JpaRepository<Convocatoria, ConvocatoriaPK> {

	/**
     * Método que busca las convocatorias de un usuario.
     * 
     * @param usuario Usuario para el que se buscan las convocatorias.
     * @return Lista de convocatorias para las que tiene reservas el usuario.
	 */
	List<Convocatoria> findConvocatoriaByUsuario(Usuario usuario);

	/**
	 * Método que busca un usuario por su id.
	 * 
	 * @param reserva Id del usuario.
	 * @return Lista de convocatorias para las que tienen los usuarios esa reserva.
	 */
	List<Convocatoria> findConvocatoriaByReserva(Reserva reserva);

}
