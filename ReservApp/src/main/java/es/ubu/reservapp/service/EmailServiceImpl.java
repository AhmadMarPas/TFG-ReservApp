package es.ubu.reservapp.service;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import es.ubu.reservapp.model.entities.Convocatoria;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio para el envío de correos.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {
    
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@reservapp.com}")
    private String fromEmail;
    
    public EmailServiceImpl(JavaMailSender mailSender) {
    	this.mailSender = mailSender;
    }
    
    @Override
    public void enviarNotificacionesConvocatoria(List<Convocatoria> convocatorias, Reserva reserva) {
        if (convocatorias == null || convocatorias.isEmpty()) {
            log.info("No hay convocatorias para enviar notificaciones");
            return;
        }
        
        log.info("Enviando {} notificaciones de convocatoria para la reserva ID: {}", convocatorias.size(), reserva.getId());
        
        for (Convocatoria convocatoria : convocatorias) {
            try {
                enviarCorreoConvocatoria(
                    convocatoria.getUsuario(), 
                    reserva, 
                    convocatoria.getEnlace(), 
                    convocatoria.getObservaciones()
                );
            } catch (Exception e) {
            	log.error("Error al enviar correo de convocatoria a usuario {}: {}", convocatoria.getUsuario().getCorreo(), e.getMessage());
            }
        }
    }
    
    @Override
    public void enviarCorreoConvocatoria(Usuario usuario, Reserva reserva, String enlaceReunion, String observaciones) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(usuario.getCorreo());
            message.setSubject("Convocatoria de Reunión - " + reserva.getEstablecimiento().getNombre());
            
            String contenido = construirContenidoCorreo(usuario, reserva, enlaceReunion, observaciones);
            message.setText(contenido);
            
            mailSender.send(message);
            log.info("Correo de convocatoria enviado exitosamente a: {}", usuario.getCorreo());
            
        } catch (Exception e) {
        	log.error("Error al enviar correo de convocatoria a {}: {}", usuario.getCorreo(), e.getMessage());
            throw new MailSendException("Error al enviar correo de convocatoria", e);
        }
    }
    
    /**
     * Construye el contenido del correo de convocatoria
     */
    private String construirContenidoCorreo(Usuario usuario, Reserva reserva, String enlaceReunion, String observaciones) {
        StringBuilder contenido = new StringBuilder();
        
        contenido.append("Estimado/a ").append(usuario.getNombre()).append(",\n\n");
        contenido.append("Ha sido convocado/a a una reunión con los siguientes detalles:\n\n");
        
        // Información de la reserva
        contenido.append("📅 Fecha: ").append(reserva.getFechaReserva().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        contenido.append("📍 Lugar: ").append(reserva.getEstablecimiento().getNombre()).append("\n");
        
        if (reserva.getEstablecimiento().getDireccion() != null && !reserva.getEstablecimiento().getDireccion().trim().isEmpty()) {
            contenido.append("🗺️ Ubicación: ").append(reserva.getEstablecimiento().getDireccion()).append("\n");
        }
        
        // Enlace de reunión si está disponible
        if (enlaceReunion != null && !enlaceReunion.trim().isEmpty()) {
            contenido.append("\n🔗 Enlace de reunión: ").append(enlaceReunion).append("\n");
        }
        
        // Observaciones si están disponibles
        if (observaciones != null && !observaciones.trim().isEmpty()) {
            contenido.append("\n📝 Observaciones:\n").append(observaciones).append("\n");
        }
        
        contenido.append("\n\nPor favor, confirme su asistencia.\n\n");
        contenido.append("Saludos cordiales,\n");
        contenido.append("Sistema de Reservas ReservApp");
        
        return contenido.toString();
    }
}