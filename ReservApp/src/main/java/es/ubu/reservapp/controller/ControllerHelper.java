package es.ubu.reservapp.controller;

import org.springframework.ui.Model;

/**
 * Clase de ayuda para los controladores que proporciona métodos comunes.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
public class ControllerHelper {

	/**
	 * Constructor privado para evitar la instanciación de esta clase. Esta clase
	 * está diseñada para ser utilizada solo con métodos estáticos.
	 */
	private ControllerHelper() {
		// Constructor privado para evitar instanciación
	}
	
	/**
	 * Establece el modo de edición en el modelo.
	 *
	 * @param model  el modelo al que se le añadirá el atributo isEdit
	 * @param isEdit true si está en modo edición, false en caso contrario
	 */
	public static void setEditMode(Model model, boolean isEdit) {
		model.addAttribute("isEdit", isEdit);
	}

}
