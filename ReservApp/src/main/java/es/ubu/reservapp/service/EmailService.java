package es.ubu.reservapp.service;

import es.ubu.reservapp.model.entities.Convocatoria;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;
import java.util.List;

/**
 * Interface que representa el servicio para el envío de correos.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
public interface EmailService {
    
	/**
	 * Envía notificaciones por correo a los usuarios convocados
	 * 
	 * @param convocatorias Lista de convocatorias creadas
	 * @param reserva       Reserva asociada a las convocatorias
	 */
    void enviarNotificacionesConvocatoria(List<Convocatoria> convocatorias, Reserva reserva);
    
	/**
	 * Envía un correo individual de convocatoria
	 * 
	 * @param usuario       Usuario destinatario
	 * @param reserva       Reserva asociada
	 * @param enlaceReunion Enlace de la reunión (opcional)
	 * @param observaciones Observaciones de la reunión (opcional)
	 */
    void enviarCorreoConvocatoria(Usuario usuario, Reserva reserva);

}