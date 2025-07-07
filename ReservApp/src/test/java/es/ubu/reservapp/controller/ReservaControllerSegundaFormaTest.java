package es.ubu.reservapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.FranjaHoraria;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.EstablecimientoService;
import es.ubu.reservapp.service.ReservaService;
import es.ubu.reservapp.util.SlotReservaUtil;

@ExtendWith(MockitoExtension.class)
class ReservaControllerSegundaFormaTest {

    @Mock
    private SessionData sessionData;

    @Mock
    private EstablecimientoService establecimientoService;

    @Mock
    private ReservaService reservaService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private ReservaController reservaController;

    private Usuario usuario;
    private Establecimiento establecimiento;
    private FranjaHoraria franjaHoraria;
    private Reserva reserva;

    @BeforeEach
    void setUp() {
        // Configurar usuario
        usuario = new Usuario();
        usuario.setId("1");
        usuario.setNombre("Usuario Test");
        usuario.setLstEstablecimientos(new ArrayList<>());

        // Configurar establecimiento
        establecimiento = new Establecimiento();
        establecimiento.setId(1);
        establecimiento.setNombre("Establecimiento Test");
        establecimiento.setActivo(true);
        establecimiento.setFranjasHorarias(new ArrayList<>());
        
        // Agregar establecimiento al usuario
        usuario.getLstEstablecimientos().add(establecimiento);

        // Configurar franja horaria
        franjaHoraria = new FranjaHoraria();
        franjaHoraria.setId(1);
        franjaHoraria.setDiaSemana(DayOfWeek.MONDAY);
        franjaHoraria.setHoraInicio(LocalTime.of(9, 0));
        franjaHoraria.setHoraFin(LocalTime.of(17, 0));
        franjaHoraria.setEstablecimiento(establecimiento);
        
        establecimiento.getFranjasHorarias().add(franjaHoraria);

        // Configurar reserva
        reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuario);
        reserva.setEstablecimiento(establecimiento);
        reserva.setFechaReserva(LocalDateTime.of(2024, 1, 15, 10, 0));
        reserva.setHoraFin(LocalTime.of(11, 0));
    }

    // ================================
    // TESTS PARA mostrarCalendarioReserva
    // ================================

    @Test
    void testMostrarCalendarioReserva_UsuarioNoAutenticado() {
        // Arrange
        when(sessionData.getUsuario()).thenReturn(null);

        // Act
        String result = reservaController.mostrarCalendarioReserva(1, model, redirectAttributes);

        // Assert
        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", "Usuario no autenticado correctamente.");
    }

    @Test
    void testMostrarCalendarioReserva_EstablecimientoNoEncontrado() {
        // Arrange
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.empty());

        // Act
        String result = reservaController.mostrarCalendarioReserva(1, model, redirectAttributes);

        // Assert
        assertEquals("redirect:/misreservas", result);
        verify(redirectAttributes).addFlashAttribute("error", "Establecimiento no encontrado.");
    }

    @Test
    void testMostrarCalendarioReserva_SinPermiso() {
        // Arrange
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId("2");
        otroUsuario.setLstEstablecimientos(new ArrayList<>());
        
        when(sessionData.getUsuario()).thenReturn(otroUsuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));

        // Act
        String result = reservaController.mostrarCalendarioReserva(1, model, redirectAttributes);

        // Assert
        assertEquals("redirect:/misreservas", result);
        verify(redirectAttributes).addFlashAttribute("error", "No tiene permiso para reservar en este establecimiento.");
    }

    @Test
    void testMostrarCalendarioReserva_ExitoCompleto() {
        // Arrange
        List<Reserva> reservasPasadas = Arrays.asList(reserva);
        List<Reserva> reservasFuturas = Arrays.asList(reserva);
        List<FranjaHoraria> franjasHorarias = Arrays.asList(franjaHoraria);
        
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(reservaService.findByUsuarioAndEstablecimientoAndFechaReservaBefore(any(), any(), any())).thenReturn(reservasPasadas);
        when(reservaService.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(any(), any(), any())).thenReturn(reservasFuturas);

        try (MockedStatic<SlotReservaUtil> mockedSlotUtil = mockStatic(SlotReservaUtil.class)) {
            mockedSlotUtil.when(() -> SlotReservaUtil.requiereSlotsPredefinidos(any())).thenReturn(false);

            // Act
            String result = reservaController.mostrarCalendarioReserva(1, model, redirectAttributes);

            // Assert
            assertEquals("reservas/calendario_reserva", result);
            verify(model).addAttribute("establecimiento", establecimiento);
            verify(model).addAttribute("franjasHorarias", franjasHorarias);
            verify(model).addAttribute("reserva", new Reserva());
            verify(model).addAttribute("reservasPasadas", reservasPasadas);
            verify(model).addAttribute("reservasFuturas", reservasFuturas);
            verify(model).addAttribute("requiereSlotsPredefinidos", false);
            verify(model).addAttribute("slotsDisponibles", new EnumMap<>(DayOfWeek.class));
        }
    }

    @Test
    void testMostrarCalendarioReserva_ConSlotsPredefinidos() {
        // Arrange
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(reservaService.findByUsuarioAndEstablecimientoAndFechaReservaBefore(any(), any(), any())).thenReturn(new ArrayList<>());
        when(reservaService.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(any(), any(), any())).thenReturn(new ArrayList<>());

        try (MockedStatic<SlotReservaUtil> mockedSlotUtil = mockStatic(SlotReservaUtil.class)) {
            mockedSlotUtil.when(() -> SlotReservaUtil.requiereSlotsPredefinidos(any())).thenReturn(true);
            mockedSlotUtil.when(() -> SlotReservaUtil.generarSlotsDisponibles(any(), any())).thenReturn(new ArrayList<>());

            // Act
            String result = reservaController.mostrarCalendarioReserva(1, model, redirectAttributes);

            // Assert
            assertEquals("reservas/calendario_reserva", result);
            verify(model).addAttribute("requiereSlotsPredefinidos", true);
            mockedSlotUtil.verify(() -> SlotReservaUtil.generarSlotsDisponibles(establecimiento, franjaHoraria));
        }
    }

    // ================================
    // TESTS PARA crearReserva
    // ================================

    @Test
    void testCrearReserva_UsuarioNoAutenticado() {
        // Arrange
        when(sessionData.getUsuario()).thenReturn(null);

        // Act
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-01-15", "10:00", "11:00", null, null, null, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", "Usuario no autenticado correctamente.");
    }

    @Test
    void testCrearReserva_EstablecimientoNoEncontrado() {
        // Arrange
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.empty());

        // Act
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-01-15", "10:00", "11:00", null, null, null, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/misreservas", result);
        verify(redirectAttributes).addFlashAttribute("error", "Establecimiento no encontrado.");
    }

    @Test
    void testCrearReserva_SinPermiso() {
        // Arrange
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId("2");
        otroUsuario.setLstEstablecimientos(new ArrayList<>());
        
        when(sessionData.getUsuario()).thenReturn(otroUsuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));

        // Act
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-01-15", "10:00", "11:00", null, null, null, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/misreservas", result);
        verify(redirectAttributes).addFlashAttribute("error", "No tiene permiso para reservar en este establecimiento.");
    }

    @Test
    void testCrearReserva_FechaInvalida() {
        // Arrange
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));

        // Act
        String result = reservaController.crearReserva(new Reserva(), 1, "fecha-invalida", "10:00", "11:00", null, null, null, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/reservas/establecimiento/1", result);
        verify(redirectAttributes).addFlashAttribute("error", "Formato de fecha u hora inválido.");
    }

    @Test
    void testCrearReserva_HoraFinAnteriorAInicio() {
        // Arrange
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));

        // Act
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-01-15", "11:00", "10:00", null, null, null, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/reservas/establecimiento/1", result);
        verify(redirectAttributes).addFlashAttribute("error", "La hora de fin debe ser posterior a la hora de inicio.");
    }

    @Test
    void testCrearReserva_HorasNulas() {
        // Arrange
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));

        // Act
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-01-15", null, null, null, null, null, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/reservas/establecimiento/1", result);
        verify(redirectAttributes).addFlashAttribute("error", "Debe especificar hora de inicio y fin");
    }

    @Test
    void testCrearReserva_SlotInvalido() {
        // Arrange
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));

        // Act
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-01-15", null, null, "slot-invalido", null, null, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/reservas/establecimiento/1", result);
        verify(redirectAttributes).addFlashAttribute("error", "Formato de slot inválido");
    }

    @Test
    void testCrearReserva_FueraFranjaHoraria() {
        // Arrange
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));

        // Act - Hora fuera de la franja (8:00-9:00, cuando la franja es 9:00-17:00)
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-01-15", "08:00", "09:00", null, null, null, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/reservas/establecimiento/1", result);
        verify(redirectAttributes).addFlashAttribute("error", "La hora seleccionada está fuera del horario de apertura del establecimiento para ese día o no es válida.");
    }

    @Test
    void testCrearReserva_ExitoConHorasDirectas() {
        // Arrange
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(reservaService.save(any(Reserva.class))).thenReturn(reserva);

        // Act
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-01-15", "10:00", "11:00", null, null, null, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/misreservas", result);
        verify(reservaService).save(any(Reserva.class));
        verify(redirectAttributes).addFlashAttribute(eq("exito"), contains("Reserva creada correctamente"));
    }

    @Test
    void testCrearReserva_ExitoConSlot() {
        // Arrange
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(reservaService.save(any(Reserva.class))).thenReturn(reserva);

        // Act
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-01-15", null, null, "10:00 - 11:00", null, null, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/misreservas", result);
        verify(reservaService).save(any(Reserva.class));
        verify(redirectAttributes).addFlashAttribute(eq("exito"), contains("Reserva creada correctamente"));
    }

    @Test
    void testCrearReserva_ErrorAlGuardar() {
        // Arrange
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(reservaService.save(any(Reserva.class))).thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-01-15", "10:00", "11:00", null, null, null, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/reservas/establecimiento/1", result);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("Error al guardar la reserva"));
    }

    // ================================
    // TESTS PARA mostrarMisReservas
    // ================================

    @Test
    void testMostrarMisReservas_UsuarioNulo() {
        // Arrange
        when(sessionData.getUsuario()).thenReturn(null);

        // Act
        String result = reservaController.mostrarMisReservas(model, redirectAttributes);

        // Assert
        assertEquals("error", result);
        verify(redirectAttributes).addFlashAttribute("error", "Usuario no autenticado correctamente.");
        verify(model).addAttribute("error", "Usuario no encontrado");
    }

    @Test
    void testMostrarMisReservas_Exito() {
        // Arrange
        List<Establecimiento> establecimientos = Arrays.asList(establecimiento);
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findAll()).thenReturn(establecimientos);

        // Act
        String result = reservaController.mostrarMisReservas(model, redirectAttributes);

        // Assert
        assertEquals("reservas/misreservas", result);
        verify(model).addAttribute("establecimientos", establecimientos);
    }

    @Test
    void testMostrarMisReservas_ExcepcionEnServicio() {
        // Arrange
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findAll()).thenThrow(new RuntimeException("Error del servicio"));

        // Act
        String result = reservaController.mostrarMisReservas(model, redirectAttributes);

        // Assert
        assertEquals("error", result);
        verify(model).addAttribute("error", "Error interno del servidor");
    }

    // ================================
    // TESTS PARA CASOS EDGE
    // ================================

    @Test
    void testCrearReserva_EstablecimientoInactivo() {
        // Arrange
        establecimiento.setActivo(false);
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));

        // Act
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-01-15", "10:00", "11:00", null, null, null, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/misreservas", result);
        verify(redirectAttributes).addFlashAttribute("error", "El establecimiento no está activo.");
    }

    @Test
    void testCrearReserva_DiferenteDiaSemana() {
        // Arrange
        // Cambiar la franja horaria a miércoles
        franjaHoraria.setDiaSemana(DayOfWeek.WEDNESDAY);
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));

        // Act - Intentar reservar un lunes cuando la franja es miércoles
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-01-15", "10:00", "11:00", null, null, null, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/reservas/establecimiento/1", result);
        verify(redirectAttributes).addFlashAttribute("error", "La hora seleccionada está fuera del horario de apertura del establecimiento para ese día o no es válida.");
    }

    @Test
    void testCrearReserva_HorasEnLimiteFranja() {
        // Arrange
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(reservaService.save(any(Reserva.class))).thenReturn(reserva);

        // Act - Usar exactamente las horas de la franja (9:00-17:00)
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-01-15", "09:00", "17:00", null, null, null, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/misreservas", result);
        verify(reservaService).save(any(Reserva.class));
    }

    @Test
    void testCrearReserva_SlotVacio() {
        // Arrange
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));

        // Act
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-01-15", null, null, "   ", null, null, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/reservas/establecimiento/1", result);
        verify(redirectAttributes).addFlashAttribute("error", "Debe especificar hora de inicio y fin");
    }

    @Test
    void testMostrarCalendarioReserva_SinFranjasHorarias() {
        // Arrange
        establecimiento.getFranjasHorarias().clear();
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(reservaService.findByUsuarioAndEstablecimientoAndFechaReservaBefore(any(), any(), any())).thenReturn(new ArrayList<>());
        when(reservaService.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(any(), any(), any())).thenReturn(new ArrayList<>());

        try (MockedStatic<SlotReservaUtil> mockedSlotUtil = mockStatic(SlotReservaUtil.class)) {
            mockedSlotUtil.when(() -> SlotReservaUtil.requiereSlotsPredefinidos(any())).thenReturn(false);

            // Act
            String result = reservaController.mostrarCalendarioReserva(1, model, redirectAttributes);

            // Assert
            assertEquals("reservas/calendario_reserva", result);
            verify(model).addAttribute(eq("franjasHorarias"), argThat(list -> 
                ((List<?>) list).isEmpty()));
        }
    }

    // ================================
    // TESTS PARA VALIDAR CONFIGURACIÓN DE RESERVA
    // ================================

    @Test
    void testCrearReserva_ValidarConfiguracionReserva() {
        // Arrange
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        
        // Capturar la reserva que se guarda
        when(reservaService.save(any(Reserva.class))).thenAnswer(invocation -> {
            Reserva reservaGuardada = invocation.getArgument(0);
            
            // Validar que la reserva esté correctamente configurada
            assertEquals(usuario, reservaGuardada.getUsuario());
            assertEquals(establecimiento, reservaGuardada.getEstablecimiento());
            assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), reservaGuardada.getFechaReserva());
            assertEquals(LocalTime.of(11, 0), reservaGuardada.getHoraFin());
            
            return reservaGuardada;
        });

        // Act
        String result = reservaController.crearReserva(new Reserva(), 1, "2024-01-15", "10:00", "11:00", null, null, null, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/misreservas", result);
        verify(reservaService).save(any(Reserva.class));
    }

    @Test
    void testMostrarCalendarioReserva_OrdenReservas() {
        // Arrange
        Reserva reserva1 = new Reserva();
        reserva1.setFechaReserva(LocalDateTime.of(2024, 1, 10, 10, 0));
        
        Reserva reserva2 = new Reserva();
        reserva2.setFechaReserva(LocalDateTime.of(2024, 1, 12, 10, 0));
        
        List<Reserva> reservasPasadas = Arrays.asList(reserva2, reserva1); // Desordenadas
        List<Reserva> reservasFuturas = Arrays.asList(reserva1, reserva2); // Desordenadas
        
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        when(reservaService.findByUsuarioAndEstablecimientoAndFechaReservaBefore(any(), any(), any())).thenReturn(reservasPasadas);
        when(reservaService.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(any(), any(), any())).thenReturn(reservasFuturas);

        try (MockedStatic<SlotReservaUtil> mockedSlotUtil = mockStatic(SlotReservaUtil.class)) {
            mockedSlotUtil.when(() -> SlotReservaUtil.requiereSlotsPredefinidos(any())).thenReturn(false);

            // Act
            String result = reservaController.mostrarCalendarioReserva(1, model, redirectAttributes);

            // Assert
            assertEquals("reservas/calendario_reserva", result);
            
            // Verificar que las reservas pasadas se ordenan por fecha descendente
            verify(model).addAttribute(eq("reservasPasadas"), argThat(list -> {
                List<Reserva> reservas = (List<Reserva>) list;
                return reservas.size() == 2 && reservas.get(0).getFechaReserva().isAfter(reservas.get(1).getFechaReserva());
            }));
            
            // Verificar que las reservas futuras se ordenan por fecha ascendente
            verify(model).addAttribute(eq("reservasFuturas"), argThat(list -> {
                List<Reserva> reservas = (List<Reserva>) list;
                return reservas.size() == 2 && reservas.get(0).getFechaReserva().isBefore(reservas.get(1).getFechaReserva());
            }));
        }
    }
}