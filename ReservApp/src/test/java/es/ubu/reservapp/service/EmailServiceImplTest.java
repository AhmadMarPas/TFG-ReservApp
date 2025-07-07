package es.ubu.reservapp.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import es.ubu.reservapp.model.entities.Convocatoria;
import es.ubu.reservapp.model.entities.ConvocatoriaPK;
import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;

/**
 * Test de la clase EmailServiceImpl.
 * 
 * @author Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    private Usuario usuario1;
    private Usuario usuario2;
    private Establecimiento establecimiento;
    private Reserva reserva;
    private Convocatoria convocatoria1;
    private Convocatoria convocatoria2;

    @BeforeEach
    void setUp() {
        // Configurar el fromEmail usando ReflectionTestUtils
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@reservapp.com");
        
        // Configurar usuarios
        usuario1 = new Usuario();
        usuario1.setId("1");
        usuario1.setNombre("Juan Pérez");
        usuario1.setCorreo("juan.perez@email.com");
        
        usuario2 = new Usuario();
        usuario2.setId("2");
        usuario2.setNombre("María García");
        usuario2.setCorreo("maria.garcia@email.com");
        
        // Configurar establecimiento
        establecimiento = new Establecimiento();
        establecimiento.setId(1);
        establecimiento.setNombre("Sala de Reuniones A");
        establecimiento.setDireccion("Calle Principal 123, Planta 2");
        
        // Configurar reserva
        reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuario1);
        reserva.setEstablecimiento(establecimiento);
        reserva.setFechaReserva(LocalDateTime.of(2024, 12, 15, 10, 30));
        reserva.setHoraFin(LocalTime.of(11, 30));
        
        // Configurar convocatorias
        convocatoria1 = new Convocatoria();
        ConvocatoriaPK id1 = new ConvocatoriaPK(1, "1");
        convocatoria1.setId(id1);
        convocatoria1.setUsuario(usuario1);
        convocatoria1.setReserva(reserva);
        convocatoria1.setEnlace("https://meet.google.com/abc-defg-hij");
        convocatoria1.setObservaciones("Reunión importante sobre el proyecto");
        
        convocatoria2 = new Convocatoria();
        ConvocatoriaPK id2 = new ConvocatoriaPK(1, "2");
        convocatoria2.setId(id2);
        convocatoria2.setUsuario(usuario2);
        convocatoria2.setReserva(reserva);
        convocatoria2.setEnlace("https://zoom.us/j/123456789");
        convocatoria2.setObservaciones("Traer documentos del proyecto");
    }

    // ================================
    // TESTS PARA enviarNotificacionesConvocatoria
    // ================================

    @Test
    void testEnviarNotificacionesConvocatoria_ConListaVacia() {
        // Arrange
        List<Convocatoria> convocatorias = new ArrayList<>();
        
        // Act
        emailService.enviarNotificacionesConvocatoria(convocatorias, reserva);
        
        // Assert
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testEnviarNotificacionesConvocatoria_ConListaNula() {
        // Act
        emailService.enviarNotificacionesConvocatoria(null, reserva);
        
        // Assert
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testEnviarNotificacionesConvocatoria_Exitoso() {
        // Arrange
        List<Convocatoria> convocatorias = Arrays.asList(convocatoria1, convocatoria2);
        
        // Act
        emailService.enviarNotificacionesConvocatoria(convocatorias, reserva);
        
        // Assert
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testEnviarNotificacionesConvocatoria_ConErrorEnEnvio() {
        // Arrange
        List<Convocatoria> convocatorias = Arrays.asList(convocatoria1, convocatoria2);
        doThrow(new RuntimeException("Error de conexión")).when(mailSender).send(any(SimpleMailMessage.class));
        
        // Act & Assert - No debe lanzar excepción, solo loggear el error
        assertDoesNotThrow(() -> emailService.enviarNotificacionesConvocatoria(convocatorias, reserva));
        
        // Verificar que se intentó enviar ambos correos
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    // ================================
    // TESTS PARA enviarCorreoConvocatoria
    // ================================

    @Test
    void testEnviarCorreoConvocatoria_Exitoso_ConTodosLosDatos() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        
        // Act
        emailService.enviarCorreoConvocatoria(usuario1, reserva, "https://meet.google.com/abc-defg-hij", "Reunión importante");
        
        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        assertEquals("test@reservapp.com", sentMessage.getFrom());
        assertEquals("juan.perez@email.com", sentMessage.getTo()[0]);
        assertEquals("Convocatoria de Reunión - Sala de Reuniones A", sentMessage.getSubject());
        
        String contenido = sentMessage.getText();
        assertNotNull(contenido);
        assertTrue(contenido.contains("Juan Pérez"));
        assertTrue(contenido.contains("15/12/2024 10:30"));
        assertTrue(contenido.contains("Sala de Reuniones A"));
        assertTrue(contenido.contains("Calle Principal 123, Planta 2"));
        assertTrue(contenido.contains("https://meet.google.com/abc-defg-hij"));
        assertTrue(contenido.contains("Reunión importante"));
    }

    @Test
    void testEnviarCorreoConvocatoria_SinEnlaceNiObservaciones() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        
        // Act
        emailService.enviarCorreoConvocatoria(usuario2, reserva, null, null);
        
        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        assertNotNull(contenido);
        assertTrue(contenido.contains("María García"));
        assertFalse(contenido.contains("🔗 Enlace de reunión:"));
        assertFalse(contenido.contains("📝 Observaciones:"));
    }

    @Test
    void testEnviarCorreoConvocatoria_ConEnlaceVacio() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        
        // Act
        emailService.enviarCorreoConvocatoria(usuario1, reserva, "   ", "Observación importante");
        
        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        assertNotNull(contenido);
        assertFalse(contenido.contains("🔗 Enlace de reunión:"));
        assertTrue(contenido.contains("Observación importante"));
    }

    @Test
    void testEnviarCorreoConvocatoria_ConObservacionesVacias() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        
        // Act
        emailService.enviarCorreoConvocatoria(usuario1, reserva, "https://zoom.us/j/123", "   ");
        
        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        assertNotNull(contenido);
        assertTrue(contenido.contains("https://zoom.us/j/123"));
        assertFalse(contenido.contains("📝 Observaciones:"));
    }

    @Test
    void testEnviarCorreoConvocatoria_EstablecimientoSinDireccion() {
        // Arrange
        establecimiento.setDireccion(null);
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        
        // Act
        emailService.enviarCorreoConvocatoria(usuario1, reserva, "https://meet.google.com/test", "Test");
        
        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        assertNotNull(contenido);
        assertFalse(contenido.contains("🗺️ Ubicación:"));
    }

    @Test
    void testEnviarCorreoConvocatoria_EstablecimientoConDireccionVacia() {
        // Arrange
        establecimiento.setDireccion("   ");
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        
        // Act
        emailService.enviarCorreoConvocatoria(usuario1, reserva, "https://meet.google.com/test", "Test");
        
        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        assertNotNull(contenido);
        assertFalse(contenido.contains("🗺️ Ubicación:"));
    }

    @Test
    void testEnviarCorreoConvocatoria_ErrorAlEnviar() {
        // Arrange
        doThrow(new RuntimeException("Error de conexión SMTP")).when(mailSender).send(any(SimpleMailMessage.class));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailService.enviarCorreoConvocatoria(usuario1, reserva, "https://meet.google.com/test", "Test");
        });
        
        assertEquals("Error al enviar correo de convocatoria", exception.getMessage());
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Error de conexión SMTP", exception.getCause().getMessage());
    }

    // ================================
    // TESTS PARA VALIDAR CONTENIDO DEL CORREO
    // ================================

    @Test
    void testContenidoCorreo_FormatoCompleto() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        
        // Act
        emailService.enviarCorreoConvocatoria(usuario1, reserva, "https://teams.microsoft.com/test", "Reunión de seguimiento del proyecto X");
        
        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        
        // Verificar estructura del correo
        assertTrue(contenido.startsWith("Estimado/a Juan Pérez,"));
        assertTrue(contenido.contains("Ha sido convocado/a a una reunión"));
        assertTrue(contenido.contains("📅 Fecha: 15/12/2024 10:30"));
        assertTrue(contenido.contains("📍 Lugar: Sala de Reuniones A"));
        assertTrue(contenido.contains("🗺️ Ubicación: Calle Principal 123, Planta 2"));
        assertTrue(contenido.contains("🔗 Enlace de reunión: https://teams.microsoft.com/test"));
        assertTrue(contenido.contains("📝 Observaciones:"));
        assertTrue(contenido.contains("Reunión de seguimiento del proyecto X"));
        assertTrue(contenido.contains("Por favor, confirme su asistencia"));
        assertTrue(contenido.contains("Saludos cordiales"));
        assertTrue(contenido.contains("Sistema de Reservas ReservApp"));
    }

    @Test
    void testContenidoCorreo_FormatoMinimo() {
        // Arrange
        establecimiento.setDireccion(null);
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        
        // Act
        emailService.enviarCorreoConvocatoria(usuario2, reserva, null, null);
        
        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        
        // Verificar que contiene elementos mínimos
        assertTrue(contenido.contains("Estimado/a María García,"));
        assertTrue(contenido.contains("📅 Fecha: 15/12/2024 10:30"));
        assertTrue(contenido.contains("📍 Lugar: Sala de Reuniones A"));
        
        // Verificar que NO contiene elementos opcionales
        assertFalse(contenido.contains("🗺️ Ubicación:"));
        assertFalse(contenido.contains("🔗 Enlace de reunión:"));
        assertFalse(contenido.contains("📝 Observaciones:"));
        
        // Verificar elementos de cierre
        assertTrue(contenido.contains("Por favor, confirme su asistencia"));
        assertTrue(contenido.contains("Sistema de Reservas ReservApp"));
    }

    // ================================
    // TESTS PARA CASOS EDGE
    // ================================

    @Test
    void testEnviarCorreoConvocatoria_ConCaracteresEspeciales() {
        // Arrange
        usuario1.setNombre("José María Ñoño");
        establecimiento.setNombre("Sala de Reuniones & Conferencias");
        establecimiento.setDireccion("Calle Ñoño #123, Piso 2º");
        
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        
        // Act
        emailService.enviarCorreoConvocatoria(usuario1, reserva, "https://meet.google.com/test", "Reunión con temas específicos & importantes");
        
        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        assertTrue(contenido.contains("José María Ñoño"));
        assertTrue(contenido.contains("Sala de Reuniones & Conferencias"));
        assertTrue(contenido.contains("Calle Ñoño #123, Piso 2º"));
        assertTrue(contenido.contains("Reunión con temas específicos & importantes"));
    }

    @Test
    void testEnviarNotificacionesConvocatoria_ConUnaConvocatoria() {
        // Arrange
        List<Convocatoria> convocatorias = Arrays.asList(convocatoria1);
        
        // Act
        emailService.enviarNotificacionesConvocatoria(convocatorias, reserva);
        
        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testEnviarNotificacionesConvocatoria_ConMultiplesConvocatorias() {
        // Arrange
        Convocatoria convocatoria3 = new Convocatoria();
        Usuario usuario3 = new Usuario();
        usuario3.setId("3");
        usuario3.setNombre("Carlos López");
        usuario3.setCorreo("carlos.lopez@email.com");
        ConvocatoriaPK id3 = new ConvocatoriaPK(1, "3");
        convocatoria3.setId(id3);
        convocatoria3.setUsuario(usuario3);
        convocatoria3.setReserva(reserva);
        
        List<Convocatoria> convocatorias = Arrays.asList(convocatoria1, convocatoria2, convocatoria3);
        
        // Act
        emailService.enviarNotificacionesConvocatoria(convocatorias, reserva);
        
        // Assert
        verify(mailSender, times(3)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testEnviarNotificacionesConvocatoria_ErrorParcial() {
        // Arrange
        List<Convocatoria> convocatorias = Arrays.asList(convocatoria1, convocatoria2);
        
        // Simular error solo en el primer envío
        doThrow(new RuntimeException("Error SMTP"))
            .doNothing()
            .when(mailSender).send(any(SimpleMailMessage.class));
        
        // Act
        assertDoesNotThrow(() -> emailService.enviarNotificacionesConvocatoria(convocatorias, reserva));
        
        // Assert
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }
}