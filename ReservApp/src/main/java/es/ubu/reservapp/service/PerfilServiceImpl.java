package es.ubu.reservapp.service;

import org.springframework.stereotype.Service;

import es.ubu.reservapp.model.entities.Perfil;
import es.ubu.reservapp.model.repositories.PerfilRepo;

/**
 * Clase que implementa el servicio de la entidad Perfil.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Service
public class PerfilServiceImpl implements PerfilService {

	/**
	 * Repositorio de la entidad Perfil.
	 */
	private final PerfilRepo perfilRepo;

	/**
	 * Constructor de la clase.
	 * 
	 * @param usuarioRepo Repositorio de la entidad Usuario.
	 */
	public PerfilServiceImpl(PerfilRepo perfilRepo) {
		this.perfilRepo = perfilRepo;
	}

	@Override
	public void save(Perfil perfil) {
		perfilRepo.save(perfil);
	}

}
