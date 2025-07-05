package es.ubu.reservapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
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

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.FranjaHoraria;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.repositories.ReservaRepo;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.EstablecimientoService;
import es.ubu.reservapp.service.UsuarioService;

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
    void testMostrarCalendarioReserva_Exito() {
    	when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1); // Asignar establecimiento al usuario
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = reservaController.mostrarCalendarioReserva(1, model, redirectAttributes);

        assertEquals("reservas/calendario_reserva", viewName);
        verify(model).addAttribute("establecimiento", establecimiento1);
        verify(model).addAttribute(eq("franjasHorarias"), anyList());
        verify(model).addAttribute(eq("reserva"), any(Reserva.class));
        assertTrue(redirectAttributes.getFlashAttributes().isEmpty());
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
    void testCrearReserva_Exito() {
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

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaInicioStr, horaFinStr, null, redirectAttributes);

        assertEquals("redirect:/misreservas", viewName);
        verify(reservaRepo).save(any(Reserva.class));
        assertNotNull(redirectAttributes.getFlashAttributes().get("exito"));
    }

    @Test
    void testCrearReserva_EstablecimientoNoEncontrado() {
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.empty());

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, "10:00", "11:00", null, redirectAttributes);

        assertEquals("redirect:/misreservas", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        verify(reservaRepo, never()).save(any(Reserva.class));
        
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
        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, "11:00", null, redirectAttributes);
        
        assertEquals("redirect:/misreservas", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("No tiene permiso para reservar en este establecimiento.", redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(redirectAttributes.getFlashAttributes().containsValue("No tiene permiso para reservar en este establecimiento."));

        verify(reservaRepo, never()).save(any(Reserva.class));
    }

    @Test
    void testCrearReserva_FueraDeFranjaHoraria_Antes() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String horaStr = "08:00"; // Antes de las 09:00

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, "09:00", null, redirectAttributes);

        assertEquals("redirect:/reservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("La hora seleccionada está fuera del horario de apertura del establecimiento para ese día o no es válida."));
        verify(reservaRepo, never()).save(any(Reserva.class));
    }

    @Test
    void testCrearReserva_FueraDeFranjaHoraria_Despues() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String horaStr = "17:30"; // Después de las 17:00

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, "18:00", null, redirectAttributes);

        assertEquals("redirect:/reservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("La hora seleccionada está fuera del horario de apertura del establecimiento para ese día o no es válida."));
        verify(reservaRepo, never()).save(any(Reserva.class));
    }
    
    @Test
    void testCrearReserva_EnHoraFinFranja() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String horaStr = "17:00"; // Exactamente a la hora de fin

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, "18:00", null, redirectAttributes);

        assertEquals("redirect:/reservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("La hora seleccionada está fuera del horario"));
        verify(reservaRepo, never()).save(any(Reserva.class));
    }

    @Test
    void testCrearReserva_DiaIncorrecto_SinFranja() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.TUESDAY).toString(); 
        String horaStr = "10:00";

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, "11:00", null, redirectAttributes);

        assertEquals("redirect:/reservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("La hora seleccionada está fuera del horario"));
        verify(reservaRepo, never()).save(any(Reserva.class));
    }
    
    @Test
    void testCrearReserva_FormatoFechaInvalido() {
    	when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = "fecha-invalida";
        String horaStr = "10:00";

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, "11:00", null, redirectAttributes);

        assertEquals("redirect:/reservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Formato de fecha u hora inválido.", redirectAttributes.getFlashAttributes().get("error"));
        verify(reservaRepo, never()).save(any(Reserva.class));
    }

    @Test
    void testCrearReserva_FormatoHoraInvalido() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String horaStr = "hora-invalida";

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaStr, "11:00", null, redirectAttributes);

        assertEquals("redirect:/", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Usuario no autenticado correctamente.", redirectAttributes.getFlashAttributes().get("error"));
        verify(reservaRepo, never()).save(any(Reserva.class));
    }
    
    @Test
    void testCrearReserva_HoraFinAnteriorAHoraInicio() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String horaInicioStr = "11:00";
        String horaFinStr = "10:00"; // Hora fin anterior a hora inicio

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaInicioStr, horaFinStr, null, redirectAttributes);

        assertEquals("redirect:/reservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("La hora de fin debe ser posterior a la hora de inicio.", redirectAttributes.getFlashAttributes().get("error"));
        verify(reservaRepo, never()).save(any(Reserva.class));
    }

    @Test
    void testCrearReserva_HoraFinIgualAHoraInicio() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String horaInicioStr = "10:00";
        String horaFinStr = "10:00"; // Hora fin igual a hora inicio

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaInicioStr, horaFinStr, null, redirectAttributes);

        assertEquals("redirect:/reservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("La hora de fin debe ser posterior a la hora de inicio.", redirectAttributes.getFlashAttributes().get("error"));
        verify(reservaRepo, never()).save(any(Reserva.class));
    }

    @Test
    void testCrearReserva_HoraFinFueraDeRangoFranja() {
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        Reserva nuevaReserva = new Reserva();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String horaInicioStr = "16:00"; // Dentro de la franja
        String horaFinStr = "18:00"; // Fuera de la franja (después de 17:00)

        String viewName = reservaController.crearReserva(nuevaReserva, 1, fechaStr, horaInicioStr, horaFinStr, null, redirectAttributes);

        assertEquals("redirect:/reservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("La hora seleccionada está fuera del horario de apertura del establecimiento para ese día o no es válida."));
        verify(reservaRepo, never()).save(any(Reserva.class));
    }

    @Test
    void crearReserva_whenSaveThrowsException_shouldReturnRedirectWithError() {
        ReservaController controller = new ReservaController(sessionData, establecimientoService, reservaRepo);
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
        when(reservaRepo.save(any(Reserva.class))).thenThrow(new RuntimeException("Error al guardar"));
        
        String result = controller.crearReserva(reserva, establecimientoId, fechaStr, horaStr, "11:00", null, redirectAttributes);
        
        assertEquals("redirect:/reservas/establecimiento/" + establecimientoId, result);
        assertTrue(((RedirectAttributesModelMap) redirectAttributes)
            .getFlashAttributes()
            .get("error").toString()
            .contains("Error al guardar la reserva"));
        verify(reservaRepo).save(any(Reserva.class));
    }
    
    // --- Pruebas para slots predefinidos ---
    
    @Test
    void testCrearReserva_ConSlotPredefinido_Exito() {
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
        
        String viewName = reservaController.crearReserva(reserva, 1, fechaStr, null, null, slotSeleccionado, redirectAttributes);
        
        assertEquals("redirect:/misreservas", viewName);
        verify(reservaRepo).save(any(Reserva.class));
        assertNotNull(redirectAttributes.getFlashAttributes().get("exito"));
    }
    
    @Test
    void testCrearReserva_SlotFormatoInvalido() {
    	Reserva reserva = new Reserva();
    	establecimiento1.setDuracionReserva(60);
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        String slotSeleccionado = "10:00-11:00"; // Formato incorrecto (sin espacios)
        
        String viewName = reservaController.crearReserva(reserva, 1, fechaStr, null, null, slotSeleccionado, redirectAttributes);
        
        assertEquals("redirect:/reservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Formato de slot inválido", redirectAttributes.getFlashAttributes().get("error"));
        verify(reservaRepo, never()).save(any(Reserva.class));
    }
    
    @Test
    void testCrearReserva_SinHorasNiSlot() {
    	Reserva reserva = new Reserva();
        usuarioAutenticado.getLstEstablecimientos().add(establecimiento1);
        when(sessionData.getUsuario()).thenReturn(usuarioAutenticado);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento1));
        
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String fechaStr = LocalDate.now().plusDays(1).with(DayOfWeek.MONDAY).toString();
        
        String viewName = reservaController.crearReserva(reserva, 1, fechaStr, null, null, null, redirectAttributes);
        
        assertEquals("redirect:/reservas/establecimiento/1", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Debe especificar hora de inicio y fin", redirectAttributes.getFlashAttributes().get("error"));
        verify(reservaRepo, never()).save(any(Reserva.class));
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
}
