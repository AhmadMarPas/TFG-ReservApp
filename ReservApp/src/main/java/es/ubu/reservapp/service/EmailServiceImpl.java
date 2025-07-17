package es.ubu.reservapp.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import es.ubu.reservapp.model.entities.Convocado;
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
    
    private static final String FROM_EMAIL = "noreply@reservapp.com";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private final JavaMailSender mailSender;
    
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    @Async
    @Override
    public void enviarNotificacionesConvocatoria(List<Convocado> convocados, Reserva reserva) {
        if (convocados == null || convocados.isEmpty()) {
            log.info("No hay convocatorias para enviar notificaciones");
            return;
        }
        
        log.info("Enviando {} notificaciones de convocatoria para la reserva ID: {}", convocados.size(), reserva.getId());
        
        convocados.forEach(convocatoria -> {
            try {
                enviarCorreoConvocado(convocatoria.getUsuario(), reserva);
            } catch (Exception e) {
                log.error("Error al enviar correo de convocatoria a usuario {}: {}", convocatoria.getUsuario().getCorreo(), e.getMessage());
                throw new MailSendException("Error al enviar correo al usuario " + convocatoria.getUsuario().getId()+ " " + e);
            }
        });
    }
    
    /**
	 * Envía un correo de convocatoria a un usuario específico.
	 * 
	 * @param usuario Usuario al que se le envía la convocatoria
	 * @param reserva Reserva asociada a la convocatoria
	 */
    private void enviarCorreoConvocado(Usuario usuario, Reserva reserva) {
        String asunto = "Convocatoria de Reunión - " + reserva.getEstablecimiento().getNombre();
        String contenido = construirContenidoCorreoConvocado(usuario, reserva);
        
        enviarCorreoInterno(usuario.getCorreo(), asunto, contenido, "convocatoria");
    }
    
    @Async
    @Override
    public void enviarNotificacionReservaCreada(Reserva reserva) {
        String asunto = "Confirmación de Reserva - " + reserva.getEstablecimiento().getNombre();
        String contenido = construirContenidoCorreoReservaCreada(reserva);
        
        enviarCorreoInterno(reserva.getUsuario().getCorreo(), asunto, contenido, "confirmación de reserva");
    }
    
    @Async
    @Override
    public void enviarNotificacionAnulacion(Reserva reserva, List<String> correosDestino) {
        if (correosDestino == null || correosDestino.isEmpty()) {
            log.info("No hay destinatarios para enviar notificaciones de anulación");
            return;
        }
        
        String asunto = "Reserva Anulada - " + reserva.getEstablecimiento().getNombre();
        String mensaje = construirContenidoCorreoAnulacion(reserva);
        
        correosDestino.stream()
            .filter(correo -> correo != null && !correo.trim().isEmpty())
            .forEach(correo -> {
                try {
                    enviarCorreoInterno(correo, asunto, mensaje, "anulación");
                } catch (Exception e) {
                    log.error("Error al enviar notificación de anulación a {}: {}", correo, e.getMessage());
                    throw new MailSendException("Error al enviar correo de " + e);
                }
            });
    }
    
    /**
     * Método centralizado para enviar correos con manejo de errores unificado.
     */
    private void enviarCorreoInterno(String destinatario, String asunto, String mensaje, String tipoCorreo) {
        if (destinatario == null || destinatario.trim().isEmpty()) {
            log.warn("Destinatario vacío para correo de {}", tipoCorreo);
            return;
        }
        
        try {
            SimpleMailMessage message = crearMensajeBase(destinatario, asunto, mensaje);
            mailSender.send(message);
            log.info("Correo de {} enviado exitosamente a: {}", tipoCorreo, destinatario);
            
        } catch (Exception e) {
            log.error("Error al enviar correo de {} a {}: {}", tipoCorreo, destinatario, e.getMessage());
            throw new MailSendException("Error al enviar correo de " + tipoCorreo, e);
        }
    }
    
    /**
     * Crea un mensaje base con la configuración común.
     */
    private SimpleMailMessage crearMensajeBase(String destinatario, String asunto, String mensaje) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_EMAIL);
        message.setTo(destinatario);
        message.setSubject(asunto);
        message.setText(mensaje);
        return message;
    }
    
    /**
     * Construye el contenido del correo de convocatoria.
     */
    private String construirContenidoCorreoConvocado(Usuario usuario, Reserva reserva) {
        StringBuilder contenido = new StringBuilder();
        
        contenido.append("Estimado/a ").append(usuario.getNombre()).append(",\n\n");
        contenido.append("Ha sido convocado/a a una reunión con los siguientes detalles:\n\n");
        
        // Información básica de la reserva
        agregarInformacionBasicaReserva(contenido, reserva);
        
        // Información específica de convocatoria
        if (reserva.getConvocatoria() != null) {
            String enlace = reserva.getConvocatoria().getEnlace();
            String observaciones = reserva.getConvocatoria().getObservaciones();
            
            agregarEnlaceReunion(contenido, enlace);
            agregarObservaciones(contenido, observaciones);
        }
        
        contenido.append("\n\nPor favor, confirme su asistencia.\n\n");
        agregarPieFirma(contenido);
        
        return contenido.toString();
    }
    
    /**
     * Construye el contenido del correo de confirmación de reserva creada.
     */
    private String construirContenidoCorreoReservaCreada(Reserva reserva) {
        StringBuilder contenido = new StringBuilder();
        
        contenido.append("Estimado/a ").append(reserva.getUsuario().getNombre()).append(",\n\n");
        contenido.append("Su reserva ha sido creada exitosamente con los siguientes detalles:\n\n");
        
        // Información básica de la reserva
        agregarInformacionBasicaReserva(contenido, reserva);
        
        // Lista de convocados si existe
        agregarListaConvocados(contenido, reserva);
        
        contenido.append("\n\n");
        agregarPieFirma(contenido);
        
        return contenido.toString();
    }
    
    /**
     * Construye el contenido del correo de anulación.
     */
    private String construirContenidoCorreoAnulacion(Reserva reserva) {
        StringBuilder contenido = new StringBuilder();
        
        contenido.append("Estimado/a usuario/a,\n\n");
        contenido.append("Le informamos que la siguiente reserva ha sido anulada:\n\n");
        
        contenido.append("Establecimiento: ").append(reserva.getEstablecimiento().getNombre()).append("\n");
        contenido.append("Fecha: ").append(reserva.getFechaReserva().format(DATE_FORMAT));
        contenido.append("\nHora: ").append(reserva.getFechaReserva().format(TIME_FORMAT));
        
        if (reserva.getHoraFin() != null) {
            contenido.append(" - ").append(reserva.getHoraFin().format(TIME_FORMAT));
        }
        
        if (reserva.getConvocatoria() != null) {
            agregarObservaciones(contenido, reserva.getConvocatoria().getObservaciones());
        }
        
        contenido.append("\n\nSi tiene alguna consulta, no dude en contactarnos.");
        contenido.append("\n\nSaludos cordiales,\nEquipo de ReservApp");
        
        return contenido.toString();
    }
    
    /**
     * Agrega información básica de la reserva al contenido.
     */
    private void agregarInformacionBasicaReserva(StringBuilder contenido, Reserva reserva) {
        contenido.append("📅 Fecha: ").append(reserva.getFechaReserva().format(DATETIME_FORMAT)).append("\n");
        contenido.append("📍 Lugar: ").append(reserva.getEstablecimiento().getNombre()).append("\n");
        
        String direccion = reserva.getEstablecimiento().getDireccion();
        if (esTextoValido(direccion)) {
            contenido.append("🗺️ Ubicación: ").append(direccion).append("\n");
        }
    }
    
    /**
     * Agrega el enlace de reunión si está disponible.
     */
    private void agregarEnlaceReunion(StringBuilder contenido, String enlace) {
        if (esTextoValido(enlace)) {
            contenido.append("\n🔗 Enlace de reunión: ").append(enlace).append("\n");
        }
    }
    
    /**
     * Agrega las observaciones si están disponibles.
     */
    private void agregarObservaciones(StringBuilder contenido, String observaciones) {
        if (esTextoValido(observaciones)) {
            contenido.append("\n📝 Observaciones:\n").append(observaciones).append("\n");
        }
    }
    
    /**
     * Agrega la lista de convocados si existe.
     */
    private void agregarListaConvocados(StringBuilder contenido, Reserva reserva) {
        if (reserva.getConvocatoria() != null && 
            reserva.getConvocatoria().getConvocados() != null && 
            !reserva.getConvocatoria().getConvocados().isEmpty()) {
            
            contenido.append("\n👥 Usuarios convocados:\n");
            String convocadosStr = reserva.getConvocatoria().getConvocados().stream()
                .map(convocado -> "- " + convocado.getUsuario().getNombre() + 
                                 " (" + convocado.getUsuario().getCorreo() + ")")
                .collect(Collectors.joining("\n"));
            contenido.append(convocadosStr).append("\n");
        }
    }
    
    /**
     * Agrega el pie de firma común.
     */
    private void agregarPieFirma(StringBuilder contenido) {
        contenido.append("Saludos cordiales,\n");
        contenido.append("Sistema de Reservas ReservApp");
    }
    
    /**
     * Valida si un texto es válido (no nulo y no vacío).
     */
    private boolean esTextoValido(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }
}