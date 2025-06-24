package es.ubu.reservapp.util;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Clase de utilidades para manejar operaciones relacionadas con fechas.
 * 
 * Proporciona métodos para formatear días de la semana en español.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
public class FechaUtil {

	/**
	 * Constructor privado para evitar la instanciación de la clase.
	 */
    private FechaUtil() {
        // Constructor privado para evitar instanciación
    }

	/**
	 * Formatea un día de la semana a su representación en español.
	 * 
	 * @param dia El día de la semana a formatear.
	 * @return El nombre del día de la semana en español, con la primera letra en
	 *         mayúscula.
	 */
    public static String formatearDiaSemana(DayOfWeek dia) {
        if (dia == null) {
            return "";
        }
        String diaFormateado = dia.getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("es-ES"));
        
        // Capitalizar la primera letra para asegurar un formato consistente (ej. "Lunes" en vez de "lunes")
        if (diaFormateado == null || diaFormateado.isEmpty()) {
            return "";
        }
        return diaFormateado.substring(0, 1).toUpperCase() + diaFormateado.substring(1);
    }
    
    /**
     * Formatea un número de mes a su representación en español.
     * 
     * @param mes El número del mes (1-12) a formatear.
     * @return El nombre del mes en español, con la primera letra en mayúscula.
     */
    public static String formatearMes(int mes) {
        if (mes < 1 || mes > 12) {
            return "";
        }
        
        Month month = Month.of(mes);
        String mesFormateado = month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("es-ES"));
        
        // Capitalizar la primera letra para asegurar un formato consistente (ej. "Enero" en vez de "enero")
        if (mesFormateado == null || mesFormateado.isEmpty()) {
            return "";
        }
        return mesFormateado.substring(0, 1).toUpperCase() + mesFormateado.substring(1);
    }
}