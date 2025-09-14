package es.ubu.reservapp.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.FranjaHoraria;
import es.ubu.reservapp.model.repositories.EstablecimientoRepo;

/**
 * Implementación del servicio para la gestión de Establecimientos.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Service
public class EstablecimientoServiceImpl implements EstablecimientoService {

    private final EstablecimientoRepo establecimientoRepo;

	/**
	 * Constructor de la clase.
	 * 
	 * @param establecimientoRepo Repositorio de la entidad Establecimiento.
	 */
    public EstablecimientoServiceImpl(EstablecimientoRepo establecimientoRepo) {
        this.establecimientoRepo = establecimientoRepo;
    }

    @Override
    public List<Establecimiento> findAll() {
        return establecimientoRepo.findAll();
    }

    @Override
    public Optional<Establecimiento> findById(Integer id) {
        Optional<Establecimiento> establecimientoOptional = establecimientoRepo.findById(id);
        establecimientoOptional.ifPresent(establecimiento -> Hibernate.initialize(establecimiento.getFranjasHorarias()));
        return establecimientoOptional;
    }

    @Override
    public Establecimiento save(Establecimiento establecimiento) {
        return establecimientoRepo.save(establecimiento);
    }

    @Override
    public void deleteById(Integer id) {
    	// TODO: Manejar excepciones si el establecimiento tiene reservas asociadas
        establecimientoRepo.deleteById(id);
    }

	@Override
	public List<Establecimiento> findAllById(List<Integer> ids) {
		return establecimientoRepo.findAllById(ids);
	}

	@Override
	public List<Establecimiento> findAllAndFranjaHoraria() {
		List<Establecimiento> establecimientos = establecimientoRepo.findAll();
        for (Establecimiento establecimiento : establecimientos) {
            if (establecimiento.getFranjasHorarias() != null) {
                establecimiento.setFranjasHorarias(new ArrayList<>(establecimiento.getFranjasHorarias()));
                establecimiento.getFranjasHorarias().sort(Comparator.comparing(FranjaHoraria::getDiaSemana));
            }
        }
		return establecimientos;
	}

	@Override
	public boolean estaAbiertoEnFecha(Establecimiento establecimiento, LocalDate fecha) {
		if (establecimiento == null || fecha == null || establecimiento.getFranjasHorarias() == null) {
			return false;
		}
		
		DayOfWeek diaSemanaFecha = fecha.getDayOfWeek();
		
		return establecimiento.getFranjasHorarias().stream()
				.anyMatch(franja -> {
					DayOfWeek diaFranja = franja.getDiaSemana();
					return diaFranja != null && diaFranja.equals(diaSemanaFecha);
				});
	}

}