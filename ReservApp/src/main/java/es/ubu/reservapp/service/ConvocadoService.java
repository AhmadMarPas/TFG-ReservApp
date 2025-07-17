package es.ubu.reservapp.service;

import java.util.List;

import es.ubu.reservapp.model.entities.Convocado;
import es.ubu.reservapp.model.entities.Convocatoria;

/**
 * Interface que representa el servicio de la entidad Convocatoria.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
public interface ConvocadoService {
	
	/**
	 * Recupera la lista de convocados de una convocatoria.
	 * 
	 * @param convocatoria
	 * @return Lista de convocados
	 */
	List<Convocado> findByConvocatoria(Convocatoria convocatoria);


}
