package es.ubu.reservapp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import es.ubu.reservapp.exception.UserNotFoundException;
import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.FranjaHoraria;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;

/**
 * Interfaz que representa el servicio de la entidad Reserva.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
public interface ReservaService {
	
	/**
	 * Crea la convocatoria de la reserva con los usuarios indicados.
	 * 
	 * @param reserva
	 * @param idUsuariosConvocados
	 * @param usuarioQueReserva
	 * @return La reserva creara.
	 */
    @Transactional
    Reserva crearReservaConConvocatorias(Reserva reserva, Usuario usuarioQueReserva, List<String> idUsuariosConvocados) throws UserNotFoundException;

    /**
     * Recupera las reservas de un establecimiento entre dos fechas.
     * 
     * @param establecimiento Establecimiento para el que se quiere consultar las reservas.
     * @param fechaInicio Fecha de inicio del rango de búsqueda.
     * @param fechaFin Fecha de fin del rango de búsqueda.
     * @return Lista de resrvas dentro del rango de fechas para el establecimiento indicado.
     */
    @Transactional(readOnly = true)
    List<Reserva> findByEstablecimientoAndFechaReservaBetween(Establecimiento establecimiento, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
	/**
	 * Busca todas las reservas de un usuario para un establecimiento específico que
	 * son posteriores o iguales a la fecha actual (reservas futuras).
	 * 
	 * @param usuario         Usuario que realizó las reservas
	 * @param establecimiento Establecimiento donde se realizaron las reservas
	 * @param fechaActual     Fecha actual para comparar
	 * @return Lista de reservas futuras
	 */
    @Transactional(readOnly = true)
    List<Reserva> findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(Usuario usuario, Establecimiento establecimiento, LocalDateTime fechaActual);

	/**
	 * Busca todas las reservas de un usuario para un establecimiento específico que
	 * son anteriores a la fecha actual (reservas pasadas).
	 * 
	 * @param usuario         Usuario que realizó las reservas
	 * @param establecimiento Establecimiento donde se realizaron las reservas
	 * @param fechaActual     Fecha actual para comparar
	 * @return Lista de reservas pasadas
	 */
    @Transactional(readOnly = true)
    List<Reserva> findByUsuarioAndEstablecimientoAndFechaReservaBefore(Usuario usuario, Establecimiento establecimiento, LocalDateTime fechaActual);

    /**
     * Recupera todas las reservas.
     * 
     * @return Lista con todas las reservas.
     */
    @Transactional(readOnly = true)
    List<Reserva> findAll();

    /**
     * Guarda la reserva.
     * 
     * @param reserva a guardar.
     * @return Reserva guardada.
     */
    @Transactional
    Reserva save(Reserva reserva);
    
    /**
     * Busca las reservas del usuario.
     * 
     * @param usuario Usuario al que se quiere consultar las reservas.
     * @return Lista de reservas realizadas por el usuario.
     */
    @Transactional(readOnly = true)
    List<Reserva> findByUsuario(Usuario usuario);

    /**
     * Busca una reserva por su ID.
     * 
     * @param id ID de la reserva a buscar.
     * @return Optional con la reserva si existe, vacío si no.
     */
    @Transactional(readOnly = true)
    Reserva findById(Integer id);

    /**
     * Borra o anula la reserva.
     * 
     * @param reserva a guardar.
     * @return Reserva guardada.
     */
    @Transactional
    void delete(Reserva reserva);

    /**
     * Verifica si hay disponibilidad para una reserva considerando el aforo del establecimiento.
     * 
     * @param establecimiento Establecimiento donde se quiere reservar
     * @param fecha Fecha de la reserva
     * @param horaInicio Hora de inicio de la reserva
     * @param horaFin Hora de fin de la reserva
     * @param reservaExcluir Reserva a excluir de la verificación (para ediciones), puede ser null
     * @return true si hay disponibilidad, false en caso contrario
     */
    @Transactional(readOnly = true)
    boolean verificarDisponibilidad(Establecimiento establecimiento, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, Reserva reservaExcluir);

    /**
     * Obtiene las franjas horarias disponibles para un establecimiento en una fecha específica,
     * considerando las reservas existentes y el aforo.
     * 
     * @param establecimiento Establecimiento para el que se quieren obtener las franjas
     * @param fecha Fecha para la que se quieren obtener las franjas
     * @return Lista de franjas horarias disponibles con información de disponibilidad
     */
    @Transactional(readOnly = true)
    List<FranjaDisponibilidad> obtenerFranjasDisponibles(Establecimiento establecimiento, LocalDate fecha);

    /**
     * Obtiene las reservas que se solapan con un horario específico.
     * 
     * @param establecimiento Establecimiento donde buscar
     * @param fecha Fecha de la reserva
     * @param horaInicio Hora de inicio
     * @param horaFin Hora de fin
     * @return Lista de reservas que se solapan
     */
    @Transactional(readOnly = true)
    List<Reserva> obtenerReservasSolapadas(Establecimiento establecimiento, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin);

    /**
     * Obtiene la disponibilidad de un día específico para un establecimiento.
     * 
     * @param establecimiento Establecimiento para verificar disponibilidad
     * @param fecha Fecha a verificar
     * @return Información de disponibilidad del día
     */
    @Transactional(readOnly = true)
    DisponibilidadDia obtenerDisponibilidadDia(Establecimiento establecimiento, LocalDate fecha);

    /**
     * Obtiene un resumen de disponibilidad para un mes completo.
     * 
     * @param establecimiento Establecimiento para verificar disponibilidad
     * @param anyo Año del mes a consultar
     * @param mes Mes a consultar (1-12)
     * @return Mapa con la disponibilidad de cada día del mes
     */
    @Transactional(readOnly = true)
    Map<LocalDate, DisponibilidadDia> obtenerDisponibilidadMensual(Establecimiento establecimiento, int anyo, int mes);

    /**
     * Clase auxiliar para representar la disponibilidad de una franja horaria.
     */
    public static class FranjaDisponibilidad {
        private final FranjaHoraria franjaHoraria;
        private final List<PeriodoDisponible> periodosDisponibles;
        private final boolean tieneDisponibilidad;

        public FranjaDisponibilidad(FranjaHoraria franjaHoraria, List<PeriodoDisponible> periodosDisponibles) {
            this.franjaHoraria = franjaHoraria;
            this.periodosDisponibles = periodosDisponibles;
            this.tieneDisponibilidad = !periodosDisponibles.isEmpty();
        }

        public FranjaHoraria getFranjaHoraria() { return franjaHoraria; }
        public List<PeriodoDisponible> getPeriodosDisponibles() { return periodosDisponibles; }
        public boolean isTieneDisponibilidad() { return tieneDisponibilidad; }
    }

    /**
     * Clase auxiliar para representar un período disponible dentro de una franja horaria.
     */
    public static class PeriodoDisponible {
        private final LocalTime horaInicio;
        private final LocalTime horaFin;

        public PeriodoDisponible(LocalTime horaInicio, LocalTime horaFin) {
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
        }

        public LocalTime getHoraInicio() { return horaInicio; }
        public LocalTime getHoraFin() { return horaFin; }
        
        @Override
        public String toString() {
            return horaInicio + " - " + horaFin;
        }
    }

    /**
     * Clase auxiliar para representar la disponibilidad de un día específico.
     */
    public static class DisponibilidadDia {
        private final LocalDate fecha;
        private final boolean tieneHorarioApertura;
        private final boolean tieneDisponibilidad;
        private final List<FranjaDisponibilidad> franjasDisponibles;
        private final String resumen;

        public DisponibilidadDia(LocalDate fecha, boolean tieneHorarioApertura, boolean tieneDisponibilidad, 
                                List<FranjaDisponibilidad> franjasDisponibles, String resumen) {
            this.fecha = fecha;
            this.tieneHorarioApertura = tieneHorarioApertura;
            this.tieneDisponibilidad = tieneDisponibilidad;
            this.franjasDisponibles = franjasDisponibles;
            this.resumen = resumen;
        }

        public LocalDate getFecha() { return fecha; }
        public boolean isTieneHorarioApertura() { return tieneHorarioApertura; }
        public boolean isTieneDisponibilidad() { return tieneDisponibilidad; }
        public List<FranjaDisponibilidad> getFranjasDisponibles() { return franjasDisponibles; }
        public String getResumen() { return resumen; }
    }
}