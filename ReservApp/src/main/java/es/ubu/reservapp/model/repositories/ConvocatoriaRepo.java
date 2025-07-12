package es.ubu.reservapp.model.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.ubu.reservapp.model.entities.Convocatoria;
import es.ubu.reservapp.model.entities.Reserva;

/**
 * Repositorio de Convocatoria.
 * <p>
 * Extiende de JpaRepository<Convocatoria, Integer>
 * <p>
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface ConvocatoriaRepo extends JpaRepository<Convocatoria, Integer> {

	/**
	 * Método que busca un usuario por su id.
	 * 
	 * @param reserva Id del usuario.
	 * @return Lista de convocatorias para las que tienen los usuarios esa reserva.
	 */
	List<Convocatoria> findConvocatoriaByReserva(Reserva reserva);


	/**
	 * Método que elimina todas las convocatorias asociadas a una reserva.
	 * 
	 * @param reserva Reserva cuyas convocatorias se van a eliminar.
	 */
	void deleteByReserva(Reserva reserva);

    /**
     * Busca una convocatoria por su ID incluyendo las marcadas como inválidas (soft delete).
     * Usa SQL nativo para evitar que Hibernate aplique automáticamente la condición WHERE valido = true.
     * 
     * @param idReserva ID de la reserva
     * @param idUsuario ID del usuario
     * @return Optional con la convocatoria si existe, incluso si está marcada como inválida
     */
    @Query(value = "SELECT * FROM convocatoria WHERE id_reserva_pk = :idReserva", nativeQuery = true)
    Optional<Convocatoria> findByIdReservaIgnoringValido(@Param("idReserva") Integer idReserva);

}
