package es.ubu.reservapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para manejar las funcionalidades que aún no están implementadas.
 * Muestra una página informativa indicando que la funcionalidad se desarrollará
 * en futuras versiones.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Controller
public class FuncionalidadFuturaController {
	
	private static final String FUNCIONALIDAD_FUTURA = "funcionalidadfutura";
	private static final String TITULO = "titulo";
	private static final String DESCRIPCION = "descripcion";
	private static final String ICONO = "icono";

    /**
     * Muestra la página informativa para "Reportes y Estadísticas".
     * 
     * @param model el modelo para pasar datos a la vista
     * @return la vista de funcionalidad no implementada
     */
    @GetMapping("/admin/reportes")
    public String reportesYEstadisticas(Model model) {
        model.addAttribute(TITULO, "Reportes y Estadísticas");
        model.addAttribute(DESCRIPCION, "Visualización de estadísticas del sistema y generación de reportes detallados");
        model.addAttribute(ICONO, "fas fa-chart-bar");
        return FUNCIONALIDAD_FUTURA;
    }

    /**
     * Muestra la página informativa para "Configuración del Sistema".
     * 
     * @param model el modelo para pasar datos a la vista
     * @return la vista de funcionalidad no implementada
     */
    @GetMapping("/admin/configuracion")
    public String configuracionDelSistema(Model model) {
        model.addAttribute(TITULO, "Configuración del Sistema");
        model.addAttribute(DESCRIPCION, "Configuración avanzada de parámetros del sistema y ajustes administrativos");
        model.addAttribute(ICONO, "fas fa-cog");
        return FUNCIONALIDAD_FUTURA;
    }

    /**
     * Muestra la página informativa para "Configuración" (usuario).
     * 
     * @param model el modelo para pasar datos a la vista
     * @return la vista de funcionalidad no implementada
     */
    @GetMapping("/configuracion")
    public String configuracionUsuario(Model model) {
        model.addAttribute(TITULO, "Configuración");
        model.addAttribute(DESCRIPCION, "Personalización de preferencias y ajustes de tu cuenta");
        model.addAttribute(ICONO, "fas fa-sliders-h");
        return FUNCIONALIDAD_FUTURA;
    }

    /**
     * Muestra la página informativa para "Ayuda y Soporte".
     * 
     * @param model el modelo para pasar datos a la vista
     * @return la vista de funcionalidad no implementada
     */
    @GetMapping("/ayuda")
    public String ayudaYSoporte(Model model) {
        model.addAttribute(TITULO, "Ayuda y Soporte");
        model.addAttribute(DESCRIPCION, "Centro de ayuda, documentación y soporte técnico");
        model.addAttribute(ICONO, "fas fa-question-circle");
        return FUNCIONALIDAD_FUTURA;
    }
}