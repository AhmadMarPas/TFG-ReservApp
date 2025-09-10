package es.ubu.reservapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import es.ubu.reservapp.model.entities.Convocado;
import es.ubu.reservapp.model.entities.ConvocadoPK;
import es.ubu.reservapp.model.entities.Convocatoria;
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

    private Usuario usuario;
    private Establecimiento establecimiento;
    private Reserva reserva;
    private Convocatoria convocatoria;
    private Convocado convocado;

    @BeforeEach
    void setUp() {
        // Configurar usuario
        usuario = new Usuario();
        usuario.setId("user1");
        usuario.setNombre("Juan Pérez");
        usuario.setCorreo("juan.perez@test.com");

        // Configurar establecimiento
        establecimiento = new Establecimiento();
        establecimiento.setId(1);
        establecimiento.setNombre("Sala de Reuniones A");
        establecimiento.setDireccion("Calle Principal 123");

        // Configurar reserva
        reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuario);
        reserva.setEstablecimiento(establecimiento);
        reserva.setFechaReserva(LocalDateTime.of(2024, 12, 25, 10, 0));
        reserva.setHoraFin(LocalTime.of(11, 0));

        // Configurar convocatoria
        convocatoria = new Convocatoria();
        convocatoria.setId(1);
        convocatoria.setReserva(reserva);
        convocatoria.setEnlace("https://meet.google.com/test");
        convocatoria.setObservaciones("Reunión importante");

        // Configurar convocado
        Usuario usuarioConvocado = new Usuario();
        usuarioConvocado.setId("user2");
        usuarioConvocado.setNombre("María García");
        usuarioConvocado.setCorreo("maria.garcia@test.com");

        ConvocadoPK pk = new ConvocadoPK(1, "user1");
        convocado = new Convocado();
        convocado.setId(pk);
        convocado.setUsuario(usuarioConvocado);
        convocado.setConvocatoria(convocatoria);

        convocatoria.setConvocados(Arrays.asList(convocado));
        reserva.setConvocatoria(convocatoria);
    }

    // ================================
    // TESTS PARA enviarNotificacionesConvocatoria
    // ================================

    @Test
    void testEnviarNotificacionesConvocatoria_ConvocadosNull() {
        // Act
        emailService.enviarNotificacionesConvocatoria(null, reserva);

        // Assert
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testEnviarNotificacionesConvocatoria_ConvocadosVacios() {
        // Arrange
        List<Convocado> convocadosVacios = new ArrayList<>();

        // Act
        emailService.enviarNotificacionesConvocatoria(convocadosVacios, reserva);

        // Assert
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testEnviarNotificacionesConvocatoria_Exitoso() {
        // Arrange
        List<Convocado> convocados = Arrays.asList(convocado);
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarNotificacionesConvocatoria(convocados, reserva);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        assertEquals("noreply@reservapp.com", sentMessage.getFrom());
        assertEquals("maria.garcia@test.com", sentMessage.getTo()[0]);
        assertEquals("Convocatoria de Reunión - Sala de Reuniones A", sentMessage.getSubject());
        
        String contenido = sentMessage.getText();
        assertNotNull(contenido);
        assertTrue(contenido.contains("María García"));
        assertTrue(contenido.contains("Ha sido convocado/a a una reunión"));
        assertTrue(contenido.contains("25/12/2024 10:00"));
        assertTrue(contenido.contains("Sala de Reuniones A"));
        assertTrue(contenido.contains("https://meet.google.com/test"));
        assertTrue(contenido.contains("Reunión importante"));
    }

    @Test
    void testEnviarNotificacionesConvocatoria_ErrorEnEnvio() {
        // Arrange
        List<Convocado> convocados = Arrays.asList(convocado);
        doThrow(new RuntimeException("Error de conexión")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(MailSendException.class, () -> {
            emailService.enviarNotificacionesConvocatoria(convocados, reserva);
        });
    }

    @Test
    void testEnviarNotificacionesConvocatoria_VariosConvocados() {
        // Arrange
        Usuario usuario2 = new Usuario();
        usuario2.setId("user3");
        usuario2.setNombre("Carlos López");
        usuario2.setCorreo("carlos.lopez@test.com");

        Convocado convocado2 = new Convocado();
        convocado2.setUsuario(usuario2);

        List<Convocado> convocados = Arrays.asList(convocado, convocado2);
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarNotificacionesConvocatoria(convocados, reserva);

        // Assert
        verify(mailSender, times(2)).send(messageCaptor.capture());
        List<SimpleMailMessage> sentMessages = messageCaptor.getAllValues();
        
        assertEquals(2, sentMessages.size());
        assertEquals("maria.garcia@test.com", sentMessages.get(0).getTo()[0]);
        assertEquals("carlos.lopez@test.com", sentMessages.get(1).getTo()[0]);
    }

    // ================================
    // TESTS PARA enviarNotificacionReservaCreada
    // ================================

    @Test
    void testEnviarNotificacionReservaCreada_Exitoso() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarNotificacionReservaCreada(reserva);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        assertEquals("noreply@reservapp.com", sentMessage.getFrom());
        assertEquals("juan.perez@test.com", sentMessage.getTo()[0]);
        assertEquals("Confirmación de Reserva - Sala de Reuniones A", sentMessage.getSubject());
        
        String contenido = sentMessage.getText();
        assertNotNull(contenido);
        assertTrue(contenido.contains("Juan Pérez"));
        assertTrue(contenido.contains("Su reserva ha sido creada exitosamente"));
        assertTrue(contenido.contains("25/12/2024 10:00"));
        assertTrue(contenido.contains("Sala de Reuniones A"));
        assertTrue(contenido.contains("Calle Principal 123"));
        assertTrue(contenido.contains("👥 Usuarios convocados:"));
        assertTrue(contenido.contains("María García"));
    }

    @Test
    void testEnviarNotificacionReservaCreada_SinConvocatoria() {
        // Arrange
        reserva.setConvocatoria(null);
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarNotificacionReservaCreada(reserva);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        assertNotNull(contenido);
        assertFalse(contenido.contains("👥 Usuarios convocados:"));
        assertTrue(contenido.contains("Su reserva ha sido creada exitosamente"));
    }

    @Test
    void testEnviarNotificacionReservaCreada_EstablecimientoSinDireccion() {
        // Arrange
        establecimiento.setDireccion(null);
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        
        // Act
        emailService.enviarNotificacionReservaCreada(reserva);
        
        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        assertNotNull(contenido);
        assertFalse(contenido.contains("🗺️ Ubicación:"));
        assertTrue(contenido.contains("Sala de Reuniones A"));
    }

    @Test
    void testEnviarNotificacionReservaCreada_EstablecimientoConDireccionVacia() {
        // Arrange
        establecimiento.setDireccion("   ");
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        
        // Act
        emailService.enviarNotificacionReservaCreada(reserva);
        
        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        assertNotNull(contenido);
        assertFalse(contenido.contains("🗺️ Ubicación:"));
        assertTrue(contenido.contains("Sala de Reuniones A"));
    }

    @Test
    void testEnviarNotificacionReservaCreada_ConvocatoriaConListaVacia() {
        // Arrange
        convocatoria.setConvocados(Arrays.asList()); // Lista vacía
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        
        // Act
        emailService.enviarNotificacionReservaCreada(reserva);
        
        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        assertNotNull(contenido);
        assertFalse(contenido.contains("👥 Usuarios convocados:"));
        assertTrue(contenido.contains("Su reserva ha sido creada exitosamente"));
    }

    @Test
    void testEnviarNotificacionReservaCreada_ConvocatoriaConListaNull() {
        // Arrange
        convocatoria.setConvocados(null);
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        
        // Act
        emailService.enviarNotificacionReservaCreada(reserva);
        
        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        assertNotNull(contenido);
        assertFalse(contenido.contains("👥 Usuarios convocados:"));
    }

    @Test
    void testEnviarNotificacionReservaCreada_ErrorEnEnvio() {
        // Arrange
        doThrow(new RuntimeException("Error de conexión")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(MailSendException.class, () -> {
            emailService.enviarNotificacionReservaCreada(reserva);
        });
    }

    // ================================
    // TESTS PARA enviarNotificacionAnulacion
    // ================================

    @Test
    void testEnviarNotificacionAnulacion_CorreosNull() {
        // Act
        emailService.enviarNotificacionAnulacion(reserva, null);

        // Assert
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testEnviarNotificacionAnulacion_CorreosVacios() {
        // Arrange
        List<String> correosVacios = new ArrayList<>();

        // Act
        emailService.enviarNotificacionAnulacion(reserva, correosVacios);

        // Assert
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testEnviarNotificacionAnulacion_Exitoso() {
        // Arrange
        List<String> correos = Arrays.asList("usuario1@test.com", "usuario2@test.com");
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarNotificacionAnulacion(reserva, correos);

        // Assert
        verify(mailSender, times(2)).send(messageCaptor.capture());
        List<SimpleMailMessage> sentMessages = messageCaptor.getAllValues();
        
        assertEquals(2, sentMessages.size());
        assertEquals("usuario1@test.com", sentMessages.get(0).getTo()[0]);
        assertEquals("usuario2@test.com", sentMessages.get(1).getTo()[0]);
        assertEquals("Reserva Anulada - Sala de Reuniones A", sentMessages.get(0).getSubject());
        
        String contenido = sentMessages.get(0).getText();
        assertTrue(contenido.contains("Le informamos que la siguiente reserva ha sido anulada"));
        assertTrue(contenido.contains("Sala de Reuniones A"));
        assertTrue(contenido.contains("25/12/2024"));
        assertTrue(contenido.contains("10:00 - 11:00"));
    }

    @Test
    void testEnviarNotificacionAnulacion_ConCorreosNulosYVacios() {
        // Arrange
        List<String> correos = Arrays.asList("usuario1@test.com", null, "", "   ", "usuario2@test.com");
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarNotificacionAnulacion(reserva, correos);

        // Assert
        verify(mailSender, times(2)).send(messageCaptor.capture());
        List<SimpleMailMessage> sentMessages = messageCaptor.getAllValues();
        
        assertEquals("usuario1@test.com", sentMessages.get(0).getTo()[0]);
        assertEquals("usuario2@test.com", sentMessages.get(1).getTo()[0]);
    }

    @Test
    void testEnviarNotificacionAnulacion_SinHoraFin() {
        // Arrange
        reserva.setHoraFin(null);
        List<String> correos = Arrays.asList("usuario1@test.com");
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarNotificacionAnulacion(reserva, correos);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        assertTrue(contenido.contains("10:00"));
        assertFalse(contenido.contains("10:00 - "));
    }

    @Test
    void testEnviarNotificacionAnulacion_ConObservaciones() {
        // Arrange
        List<String> correos = Arrays.asList("usuario1@test.com");
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarNotificacionAnulacion(reserva, correos);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        assertTrue(contenido.contains("📝 Observaciones:"));
        assertTrue(contenido.contains("Reunión importante"));
    }

    @Test
    void testEnviarNotificacionAnulacion_SinObservaciones() {
        // Arrange
        convocatoria.setObservaciones(null);
        List<String> correos = Arrays.asList("usuario1@test.com");
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarNotificacionAnulacion(reserva, correos);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        assertFalse(contenido.contains("📝 Observaciones:"));
    }

    @Test
    void testEnviarNotificacionAnulacion_SinConvocatoria() {
        // Arrange
        reserva.setConvocatoria(null);
        List<String> correos = Arrays.asList("usuario1@test.com");
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarNotificacionAnulacion(reserva, correos);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        assertFalse(contenido.contains("📝 Observaciones:"));
        assertTrue(contenido.contains("Le informamos que la siguiente reserva ha sido anulada"));
    }

    @Test
    void testEnviarNotificacionAnulacion_ErrorEnEnvio() {
        // Arrange
        List<String> correos = Arrays.asList("usuario1@test.com");
        doThrow(new RuntimeException("Error de conexión")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(MailSendException.class, () -> {
            emailService.enviarNotificacionAnulacion(reserva, correos);
        });
    }

    // ================================
    // TESTS PARA MÉTODOS PRIVADOS (a través de métodos públicos)
    // ================================

    @Test
    void testEnviarCorreoInterno_DestinatarioNull() {
        // Arrange - Crear un usuario con correo null
        Usuario usuarioSinCorreo = new Usuario();
        usuarioSinCorreo.setId("user_sin_correo");
        usuarioSinCorreo.setNombre("Usuario Sin Correo");
        usuarioSinCorreo.setCorreo(null);
        
        Convocado convocadoSinCorreo = new Convocado();
        convocadoSinCorreo.setUsuario(usuarioSinCorreo);
        
        List<Convocado> convocados = Arrays.asList(convocadoSinCorreo);

        // Act
        emailService.enviarNotificacionesConvocatoria(convocados, reserva);

        // Assert
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testEnviarCorreoInterno_DestinatarioVacio() {
        // Arrange - Crear un usuario con correo vacío
        Usuario usuarioCorreoVacio = new Usuario();
        usuarioCorreoVacio.setId("user_correo_vacio");
        usuarioCorreoVacio.setNombre("Usuario Correo Vacío");
        usuarioCorreoVacio.setCorreo("   ");
        
        Convocado convocadoCorreoVacio = new Convocado();
        convocadoCorreoVacio.setUsuario(usuarioCorreoVacio);
        
        List<Convocado> convocados = Arrays.asList(convocadoCorreoVacio);

        // Act
        emailService.enviarNotificacionesConvocatoria(convocados, reserva);

        // Assert
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testConstruirContenidoCorreoConvocado_SinEnlace() {
        // Arrange
        convocatoria.setEnlace(null);
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarNotificacionesConvocatoria(Arrays.asList(convocado), reserva);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        assertFalse(contenido.contains("🔗 Enlace de reunión:"));
    }

    @Test
    void testConstruirContenidoCorreoConvocado_EnlaceVacio() {
        // Arrange
        convocatoria.setEnlace("   ");
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarNotificacionesConvocatoria(Arrays.asList(convocado), reserva);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        assertFalse(contenido.contains("🔗 Enlace de reunión:"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void testConstruirContenidoCorreoConvocado_SinObservacionesInvalidas(String observaciones) {
        // Arrange
        // En cada ejecución del test, la observación de la convocatoria se establecerá con el valor del parámetro
        convocatoria.setObservaciones(observaciones);
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarNotificacionesConvocatoria(Arrays.asList(convocado), reserva);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        // Verifica que la cadena "📝 Observaciones:" no está presente en el contenido del correo
        assertFalse(contenido.contains("📝 Observaciones:"));
    }

    @Test
    void testConstruirContenidoCorreoConvocado_SinConvocatoria() {
        // Arrange
        Reserva reservaSinConvocatoria = new Reserva();
        reservaSinConvocatoria.setId(2);
        reservaSinConvocatoria.setUsuario(usuario);
        reservaSinConvocatoria.setEstablecimiento(establecimiento);
        reservaSinConvocatoria.setFechaReserva(LocalDateTime.of(2024, 12, 25, 10, 0));
        reservaSinConvocatoria.setConvocatoria(null);
        
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarNotificacionesConvocatoria(Arrays.asList(convocado), reservaSinConvocatoria);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        assertFalse(contenido.contains("🔗 Enlace de reunión:"));
        assertFalse(contenido.contains("📝 Observaciones:"));
        assertTrue(contenido.contains("Ha sido convocado/a a una reunión"));
    }

    // ================================
    // TESTS ADICIONALES PARA COBERTURA COMPLETA
    // ================================

    @Test
    void testCrearMensajeBase() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarNotificacionReservaCreada(reserva);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        assertEquals("noreply@reservapp.com", sentMessage.getFrom());
        assertEquals("juan.perez@test.com", sentMessage.getTo()[0]);
        assertEquals("Confirmación de Reserva - Sala de Reuniones A", sentMessage.getSubject());
        assertNotNull(sentMessage.getText());
    }

    @Test
    void testAgregarPieFirma() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarNotificacionReservaCreada(reserva);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        assertTrue(contenido.contains("Saludos cordiales,"));
        assertTrue(contenido.contains("Sistema de Reservas ReservApp"));
    }

    @Test
    void testFormateoFechas() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarNotificacionReservaCreada(reserva);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        assertTrue(contenido.contains("25/12/2024 10:00"));
    }

    @Test
    void testContenidoCompleto_ConTodosLosCampos() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarNotificacionReservaCreada(reserva);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        String contenido = sentMessage.getText();
        
        // Verificar todos los elementos del contenido
        assertTrue(contenido.contains("Juan Pérez"));
        assertTrue(contenido.contains("Su reserva ha sido creada exitosamente"));
        assertTrue(contenido.contains("📅 Fecha: 25/12/2024 10:00"));
        assertTrue(contenido.contains("📍 Lugar: Sala de Reuniones A"));
        assertTrue(contenido.contains("🗺️ Ubicación: Calle Principal 123"));
        assertTrue(contenido.contains("👥 Usuarios convocados:"));
        assertTrue(contenido.contains("- María García (maria.garcia@test.com)"));
        assertTrue(contenido.contains("Saludos cordiales,"));
        assertTrue(contenido.contains("Sistema de Reservas ReservApp"));
    }

    // ================================
    // TESTS PARA enviarNotificacionReservaModificada
    // ================================

    @Test
    void testEnviarNotificacionReservaModificada_ConConvocatoria() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarNotificacionReservaModificada(reserva);

        // Assert - Esperar 2 llamadas: 1 al propietario + 1 al convocado
        verify(mailSender, times(2)).send(messageCaptor.capture());
        List<SimpleMailMessage> sentMessages = messageCaptor.getAllValues();
        
        // Verificar el mensaje al propietario (primer mensaje)
        SimpleMailMessage ownerMessage = sentMessages.get(0);
        assertEquals("noreply@reservapp.com", ownerMessage.getFrom());
        assertEquals("juan.perez@test.com", ownerMessage.getTo()[0]);
        assertEquals("Reserva Modificada - Sala de Reuniones A", ownerMessage.getSubject());
        
        String contenido = ownerMessage.getText();
        assertNotNull(contenido);
        assertTrue(contenido.contains("Juan Pérez"));
        assertTrue(contenido.contains("Su reserva ha sido modificada"));
        assertTrue(contenido.contains("25/12/2024 10:00"));
        assertTrue(contenido.contains("Sala de Reuniones A"));
    }

    @Test
    void testEnviarNotificacionReservaModificada_SinConvocatoria() {
        // Arrange
        reserva.setConvocatoria(null);

        // Act
        emailService.enviarNotificacionReservaModificada(reserva);

        // Assert - Solo se envía 1 correo al propietario
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testEnviarNotificacionReservaModificada_ConvocatoriaVacia() {
        // Arrange
        convocatoria.setConvocados(new ArrayList<>());

        // Act
        emailService.enviarNotificacionReservaModificada(reserva);

        // Assert - Solo se envía 1 correo al propietario, no a convocados
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testEnviarNotificacionReservaModificada_ConvocatoriaNull() {
        // Arrange
        convocatoria.setConvocados(null);

        // Act
        emailService.enviarNotificacionReservaModificada(reserva);

        // Assert - Solo se envía 1 correo al propietario, no a convocados
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testEnviarNotificacionReservaModificada_ErrorEnEnvio() {
        // Arrange
        doThrow(new RuntimeException("Error de conexión")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(MailSendException.class, () -> {
            emailService.enviarNotificacionReservaModificada(reserva);
        });
    }

    @Test
    void testNotificarComvocatoria_ErrorEnEnvioAConvocados() {
        // Arrange
        // Configurar el mock para que el primer envío (al propietario) sea exitoso
        // pero el segundo envío (al convocado) falle
        doNothing()
            .doThrow(new RuntimeException("Error al enviar correo al convocado"))
            .when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        MailSendException exception = assertThrows(MailSendException.class, () -> {
            emailService.enviarNotificacionReservaModificada(reserva);
        });
        
        // Verificar que la excepción contiene el mensaje esperado del método notificarComvocatoria
        assertTrue(exception.getMessage().contains("Error al enviar correo al usuario"));
        assertTrue(exception.getMessage().contains(convocado.getUsuario().getId()));
        
        // Verificar que se intentó enviar correo tanto al propietario como al convocado
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testNotificarComvocatoria_MultipleConvocados_ErrorEnUno() {
        // Arrange
        // Crear un segundo convocado
        Usuario usuario2 = new Usuario();
        usuario2.setId("user3");
        usuario2.setNombre("Carlos López");
        usuario2.setCorreo("carlos.lopez@test.com");

        Convocado convocado2 = new Convocado();
        ConvocadoPK pk2 = new ConvocadoPK(1, "user3");
        convocado2.setId(pk2);
        convocado2.setUsuario(usuario2);
        convocado2.setConvocatoria(convocatoria);

        // Actualizar la lista de convocados
        convocatoria.setConvocados(Arrays.asList(convocado, convocado2));

        // Configurar el mock para que falle en el segundo convocado
        doNothing()              // Envío al propietario - exitoso
            .doNothing()         // Primer convocado - exitoso  
            .doThrow(new RuntimeException("Error específico del segundo convocado"))  // Segundo convocado - falla
            .when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        MailSendException exception = assertThrows(MailSendException.class, () -> {
            emailService.enviarNotificacionReservaModificada(reserva);
        });
        
        // Verificar que la excepción contiene información del usuario que falló
        assertTrue(exception.getMessage().contains("Error al enviar correo al usuario"));
        assertTrue(exception.getMessage().contains("user3")); // ID del segundo usuario
        
        // Verificar que se intentaron enviar 3 correos (propietario + 2 convocados)
        verify(mailSender, times(3)).send(any(SimpleMailMessage.class));
    }
}