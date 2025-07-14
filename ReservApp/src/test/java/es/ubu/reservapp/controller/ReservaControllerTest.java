package es.ubu.reservapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
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
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private SessionData sessionData;
    
    @Mock
    private EstablecimientoService establecimientoService;
    
    @Mock
    private ReservaService reservaService;
    
    @Mock
    private ConvocatoriaService convocatoriaService;
    
    @Mock
    private UsuarioService usuarioService;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private Model model;
    
    @Mock
    private RedirectAttributes redirectAttributes;
    
    @InjectMocks
    private ReservaController reservaController;
    
    private Usuario usuario;
    private Establecimiento establecimiento;
    private Reserva reserva;
    private FranjaHoraria franjaHoraria;
    private Convocatoria convocatoria;
    
    @BeforeEach
    void setUp() {
        // Configurar usuario
        usuario = new Usuario();
        usuario.setId("user1");
        usuario.setNombre("Test");
        usuario.setApellidos("User");
        usuario.setCorreo("test@test.com");
        
        // Configurar establecimiento
        establecimiento = new Establecimiento();
        establecimiento.setId(1);
        establecimiento.setNombre("Test Establecimiento");
        establecimiento.setActivo(true);
        
        // Configurar franja horaria
        franjaHoraria = new FranjaHoraria();
        franjaHoraria.setDiaSemana(DayOfWeek.MONDAY);
        franjaHoraria.setHoraInicio(LocalTime.of(9, 0));
        franjaHoraria.setHoraFin(LocalTime.of(18, 0));
        franjaHoraria.setEstablecimiento(establecimiento);
        
        establecimiento.setFranjasHorarias(Arrays.asList(franjaHoraria));
        
        // Configurar reserva
        reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuario);
        reserva.setEstablecimiento(establecimiento);
        reserva.setFechaReserva(LocalDateTime.now().plusDays(1));
        reserva.setHoraFin(LocalTime.of(11, 0));
        
        // Configurar convocatoria
        convocatoria = new Convocatoria();
        convocatoria.setId(1);
        convocatoria.setReserva(reserva);
        convocatoria.setEnlace("https://meet.google.com/test");
        convocatoria.setObservaciones("Test meeting");
        convocatoria.setConvocados(new ArrayList<>());
    }
    
    // ================================
    // TESTS PARA mostrarCalendarioReserva
    // ================================
    
    @Test
    void testMostrarCalendarioReserva_UsuarioNoAutenticado() {
        // Given
        when(sessionData.getUsuario()).thenReturn(null);
        
        // When
        String result = reservaController.mostrarCalendarioReserva(1, model, redirectAttributes);
        
        // Then
        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", "Usuario no autenticado correctamente.");
    }
    
    @Test
    void testMostrarCalendarioReserva_EstablecimientoNoEncontrado() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.empty());
        
        // When
        String result = reservaController.mostrarCalendarioReserva(1, model, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas", result);
        verify(redirectAttributes).addFlashAttribute("error", "Establecimiento no encontrado.");
    }
    
    @Test
    void testMostrarCalendarioReserva_EstablecimientoInactivo() {
        // Given
        establecimiento.setActivo(false);
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        
        // When
        String result = reservaController.mostrarCalendarioReserva(1, model, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas", result);
        verify(redirectAttributes).addFlashAttribute("error", "El establecimiento no está activo.");
    }
    
    @Test
    void testMostrarCalendarioReserva_UsuarioSinPermisos() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(usuarioService.establecimientoAsignado(usuario, establecimiento)).thenReturn(false);
        
        // When
        String result = reservaController.mostrarCalendarioReserva(1, model, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas", result);
        verify(redirectAttributes).addFlashAttribute("error", "No tiene permiso para reservar en este establecimiento.");
    }
    
    @Test
    void testMostrarCalendarioReserva_Exitoso() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(usuarioService.establecimientoAsignado(usuario, establecimiento)).thenReturn(true);
        when(reservaService.findByUsuarioAndEstablecimientoAndFechaReservaBefore(eq(usuario), eq(establecimiento), any(LocalDateTime.class)))
            .thenReturn(new ArrayList<>());
        when(reservaService.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(eq(usuario), eq(establecimiento), any(LocalDateTime.class)))
            .thenReturn(new ArrayList<>());
        
        // When
        String result = reservaController.mostrarCalendarioReserva(1, model, redirectAttributes);
        
        // Then
        assertEquals("reservas/calendario_reserva", result);
        verify(model).addAttribute("establecimiento", establecimiento);
        verify(model).addAttribute(eq("franjasHorarias"), any());
        verify(model).addAttribute(eq("reserva"), any(Reserva.class));
    }
    
    // ================================
    // TESTS PARA crearReserva
    // ================================
    
    @Test
    void testCrearReserva_UsuarioNoAutenticado() {
        // Given
        when(sessionData.getUsuario()).thenReturn(null);
        
        // When
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-12-25", "10:00", "11:00", 
            null, null, null, null, redirectAttributes);
        
        // Then
        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", "Usuario no autenticado correctamente.");
    }
    
    @Test
    void testCrearReserva_EstablecimientoNoEncontrado() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.empty());
        
        // When
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-12-25", "10:00", "11:00", 
            null, null, null, null, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas", result);
        verify(redirectAttributes).addFlashAttribute("error", "Establecimiento no encontrado.");
    }
    
    @Test
    void testCrearReserva_UsuarioSinPermisos() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(usuarioService.establecimientoAsignado(usuario, establecimiento)).thenReturn(false);
        
        // When
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-12-25", "10:00", "11:00", 
            null, null, null, null, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas", result);
        verify(redirectAttributes).addFlashAttribute("error", "No tiene permiso para reservar en este establecimiento.");
    }
    
    @Test
    void testCrearReserva_FechaInvalida() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(usuarioService.establecimientoAsignado(usuario, establecimiento)).thenReturn(true);
        
        // When
        String result = reservaController.crearReserva(new Reserva(), 1, "fecha-invalida", "10:00", "11:00", 
            null, null, null, null, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas/establecimiento/1", result);
        verify(redirectAttributes).addFlashAttribute("error", "Formato de fecha u hora inválido.");
    }
    
    @Test
    void testCrearReserva_HoraFinAnteriorAInicio() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(usuarioService.establecimientoAsignado(usuario, establecimiento)).thenReturn(true);
        
        // When
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-12-25", "11:00", "10:00", 
            null, null, null, null, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas/establecimiento/1", result);
        verify(redirectAttributes).addFlashAttribute("error", "La hora de fin debe ser posterior a la hora de inicio.");
    }
    
    @Test
    void testCrearReserva_FueraDeHorario() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(usuarioService.establecimientoAsignado(usuario, establecimiento)).thenReturn(true);
        
        // When - Intentar reservar fuera del horario (8:00-9:00, cuando el horario es 9:00-18:00)
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-12-23", "08:00", "09:00", 
            null, null, null, null, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas/establecimiento/1", result);
        verify(redirectAttributes).addFlashAttribute("error", "La hora seleccionada está fuera del horario de apertura del establecimiento para ese día o no es válida.");
    }
    
    @Test
    void testCrearReserva_ConSlotSeleccionado() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(usuarioService.establecimientoAsignado(usuario, establecimiento)).thenReturn(true);
        when(reservaService.save(any(Reserva.class))).thenReturn(reserva);
        
        // When
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-12-23", null, null, 
            "10:00 - 11:00", null, null, null, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas", result);
        verify(reservaService).save(any(Reserva.class));
        verify(redirectAttributes).addFlashAttribute(eq("exito"), anyString());
    }
    
    @Test
    void testCrearReserva_ConConvocatoria() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(usuarioService.establecimientoAsignado(usuario, establecimiento)).thenReturn(true);
        when(reservaService.save(any(Reserva.class))).thenReturn(reserva);
        
        Usuario usuario2 = new Usuario();
        usuario2.setId("user2");
        when(usuarioService.findUsuarioById("user2")).thenReturn(usuario2);
        
        String[] usuariosConvocados = {"user2"};
        
        // When
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-12-23", "10:00", "11:00", 
            null, "https://meet.google.com/test", "Test meeting", usuariosConvocados, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas", result);
        verify(reservaService).save(any(Reserva.class));
        verify(convocatoriaService).save(any(Convocatoria.class));
        verify(emailService).enviarNotificacionesConvocatoria(any(), any());
    }
    
    @Test
    void testCrearReserva_ErrorAlGuardar() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(usuarioService.establecimientoAsignado(usuario, establecimiento)).thenReturn(true);
        when(reservaService.save(any(Reserva.class))).thenThrow(new RuntimeException("Error de base de datos"));
        
        // When
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-12-23", "10:00", "11:00", 
            null, null, null, null, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas/establecimiento/1", result);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("Error al guardar la reserva"));
    }
    
    @Test
    void testCrearReserva_SlotInvalido() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(usuarioService.establecimientoAsignado(usuario, establecimiento)).thenReturn(true);
        
        // When
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-12-23", null, null, 
            "slot-invalido", null, null, null, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas/establecimiento/1", result);
        verify(redirectAttributes).addFlashAttribute("error", "Formato de slot inválido");
    }
    
    @Test
    void testCrearReserva_SinHorasNiSlot() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(usuarioService.establecimientoAsignado(usuario, establecimiento)).thenReturn(true);
        
        // When
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-12-23", null, null, 
            null, null, null, null, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas/establecimiento/1", result);
        verify(redirectAttributes).addFlashAttribute("error", "Debe especificar hora de inicio y fin");
    }
    
    // ================================
    // TESTS PARA mostrarMisReservas
    // ================================
    
    @Test
    void testMostrarMisReservas_UsuarioNoAutenticado() {
        // Given
        when(sessionData.getUsuario()).thenReturn(null);
        
        // When
        String result = reservaController.mostrarMisReservas(model, redirectAttributes);
        
        // Then
        assertEquals("error", result);
        verify(model).addAttribute("error", "Usuario no encontrado");
        verify(redirectAttributes).addFlashAttribute("error", "Usuario no autenticado correctamente.");
    }
    
    @Test
    void testMostrarMisReservas_Exitoso() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findAll()).thenReturn(Arrays.asList(establecimiento));
        
        // When
        String result = reservaController.mostrarMisReservas(model, redirectAttributes);
        
        // Then
        assertEquals("reservas/misreservas", result);
        verify(model).addAttribute("establecimientos", Arrays.asList(establecimiento));
    }
    
    @Test
    void testMostrarMisReservas_ErrorInterno() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findAll()).thenThrow(new RuntimeException("Error de base de datos"));
        
        // When
        String result = reservaController.mostrarMisReservas(model, redirectAttributes);
        
        // Then
        assertEquals("error", result);
        verify(model).addAttribute("error", "Error interno del servidor");
    }
    
    // ================================
    // TESTS PARA buscarUsuarios
    // ================================
    
    @Test
    void testBuscarUsuarios_QueryCorto() {
        // Given
//        when(sessionData.getUsuario()).thenReturn(usuario);
        
        // When
        List<ReservaController.UsuarioDTO> result = reservaController.buscarUsuarios("a");
        
        // Then
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testBuscarUsuarios_QueryNulo() {
        // Given
//        when(sessionData.getUsuario()).thenReturn(usuario);
        
        // When
        List<ReservaController.UsuarioDTO> result = reservaController.buscarUsuarios(null);
        
        // Then
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testBuscarUsuarios_Exitoso() {
        // Given
        Usuario usuario2 = new Usuario();
        usuario2.setId("user2");
        usuario2.setNombre("Test2");
        usuario2.setApellidos("User2");
        usuario2.setCorreo("test2@test.com");
        
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(usuarioService.findAll()).thenReturn(Arrays.asList(usuario, usuario2));
        
        // When
        List<ReservaController.UsuarioDTO> result = reservaController.buscarUsuarios("test2");
        
        // Then
        assertEquals(1, result.size());
        assertEquals("user2", result.get(0).getId());
        assertEquals("Test2 User2", result.get(0).getNombre());
    }
    
    @Test
    void testBuscarUsuarios_ExcluyeUsuarioActual() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(usuarioService.findAll()).thenReturn(Arrays.asList(usuario));
        
        // When
        List<ReservaController.UsuarioDTO> result = reservaController.buscarUsuarios("test");
        
        // Then
        assertTrue(result.isEmpty());
    }
    
    // ================================
    // TESTS PARA mostrarFormularioEditar
    // ================================
    
    @Test
    void testMostrarFormularioEditar_UsuarioNoAutenticado() {
        // Given
        when(sessionData.getUsuario()).thenReturn(null);
        
        // When
        String result = reservaController.mostrarFormularioEditar(1, model, redirectAttributes);
        
        // Then
        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", "Usuario no autenticado correctamente.");
    }
    
    @Test
    void testMostrarFormularioEditar_ReservaNoEncontrada() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(reservaService.findById(1)).thenReturn(null);
        
        // When
        String result = reservaController.mostrarFormularioEditar(1, model, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas", result);
        verify(redirectAttributes).addFlashAttribute("error", "Reserva no encontrada.");
    }
    
    @Test
    void testMostrarFormularioEditar_SinPermisos() {
        // Given
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId("otro");
        reserva.setUsuario(otroUsuario);
        
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        // When
        String result = reservaController.mostrarFormularioEditar(1, model, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas", result);
        verify(redirectAttributes).addFlashAttribute("error", "No tienes permisos para editar esta reserva.");
    }
    
    @Test
    void testMostrarFormularioEditar_ReservaPasada() {
        // Given
        reserva.setFechaReserva(LocalDateTime.now().minusDays(1));
        
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        // When
        String result = reservaController.mostrarFormularioEditar(1, model, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas/establecimiento/1", result);
        verify(redirectAttributes).addFlashAttribute("error", "No se pueden editar reservas pasadas.");
    }
    
    @Test
    void testMostrarFormularioEditar_Exitoso() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        // When
        String result = reservaController.mostrarFormularioEditar(1, model, redirectAttributes);
        
        // Then
        assertEquals("reservas/editar_reserva", result);
        verify(model).addAttribute("reserva", reserva);
        verify(model).addAttribute("establecimiento", establecimiento);
        verify(model).addAttribute("isEdit", true);
    }
    
    @Test
    void testMostrarFormularioEditar_ConConvocatoria() {
        // Given
        reserva.setConvocatoria(convocatoria);
        
        Convocado convocado = new Convocado();
        convocado.setUsuario(usuario);
        convocatoria.setConvocados(Arrays.asList(convocado));
        
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        // When
        String result = reservaController.mostrarFormularioEditar(1, model, redirectAttributes);
        
        // Then
        assertEquals("reservas/editar_reserva", result);
        verify(model).addAttribute("convocatoria", convocatoria);
        verify(model).addAttribute(eq("convocados"), any(List.class));
    }
    
    @Test
    void testMostrarFormularioEditar_ErrorInterno() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(reservaService.findById(1)).thenThrow(new RuntimeException("Error de base de datos"));
        
        // When
        String result = reservaController.mostrarFormularioEditar(1, model, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas", result);
        verify(redirectAttributes).addFlashAttribute("error", "Error al cargar la reserva para edición.");
    }
    
    // ================================
    // TESTS PARA actualizarReserva
    // ================================
    
    @Test
    void testActualizarReserva_UsuarioNoAutenticado() {
        // Given
        when(sessionData.getUsuario()).thenReturn(null);
        
        // When
        String result = reservaController.actualizarReserva(1, "2024-12-25", "10:00", "11:00", 
            null, null, null, null, redirectAttributes);
        
        // Then
        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", "Usuario no autenticado correctamente.");
    }
    
    @Test
    void testActualizarReserva_ReservaNoEncontrada() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(reservaService.findById(1)).thenReturn(null);
        
        // When
        String result = reservaController.actualizarReserva(1, "2024-12-25", "10:00", "11:00", 
            null, null, null, null, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas", result);
        verify(redirectAttributes).addFlashAttribute("error", "Reserva no encontrada.");
    }
    
    @Test
    void testActualizarReserva_SinPermisos() {
        // Given
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId("otro");
        reserva.setUsuario(otroUsuario);
        
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        // When
        String result = reservaController.actualizarReserva(1, "2024-12-25", "10:00", "11:00", 
            null, null, null, null, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas", result);
        verify(redirectAttributes).addFlashAttribute("error", "No tienes permisos para editar esta reserva.");
    }
    
    @Test
    void testActualizarReserva_ReservaPasada() {
        // Given
        reserva.setFechaReserva(LocalDateTime.now().minusDays(1));
        
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        // When
        String result = reservaController.actualizarReserva(1, "2024-12-25", "10:00", "11:00", 
            null, null, null, null, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas/establecimiento/1", result);
        verify(redirectAttributes).addFlashAttribute("error", "No se pueden editar reservas pasadas.");
    }
    
    @Test
    void testActualizarReserva_Exitoso() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(reservaService.findById(1)).thenReturn(reserva);
        when(reservaService.save(any(Reserva.class))).thenReturn(reserva);
        
        // When
        String result = reservaController.actualizarReserva(1, "2024-12-23", "10:00", "11:00", 
            null, null, null, null, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas/establecimiento/1", result);
        verify(reservaService).save(any(Reserva.class));
        verify(redirectAttributes).addFlashAttribute("exito", "Reserva actualizada correctamente.");
    }
    
    @Test
    void testActualizarReserva_ConConvocatoria() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(reservaService.findById(1)).thenReturn(reserva);
        when(reservaService.save(any(Reserva.class))).thenReturn(reserva);
        
        Usuario usuario2 = new Usuario();
        usuario2.setId("user2");
        when(usuarioService.findUsuarioById("user2")).thenReturn(usuario2);
        
        String[] usuariosConvocados = {"user2"};
        
        // When
        String result = reservaController.actualizarReserva(1, "2024-12-23", "10:00", "11:00", 
            null, "https://meet.google.com/test", "Test meeting", usuariosConvocados, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas/establecimiento/1", result);
        verify(convocatoriaService, times(2)).deleteByReserva(reserva);
        verify(convocatoriaService, times(2)).save(any(Convocatoria.class));
    }
    
    @Test
    void testActualizarReserva_ErrorAlActualizar() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(reservaService.findById(1)).thenReturn(reserva);
        when(reservaService.save(any(Reserva.class))).thenThrow(new RuntimeException("Error de base de datos"));
        
        // When
        String result = reservaController.actualizarReserva(1, "2024-12-23", "10:00", "11:00", 
            null, null, null, null, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas/editar/1", result);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("Error al actualizar la reserva"));
    }
    
    // ================================
    // TESTS ADICIONALES PARA COBERTURA COMPLETA
    // ================================
    
    @Test
    void testCrearReserva_ErrorEnvioEmail() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(usuarioService.establecimientoAsignado(usuario, establecimiento)).thenReturn(true);
        when(reservaService.save(any(Reserva.class))).thenReturn(reserva);
        
        Usuario usuario2 = new Usuario();
        usuario2.setId("user2");
        when(usuarioService.findUsuarioById("user2")).thenReturn(usuario2);
        
        doThrow(new RuntimeException("Error de email")).when(emailService)
            .enviarNotificacionesConvocatoria(any(), any());
        
        String[] usuariosConvocados = {"user2"};
        
        // When
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-12-23", "10:00", "11:00", 
            null, "https://meet.google.com/test", "Test meeting", usuariosConvocados, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas", result);
        verify(reservaService).save(any(Reserva.class));
        verify(convocatoriaService).save(any(Convocatoria.class));
        // El error de email no debe interrumpir el flujo
    }
    
    @Test
    void testCrearReserva_UsuarioConvocadoNoEncontrado() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(usuarioService.establecimientoAsignado(usuario, establecimiento)).thenReturn(true);
        when(reservaService.save(any(Reserva.class))).thenReturn(reserva);
        when(usuarioService.findUsuarioById("user_inexistente")).thenReturn(null);
        
        String[] usuariosConvocados = {"user_inexistente"};
        
        // When
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-12-23", "10:00", "11:00", 
            null, null, null, usuariosConvocados, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas", result);
        verify(reservaService).save(any(Reserva.class));
        // No debe crear convocatoria si no encuentra el usuario
    }
    
    @Test
    void testBuscarUsuarios_BusquedaPorCorreo() {
        // Given
        Usuario usuario2 = new Usuario();
        usuario2.setId("user2");
        usuario2.setNombre("Otro");
        usuario2.setApellidos("Usuario");
        usuario2.setCorreo("especial@test.com");
        
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(usuarioService.findAll()).thenReturn(Arrays.asList(usuario, usuario2));
        
        // When
        List<ReservaController.UsuarioDTO> result = reservaController.buscarUsuarios("especial");
        
        // Then
        assertEquals(1, result.size());
        assertEquals("user2", result.get(0).getId());
    }
    
    @Test
    void testMostrarFormularioEditar_ConvocatoriaSinConvocados() {
        // Given
        convocatoria.setConvocados(new ArrayList<>());
        reserva.setConvocatoria(convocatoria);
        
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(reservaService.findById(1)).thenReturn(reserva);
        
        // When
        String result = reservaController.mostrarFormularioEditar(1, model, redirectAttributes);
        
        // Then
        assertEquals("reservas/editar_reserva", result);
        verify(model).addAttribute("convocatoria", convocatoria);
        verify(model).addAttribute("convocados", new ArrayList<>());
    }
    
    @Test
    void testActualizarReserva_ConSlotSeleccionado() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(reservaService.findById(1)).thenReturn(reserva);
        when(reservaService.save(any(Reserva.class))).thenReturn(reserva);
        
        // When
        String result = reservaController.actualizarReserva(1, "2024-12-23", null, null, 
            "10:00 - 11:00", null, null, null, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas/establecimiento/1", result);
        verify(reservaService).save(any(Reserva.class));
    }
    
    @Test
    void testCrearReserva_HorasIguales() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(usuarioService.establecimientoAsignado(usuario, establecimiento)).thenReturn(true);
        
        // When
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-12-23", "10:00", "10:00", 
            null, null, null, null, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas/establecimiento/1", result);
        verify(redirectAttributes).addFlashAttribute("error", "La hora de fin debe ser posterior a la hora de inicio.");
    }
    
    @Test
    void testCrearReserva_UsuariosConvocadosVacios() {
        // Given
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(usuarioService.establecimientoAsignado(usuario, establecimiento)).thenReturn(true);
        when(reservaService.save(any(Reserva.class))).thenReturn(reserva);
        
        String[] usuariosConvocados = {"", "   ", null};
        
        // When
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-12-23", "10:00", "11:00", 
            null, null, null, usuariosConvocados, redirectAttributes);
        
        // Then
        assertEquals("redirect:/misreservas", result);
        verify(reservaService).save(any(Reserva.class));
        // No debe crear convocatorias para usuarios vacíos
    }
}