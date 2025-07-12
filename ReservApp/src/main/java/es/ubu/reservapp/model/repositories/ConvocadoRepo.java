package es.ubu.reservapp.model.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.ubu.reservapp.model.entities.Convocado;
import es.ubu.reservapp.model.entities.ConvocadoPK;
import es.ubu.reservapp.model.entities.Convocatoria;

/**
 * Repositorio de Convocado.
 * <p>
 * Extiende de JpaRepository<Convocado, ConvocadoPK>
 * <p>
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
public interface ConvocadoRepo extends JpaRepository<Convocado, ConvocadoPK> {

	/**
	 * Recupera la lista de convocados de una convocatoria.
	 * 
	 * @param convocatoria
	 * @return Lista de convocados
	 */
	List<Convocado> findByConvocatoria(Convocatoria convocatoria);

}
