package es.ubu.reservapp.service;

import java.util.List;

import es.ubu.reservapp.model.entities.Convocado;
import es.ubu.reservapp.model.entities.Convocatoria;
import es.ubu.reservapp.model.repositories.ConvocadoRepo;

/**
 * Clase que implementa el servicio de la entidad Convocado.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
public class ConvocadoServiceImpl implements ConvocadoService {
	
	/**
	 * Repositorio para la entidad Convocado.	
	 */
	private final ConvocadoRepo convocadoRepo;
	
    /**
	 * Constructor del servicio que inyecta el repositorio de convocado.
	 * 
	 * @param convocadoRepo el repositorio para gestionar convocado.
     */
	public ConvocadoServiceImpl(ConvocadoRepo convocadoRepo) {
		this.convocadoRepo = convocadoRepo;
	}

	@Override
	public List<Convocado> findByConvocatoria(Convocatoria convocatoria) {
		return convocadoRepo.findByConvocatoria(convocatoria);
	}

}
