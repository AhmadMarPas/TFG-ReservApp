package es.ubu.reservapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.service.EstablecimientoService;
import es.ubu.reservapp.service.ReservaService;

/**
 * Test para el controlador AdminReservaController
 */
@ExtendWith(MockitoExtension.class)
class AdminReservaControllerTest {

    @Mock
    private EstablecimientoService establecimientoService;

    @Mock
    private ReservaService reservaService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AdminReservaController adminReservaController;

    private Establecimiento establecimiento1;
    private Establecimiento establecimiento2;
    private List<Establecimiento> establecimientos;
    private Reserva reserva1;
    private Reserva reserva2;
    private List<Reserva> reservas;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        // Configurar establecimientos
        establecimiento1 = new Establecimiento();
        establecimiento1.setId(1);
        establecimiento1.setNombre("Establecimiento Test 1");
        establecimiento1.setActivo(true);

        establecimiento2 = new Establecimiento();
        establecimiento2.setId(2);
        establecimiento2.setNombre("Establecimiento Test 2");
        establecimiento2.setActivo(true);

        establecimientos = new ArrayList<>();
        establecimientos.add(establecimiento1);
        establecimientos.add(establecimiento2);

        // Configurar usuario
        usuario = new Usuario();
        usuario.setId("usuario1");
        usuario.setNombre("Usuario");
        usuario.setApellidos("Test");
        usuario.setCorreo("usuario@test.com");

        // Configurar reservas
        reserva1 = new Reserva();
        reserva1.setId(1);
        reserva1.setUsuario(usuario);
        reserva1.setEstablecimiento(establecimiento1);
        reserva1.setFechaReserva(LocalDateTime.of(2024, 1, 15, 10, 0));
        reserva1.setHoraFin(LocalTime.of(11, 0));

        reserva2 = new Reserva();
        reserva2.setId(2);
        reserva2.setUsuario(usuario);
        reserva2.setEstablecimiento(establecimiento1);
        reserva2.setFechaReserva(LocalDateTime.of(2024, 1, 15, 14, 0));
        reserva2.setHoraFin(LocalTime.of(15, 0));

