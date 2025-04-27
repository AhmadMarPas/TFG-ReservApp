package es.ubu.reservapp.service;

import org.springframework.stereotype.Service;

import es.ubu.reservapp.model.repositories.EstablecimientoRepo;

/**
 * Clase que implementa el servicio de la entidad Establecimiento.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Service
public class EstablecimientoServiceImpl implements EstablecimientoService {

	/**
	 * Repositorio de la entidad Perfil.
	 */
	private final EstablecimientoRepo establecimientoRepo;

	/**
	 * Constructor de la clase.
	 * 
	 * @param usuarioRepo Repositorio de la entidad Usuario.
	 */
	public EstablecimientoServiceImpl(EstablecimientoRepo establecimientoRepo) {
		this.establecimientoRepo = establecimientoRepo;
	}

}
