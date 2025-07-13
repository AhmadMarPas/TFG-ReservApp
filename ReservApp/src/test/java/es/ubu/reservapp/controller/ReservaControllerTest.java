package es.ubu.reservapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import es.ubu.reservapp.controller.ReservaController.UsuarioSimpleDTO;
import es.ubu.reservapp.model.entities.Convocado;
import es.ubu.reservapp.model.entities.Convocatoria;
import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.FranjaHoraria;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.ConvocatoriaService;
import es.ubu.reservapp.service.EmailService;
import es.ubu.reservapp.service.EstablecimientoService;
import es.ubu.reservapp.service.ReservaService;
import es.ubu.reservapp.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
class ReservaControllerTest {

    @Mock
    private EstablecimientoService establecimientoService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ConvocatoriaService convocatoriaService;

    @Mock
    private EmailService emailService;
    
    @Mock
    private ReservaService reservaService;

    @Mock
    private Model model;
    
    @Mock
    private SessionData sessionData;
    
    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReservaController reservaController;

    private Usuario usuarioAutenticado;
    private Establecimiento establecimiento1;
    private FranjaHoraria franjaLunes;

    @BeforeEach
    void setUp() {
        usuarioAutenticado = new Usuario();
        usuarioAutenticado.setCorreo("testUser@example.com"); 
        usuarioAutenticado.setNombre("Test");
        usuarioAutenticado.setApellidos("User");
        usuarioAutenticado.setLstEstablecimientos(new ArrayList<>()); // Inicializar lista

        SecurityContextHolder.setContext(securityContext);

        establecimiento1 = new Establecimiento();
        establecimiento1.setId(1);
        establecimiento1.setNombre("Establecimiento Test 1");
        establecimiento1.setActivo(true);
        establecimiento1.setFranjasHorarias(new ArrayList<>());
        
        franjaLunes = new FranjaHoraria();
        franjaLunes.setDiaSemana(DayOfWeek.MONDAY); // Establecer día usando el setter
        franjaLunes.setHoraInicio(LocalTime.of(9, 0));
        franjaLunes.setHoraFin(LocalTime.of(17, 0));
        franjaLunes.setEstablecimiento(establecimiento1);
        establecimiento1.getFranjasHorarias().add(franjaLunes);
    }

    // --- Pruebas para mostrarCalendarioReserva ---

    @Test
    void testMostrarCalendarioReserva_ErrorPermisos() {
    	when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1); // Asignar establecimiento al usuario
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = reservaController.mostrarCalendarioReserva(1, model, redirectAttributes);

