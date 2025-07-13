package es.ubu.reservapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ubu.reservapp.model.entities.Convocatoria;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.repositories.ConvocatoriaRepo;

/**
 * Clase que implementa el servicio de la entidad Convocatoria.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Service
public class ConvocatoriaServiceImpl implements ConvocatoriaService {

	/**
	 * Repositorio para la entidad Convocatoria.	
	 */
    private final ConvocatoriaRepo convocatoriaRepo;

    /**
	 * Constructor del servicio que inyecta el repositorio de convocatorias.
	 * 
	 * @param convocatoriaRepo el repositorio para gestionar convocatorias.
     */
    public ConvocatoriaServiceImpl(ConvocatoriaRepo convocatoriaRepo) {
        this.convocatoriaRepo = convocatoriaRepo;
    }

	@Override
	public Optional<Convocatoria> findById(Integer id) {
		return convocatoriaRepo.findById(id);
	}

	/**
	 * Método para obtener el nombre del servicio.
	 * 
	 * @return el nombre del servicio.
	 */
    @Override
    @Transactional(readOnly = true)
    public List<Convocatoria> findAll() {
        return convocatoriaRepo.findAll();
    }

	/**
	 * Método para guardar un perfil.
	 * 
	 * @param convocatoria el perfil a guardar.
	 * @return el perfil guardado.
	 */
    @Override
    @Transactional
    public Convocatoria save(Convocatoria convocatoria) {
        return convocatoriaRepo.save(convocatoria);
    }

	@Override
	public List<Convocatoria> findConvocatoriaByReserva(Reserva reserva) {
		return convocatoriaRepo.findConvocatoriaByReserva(reserva);
	}

	@Override
    @Transactional
    public void deleteByReserva(Reserva reserva) {
        convocatoriaRepo.deleteByReserva(reserva);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Convocatoria findByIdIgnoringValido(Integer idReserva) {
    	Convocatoria convocatoria = convocatoriaRepo.findByIdReservaIgnoringValido(idReserva).orElse(null);
    	if (convocatoria != null && convocatoria.getConvocados() != null && !convocatoria.getConvocados().isEmpty()) {
			// Evitar LazyInitializationException al acceder a los convocados
			// Esto es necesario si la sesión de Hibernate ya se ha cerrado
			// y queremos acceder a los datos de los convocados.
			// Se inicializa la colección para evitar problemas de carga perezosa.
			// Inicializar la lista de convocados para evitar LazyInitializationException
			convocatoria.getConvocados().forEach(convocado -> {
				// Asegurarse de que cada convocado tenga su usuario inicializado
				if (convocado.getUsuario() != null) {
					convocado.getUsuario().getId(); // Acceder al ID para forzar la carga
				}
			});
    		
    	}
        return convocatoria;
	}

    @Override
    @Transactional
    public Convocatoria merge(Convocatoria convocatoria) {
        // Usar save() que internamente hace merge si la entidad ya tiene ID
        return convocatoriaRepo.save(convocatoria);
    }

}