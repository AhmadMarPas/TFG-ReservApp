package es.ubu.reservapp.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.FranjaHoraria;
import es.ubu.reservapp.model.entities.Reserva;

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
        private boolean disponible;
        private int reservasExistentes;
        private int aforoMaximo;
        
        public SlotTiempo() {
        	
        }
        
        public SlotTiempo(LocalTime horaInicio, LocalTime horaFin) {
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
            this.etiqueta = horaInicio + " - " + horaFin;
            this.disponible = true;
            this.reservasExistentes = 0;
            this.aforoMaximo = 0;
        }

        public SlotTiempo(LocalTime horaInicio, LocalTime horaFin, boolean disponible, int reservasExistentes, int aforoMaximo) {
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
            this.etiqueta = horaInicio + " - " + horaFin;
            this.disponible = disponible;
            this.reservasExistentes = reservasExistentes;
            this.aforoMaximo = aforoMaximo;
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

        public boolean isDisponible() {
            return disponible;
        }

        public void setDisponible(boolean disponible) {
            this.disponible = disponible;
        }

        public int getReservasExistentes() {
            return reservasExistentes;
        }

        public void setReservasExistentes(int reservasExistentes) {
            this.reservasExistentes = reservasExistentes;
        }

        public int getAforoMaximo() {
            return aforoMaximo;
        }

        public void setAforoMaximo(int aforoMaximo) {
            this.aforoMaximo = aforoMaximo;
        }

        /**
         * Obtiene información de disponibilidad para mostrar al usuario.
         */
        public String getInfoDisponibilidad() {
            if (aforoMaximo <= 0) {
                return "Sin límite";
            }
            return String.format("%d/%d disponibles", (aforoMaximo - reservasExistentes), aforoMaximo);
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
        // También si la duración es mayor que el tiempo disponible en la franja horaria
        if (establecimiento.getDuracionReserva() == null || establecimiento.getDuracionReserva() <= 0 ||
            establecimiento.getDuracionReserva() > Duration.between(franjaHoraria.getHoraInicio(), franjaHoraria.getHoraFin()).toMinutes()) {
            return slots;
        }

        int descansoMinutos = establecimiento.getDescansoServicios() != null ? establecimiento.getDescansoServicios() : 0;
        int duracionMinutos = establecimiento.getDuracionReserva();

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
     * Genera los slots de tiempo disponibles considerando las reservas existentes y el aforo.
     * 
     * @param establecimiento El establecimiento para el cual generar los slots
     * @param franjaHoraria La franja horaria del día específico
     * @param fecha La fecha para la cual generar los slots
     * @param reservasDelDia Lista de reservas existentes en esa fecha
     * @return Lista de slots de tiempo con información de disponibilidad
     */
    public static List<SlotTiempo> generarSlotsConDisponibilidad(Establecimiento establecimiento, 
                                                               FranjaHoraria franjaHoraria, 
                                                               LocalDate fecha,
                                                               List<Reserva> reservasDelDia) {
        List<SlotTiempo> slotsBase = generarSlotsDisponibles(establecimiento, franjaHoraria);
        
        // Si no hay aforo definido o es 0, todos los slots están disponibles
        Integer aforo = establecimiento.getAforo();
        if (aforo == null || aforo <= 0) {
            return slotsBase;
        }

        // Calcular disponibilidad para cada slot
        return slotsBase.stream()
            .map(slot -> {
                int reservasEnSlot = contarReservasEnSlot(slot, reservasDelDia);
                boolean disponible = reservasEnSlot < aforo;
                return new SlotTiempo(slot.getHoraInicio(), slot.getHoraFin(), disponible, reservasEnSlot, aforo);
            }).toList();
    }

    /**
     * Cuenta cuántas reservas se solapan con un slot específico.
     * 
     * @param slot El slot a verificar
     * @param reservasDelDia Lista de reservas del día
     * @return Número de reservas que se solapan con el slot
     */
    private static int contarReservasEnSlot(SlotTiempo slot, List<Reserva> reservasDelDia) {
        return (int) reservasDelDia.stream()
            .filter(reserva -> seSolapanHorarios(
                slot.getHoraInicio(), slot.getHoraFin(),
                reserva.getFechaReserva().toLocalTime(), 
                reserva.getHoraFin() != null ? reserva.getHoraFin() : reserva.getFechaReserva().toLocalTime().plusHours(1)
            ))
            .count();
    }

    /**
     * Verifica si dos horarios se solapan.
     * 
     * @param inicio1 Hora de inicio del primer horario
     * @param fin1 Hora de fin del primer horario
     * @param inicio2 Hora de inicio del segundo horario
     * @param fin2 Hora de fin del segundo horario
     * @return true si los horarios se solapan, false en caso contrario
     */
    public static boolean seSolapanHorarios(LocalTime inicio1, LocalTime fin1, LocalTime inicio2, LocalTime fin2) {
        // Dos horarios se solapan si:
        // - El inicio del primero es antes del fin del segundo Y
        // - El fin del primero es después del inicio del segundo
        return inicio1.isBefore(fin2) && fin1.isAfter(inicio2);
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

    /**
     * Filtra los slots disponibles según el aforo del establecimiento.
     * 
     * @param slots Lista de slots base
     * @param establecimiento Establecimiento con información de aforo
     * @param reservasDelDia Reservas existentes en el día
     * @return Lista de slots filtrados por disponibilidad
     */
    public static List<SlotTiempo> filtrarSlotsPorAforo(List<SlotTiempo> slots, 
                                                       Establecimiento establecimiento, 
                                                       List<Reserva> reservasDelDia) {
        Integer aforo = establecimiento.getAforo();
        
        // Si no hay límite de aforo, devolver todos los slots
        if (aforo == null || aforo <= 0) {
            return slots;
        }

        return slots.stream()
            .map(slot -> {
                int reservasEnSlot = contarReservasEnSlot(slot, reservasDelDia);
                boolean disponible = reservasEnSlot < aforo;
                return new SlotTiempo(slot.getHoraInicio(), slot.getHoraFin(), disponible, reservasEnSlot, aforo);
            }).toList();
    }

    /**
     * Obtiene información detallada de disponibilidad para un horario específico.
     * 
     * @param establecimiento Establecimiento a verificar
     * @param horaInicio Hora de inicio del horario
     * @param horaFin Hora de fin del horario
     * @param reservasDelDia Reservas existentes en el día
     * @return Información de disponibilidad
     */
    public static DisponibilidadInfo obtenerInfoDisponibilidad(Establecimiento establecimiento,
                                                              LocalTime horaInicio, LocalTime horaFin,
                                                              List<Reserva> reservasDelDia) {
        Integer aforo = establecimiento.getAforo();
        
        if (aforo == null || aforo <= 0) {
            return new DisponibilidadInfo(true, 0, 0, "Sin límite de aforo");
        }

        SlotTiempo slotTemporal = new SlotTiempo(horaInicio, horaFin);
        int reservasExistentes = contarReservasEnSlot(slotTemporal, reservasDelDia);
        boolean disponible = reservasExistentes < aforo;
        
        String mensaje = disponible ? 
            String.format("Disponible (%d/%d ocupado)", reservasExistentes, aforo) :
            String.format("No disponible (%d/%d ocupado)", reservasExistentes, aforo);

        return new DisponibilidadInfo(disponible, reservasExistentes, aforo, mensaje);
    }

    /**
     * Clase auxiliar para encapsular información de disponibilidad.
     */
    public static class DisponibilidadInfo {
        private final boolean disponible;
        private final int reservasExistentes;
        private final int aforoMaximo;
        private final String mensaje;

        public DisponibilidadInfo(boolean disponible, int reservasExistentes, int aforoMaximo, String mensaje) {
            this.disponible = disponible;
            this.reservasExistentes = reservasExistentes;
            this.aforoMaximo = aforoMaximo;
            this.mensaje = mensaje;
        }

        public boolean isDisponible() { return disponible; }
        public int getReservasExistentes() { return reservasExistentes; }
        public int getAforoMaximo() { return aforoMaximo; }
        public String getMensaje() { return mensaje; }
    }
}