        assertEquals("redirect:/misreservas", viewName);
        assertFalse(redirectAttributes.getFlashAttributes().isEmpty());
        assertTrue(redirectAttributes.getFlashAttributes().containsValue("No tiene permiso para reservar en este establecimiento."));
    }

    @Test
    void testMostrarCalendarioReserva_EstablecimientoNoEncontrado() {
    	when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.empty());
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = reservaController.mostrarCalendarioReserva(1, model, redirectAttributes);

        assertEquals("redirect:/misreservas", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Establecimiento no encontrado.", redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(redirectAttributes.getFlashAttributes().containsValue("Establecimiento no encontrado."));
    }
    
    @Test
    void testMostrarCalendarioReserva_UsuarioNoEncontrado() {
        Establecimiento otroEstablecimiento = new Establecimiento();
        otroEstablecimiento.setId(2);
        otroEstablecimiento.setNombre("Otro Est");
        
        usuarioAutenticado.getLstEstablecimientos().add(otroEstablecimiento); 
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = reservaController.mostrarCalendarioReserva(1, model, redirectAttributes);

        assertEquals("redirect:/", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Usuario no autenticado correctamente.", redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(redirectAttributes.getFlashAttributes().containsValue("Usuario no autenticado correctamente."));
    }
    
    @Test
    void testMostrarCalendarioReserva_UsuarioNoAsignadoAEstablecimiento() {
        Establecimiento otroEstablecimiento = new Establecimiento();
        otroEstablecimiento.setId(3);
        otroEstablecimiento.setNombre("Otro Establecimiento");
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        
        usuarioAutenticado.getLstEstablecimientos().add(otroEstablecimiento); 
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = reservaController.mostrarCalendarioReserva(1, model, redirectAttributes);

        assertEquals("redirect:/misreservas", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("No tiene permiso para reservar en este establecimiento.", redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(redirectAttributes.getFlashAttributes().containsValue("No tiene permiso para reservar en este establecimiento."));
    }

    
    // --- Pruebas para crearReserva ---

    @Test
    void testCrearReserva_ErrorPermisos() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        
        LocalDate fechaTest = LocalDate.now().plusDays(1);
        while (fechaTest.getDayOfWeek() != DayOfWeek.MONDAY) {
            fechaTest = fechaTest.plusDays(1);
        }
        String fechaStr = fechaTest.toString();
        String horaInicioStr = "10:00"; // Dentro de la franja 09:00-17:00
        String horaFinStr = "11:00"; // Una hora después

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaInicioStr, horaFinStr, null, null, null, null, redirectAttributes);

        assertEquals("redirect:/misreservas", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(redirectAttributes.getFlashAttributes().containsValue("No tiene permiso para reservar en este establecimiento."));
    }
    
    @Test
    void testCrearReserva_ConConvocatoria() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        
        LocalDate fechaTest = LocalDate.now().plusDays(1);
        while (fechaTest.getDayOfWeek() != DayOfWeek.MONDAY) {
            fechaTest = fechaTest.plusDays(1);
        }
        String fechaStr = fechaTest.toString();
        String horaInicioStr = "10:00";
        String horaFinStr = "11:00";
        String enlaceReunion = "https://meet.example.com";
        String observaciones = "Observaciones de prueba";
        String[] usuariosConvocados = new String[]{"user1", "user2"};
        
        Usuario usuario1 = new Usuario();
        usuario1.setId("user1");
        Usuario usuario2 = new Usuario();
        usuario2.setId("user2");

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaInicioStr, horaFinStr, null, enlaceReunion, observaciones, usuariosConvocados, redirectAttributes);

        assertEquals("redirect:/misreservas", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(redirectAttributes.getFlashAttributes().containsValue("No tiene permiso para reservar en este establecimiento."));
    }
    
    @Test
    @Disabled("Para revisar")
    void testCrearReserva_ConConvocatoria_ErrorAlGuardarConvocatoria() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        
        LocalDate fechaTest = LocalDate.now().plusDays(1);
        while (fechaTest.getDayOfWeek() != DayOfWeek.MONDAY) {
            fechaTest = fechaTest.plusDays(1);
        }
        String fechaStr = fechaTest.toString();
        String horaInicioStr = "10:00";
        String horaFinStr = "11:00";
        String enlaceReunion = "https://meet.example.com";
        String observaciones = "Observaciones de prueba";
        String[] usuariosConvocados = new String[]{"user1", "user2"};
        
        Usuario usuario1 = new Usuario();
        usuario1.setId("user1");
        Usuario usuario2 = new Usuario();
        usuario2.setId("user2");
        
        when(usuarioService.findUsuarioById("user1")).thenReturn(usuario1);
        when(usuarioService.findUsuarioById("user2")).thenReturn(usuario2);
        when(convocatoriaService.save(any(Convocatoria.class))).thenThrow(new RuntimeException("Error al guardar convocatoria"));

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaInicioStr, horaFinStr, null, enlaceReunion, observaciones, usuariosConvocados, redirectAttributes);

        assertEquals("redirect:/misreservas/establecimiento/1", viewName);
        verify(reservaService).save(any(Reserva.class));
        verify(convocatoriaService).save(any(Convocatoria.class));
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
    }
    
    @Test
    void testCrearReserva_ConConvocatoria_ErrorAlEnviarCorreos() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        
        LocalDate fechaTest = LocalDate.now().plusDays(1);
        while (fechaTest.getDayOfWeek() != DayOfWeek.MONDAY) {
            fechaTest = fechaTest.plusDays(1);
        }
        String fechaStr = fechaTest.toString();
        String horaInicioStr = "10:00";
        String horaFinStr = "11:00";
        String enlaceReunion = "https://meet.example.com";
        String observaciones = "Observaciones de prueba";
        String[] usuariosConvocados = new String[]{"user1", "user2"};
        
        Usuario usuario1 = new Usuario();
        usuario1.setId("user1");
        Usuario usuario2 = new Usuario();
        usuario2.setId("user2");

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaInicioStr, horaFinStr, null, enlaceReunion, observaciones, usuariosConvocados, redirectAttributes);

        // Verificar que a pesar del error en el envío de correos, la operación se completa correctamente
        assertEquals("redirect:/misreservas", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("No tiene permiso para reservar en este establecimiento.", redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(redirectAttributes.getFlashAttributes().containsValue("No tiene permiso para reservar en este establecimiento."));
    }
    
    @Test
    @Disabled("Para revisar")
    void testCrearReserva_ConHoraFinInvalida() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        
        LocalDate fechaTest = LocalDate.now().plusDays(1);
        while (fechaTest.getDayOfWeek() != DayOfWeek.MONDAY) {
            fechaTest = fechaTest.plusDays(1);
        }
        String fechaStr = fechaTest.toString();
        String horaInicioStr = "10:00";
        String horaFinStr = "hora-invalida"; // Formato inválido

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaInicioStr, horaFinStr, null, null, null, null, redirectAttributes);

        assertEquals("redirect:/misreservas/establecimiento/1", viewName);
        verify(reservaService, never()).save(any(Reserva.class));
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
    }
    
    @Test
    @Disabled("Para revisar")
    void testCrearReserva_HoraFinAnteriorAHoraInicio() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        
        LocalDate fechaTest = LocalDate.now().plusDays(1);
        while (fechaTest.getDayOfWeek() != DayOfWeek.MONDAY) {
            fechaTest = fechaTest.plusDays(1);
        }
        String fechaStr = fechaTest.toString();
        String horaInicioStr = "11:00";
        String horaFinStr = "10:00"; // Hora fin anterior a hora inicio

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaInicioStr, horaFinStr, null, null, null, null, redirectAttributes);

        assertEquals("redirect:/misreservas/establecimiento/1", viewName);
        verify(reservaService, never()).save(any(Reserva.class));
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("La hora de fin debe ser posterior a la hora de inicio"));
    }

    @Test
    void testCrearReserva_EstablecimientoNoEncontrado() {
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.empty());

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, "10:00", "11:00", null, null, null, null, redirectAttributes);

        assertEquals("redirect:/misreservas", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        verify(reservaService, never()).save(any(Reserva.class));
        
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Establecimiento no encontrado.", redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(redirectAttributes.getFlashAttributes().containsValue("Establecimiento no encontrado."));
    }
    
    @Test
    void testCrearReserva_UsuarioNoAsignadoAEstablecimiento() {
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String horaStr = "10:00";
        Establecimiento otroEstablecimiento = new Establecimiento();
        otroEstablecimiento.setId(3);
        otroEstablecimiento.setNombre("Otro Establecimiento");
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        
        usuarioAutenticado.getLstEstablecimientos().add(otroEstablecimiento); 
        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, "11:00", null, null, null, null, redirectAttributes);
        
        assertEquals("redirect:/misreservas", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("No tiene permiso para reservar en este establecimiento.", redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(redirectAttributes.getFlashAttributes().containsValue("No tiene permiso para reservar en este establecimiento."));

        verify(reservaService, never()).save(any(Reserva.class));
    }

    @Test
    @Disabled("Para revisar")
    void testCrearReserva_FueraDeFranjaHoraria_Antes() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String horaStr = "08:00"; // Antes de las 09:00

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, "09:00", null, null, null, null, redirectAttributes);

        assertEquals("redirect:/misreservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("La hora seleccionada está fuera del horario de apertura del establecimiento para ese día o no es válida."));
        verify(reservaService, never()).save(any(Reserva.class));
    }

    @Test
    @Disabled("Para revisar")
    void testCrearReserva_FueraDeFranjaHoraria_Despues() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String horaStr = "17:30"; // Después de las 17:00

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, "18:00", null, null, null, null, redirectAttributes);

        assertEquals("redirect:/misreservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("La hora seleccionada está fuera del horario de apertura del establecimiento para ese día o no es válida."));
        verify(reservaService, never()).save(any(Reserva.class));
    }
    
    @Test
    @Disabled("Para revisar")
    void testCrearReserva_EnHoraFinFranja() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String horaStr = "17:00"; // Exactamente a la hora de fin

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, "18:00", null, null, null, null, redirectAttributes);

        assertEquals("redirect:/misreservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("La hora seleccionada está fuera del horario"));
        verify(reservaService, never()).save(any(Reserva.class));
    }

    @Test
    @Disabled("Para revisar")
    void testCrearReserva_DiaIncorrecto_SinFranja() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.TUESDAY).toString(); 
        String horaStr = "10:00";

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, "11:00", null, null, null, null, redirectAttributes);

        assertEquals("redirect:/misreservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("La hora seleccionada está fuera del horario"));
        verify(reservaService, never()).save(any(Reserva.class));
    }
    
    @Test
    @Disabled("Para revisar")
    void testCrearReserva_FormatoFechaInvalido() {
    	when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = "fecha-invalida";
        String horaStr = "10:00";

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, "11:00", null, null, null, null, redirectAttributes);

        assertEquals("redirect:/misreservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Formato de fecha u hora inválido.", redirectAttributes.getFlashAttributes().get("error"));
        verify(reservaService, never()).save(any(Reserva.class));
    }

    @Test
    @Disabled("Para revisar")
    void testCrearReserva_FormatoHoraInvalido() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String horaStr = "hora-invalida";

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, "11:00", null, null, null, null, redirectAttributes);

        assertEquals("redirect:/misreservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Formato de fecha u hora inválido.", redirectAttributes.getFlashAttributes().get("error"));
        verify(reservaService, never()).save(any(Reserva.class));
    }
    
    @Test
    @Disabled("Para revisar")
    void testCrearReserva_FormatoHoraFinInvalido() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String horaInicioStr = "10:00";
        String horaFinStr = "hora-fin-invalida";

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaInicioStr, horaFinStr, null, null, null, null, redirectAttributes);

        assertEquals("redirect:/misreservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Formato de fecha u hora inválido.", redirectAttributes.getFlashAttributes().get("error"));
        verify(reservaService, never()).save(any(Reserva.class));
    }
    
    @Test
    @Disabled("Para revisar")
    void testCrearReserva_HoraFinAnteriorAHoraInicio2() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String horaInicioStr = "11:00";
        String horaFinStr = "10:00"; // Hora fin anterior a hora inicio

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaInicioStr, horaFinStr, null, null, null, null, redirectAttributes);

        assertEquals("redirect:/misreservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("La hora de fin debe ser posterior a la hora de inicio.", redirectAttributes.getFlashAttributes().get("error"));
        verify(reservaService, never()).save(any(Reserva.class));
    }

    @Test
    @Disabled("Para revisar")
    void testCrearReserva_HoraFinIgualAHoraInicio() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String horaInicioStr = "10:00";
        String horaFinStr = "10:00"; // Hora fin igual a hora inicio

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaInicioStr, horaFinStr, null, null, null, null, redirectAttributes);

        assertEquals("redirect:/misreservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("La hora de fin debe ser posterior a la hora de inicio.", redirectAttributes.getFlashAttributes().get("error"));
        verify(reservaService, never()).save(any(Reserva.class));
    }

    @Test
    @Disabled("Para revisar")
    void testCrearReserva_HoraFinFueraDeRangoFranja() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String horaInicioStr = "16:00"; // Dentro de la franja
        String horaFinStr = "18:00"; // Fuera de la franja (después de 17:00)

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaInicioStr, horaFinStr, null, null, null, null, redirectAttributes);

        assertEquals("redirect:/misreservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("La hora seleccionada está fuera del horario de apertura del establecimiento para ese día o no es válida."));
        verify(reservaService, never()).save(any(Reserva.class));
    }

    @Test
    @Disabled("Para revisar")
    void crearReserva_whenSaveThrowsException_shouldReturnRedirectWithError() {
        ReservaController controller = new ReservaController(sessionData, establecimientoService, reservaService, convocatoriaService, usuarioService, emailService);
        Reserva reserva = new Reserva();
        Integer establecimientoId = 1;
        String fechaStr = "2024-02-20";
        String horaStr = "10:00";
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        
        Usuario usuario = new Usuario();
        Establecimiento establecimiento = new Establecimiento();
        establecimiento.setId(establecimientoId);
        FranjaHoraria franja = new FranjaHoraria();
        franja.setDiaSemana(DayOfWeek.TUESDAY);
        franja.setHoraInicio(LocalTime.of(9, 0));
        franja.setHoraFin(LocalTime.of(18, 0));
        establecimiento.setFranjasHorarias(Arrays.asList(franja));
        usuario.setLstEstablecimientos(Arrays.asList(establecimiento));
        
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(establecimientoId)).thenReturn(Optional.of(establecimiento));
        when(reservaService.save(any(Reserva.class))).thenThrow(new RuntimeException("Error al guardar"));
        
        String result = controller.crearReserva(reserva, establecimientoId, fechaStr, horaStr, "11:00", null, null, null, null, redirectAttributes);
        
        assertEquals("redirect:/misreservas/establecimiento/" + establecimientoId, result);
        assertTrue(((RedirectAttributesModelMap) redirectAttributes)
            .getFlashAttributes()
            .get("error").toString()
            .contains("Error al guardar la reserva"));
        verify(reservaService).save(any(Reserva.class));
    }
    
    // --- Pruebas para slots predefinidos ---
    
    @Test
    void testCrearReserva_ConSlotPredefinido_ErrorPermisos() {
    	Reserva reserva = new Reserva();
        
    	// Configurar establecimiento con duración fija
        establecimiento1.setDuracionReserva(60); // 60 minutos
        establecimiento1.setDescansoServicios(15); // 15 minutos de descanso
        
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        
        LocalDate fechaTest = LocalDate.now().plusDays(1);
        while (fechaTest.getDayOfWeek() != DayOfWeek.MONDAY) {
            fechaTest = fechaTest.plusDays(1);
        }
        String fechaStr = fechaTest.toString();
        String slotSeleccionado = "10:00 - 11:00"; // Slot válido dentro de la franja
        
        String viewName = reservaController.crearReserva(reserva, 1, fechaStr, null, null, slotSeleccionado, null, null, null, redirectAttributes);
        
        assertEquals("redirect:/misreservas", viewName);
        assertNull(redirectAttributes.getFlashAttributes().get("exito"));
    }
    
    @Test
    @Disabled("Para revisar")
    void testCrearReserva_SlotFormatoInvalido() {
    	Reserva reserva = new Reserva();
    	establecimiento1.setDuracionReserva(60);
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String slotSeleccionado = "10:00-11:00"; // Formato incorrecto (sin espacios)
        
        String viewName = reservaController.crearReserva(reserva, 1, fechaStr, null, null, slotSeleccionado, null, null, null, redirectAttributes);
        
        assertEquals("redirect:/misreservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Formato de slot inválido", redirectAttributes.getFlashAttributes().get("error"));
        verify(reservaService, never()).save(any(Reserva.class));
    }
    
    @Test
    @Disabled("Para revisar")
    void testCrearReserva_SinHorasNiSlot() {
    	Reserva reserva = new Reserva();
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        
        String viewName = reservaController.crearReserva(reserva, 1, fechaStr, null, null, null, null, null, null, redirectAttributes);
        
        assertEquals("redirect:/misreservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Debe especificar hora de inicio y fin", redirectAttributes.getFlashAttributes().get("error"));
        verify(reservaService, never()).save(any(Reserva.class));
    }
    
    @Test
    @Disabled("Para revisar")
    void testCrearReserva_ConConvocatoria_ErrorAlGuardarConvocatoria2() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        
        // Simular que la reserva se guarda correctamente
        when(reservaService.save(any(Reserva.class))).thenAnswer(invocation -> {
            Reserva r = invocation.getArgument(0);
            r.setId(1); // Asignar un ID a la reserva guardada
            return r;
        });
        
        // Simular error al guardar la convocatoria
        when(convocatoriaService.save(any())).thenThrow(new RuntimeException("Error al guardar convocatoria"));
        
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        
        LocalDate fechaTest = LocalDate.now().plusDays(1);
        while (fechaTest.getDayOfWeek() != DayOfWeek.MONDAY) {
            fechaTest = fechaTest.plusDays(1);
        }
        String fechaStr = fechaTest.toString();
        String horaInicioStr = "10:00";
        String horaFinStr = "11:00";
        String[] usuariosConvocados = new String[]{"user1"};
        
        Usuario usuario1 = new Usuario();
        usuario1.setId("user1");
        when(usuarioService.findUsuarioById("user1")).thenReturn(usuario1);
        
        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaInicioStr, horaFinStr, null, "https://meet.example.com", "Observaciones de prueba", usuariosConvocados, redirectAttributes);
        
        assertEquals("redirect:/misreservas/editar/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("Error al crear la reserva"));
        
        // Verificar que se intentó guardar la reserva
        verify(reservaService).save(any(Reserva.class));
        // Verificar que se intentó guardar la convocatoria
        verify(convocatoriaService).save(any());
    }

    // --- Pruebas para mostrarMisReservas ---
    
    @Test
    void testMostrarMisReservas_Exito() {
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        List<Establecimiento> establecimientos = List.of(establecimiento1);
        when(establecimientoService.findAll()).thenReturn(establecimientos);
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        
        String viewName = reservaController.mostrarMisReservas(model, redirectAttributes);
        
        assertEquals("reservas/misreservas", viewName);
        verify(model).addAttribute("establecimientos", establecimientos);
        assertTrue(redirectAttributes.getFlashAttributes().isEmpty());
    }
    
    @Test
    void testMostrarMisReservas_ConReservas() {
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        List<Establecimiento> establecimientos = List.of(establecimiento1);
        when(establecimientoService.findAll()).thenReturn(establecimientos);
        
        // Configurar algunas reservas para el usuario
        Reserva reserva1 = new Reserva();
        reserva1.setId(1);
        reserva1.setUsuario(usuarioAutenticado);
        reserva1.setEstablecimiento(establecimiento1);
        reserva1.setFechaReserva(LocalDateTime.now().plusDays(1));
        
        Reserva reserva2 = new Reserva();
        reserva2.setId(2);
        reserva2.setUsuario(usuarioAutenticado);
        reserva2.setEstablecimiento(establecimiento1);
        reserva2.setFechaReserva(LocalDateTime.now().minusDays(1));
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        
        String viewName = reservaController.mostrarMisReservas(model, redirectAttributes);
        
        assertEquals("reservas/misreservas", viewName);
        verify(model).addAttribute("establecimientos", establecimientos);
        assertTrue(redirectAttributes.getFlashAttributes().isEmpty());
    }
    
    @Test
    void testMostrarMisReservas_UsuarioNoAutenticado() {
        when(sessionData.getUsuario()).thenReturn(null);
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        
        String viewName = reservaController.mostrarMisReservas(model, redirectAttributes);
        
        assertEquals("error", viewName);
        verify(model).addAttribute("error", "Usuario no encontrado");
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Usuario no autenticado correctamente.", redirectAttributes.getFlashAttributes().get("error"));
    }
    
    @Test
    void testMostrarMisReservas_ExcepcionEnServicio() {
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findAll()).thenThrow(new RuntimeException("Error de base de datos"));
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        
        String viewName = reservaController.mostrarMisReservas(model, redirectAttributes);
        
        assertEquals("error", viewName);
        verify(model).addAttribute("error", "Error interno del servidor");
        assertTrue(redirectAttributes.getFlashAttributes().isEmpty());
    }
    
    // --- Pruebas para buscarUsuarios ---
    
    @Test
    void testBuscarUsuarios() {
    	usuarioAutenticado.setId("admin");
        Usuario usuario1 = new Usuario();
        usuario1.setId("user1");
        usuario1.setNombre("Juan");
        usuario1.setApellidos("Pérez");
        usuario1.setCorreo("juan@example.com");
        
        Usuario usuario2 = new Usuario();
        usuario2.setId("user2");
        usuario2.setNombre("María");
        usuario2.setApellidos("López");
        usuario2.setCorreo("maria@example.com");
        
        List<Usuario> usuarios = Arrays.asList(usuario1, usuario2, usuarioAutenticado);
        
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(usuarioService.findAll()).thenReturn(usuarios);
        
        List<ReservaController.UsuarioDTO> resultado = reservaController.buscarUsuarios("juan");
        
        assertEquals(1, resultado.size());
        assertEquals("user1", resultado.get(0).getId());
        assertEquals("Juan Pérez", resultado.get(0).getNombre());
        assertEquals("juan@example.com", resultado.get(0).getCorreo());
    }
    
    @Test
    void testBuscarUsuarios_QueryCorta() {
        List<ReservaController.UsuarioDTO> resultado = reservaController.buscarUsuarios("a");
        
        assertTrue(resultado.isEmpty());
    }
    
    @Test
    void testBuscarUsuarios_QueryNull() {
        List<ReservaController.UsuarioDTO> resultado = reservaController.buscarUsuarios(null);
        
        assertTrue(resultado.isEmpty());
    }
    
    // --- Pruebas para mostrarFormularioEditar ---
    
    @Test
    @Disabled("Para revisar")
    void testMostrarFormularioEditar_Exito() {
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuarioAutenticado);
        reserva.setEstablecimiento(establecimiento1);
        reserva.setFechaReserva(LocalDateTime.now().plusDays(1));
        
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        String viewName = reservaController.mostrarFormularioEditar(1, model, redirectAttributes);
        
        assertEquals("reservas/editar_reserva", viewName);
        verify(model).addAttribute("reserva", reserva);
        verify(model).addAttribute("usuarios", List.of(usuarioAutenticado));
    }
    
    @Test
    void testMostrarFormularioEditar_ConConvocatorias() {
    	usuarioAutenticado.setId("user1");
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuarioAutenticado);
        reserva.setEstablecimiento(establecimiento1);
        reserva.setFechaReserva(LocalDateTime.now().plusDays(1));
        
        // Configurar convocatorias existentes
        Convocatoria convocatoria = new Convocatoria();
        convocatoria.setId(1);
        convocatoria.setReserva(reserva);
        reserva.setConvocatoria(convocatoria);
        convocatoria.setEnlace("https://meet.example.com");
        convocatoria.setObservaciones("Observaciones de prueba");

        Usuario usuarioConvocado = new Usuario();
        usuarioConvocado.setId("user1");
        Convocado convocado = new Convocado();
        convocado.setConvocatoria(convocatoria);
        convocado.setUsuario(usuarioConvocado);
        convocatoria.setConvocados(List.of(convocado));
        convocado.setUsuario(usuarioConvocado);
        convocatoria.setConvocados(List.of(convocado));
        List<UsuarioSimpleDTO> usuariosConvocados = new ArrayList<>();
        usuariosConvocados.add(new UsuarioSimpleDTO(usuarioConvocado.getId(), usuarioConvocado.getNombre(), usuarioConvocado.getApellidos(), usuarioConvocado.getCorreo()));
        
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        String viewName = reservaController.mostrarFormularioEditar(1, model, redirectAttributes);
        
        assertEquals("reservas/editar_reserva", viewName);
        verify(model).addAttribute("reserva", reserva);
        verify(model).addAttribute("isEdit", true);
        verify(model).addAttribute("establecimiento", establecimiento1);
        verify(model).addAttribute("convocatoria", convocatoria);
    }
    
    @Test
    void testMostrarFormularioEditar_ReservaNoEncontrada() {
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(reservaService.findById(999)).thenThrow(new IllegalArgumentException("Reserva no encontrada"));
        
        String viewName = reservaController.mostrarFormularioEditar(999, model, redirectAttributes);
        
        assertEquals("redirect:/misreservas", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Error al cargar la reserva para edición.", redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(redirectAttributes.getFlashAttributes().containsValue("Error al cargar la reserva para edición."));
    }
    
    @Test
    void testMostrarFormularioEditar_SinPermiso() {
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId("otro");
        
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(otroUsuario);
        reserva.setEstablecimiento(establecimiento1);
        
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        String viewName = reservaController.mostrarFormularioEditar(1, model, redirectAttributes);
        
        assertEquals("redirect:/misreservas", viewName);
        assertTrue(redirectAttributes.getFlashAttributes().containsValue("No tienes permisos para editar esta reserva."));
    }
    
    @Test
    void testMostrarFormularioEditar_ReservaPasada() {
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        usuarioAutenticado.setId("user1");
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuarioAutenticado);
        reserva.setEstablecimiento(establecimiento1);
        reserva.setFechaReserva(LocalDateTime.now().minusDays(1)); // Reserva pasada
        
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        String viewName = reservaController.mostrarFormularioEditar(1, model, redirectAttributes);
        
        assertEquals("redirect:/misreservas/establecimiento/1", viewName);
        assertEquals("No se pueden editar reservas pasadas.", redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(redirectAttributes.getFlashAttributes().containsValue("No se pueden editar reservas pasadas."));
    }
    
    @Test
    void testMostrarFormularioEditar_ErrorAlObtenerConvocatorias() {
    	usuarioAutenticado.setId("user1");
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuarioAutenticado);
        reserva.setEstablecimiento(establecimiento1);
        reserva.setFechaReserva(LocalDateTime.now().plusDays(1));
        
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        String viewName = reservaController.mostrarFormularioEditar(1, model, redirectAttributes);
        
        assertEquals("reservas/editar_reserva", viewName);
        verify(model).addAttribute("reserva", reserva);
        verify(model).addAttribute("establecimiento", establecimiento1);
        verify(model).addAttribute("isEdit", true);
    }
    
    // --- Pruebas para actualizarReserva ---
    
    @Test
    void testActualizarReserva_Exito() {
    	usuarioAutenticado.setId("user1");
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuarioAutenticado);
        reserva.setEstablecimiento(establecimiento1);
        reserva.setFechaReserva(LocalDateTime.now().plusDays(1));
        
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(reservaService.findById(1)).thenReturn(reserva);
        when(reservaService.save(any(Reserva.class))).thenReturn(reserva);
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        
        String viewName = reservaController.actualizarReserva(1, fechaStr, "10:00", "11:00", null, null, null, null, redirectAttributes);
        
        assertEquals("redirect:/misreservas/establecimiento/1", viewName);
        verify(reservaService).save(any(Reserva.class));
        assertNotNull(redirectAttributes.getFlashAttributes().get("exito"));
    }
    
    @Test
    void testActualizarReserva_ReservaNoEncontrada() {
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(reservaService.findById(999)).thenThrow(new IllegalArgumentException("Reserva no encontrada"));
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        
        String viewName = reservaController.actualizarReserva(999, "2024-01-15", "10:00", "11:00", null, null, null, null, redirectAttributes);
        
        assertEquals("redirect:/misreservas/editar/999", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Error al actualizar la reserva: Reserva no encontrada", redirectAttributes.getFlashAttributes().get("error"));
    }
    
    @Test
    void testActualizarReserva_FormatoFechaInvalido2() {
    	usuarioAutenticado.setId("user1");
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuarioAutenticado);
        reserva.setEstablecimiento(establecimiento1);
        reserva.setFechaReserva(LocalDateTime.now().plusDays(1));
        
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        
        String viewName = reservaController.actualizarReserva(1, "fecha-invalida", "10:00", "11:00", null, null, null, null, redirectAttributes);
        
        assertEquals("redirect:/misreservas/editar/1", viewName);
        verify(reservaService, never()).save(any(Reserva.class));
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
    }
    
    @Test
    void testActualizarReserva_FormatoHoraInvalido() {
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuarioAutenticado);
        reserva.setEstablecimiento(establecimiento1);
        reserva.setFechaReserva(LocalDateTime.now().plusDays(1));
        
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        
        String viewName = reservaController.actualizarReserva(1, fechaStr, "hora-invalida", "11:00", null, null, null, null, redirectAttributes);
        
        assertEquals("redirect:/misreservas/editar/1", viewName);
        verify(reservaService, never()).save(any(Reserva.class));
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
    }
    
    @Test
    void testActualizarReserva_SinPermiso() {
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId("otro");
        
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(otroUsuario);
        reserva.setEstablecimiento(establecimiento1);
        
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        
        String viewName = reservaController.actualizarReserva(1, "2024-01-15", "10:00", "11:00", null, null, null, null, redirectAttributes);
        
        assertEquals("redirect:/misreservas", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("No tienes permisos para editar esta reserva.", redirectAttributes.getFlashAttributes().get("error"));
    }
    
    @Test
    void testActualizarReserva_ReservaPasada() {
    	usuarioAutenticado.setId("user1");
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuarioAutenticado);
        reserva.setEstablecimiento(establecimiento1);
        reserva.setFechaReserva(LocalDateTime.now().minusDays(1)); // Reserva pasada
        
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        
        String viewName = reservaController.actualizarReserva(1, "2024-01-15", "10:00", "11:00", null, null, null, null, redirectAttributes);
        
        assertEquals("redirect:/misreservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("No se pueden editar reservas pasadas.", redirectAttributes.getFlashAttributes().get("error"));
    }
    
    @Test
    void testActualizarReserva_ErrorAlGuardar() {
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuarioAutenticado);
        reserva.setEstablecimiento(establecimiento1);
        reserva.setFechaReserva(LocalDateTime.now().plusDays(1));
        
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        
        String viewName = reservaController.actualizarReserva(1, fechaStr, "10:00", "11:00", null, null, null, null, redirectAttributes);
        
        assertEquals("redirect:/misreservas/editar/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("Error al actualizar la reserva"));
    }
    
    @Test
    void testActualizarReserva_ConConvocatoria() {
    	usuarioAutenticado.setId("user1");
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuarioAutenticado);
        reserva.setEstablecimiento(establecimiento1);
        reserva.setFechaReserva(LocalDateTime.now().plusDays(1));
        
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(reservaService.findById(1)).thenReturn(reserva);
        when(reservaService.save(any(Reserva.class))).thenReturn(reserva);
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String[] usuariosConvocados = new String[]{"user1", "user2"};
        
        Usuario usuario1 = new Usuario();
        usuario1.setId("user1");
        Usuario usuario2 = new Usuario();
        usuario2.setId("user2");
        
        when(usuarioService.findUsuarioById("user1")).thenReturn(usuario1);
        when(usuarioService.findUsuarioById("user2")).thenReturn(usuario2);
        
        String viewName = reservaController.actualizarReserva(1, fechaStr, "10:00", "11:00", null, "https://meet.example.com", "Observaciones de prueba", usuariosConvocados, redirectAttributes);
        
        assertEquals("redirect:/misreservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("exito"));
        verify(convocatoriaService, times(2)).save(any(Convocatoria.class));
    }
    
    @Test
    void testActualizarReserva_ConSlotSeleccionado() {
    	usuarioAutenticado.setId("user1");
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuarioAutenticado);
        reserva.setEstablecimiento(establecimiento1);
        reserva.setFechaReserva(LocalDateTime.now().plusDays(1));
        
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(reservaService.findById(1)).thenReturn(reserva);
        when(reservaService.save(any(Reserva.class))).thenReturn(reserva);
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        
        String viewName = reservaController.actualizarReserva(1, fechaStr, null, null, "10:00 - 11:00", null, null, null, redirectAttributes);
        
        assertEquals("redirect:/misreservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("exito"));
    }
    
    @Test
    void testActualizarReserva_FormatoFechaInvalido() {
    	usuarioAutenticado.setId("user1");
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuarioAutenticado);
        reserva.setEstablecimiento(establecimiento1);
        reserva.setFechaReserva(LocalDateTime.now().plusDays(1));
        
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        
        String viewName = reservaController.actualizarReserva(1, "fecha-invalida", "10:00", "11:00", null, null, null, null, redirectAttributes);
        
        assertEquals("redirect:/misreservas/editar/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
    }
    
    @Test
    void testActualizarReserva_ErrorAlGuardarConvocatoria() {
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuarioAutenticado);
        reserva.setEstablecimiento(establecimiento1);
        reserva.setFechaReserva(LocalDateTime.now().plusDays(1));
        
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String[] usuariosConvocados = new String[]{"user1"};
        
        Usuario usuario1 = new Usuario();
        usuario1.setId("user1");
        
        String viewName = reservaController.actualizarReserva(1, fechaStr, "10:00", "11:00", null, "https://meet.example.com", "Observaciones de prueba", usuariosConvocados, redirectAttributes);
        
        assertEquals("redirect:/misreservas/editar/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("Error al actualizar la reserva"));
    }
    
    @Test
    void testActualizarReserva_HoraFinAnteriorAHoraInicio() {
    	usuarioAutenticado.setId("user1");
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuarioAutenticado);
        reserva.setEstablecimiento(establecimiento1);
        reserva.setFechaReserva(LocalDateTime.now().plusDays(1));
        
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        
        String viewName = reservaController.actualizarReserva(1, fechaStr, "11:00", "10:00", null, null, null, null, redirectAttributes);
        
        assertEquals("redirect:/misreservas/editar/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("La hora de fin debe ser posterior a la hora de inicio."));
    }
    
    @Test
    void testActualizarReserva_ErrorAlEnviarCorreos() {
    	usuarioAutenticado.setId("user1");
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuarioAutenticado);
        reserva.setEstablecimiento(establecimiento1);
        reserva.setFechaReserva(LocalDateTime.now().plusDays(1));
        
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(reservaService.findById(1)).thenReturn(reserva);
        when(reservaService.save(any(Reserva.class))).thenReturn(reserva);
        
        // Configurar el comportamiento normal para guardar la convocatoria
        when(convocatoriaService.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Simular error al enviar correos electrónicos
        doThrow(new RuntimeException("Error al enviar correos"))
            .when(emailService).enviarNotificacionesConvocatoria(anyList(), any(Reserva.class));
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String[] usuariosConvocados = new String[]{"user1"};
        
        Usuario usuario1 = new Usuario();
        usuario1.setId("user1");
        when(usuarioService.findUsuarioById("user1")).thenReturn(usuario1);
        
        String viewName = reservaController.actualizarReserva(
                1, fechaStr, "10:00", "11:00", null,
                "https://meet.example.com", "Observaciones de prueba", usuariosConvocados,
                redirectAttributes);
        
        // Verificar que la actualización fue exitosa a pesar del error al enviar correos
        assertEquals("redirect:/misreservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("exito"));
        assertEquals("Reserva actualizada correctamente.", redirectAttributes.getFlashAttributes().get("exito"));
        
        // Verificar que se llamaron los métodos esperados
        verify(reservaService).save(any(Reserva.class));
        verify(emailService).enviarNotificacionesConvocatoria(anyList(), any(Reserva.class));
    }
    
    @Test
    void testActualizarReserva_FormatoHoraFinInvalido() {
    	usuarioAutenticado.setId("user1");
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuarioAutenticado);
        reserva.setEstablecimiento(establecimiento1);
        reserva.setFechaReserva(LocalDateTime.now().plusDays(1));
        
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        
        String viewName = reservaController.actualizarReserva(
                1, fechaStr, "10:00", "hora-fin-invalida", null,
                null, null, null,
                redirectAttributes);
        
        assertEquals("redirect:/misreservas/editar/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("Formato de fecha u hora inválido."));
        verify(reservaService, never()).save(any(Reserva.class));
    }
    
    @Test
    void testActualizarReserva_SlotFormatoInvalido() {
    	usuarioAutenticado.setId("user1");
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuarioAutenticado);
        reserva.setEstablecimiento(establecimiento1);
        reserva.setFechaReserva(LocalDateTime.now().plusDays(1));
        establecimiento1.setDuracionReserva(60); // Duración de 60 minutos
        
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        
        // Usar un formato de slot inválido
        String viewName = reservaController.actualizarReserva(
                1, fechaStr, null, null, "formato-slot-invalido",
                null, null, null,
                redirectAttributes);
        
        assertEquals("redirect:/misreservas/editar/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("Formato de slot inválido"));
        verify(reservaService, never()).save(any(Reserva.class));
    }
}
