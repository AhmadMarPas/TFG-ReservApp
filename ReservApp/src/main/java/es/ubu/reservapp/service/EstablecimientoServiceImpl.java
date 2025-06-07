package es.ubu.reservapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ubu.reservapp.model.entities.Establecimiento;
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
    @Transactional(readOnly = true)
    public List<Establecimiento> findAll() {
        return establecimientoRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Establecimiento> findById(Integer id) {
        return establecimientoRepo.findById(id);
    }

    @Override
    @Transactional // Transacción de escritura/lectura
    public Establecimiento save(Establecimiento establecimiento) {
        return establecimientoRepo.save(establecimiento);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        establecimientoRepo.deleteById(id);
    }
}
