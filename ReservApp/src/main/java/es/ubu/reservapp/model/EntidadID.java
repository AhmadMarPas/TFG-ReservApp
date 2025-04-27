package es.ubu.reservapp.model;

import java.io.Serializable;

/**
 * Interfaz que define el método getId() para obtener el identificador de una
 * entidad.
 * 
 * @param <E> tipo de dato del identificador
 * 
 * @author Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
public interface EntidadID<E extends Serializable> {
	
	/**
	 * Devuelve el identificador de la entidad.
	 * 
	 * @return identificador de la entidad
	 */
	E getId();
	
	/**
	 * Establece el identificador de la entidad.
	 * 
	 * @param id identificador de la entidad
	 */
	void setId(E id);

}