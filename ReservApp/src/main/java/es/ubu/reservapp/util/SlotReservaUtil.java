package es.ubu.reservapp.util;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.FranjaHoraria;

/**
 * Utilidad para generar slots de reserva basados en la duración y descanso del establecimiento.
 * 
 * @author Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
public class SlotReservaUtil {
	
	/**
	 * Constructor privado para evitar la instanciación de la clase.
	 */
	private SlotReservaUtil() {
		throw new UnsupportedOperationException("La clase SlotReservaUtil no puede ser instanciada");
	}

    /**
     * Clase que representa un slot de tiempo disponible para reserva.
     */
    public static class SlotTiempo {
        private LocalTime horaInicio;
        private LocalTime horaFin;
        private String etiqueta;
        
        public SlotTiempo() {
        	
        }
        
        public SlotTiempo(LocalTime horaInicio, LocalTime horaFin) {
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
            this.etiqueta = horaInicio + " - " + horaFin;
        }
        
        // Getters y setters
        public LocalTime getHoraInicio() {
            return horaInicio;
        }
        
        public void setHoraInicio(LocalTime horaInicio) {
            this.horaInicio = horaInicio;
        }
        
        public LocalTime getHoraFin() {
            return horaFin;
        }
        
        public void setHoraFin(LocalTime horaFin) {
            this.horaFin = horaFin;
        }
        
        public String getEtiqueta() {
            return etiqueta;
        }
        
        public void setEtiqueta(String etiqueta) {
            this.etiqueta = etiqueta;
        }
    }
    
    /**
     * Genera los slots de tiempo disponibles para un establecimiento y franja horaria específica.
     * 
     * @param establecimiento El establecimiento para el cual generar los slots
     * @param franjaHoraria La franja horaria del día específico
     * @return Lista de slots de tiempo disponibles
     */
    public static List<SlotTiempo> generarSlotsDisponibles(Establecimiento establecimiento, FranjaHoraria franjaHoraria) {
        List<SlotTiempo> slots = new ArrayList<>();

        // Si el establecimiento no tiene duración definida o es 0, no generar slots predefinidos
        if (establecimiento.getDuracionReserva() == null || establecimiento.getDuracionReserva() <= 0) {
            return slots;
        }

        int duracionMinutos = establecimiento.getDuracionReserva();
        int descansoMinutos = establecimiento.getDescansoServicios() != null ? establecimiento.getDescansoServicios() : 0;

        LocalTime horaActual = franjaHoraria.getHoraInicio();
        LocalTime horaFinFranja = franjaHoraria.getHoraFin();

        while (horaActual.isBefore(horaFinFranja)) {
            LocalTime horaFinSlot = horaActual.plusMinutes(duracionMinutos);

            // Verificar que el slot completo esté dentro de la franja horaria
            if (horaFinSlot.isAfter(horaFinFranja)) {
                horaActual = horaFinFranja; // Fuerza la salida del bucle
            } else {
                slots.add(new SlotTiempo(horaActual, horaFinSlot));

                // Avanzar al siguiente slot considerando el tiempo de descanso
                LocalTime siguienteHora = horaFinSlot.plusMinutes(descansoMinutos);

                // Verificar que no haya desbordamiento de horas (evitar que pase de 23:59 a 00:00)
                if (siguienteHora.isBefore(horaFinSlot)) {
                    horaActual = horaFinFranja; // Fuerza la salida del bucle
                } else {
                    horaActual = siguienteHora;
                }
            }
        }

        return slots;
    }
    
    /**
     * Verifica si un establecimiento requiere slots predefinidos.
     * 
     * @param establecimiento El establecimiento a verificar
     * @return true si requiere slots predefinidos, false en caso contrario
     */
    public static boolean requiereSlotsPredefinidos(Establecimiento establecimiento) {
        return establecimiento.getDuracionReserva() != null && establecimiento.getDuracionReserva() > 0;
    }
}