        reservas = new ArrayList<>();
        reservas.add(reserva1);
        reservas.add(reserva2);
    }

    @Test
    void testListarEstablecimientos() {
        // Given
        when(establecimientoService.findAll()).thenReturn(establecimientos);

        // When
        String viewName = adminReservaController.listarEstablecimientos(model);

        // Then
        verify(establecimientoService).findAll();
        verify(model).addAttribute("establecimientos", establecimientos);
        assertEquals("admin/reservas/lista_establecimientos", viewName);
    }

    @Test
    void testListarEstablecimientosVacio() {
        // Given
        List<Establecimiento> establecimientosVacios = new ArrayList<>();
        when(establecimientoService.findAll()).thenReturn(establecimientosVacios);

        // When
        String viewName = adminReservaController.listarEstablecimientos(model);

        // Then
        verify(establecimientoService).findAll();
        verify(model).addAttribute("establecimientos", establecimientosVacios);
        assertEquals("admin/reservas/lista_establecimientos", viewName);
    }

    @Test
    void testMostrarCalendarioReservas() {
        // Given
        Integer establecimientoId = 1;
        when(establecimientoService.findById(establecimientoId)).thenReturn(Optional.of(establecimiento1));

        // When
        String viewName = adminReservaController.mostrarCalendarioReservas(establecimientoId, null, null, model, redirectAttributes);

        // Then
        verify(establecimientoService).findById(establecimientoId);
        verify(model).addAttribute("establecimiento", establecimiento1);
        verify(model).addAttribute(eq("mesActual"), any(Integer.class));
        verify(model).addAttribute(eq("anioActual"), any(Integer.class));
        assertEquals("admin/reservas/calendario_mensual", viewName);
    }

    @Test
    void testMostrarCalendarioReservasConMesYAnio() {
        // Given
        Integer establecimientoId = 1;
        Integer mes = 6;
        Integer anio = 2024;
        when(establecimientoService.findById(establecimientoId)).thenReturn(Optional.of(establecimiento1));

        // When
        String viewName = adminReservaController.mostrarCalendarioReservas(establecimientoId, mes, anio, model, redirectAttributes);

        // Then
        verify(establecimientoService).findById(establecimientoId);
        verify(model).addAttribute("establecimiento", establecimiento1);
        verify(model).addAttribute("mesActual", mes);
        verify(model).addAttribute("anioActual", anio);
        assertEquals("admin/reservas/calendario_mensual", viewName);
    }

    @Test
    void testMostrarCalendarioReservasEstablecimientoNoEncontrado() {
        // Given
        Integer establecimientoId = 999;
        when(establecimientoService.findById(establecimientoId)).thenReturn(Optional.empty());

        // When
        String viewName = adminReservaController.mostrarCalendarioReservas(establecimientoId, null, null, model, redirectAttributes);

        // Then
        verify(establecimientoService).findById(establecimientoId);
        verify(model, never()).addAttribute(eq("establecimiento"), any());
        verify(redirectAttributes).addFlashAttribute("error", "Establecimiento no encontrado.");
        assertEquals("redirect:/admin/reservas", viewName);
    }

    @Test
    void testMostrarReservasDia() {
        // Given
        Integer establecimientoId = 1;
        Integer dia = 15;
        Integer mes = 1;
        Integer anio = 2024;
        LocalDate fechaSeleccionada = LocalDate.of(anio, mes, dia);
        LocalDateTime inicioDelDia = fechaSeleccionada.atStartOfDay();
        LocalDateTime finDelDia = fechaSeleccionada.plusDays(1).atStartOfDay();
        
        when(establecimientoService.findById(establecimientoId)).thenReturn(Optional.of(establecimiento1));
        when(reservaService.findByEstablecimientoAndFechaReservaBetween(establecimiento1, inicioDelDia, finDelDia))
                .thenReturn(reservas);

        // When
        String viewName = adminReservaController.mostrarReservasDia(establecimientoId, dia, mes, anio, model, redirectAttributes);

        // Then
        verify(establecimientoService).findById(establecimientoId);
        verify(reservaService).findByEstablecimientoAndFechaReservaBetween(establecimiento1, inicioDelDia, finDelDia);
        verify(model).addAttribute("establecimiento", establecimiento1);
        verify(model).addAttribute("fechaSeleccionada", fechaSeleccionada);
        verify(model).addAttribute("reservas", reservas);
        assertEquals("admin/reservas/detalle_dia", viewName);
    }

    @Test
    void testMostrarReservasDiaEstablecimientoNoEncontrado() {
        // Given
        Integer establecimientoId = 999;
        Integer dia = 15;
        Integer mes = 1;
        Integer anio = 2024;
        when(establecimientoService.findById(establecimientoId)).thenReturn(Optional.empty());

        // When
        String viewName = adminReservaController.mostrarReservasDia(establecimientoId, dia, mes, anio, model, redirectAttributes);

        // Then
        verify(establecimientoService).findById(establecimientoId);
        verify(reservaService, never()).findByEstablecimientoAndFechaReservaBetween(any(), any(), any());
        verify(redirectAttributes).addFlashAttribute("error", "Establecimiento no encontrado.");
        assertEquals("redirect:/admin/reservas", viewName);
    }

    @Test
    void testMostrarReservasDiaSinReservas() {
        // Given
        Integer establecimientoId = 1;
        Integer dia = 16;
        Integer mes = 1;
        Integer anio = 2024;
        LocalDate fechaSeleccionada = LocalDate.of(anio, mes, dia);
        LocalDateTime inicioDelDia = fechaSeleccionada.atStartOfDay();
        LocalDateTime finDelDia = fechaSeleccionada.plusDays(1).atStartOfDay();
        List<Reserva> reservasVacias = new ArrayList<>();
        
        when(establecimientoService.findById(establecimientoId)).thenReturn(Optional.of(establecimiento1));
        when(reservaService.findByEstablecimientoAndFechaReservaBetween(establecimiento1, inicioDelDia, finDelDia))
                .thenReturn(reservasVacias);

        // When
        String viewName = adminReservaController.mostrarReservasDia(establecimientoId, dia, mes, anio, model, redirectAttributes);

        // Then
        verify(establecimientoService).findById(establecimientoId);
        verify(reservaService).findByEstablecimientoAndFechaReservaBetween(establecimiento1, inicioDelDia, finDelDia);
        verify(model).addAttribute("establecimiento", establecimiento1);
        verify(model).addAttribute("fechaSeleccionada", fechaSeleccionada);
        verify(model).addAttribute("reservas", reservasVacias);
        assertEquals("admin/reservas/detalle_dia", viewName);
    }

    @Test
    void testMostrarReservasDiaConParametrosValidos() {
        // Given
        Integer establecimientoId = 1;
        Integer dia = 31;
        Integer mes = 12;
        Integer anio = 2024;
        LocalDate fechaSeleccionada = LocalDate.of(anio, mes, dia);
        LocalDateTime inicioDelDia = fechaSeleccionada.atStartOfDay();
        LocalDateTime finDelDia = fechaSeleccionada.plusDays(1).atStartOfDay();
        
        when(establecimientoService.findById(establecimientoId)).thenReturn(Optional.of(establecimiento1));
        when(reservaService.findByEstablecimientoAndFechaReservaBetween(establecimiento1, inicioDelDia, finDelDia))
                .thenReturn(new ArrayList<>());

        // When
        String viewName = adminReservaController.mostrarReservasDia(establecimientoId, dia, mes, anio, model, redirectAttributes);

        // Then
        verify(establecimientoService).findById(establecimientoId);
        verify(reservaService).findByEstablecimientoAndFechaReservaBetween(establecimiento1, inicioDelDia, finDelDia);
        verify(model).addAttribute("establecimiento", establecimiento1);
        verify(model).addAttribute("fechaSeleccionada", fechaSeleccionada);
        assertEquals("admin/reservas/detalle_dia", viewName);
    }

    @Test
    void testMostrarCalendarioReservasFiltradoPorDia() {
        // Given
        Integer establecimientoId = 1;
        Integer mes = 1;
        Integer anio = 2024;
        Establecimiento est = establecimiento1;
        // Reserva el día 10
        Reserva reservaDia10 = new Reserva();
        reservaDia10.setId(3);
        reservaDia10.setUsuario(usuario);
        reservaDia10.setEstablecimiento(est);
        reservaDia10.setFechaReserva(LocalDateTime.of(2024, 1, 10, 9, 0));
        reservaDia10.setHoraFin(LocalTime.of(10, 0));
        // Reserva el día 15
        Reserva reservaDia15 = new Reserva();
        reservaDia15.setId(4);
        reservaDia15.setUsuario(usuario);
        reservaDia15.setEstablecimiento(est);
        reservaDia15.setFechaReserva(LocalDateTime.of(2024, 1, 15, 12, 0));
        reservaDia15.setHoraFin(LocalTime.of(13, 0));
        // Reserva el día 31
        Reserva reservaDia31 = new Reserva();
        reservaDia31.setId(5);
        reservaDia31.setUsuario(usuario);
        reservaDia31.setEstablecimiento(est);
        reservaDia31.setFechaReserva(LocalDateTime.of(2024, 1, 31, 18, 0));
        reservaDia31.setHoraFin(LocalTime.of(19, 0));
        List<Reserva> reservasMes = List.of(reservaDia10, reservaDia15, reservaDia31);
        when(establecimientoService.findById(establecimientoId)).thenReturn(Optional.of(est));
        when(reservaService.findByEstablecimientoAndFechaReservaBetween(eq(est), any(), any())).thenReturn(reservasMes);

        // When
        String viewName = adminReservaController.mostrarCalendarioReservas(establecimientoId, mes, anio, model, redirectAttributes);

        // Then
        verify(establecimientoService).findById(establecimientoId);
        verify(reservaService).findByEstablecimientoAndFechaReservaBetween(eq(est), any(), any());
        verify(model).addAttribute(eq("establecimiento"), eq(est));
        verify(model).addAttribute(eq("mesActual"), eq(mes));
        verify(model).addAttribute(eq("anioActual"), eq(anio));
        // Verifica que el mapa reservasPorDia contiene las reservas agrupadas correctamente
        // (captura el argumento del modelo)
        ArgumentCaptor<Map<Integer, List<Reserva>>> captor = ArgumentCaptor.forClass(Map.class);
        verify(model).addAttribute(eq("reservasPorDia"), captor.capture());
        Map<Integer, List<Reserva>> reservasPorDia = captor.getValue();
        assertEquals(1, reservasPorDia.get(10).size());
        assertEquals(reservaDia10, reservasPorDia.get(10).get(0));
        assertEquals(1, reservasPorDia.get(15).size());
        assertEquals(reservaDia15, reservasPorDia.get(15).get(0));
        assertEquals(1, reservasPorDia.get(31).size());
        assertEquals(reservaDia31, reservasPorDia.get(31).get(0));
        assertEquals("admin/reservas/calendario_mensual", viewName);
    }
}