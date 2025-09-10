package es.ubu.reservapp.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import es.ubu.reservapp.model.entities.Establecimiento;

/**
 * Interfaz para el servicio de gestión de Establecimientos.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
public interface EstablecimientoService {

    /**
     * Recupera todos los establecimientos.
     * 
     * @return una lista de todos los establecimientos.
     */
    @Transactional(readOnly = true)
    List<Establecimiento> findAll();

    /**
     * Recupera todos los establecimientos.
     * 
     * @return una lista de todos los establecimientos.
     */
    @Transactional(readOnly = true)
    List<Establecimiento> findAllAndFranjaHoraria();

    /**
     * Busca un establecimiento por su ID.
     * 
     * @param id el ID del establecimiento a buscar.
     * @return un Optional conteniendo el establecimiento si se encuentra, o un Optional vacío si no.
     */
    @Transactional(readOnly = true)
    Optional<Establecimiento> findById(Integer id);

    /**
     * Guarda o actualiza un establecimiento.
     * Si el establecimiento tiene un ID nulo, se considera una nueva entidad.
     * Si tiene un ID, se intenta actualizar la entidad existente.
     * 
     * @param establecimiento el establecimiento a guardar.
     * @return el establecimiento guardado (puede tener el ID actualizado si es nuevo).
     */
    @Transactional
    Establecimiento save(Establecimiento establecimiento);
    
    /**
     * Recupera una lista de establecimientos basada en una lista de IDs.
     * 
     * @param ids lista de IDs de los establecimientos a buscar
     * @return lista de establecimientos encontrados que corresponden a los IDs proporcionados
     */
    @Transactional(readOnly = true)
    List<Establecimiento> findAllById(List<Integer> ids);

    /**
     * Elimina un establecimiento por su ID.
     * 
     * @param id el ID del establecimiento a eliminar.
     */
    @Transactional
    void deleteById(Integer id);

    /**
	 * Verifica si un establecimiento está abierto en una fecha específica.
	 * 
	 * @param establecimiento el establecimiento a verificar.
	 * @param fecha la fecha a comprobar.
	 * @return true si el establecimiento está abierto en la fecha dada, false en caso contrario.
	 */
	boolean estaAbiertoEnFecha(Establecimiento establecimiento, LocalDate fecha);

}
