package es.ubu.reservapp.controller;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.FranjaHoraria;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.repositories.ReservaRepo;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.EstablecimientoService;
import es.ubu.reservapp.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Comparator; // Asegurarse de que esta importación está

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaControllerTest {

    @Mock
    private EstablecimientoService establecimientoService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ReservaRepo reservaRepo;

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
        usuarioAutenticado.setEstablecimiento(new ArrayList<>()); // Inicializar lista

//        when(authentication.getPrincipal()).thenReturn(usuarioAutenticado);
//        when(usuarioAutenticado.getCorreo()).thenReturn(usuarioAutenticado.getCorreo());
//        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

//        when(usuarioService.findUsuarioById(usuarioAutenticado.getId())).thenReturn(usuarioAutenticado);

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
    void testMostrarCalendarioReserva_Exito() {
    	when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        usuarioAutenticado.getEstablecimiento().add(establecimiento1); // Asignar establecimiento al usuario
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = reservaController.mostrarCalendarioReserva(1, model, redirectAttributes);

        assertEquals("reservas/calendario_reserva", viewName);
        verify(model).addAttribute(eq("establecimiento"), eq(establecimiento1));
        verify(model).addAttribute(eq("franjasHorarias"), anyList());
        verify(model).addAttribute(eq("reserva"), any(Reserva.class));
        assertTrue(redirectAttributes.getFlashAttributes().isEmpty());
    }

    @Test
    void testMostrarCalendarioReserva_EstablecimientoNoEncontrado() {
//        when(establecimientoService.findById(1)).thenReturn(Optional.empty());
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = reservaController.mostrarCalendarioReserva(1, model, redirectAttributes);

        assertEquals("redirect:/", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
    }
    
    @Test
    void testMostrarCalendarioReserva_UsuarioNoAsignadoAEstablecimiento() {
        Establecimiento otroEstablecimiento = new Establecimiento();
        otroEstablecimiento.setId(2);
        otroEstablecimiento.setNombre("Otro Est");
        
        usuarioAutenticado.getEstablecimiento().add(otroEstablecimiento); 
        
//        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1)); 
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = reservaController.mostrarCalendarioReserva(1, model, redirectAttributes);

        assertEquals("redirect:/", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Usuario no encontrado.", redirectAttributes.getFlashAttributes().get("error"));
    }
    
    // --- Pruebas para crearReserva ---

    @Test
    void testCrearReserva_Exito() {
        usuarioAutenticado.getEstablecimiento().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        
        LocalDate fechaTest = LocalDate.now().plusDays(1);
        while (fechaTest.getDayOfWeek() != DayOfWeek.MONDAY) {
            fechaTest = fechaTest.plusDays(1);
        }
        String fechaStr = fechaTest.toString();
        String horaStr = "10:00"; // Dentro de la franja 09:00-17:00

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, redirectAttributes);

        assertEquals("redirect:/misreservas", viewName);
        verify(reservaRepo).save(any(Reserva.class));
        assertNotNull(redirectAttributes.getFlashAttributes().get("exito"));
    }

    @Test
    void testCrearReserva_EstablecimientoNoEncontrado() {
//        when(establecimientoService.findById(1)).thenReturn(Optional.empty());
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();


        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, "10:00", redirectAttributes);

        assertEquals("redirect:/", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        verify(reservaRepo, never()).save(any(Reserva.class));
    }
    
    @Test
    void testCrearReserva_UsuarioNoAsignadoAEstablecimiento() {
//        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String horaStr = "10:00";
        
        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, redirectAttributes);
        
        assertEquals("redirect:/", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Usuario no autenticado correctamente.", redirectAttributes.getFlashAttributes().get("error"));
        verify(reservaRepo, never()).save(any(Reserva.class));
    }

    @Test
    void testCrearReserva_FueraDeFranjaHoraria_Antes() {
        usuarioAutenticado.getEstablecimiento().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String horaStr = "08:00"; // Antes de las 09:00

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, redirectAttributes);

        assertEquals("redirect:/reservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("La hora seleccionada está fuera del horario de apertura del establecimiento para ese día o no es válida."));
        verify(reservaRepo, never()).save(any(Reserva.class));
    }

    @Test
    void testCrearReserva_FueraDeFranjaHoraria_Despues() {
        usuarioAutenticado.getEstablecimiento().add(establecimiento1);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String horaStr = "17:30"; // Después de las 17:00

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, redirectAttributes);

        assertEquals("redirect:/reservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("La hora seleccionada está fuera del horario de apertura del establecimiento para ese día o no es válida."));
        verify(reservaRepo, never()).save(any(Reserva.class));
    }
    
    @Test
    void testCrearReserva_EnHoraFinFranja() {
        usuarioAutenticado.getEstablecimiento().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String horaStr = "17:00"; // Exactamente a la hora de fin

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, redirectAttributes);

        assertEquals("redirect:/reservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("La hora seleccionada está fuera del horario"));
        verify(reservaRepo, never()).save(any(Reserva.class));
    }

    @Test
    void testCrearReserva_DiaIncorrecto_SinFranja() {
        usuarioAutenticado.getEstablecimiento().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.TUESDAY).toString(); 
        String horaStr = "10:00";

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, redirectAttributes);

        assertEquals("redirect:/reservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("La hora seleccionada está fuera del horario"));
        verify(reservaRepo, never()).save(any(Reserva.class));
    }
    
    @Test
    void testCrearReserva_FormatoFechaInvalido() {
    	when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        usuarioAutenticado.getEstablecimiento().add(establecimiento1);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = "fecha-invalida";
        String horaStr = "10:00";

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, redirectAttributes);

        assertEquals("redirect:/reservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Formato de fecha u hora inválido.", redirectAttributes.getFlashAttributes().get("error"));
        verify(reservaRepo, never()).save(any(Reserva.class));
    }

    @Test
    void testCrearReserva_FormatoHoraInvalido() {
        usuarioAutenticado.getEstablecimiento().add(establecimiento1);
//        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String horaStr = "hora-invalida";

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, redirectAttributes);

        assertEquals("redirect:/", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Usuario no autenticado correctamente.", redirectAttributes.getFlashAttributes().get("error"));
        verify(reservaRepo, never()).save(any(Reserva.class));
    }
}
