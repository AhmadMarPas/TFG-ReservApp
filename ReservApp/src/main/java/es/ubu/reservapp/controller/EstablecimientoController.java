package es.ubu.reservapp.controller;

import org.springframework.stereotype.Controller;

import es.ubu.reservapp.service.EstablecimientoService;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador de Establecimiento.
 * <p>
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Controller
@Slf4j
public class EstablecimientoController {
	
	/**
	 * Servicio de la entidad Establecimiento.
	 */
	private EstablecimientoService establecimientoService;
	
	/**
	 * Constructor de EstablecimientoController.
	 * 
	 * @param establecimientoService Servicio de la entidad Establecimiento
	 */
	public EstablecimientoController(EstablecimientoService establecimientoService) {
		this.establecimientoService = establecimientoService;
	}

}
