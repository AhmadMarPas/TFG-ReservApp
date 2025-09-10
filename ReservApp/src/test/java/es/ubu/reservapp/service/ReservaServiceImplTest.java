package es.ubu.reservapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.ubu.reservapp.exception.UserNotFoundException;
import es.ubu.reservapp.model.entities.Convocado;
import es.ubu.reservapp.model.entities.Convocatoria;
import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.FranjaHoraria;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.repositories.ReservaRepo;
import es.ubu.reservapp.model.repositories.UsuarioRepo;
import es.ubu.reservapp.service.ReservaService.DisponibilidadDia;
import es.ubu.reservapp.service.ReservaService.FranjaDisponibilidad;
import es.ubu.reservapp.service.ReservaService.PeriodoDisponible;

/**
 * Test para la clase ReservaServiceImpl.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    @Mock
    private ReservaRepo reservaRepo;

    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private ConvocatoriaService convocatoriaService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReservaServiceImpl reservaService;

    private Reserva reserva;
    private Usuario usuario;
    private Convocatoria convocatoria;
    private Usuario usuarioConvocado1;
    private Usuario usuarioConvocado2;
    private Establecimiento establecimiento;
    private LocalDateTime fechaReserva;

    @BeforeEach
    void setUp() {
        // Setup usuario que reserva
        usuario = new Usuario();
        usuario.setId("user1");
        usuario.setNombre("Usuario Test");
        usuario.setCorreo("user@test.com");

        // Setup usuarios convocados
        usuarioConvocado1 = new Usuario();
        usuarioConvocado1.setId("convocado1");
        usuarioConvocado1.setNombre("Convocado 1");
        usuarioConvocado1.setCorreo("convocado1@test.com");

        usuarioConvocado2 = new Usuario();
        usuarioConvocado2.setId("convocado2");
        usuarioConvocado2.setNombre("Convocado 2");
        usuarioConvocado2.setCorreo("convocado2@test.com");

        // Setup establecimiento
        establecimiento = new Establecimiento();
        establecimiento.setId(1);
        establecimiento.setNombre("Establecimiento Test");
        establecimiento.setDireccion("Dirección Test");

        // Setup reserva
        fechaReserva = LocalDateTime.now().plusDays(1);
        reserva = new Reserva();
        reserva.setId(1);
        reserva.setFechaReserva(fechaReserva);
        reserva.setEstablecimiento(establecimiento);
        
        // Setup convocatoria (después de reserva)
        convocatoria = new Convocatoria();
        convocatoria.setId(reserva.getId());
        convocatoria.setConvocados(new ArrayList<>());
        reserva.setConvocatoria(convocatoria);
    }

    @Test
    void testCrearReservaConConvocatorias_ConUsuariosConvocados_Success() throws UserNotFoundException {
        // Arrange
        List<String> idUsuariosConvocados = Arrays.asList("convocado1", "convocado2");
        String enlaceReunion = "https://meet.google.com/abc-def-ghi";
        String observaciones = "Reunión importante";
        convocatoria.setEnlace(enlaceReunion);
        convocatoria.setObservaciones(observaciones);
        
        // Crear reserva sin ID para que se ejecute la lógica completa
        Reserva reservaSinId = new Reserva();
        reservaSinId.setFechaReserva(fechaReserva);
        reservaSinId.setEstablecimiento(establecimiento);

        when(reservaRepo.save(any(Reserva.class))).thenAnswer(invocation -> {
            Reserva reservaGuardada = invocation.getArgument(0);
            reservaGuardada.setId(1); // Simular ID generado
            return reservaGuardada;
        });
        when(usuarioRepo.findById("convocado1")).thenReturn(Optional.of(usuarioConvocado1));
        when(usuarioRepo.findById("convocado2")).thenReturn(Optional.of(usuarioConvocado2));

        // Act
        Reserva resultado = reservaService.crearReservaConConvocatorias(reservaSinId, usuario, idUsuariosConvocados);

        // Assert
        assertNotNull(resultado);
        assertEquals(usuario, resultado.getUsuario());
        assertNotNull(resultado.getConvocatoria());
        assertEquals(2, resultado.getConvocatoria().getConvocados().size());
        verify(reservaRepo).save(reservaSinId);
        verify(usuarioRepo).findById("convocado1");
        verify(usuarioRepo).findById("convocado2");
    }

    @Test
    void testCrearReservaConConvocatorias_SinUsuariosConvocados_Success() throws UserNotFoundException {
        // Arrange
        List<String> idUsuariosConvocados = new ArrayList<>();
        String enlaceReunion = "https://meet.google.com/abc-def-ghi";
        String observaciones = "Sin convocados";
        convocatoria.setEnlace(enlaceReunion);
        convocatoria.setObservaciones(observaciones);
        
        // Crear reserva sin ID para que se ejecute la lógica completa
        Reserva reservaSinId = new Reserva();
        reservaSinId.setFechaReserva(fechaReserva);
        reservaSinId.setEstablecimiento(establecimiento);

        when(reservaRepo.save(any(Reserva.class))).thenAnswer(invocation -> {
            Reserva reservaGuardada = invocation.getArgument(0);
            reservaGuardada.setId(1); // Simular ID generado
            return reservaGuardada;
        });

        // Act
        Reserva resultado = reservaService.crearReservaConConvocatorias(reservaSinId, usuario, idUsuariosConvocados);

        // Assert
        assertNotNull(resultado);
        assertEquals(usuario, resultado.getUsuario());
        // Sin usuarios convocados, no se crea convocatoria
        verify(reservaRepo).save(reservaSinId);
        verify(usuarioRepo, never()).findById(anyString());
    }

    @Test
    void testCrearReservaConConvocatorias_ListaUsuariosNull_Success() throws UserNotFoundException {
        // Arrange
        List<String> idUsuariosConvocados = null;
        String enlaceReunion = "https://meet.google.com/abc-def-ghi";
        String observaciones = "Lista null";
        convocatoria.setEnlace(enlaceReunion);
        convocatoria.setObservaciones(observaciones);
        
        // Crear reserva sin ID para que se ejecute la lógica completa
        Reserva reservaSinId = new Reserva();
        reservaSinId.setFechaReserva(fechaReserva);
        reservaSinId.setEstablecimiento(establecimiento);

        when(reservaRepo.save(any(Reserva.class))).thenAnswer(invocation -> {
            Reserva reservaGuardada = invocation.getArgument(0);
            reservaGuardada.setId(1); // Simular ID generado
            return reservaGuardada;
        });

        // Act
        Reserva resultado = reservaService.crearReservaConConvocatorias(reservaSinId, usuario, idUsuariosConvocados);

        // Assert
        assertNotNull(resultado);
        assertEquals(usuario, resultado.getUsuario());
        // Con lista null, no se crea convocatoria
        verify(reservaRepo).save(reservaSinId);
        verify(usuarioRepo, never()).findById(anyString());
    }

    @Test
    void testCrearReservaConConvocatorias_UsuarioNoEncontrado_ThrowsException() {
        // Arrange
        List<String> idUsuariosConvocados = Arrays.asList("usuarioInexistente");
        String enlaceReunion = "https://meet.google.com/abc-def-ghi";
        String observaciones = "Usuario no existe";
        convocatoria.setEnlace(enlaceReunion);
        convocatoria.setObservaciones(observaciones);

        // Crear reserva sin ID para que se ejecute la lógica completa
        Reserva reservaSinId = new Reserva();
        reservaSinId.setFechaReserva(fechaReserva);
        reservaSinId.setEstablecimiento(establecimiento);

        when(reservaRepo.save(any(Reserva.class))).thenAnswer(invocation -> {
            Reserva reservaGuardada = invocation.getArgument(0);
            reservaGuardada.setId(1); // Simular ID generado
            return reservaGuardada;
        });
        when(usuarioRepo.findById("usuarioInexistente")).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            reservaService.crearReservaConConvocatorias(reservaSinId, usuario, idUsuariosConvocados);
        });

        assertEquals("El Usuario con ID usuarioInexistente no fue encontrado.", exception.getMessage());
        verify(reservaRepo).save(reservaSinId);
        verify(usuarioRepo).findById("usuarioInexistente");
    }

    @Test
    void testCrearReservaConConvocatorias_AlgunosUsuariosNoEncontrados_ThrowsException() {
        // Arrange
        List<String> idUsuariosConvocados = Arrays.asList("convocado1", "usuarioInexistente");
        String enlaceReunion = "https://meet.google.com/abc-def-ghi";
        String observaciones = "Algunos usuarios no existen";
        convocatoria.setEnlace(enlaceReunion);
        convocatoria.setObservaciones(observaciones);

        // Crear reserva sin ID para que se ejecute la lógica completa
        Reserva reservaSinId = new Reserva();
        reservaSinId.setFechaReserva(fechaReserva);
        reservaSinId.setEstablecimiento(establecimiento);

        when(reservaRepo.save(any(Reserva.class))).thenAnswer(invocation -> {
            Reserva reservaGuardada = invocation.getArgument(0);
            reservaGuardada.setId(1); // Simular ID generado
            return reservaGuardada;
        });
        when(usuarioRepo.findById("convocado1")).thenReturn(Optional.of(usuarioConvocado1));
        when(usuarioRepo.findById("usuarioInexistente")).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            reservaService.crearReservaConConvocatorias(reservaSinId, usuario, idUsuariosConvocados);
        });

        assertEquals("El Usuario con ID usuarioInexistente no fue encontrado.", exception.getMessage());
        verify(reservaRepo).save(reservaSinId);
        verify(usuarioRepo).findById("convocado1");
        verify(usuarioRepo).findById("usuarioInexistente");
    }

    @Test
    void testCrearReservaConConvocatorias_ConEnlaceYObservacionesNull() throws UserNotFoundException {
        // Arrange
        List<String> idUsuariosConvocados = Arrays.asList("convocado1");
        
        // Crear reserva sin ID para que se ejecute la lógica completa
        Reserva reservaSinId = new Reserva();
        reservaSinId.setFechaReserva(fechaReserva);
        reservaSinId.setEstablecimiento(establecimiento);

        when(reservaRepo.save(any(Reserva.class))).thenAnswer(invocation -> {
            Reserva reservaGuardada = invocation.getArgument(0);
            reservaGuardada.setId(1); // Simular ID generado
            return reservaGuardada;
        });
        when(usuarioRepo.findById("convocado1")).thenReturn(Optional.of(usuarioConvocado1));

        // Act
        Reserva resultado = reservaService.crearReservaConConvocatorias(reservaSinId, usuario, idUsuariosConvocados);

        // Assert
        assertNotNull(resultado);
        assertEquals(usuario, resultado.getUsuario());
        assertNotNull(resultado.getConvocatoria());
        assertEquals(1, resultado.getConvocatoria().getConvocados().size());
        verify(reservaRepo).save(reservaSinId);
        verify(usuarioRepo).findById("convocado1");
    }

    @Test
    void testFindAll() {
        // Arrange
        List<Reserva> reservasEsperadas = Arrays.asList(reserva, new Reserva());
        when(reservaRepo.findAll()).thenReturn(reservasEsperadas);

        // Act
        List<Reserva> resultado = reservaService.findAll();

        // Assert
        assertEquals(reservasEsperadas, resultado);
        verify(reservaRepo).findAll();
    }

    @Test
    void testFindAll_ListaVacia() {
        // Arrange
        List<Reserva> reservasVacias = new ArrayList<>();
        when(reservaRepo.findAll()).thenReturn(reservasVacias);

        // Act
        List<Reserva> resultado = reservaService.findAll();

        // Assert
        assertTrue(resultado.isEmpty());
        verify(reservaRepo).findAll();
    }

    @Test
    void testSave() {
        // Arrange
        when(reservaRepo.save(reserva)).thenReturn(reserva);

        // Act
        Reserva resultado = reservaService.save(reserva);

        // Assert
        assertEquals(reserva, resultado);
        verify(reservaRepo).save(reserva);
    }

    @Test
    void testFindByUsuario() {
        // Arrange
        List<Reserva> reservasEsperadas = Arrays.asList(reserva);
        when(reservaRepo.findByUsuario(usuario)).thenReturn(reservasEsperadas);

        // Act
        List<Reserva> resultado = reservaService.findByUsuario(usuario);

        // Assert
        assertEquals(reservasEsperadas, resultado);
        verify(reservaRepo).findByUsuario(usuario);
    }

    @Test
    void testFindByUsuario_SinReservas() {
        // Arrange
        List<Reserva> reservasVacias = new ArrayList<>();
        when(reservaRepo.findByUsuario(usuario)).thenReturn(reservasVacias);

        // Act
        List<Reserva> resultado = reservaService.findByUsuario(usuario);

        // Assert
        assertTrue(resultado.isEmpty());
        verify(reservaRepo).findByUsuario(usuario);
    }

    @Test
    void testFindByEstablecimientoAndFechaReservaBetween() {
        // Arrange
        LocalDateTime fechaInicio = LocalDateTime.now();
        LocalDateTime fechaFin = LocalDateTime.now().plusDays(7);
        List<Reserva> reservasEsperadas = Arrays.asList(reserva);
        when(reservaRepo.findByEstablecimientoAndFechaReservaBetween(establecimiento, fechaInicio, fechaFin))
            .thenReturn(reservasEsperadas);

        // Act
        List<Reserva> resultado = reservaService.findByEstablecimientoAndFechaReservaBetween(
            establecimiento, fechaInicio, fechaFin);

        // Assert
        assertEquals(reservasEsperadas, resultado);
        verify(reservaRepo).findByEstablecimientoAndFechaReservaBetween(establecimiento, fechaInicio, fechaFin);
    }

    @Test
    void testFindByEstablecimientoAndFechaReservaBetween_SinReservas() {
        // Arrange
        LocalDateTime fechaInicio = LocalDateTime.now();
        LocalDateTime fechaFin = LocalDateTime.now().plusDays(7);
        List<Reserva> reservasVacias = new ArrayList<>();
        when(reservaRepo.findByEstablecimientoAndFechaReservaBetween(establecimiento, fechaInicio, fechaFin))
            .thenReturn(reservasVacias);

        // Act
        List<Reserva> resultado = reservaService.findByEstablecimientoAndFechaReservaBetween(
            establecimiento, fechaInicio, fechaFin);

        // Assert
        assertTrue(resultado.isEmpty());
        verify(reservaRepo).findByEstablecimientoAndFechaReservaBetween(establecimiento, fechaInicio, fechaFin);
    }

    @Test
    void testFindByUsuarioAndEstablecimientoAndFechaReservaBefore() {
        // Arrange
        LocalDateTime fechaActual = LocalDateTime.now();
        List<Reserva> reservasEsperadas = Arrays.asList(reserva);
        when(reservaRepo.findByUsuarioAndEstablecimientoAndFechaReservaBefore(usuario, establecimiento, fechaActual))
            .thenReturn(reservasEsperadas);

        // Act
        List<Reserva> resultado = reservaService.findByUsuarioAndEstablecimientoAndFechaReservaBefore(
            usuario, establecimiento, fechaActual);

        // Assert
        assertEquals(reservasEsperadas, resultado);
        verify(reservaRepo).findByUsuarioAndEstablecimientoAndFechaReservaBefore(usuario, establecimiento, fechaActual);
    }

    @Test
    void testFindByUsuarioAndEstablecimientoAndFechaReservaBefore_SinReservas() {
        // Arrange
        LocalDateTime fechaActual = LocalDateTime.now();
        List<Reserva> reservasVacias = new ArrayList<>();
        when(reservaRepo.findByUsuarioAndEstablecimientoAndFechaReservaBefore(usuario, establecimiento, fechaActual))
            .thenReturn(reservasVacias);

        // Act
        List<Reserva> resultado = reservaService.findByUsuarioAndEstablecimientoAndFechaReservaBefore(
            usuario, establecimiento, fechaActual);

        // Assert
        assertTrue(resultado.isEmpty());
        verify(reservaRepo).findByUsuarioAndEstablecimientoAndFechaReservaBefore(usuario, establecimiento, fechaActual);
    }

    @Test
    void testFindByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual() {
        // Arrange
        LocalDateTime fechaActual = LocalDateTime.now();
        List<Reserva> reservasEsperadas = Arrays.asList(reserva);
        when(reservaRepo.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(usuario, establecimiento, fechaActual))
            .thenReturn(reservasEsperadas);

        // Act
        List<Reserva> resultado = reservaService.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(
            usuario, establecimiento, fechaActual);

        // Assert
        assertEquals(reservasEsperadas, resultado);
        verify(reservaRepo).findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(usuario, establecimiento, fechaActual);
    }

    @Test
    void testFindByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual_SinReservas() {
        // Arrange
        LocalDateTime fechaActual = LocalDateTime.now();
        List<Reserva> reservasVacias = new ArrayList<>();
        when(reservaRepo.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(usuario, establecimiento, fechaActual))
            .thenReturn(reservasVacias);

        // Act
        List<Reserva> resultado = reservaService.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(
            usuario, establecimiento, fechaActual);

        // Assert
        assertTrue(resultado.isEmpty());
        verify(reservaRepo).findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(usuario, establecimiento, fechaActual);
    }

    @Test
    void testFindById() {
        // Arrange
        Integer id = 1;
        Reserva reservaTest = new Reserva();
        reservaTest.setId(id);
        reservaTest.setEstablecimiento(establecimiento);
        when(reservaRepo.findById(id)).thenReturn(Optional.of(reservaTest));
        when(convocatoriaService.findByIdIgnoringValido(id)).thenReturn(null);

        // Act
        Reserva resultado = reservaService.findById(id);

        // Assert
        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertEquals(reservaTest, resultado);

        // Verify
        verify(reservaRepo).findById(id);
        verify(convocatoriaService).findByIdIgnoringValido(id);
    }

    @Test
    void testFindById_ConFranjasHorarias() {
        // Arrange
        Integer id = 1;
        Reserva reservaTest = new Reserva();
        reservaTest.setId(id);
        reservaTest.setEstablecimiento(establecimiento);
        
        List<FranjaHoraria> franjas = new ArrayList<>();
        FranjaHoraria franja1 = new FranjaHoraria();
        franja1.setDiaSemana(DayOfWeek.TUESDAY);
        FranjaHoraria franja2 = new FranjaHoraria();
        franja2.setDiaSemana(DayOfWeek.MONDAY);
        franjas.add(franja1);
        franjas.add(franja2);
        establecimiento.setFranjasHorarias(franjas);
        
        when(reservaRepo.findById(id)).thenReturn(Optional.of(reservaTest));
        when(convocatoriaService.findByIdIgnoringValido(id)).thenReturn(null);

        // Act
        Reserva resultado = reservaService.findById(id);

        // Assert
        assertNotNull(resultado);
        assertEquals(reservaTest, resultado);
        // Verificar que las franjas están ordenadas por día de semana
        assertEquals(DayOfWeek.MONDAY, resultado.getEstablecimiento().getFranjasHorarias().get(0).getDiaSemana());
        assertEquals(DayOfWeek.TUESDAY, resultado.getEstablecimiento().getFranjasHorarias().get(1).getDiaSemana());

        // Verify
        verify(reservaRepo).findById(id);
        verify(convocatoriaService).findByIdIgnoringValido(id);
    }

    @Test
    void testFindById_ConConvocatoria() {
        // Arrange
        Integer id = 1;
        Reserva reservaTest = new Reserva();
        reservaTest.setId(id);
        reservaTest.setEstablecimiento(establecimiento);
        
        Convocatoria convocatoriaTest = new Convocatoria();
        List<Convocado> convocados = new ArrayList<>();
        Convocado convocado = new Convocado();
        convocado.setUsuario(usuarioConvocado1);
        convocados.add(convocado);
        convocatoriaTest.setConvocados(convocados);
        
        when(reservaRepo.findById(id)).thenReturn(Optional.of(reservaTest));
        when(convocatoriaService.findByIdIgnoringValido(id)).thenReturn(convocatoriaTest);

        // Act
        Reserva resultado = reservaService.findById(id);

        // Assert
        assertNotNull(resultado);
        assertNotNull(resultado.getConvocatoria());
        assertEquals(1, resultado.getConvocatoria().getConvocados().size());

        // Verify
        verify(reservaRepo).findById(id);
        verify(convocatoriaService).findByIdIgnoringValido(id);
    }

    @Test
    void testFindById_ConConvocatoriaYaExistente() {
        // Arrange
        Integer id = 1;
        Reserva reservaTest = new Reserva();
        reservaTest.setId(id);
        reservaTest.setEstablecimiento(establecimiento);
        
        Convocatoria convocatoriaExistente = new Convocatoria();
        List<Convocado> convocados = new ArrayList<>();
        Convocado convocado = new Convocado();
        convocado.setUsuario(usuarioConvocado1);
        convocados.add(convocado);
        convocatoriaExistente.setConvocados(convocados);
        reservaTest.setConvocatoria(convocatoriaExistente);
        
        when(reservaRepo.findById(id)).thenReturn(Optional.of(reservaTest));

        // Act
        Reserva resultado = reservaService.findById(id);

        // Assert
        assertNotNull(resultado);
        assertNotNull(resultado.getConvocatoria());
        assertEquals(1, resultado.getConvocatoria().getConvocados().size());

        // Verify
        verify(reservaRepo).findById(id);
        // No debe llamar al servicio de convocatoria porque ya existe
        verify(convocatoriaService, never()).findByIdIgnoringValido(anyInt());
    }

    @Test
    void testFindById_ReservaNoEncontrada() {
        // Arrange
        Integer id = 999;
        when(reservaRepo.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.findById(id);
        });

        assertEquals("Reserva con ID 999 no encontrada.", exception.getMessage());
        verify(reservaRepo).findById(id);
    }

    @Test
    void testFindByEstablecimientoAndFechaReservaBetween_ConConvocatorias() {
        // Arrange
        LocalDateTime fechaInicio = LocalDateTime.now();
        LocalDateTime fechaFin = LocalDateTime.now().plusDays(7);
        
        // Crear reserva sin convocatoria para que se llame al servicio
        Reserva reservaSinConvocatoria = new Reserva();
        reservaSinConvocatoria.setId(1);
        reservaSinConvocatoria.setFechaReserva(fechaReserva);
        reservaSinConvocatoria.setEstablecimiento(establecimiento);
        reservaSinConvocatoria.setConvocatoria(null);
        
        List<Reserva> reservasEsperadas = Arrays.asList(reservaSinConvocatoria);
        
        Convocatoria convocatoriaTest = new Convocatoria();
        List<Convocado> convocados = new ArrayList<>();
        Convocado convocado = new Convocado();
        convocado.setUsuario(usuarioConvocado1);
        convocados.add(convocado);
        convocatoriaTest.setConvocados(convocados);
        
        when(reservaRepo.findByEstablecimientoAndFechaReservaBetween(establecimiento, fechaInicio, fechaFin))
            .thenReturn(reservasEsperadas);
        when(convocatoriaService.findByIdIgnoringValido(reservaSinConvocatoria.getId())).thenReturn(convocatoriaTest);

        // Act
        List<Reserva> resultado = reservaService.findByEstablecimientoAndFechaReservaBetween(
            establecimiento, fechaInicio, fechaFin);

        // Assert
        assertEquals(reservasEsperadas, resultado);
        verify(reservaRepo).findByEstablecimientoAndFechaReservaBetween(establecimiento, fechaInicio, fechaFin);
        verify(convocatoriaService).findByIdIgnoringValido(reservaSinConvocatoria.getId());
    }

    @Test
    void testFindByUsuarioAndEstablecimientoAndFechaReservaBefore_ConConvocatorias() {
        // Arrange
        LocalDateTime fechaActual = LocalDateTime.now();
        
        // Crear reserva sin convocatoria para que se llame al servicio
        Reserva reservaSinConvocatoria = new Reserva();
        reservaSinConvocatoria.setId(2);
        reservaSinConvocatoria.setFechaReserva(fechaReserva);
        reservaSinConvocatoria.setEstablecimiento(establecimiento);
        reservaSinConvocatoria.setConvocatoria(null);
        
        List<Reserva> reservasEsperadas = Arrays.asList(reservaSinConvocatoria);
        
        Convocatoria convocatoriaTest = new Convocatoria();
        List<Convocado> convocados = new ArrayList<>();
        Convocado convocado = new Convocado();
        convocado.setUsuario(usuarioConvocado1);
        convocados.add(convocado);
        convocatoriaTest.setConvocados(convocados);
        
        when(reservaRepo.findByUsuarioAndEstablecimientoAndFechaReservaBefore(usuario, establecimiento, fechaActual))
            .thenReturn(reservasEsperadas);
        when(convocatoriaService.findByIdIgnoringValido(reservaSinConvocatoria.getId())).thenReturn(convocatoriaTest);

        // Act
        List<Reserva> resultado = reservaService.findByUsuarioAndEstablecimientoAndFechaReservaBefore(
            usuario, establecimiento, fechaActual);

        // Assert
        assertEquals(reservasEsperadas, resultado);
        verify(reservaRepo).findByUsuarioAndEstablecimientoAndFechaReservaBefore(usuario, establecimiento, fechaActual);
        verify(convocatoriaService).findByIdIgnoringValido(reservaSinConvocatoria.getId());
    }

    @Test
    void testFindByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual_ConConvocatorias() {
        // Arrange
        LocalDateTime fechaActual = LocalDateTime.now();
        
        // Crear reserva sin convocatoria para que se llame al servicio
        Reserva reservaSinConvocatoria = new Reserva();
        reservaSinConvocatoria.setId(3);
        reservaSinConvocatoria.setFechaReserva(fechaReserva);
        reservaSinConvocatoria.setEstablecimiento(establecimiento);
        reservaSinConvocatoria.setConvocatoria(null);
        
        List<Reserva> reservasEsperadas = Arrays.asList(reservaSinConvocatoria);
        
        Convocatoria convocatoriaTest = new Convocatoria();
        List<Convocado> convocados = new ArrayList<>();
        Convocado convocado = new Convocado();
        convocado.setUsuario(usuarioConvocado1);
        convocados.add(convocado);
        convocatoriaTest.setConvocados(convocados);
        
        when(reservaRepo.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(usuario, establecimiento, fechaActual))
            .thenReturn(reservasEsperadas);
        when(convocatoriaService.findByIdIgnoringValido(reservaSinConvocatoria.getId())).thenReturn(convocatoriaTest);

        // Act
        List<Reserva> resultado = reservaService.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(
            usuario, establecimiento, fechaActual);

        // Assert
        assertEquals(reservasEsperadas, resultado);
        verify(reservaRepo).findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(usuario, establecimiento, fechaActual);
        verify(convocatoriaService).findByIdIgnoringValido(reservaSinConvocatoria.getId());
    }

    @Test
    void testCrearReservaConConvocatorias_ReservaConIdExistente() throws UserNotFoundException {
        // Arrange
        reserva.setId(1); // Reserva ya tiene ID
        List<String> idUsuariosConvocados = Arrays.asList("convocado1");
        String enlaceReunion = "https://meet.google.com/abc-def-ghi";
        String observaciones = "Reserva editada";
        convocatoria.setEnlace(enlaceReunion);
        convocatoria.setObservaciones(observaciones);
        
        when(reservaRepo.save(any(Reserva.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(usuarioRepo.findById("convocado1")).thenReturn(Optional.of(usuarioConvocado1));

        // Act
        Reserva resultado = reservaService.crearReservaConConvocatorias(reserva, usuario, idUsuariosConvocados);

        // Assert
        assertNotNull(resultado);
        assertEquals(usuario, resultado.getUsuario());
        assertEquals(Integer.valueOf(1), resultado.getId());
        assertNotNull(resultado.getConvocatoria());
        assertEquals(1, resultado.getConvocatoria().getConvocados().size());
        
        verify(reservaRepo).save(reserva);
        verify(usuarioRepo).findById("convocado1");
    }

    @Test
    void testObtenerConvocatoria_ConConvocatoriaNull() {
        // Arrange
        Reserva reservaTest = new Reserva();
        reservaTest.setId(1);
        reservaTest.setConvocatoria(null);
        
        when(convocatoriaService.findByIdIgnoringValido(1)).thenReturn(null);

        // Act
        List<Reserva> reservas = Arrays.asList(reservaTest);
        
        when(reservaRepo.findByEstablecimientoAndFechaReservaBetween(any(), any(), any()))
            .thenReturn(reservas);
        
        List<Reserva> resultado = reservaService.findByEstablecimientoAndFechaReservaBetween(
            establecimiento, LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertNull(resultado.get(0).getConvocatoria());
    }

    @Test
    void testFindById_EstablecimientoSinFranjasHorarias() {
        // Arrange
        Integer id = 1;
        Reserva reservaTest = new Reserva();
        reservaTest.setId(id);
        reservaTest.setFechaReserva(fechaReserva);
        
        // Establecimiento sin franjas horarias (null)
        Establecimiento establecimientoSinFranjas = new Establecimiento();
        establecimientoSinFranjas.setId(1);
        establecimientoSinFranjas.setNombre("Establecimiento Test");
        establecimientoSinFranjas.setFranjasHorarias(null); // Franjas horarias null
        reservaTest.setEstablecimiento(establecimientoSinFranjas);
        
        when(reservaRepo.findById(id)).thenReturn(Optional.of(reservaTest));
        when(convocatoriaService.findByIdIgnoringValido(id)).thenReturn(null);

        // Act
        Reserva resultado = reservaService.findById(id);

        // Assert
        assertNotNull(resultado);
        assertNotNull(resultado.getEstablecimiento());
        assertNull(resultado.getEstablecimiento().getFranjasHorarias());
        
        verify(reservaRepo).findById(id);
        verify(convocatoriaService).findByIdIgnoringValido(id);
    }

    @Test
    void testFindById_ConvocatoriaConConvocadosNull() {
        // Arrange
        Integer id = 1;
        Reserva reservaTest = new Reserva();
        reservaTest.setId(id);
        reservaTest.setFechaReserva(fechaReserva);
        reservaTest.setEstablecimiento(establecimiento);
        
        // Convocatoria con lista de convocados null
        Convocatoria convocatoriaConConvocadosNull = new Convocatoria();
        convocatoriaConConvocadosNull.setConvocados(null); // Lista de convocados null
        reservaTest.setConvocatoria(convocatoriaConConvocadosNull);
        
        when(reservaRepo.findById(id)).thenReturn(Optional.of(reservaTest));

        // Act
        Reserva resultado = reservaService.findById(id);

        // Assert
        assertNotNull(resultado);
        assertNotNull(resultado.getConvocatoria());
        assertNull(resultado.getConvocatoria().getConvocados());
        
        verify(reservaRepo).findById(id);
        // No debe llamar al servicio de convocatoria porque ya existe
        verify(convocatoriaService, never()).findByIdIgnoringValido(anyInt());
    }

    @Test
    void testDelete() {
        // Arrange
        Reserva reservaTest = new Reserva();
        reservaTest.setId(1);
        reservaTest.setFechaReserva(fechaReserva);
        reservaTest.setEstablecimiento(establecimiento);

        // Act
        reservaService.delete(reservaTest);

        // Assert
        verify(reservaRepo).delete(reservaTest);
    }

    @Test
    void testVerificarDisponibilidad_AforoIlimitado() {
        // Arrange
        establecimiento.setAforo(null); // Aforo ilimitado
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = LocalTime.of(11, 0);

        // Act
        boolean resultado = reservaService.verificarDisponibilidad(establecimiento, fecha, horaInicio, horaFin, null);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void testVerificarDisponibilidad_AforoCero() {
        // Arrange
        establecimiento.setAforo(0); // Aforo cero = ilimitado
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = LocalTime.of(11, 0);

        // Act
        boolean resultado = reservaService.verificarDisponibilidad(establecimiento, fecha, horaInicio, horaFin, null);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void testVerificarDisponibilidad_ConAforo_Disponible() {
        // Arrange
        establecimiento.setAforo(5);
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = LocalTime.of(11, 0);
        LocalDateTime fechaInicio = LocalDateTime.of(fecha, horaInicio);
        LocalDateTime fechaFin = LocalDateTime.of(fecha, horaFin);
        
        List<Reserva> reservasSolapadas = Arrays.asList(new Reserva(), new Reserva()); // 2 reservas
        when(reservaRepo.findReservasSolapadas(establecimiento, fechaInicio, fechaFin)).thenReturn(reservasSolapadas);

        // Act
        boolean resultado = reservaService.verificarDisponibilidad(establecimiento, fecha, horaInicio, horaFin, null);

        // Assert
        assertTrue(resultado); // 2 < 5, hay disponibilidad
        verify(reservaRepo).findReservasSolapadas(establecimiento, fechaInicio, fechaFin);
    }

    @Test
    void testObtenerReservasSolapadas() {
        // Arrange
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = LocalTime.of(11, 0);
        LocalDateTime fechaInicio = LocalDateTime.of(fecha, horaInicio);
        LocalDateTime fechaFin = LocalDateTime.of(fecha, horaFin);
        
        List<Reserva> reservasEsperadas = Arrays.asList(reserva);
        when(reservaRepo.findReservasSolapadas(establecimiento, fechaInicio, fechaFin)).thenReturn(reservasEsperadas);

        // Act
        List<Reserva> resultado = reservaService.obtenerReservasSolapadas(establecimiento, fecha, horaInicio, horaFin);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(reservasEsperadas, resultado);
        verify(reservaRepo).findReservasSolapadas(establecimiento, fechaInicio, fechaFin);
    }

    @Test
    void testVerificarDisponibilidad_ConAforo_NoDisponible() {
        // Arrange
        establecimiento.setAforo(2);
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = LocalTime.of(11, 0);
        LocalDateTime fechaInicio = LocalDateTime.of(fecha, horaInicio);
        LocalDateTime fechaFin = LocalDateTime.of(fecha, horaFin);
        
        List<Reserva> reservasSolapadas = Arrays.asList(new Reserva(), new Reserva()); // 2 reservas
        when(reservaRepo.findReservasSolapadas(establecimiento, fechaInicio, fechaFin)).thenReturn(reservasSolapadas);

        // Act
        boolean resultado = reservaService.verificarDisponibilidad(establecimiento, fecha, horaInicio, horaFin, null);

        // Assert
        assertTrue(!resultado); // 2 >= 2, no hay disponibilidad
        verify(reservaRepo).findReservasSolapadas(establecimiento, fechaInicio, fechaFin);
    }

    @Test
    void testVerificarDisponibilidad_ConReservaExcluir() {
        // Arrange
        establecimiento.setAforo(2);
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = LocalTime.of(11, 0);
        LocalDateTime fechaInicio = LocalDateTime.of(fecha, horaInicio);
        LocalDateTime fechaFin = LocalDateTime.of(fecha, horaFin);
        
        Reserva reservaExcluir = new Reserva();
        reservaExcluir.setId(1);
        
        Reserva otraReserva = new Reserva();
        otraReserva.setId(2);
        
        List<Reserva> reservasSolapadas = Arrays.asList(reservaExcluir, otraReserva); // 2 reservas
        when(reservaRepo.findReservasSolapadas(establecimiento, fechaInicio, fechaFin)).thenReturn(reservasSolapadas);

        // Act
        boolean resultado = reservaService.verificarDisponibilidad(establecimiento, fecha, horaInicio, horaFin, reservaExcluir);

        // Assert
        assertTrue(resultado); // 1 < 2 (excluyendo la reserva), hay disponibilidad
        verify(reservaRepo).findReservasSolapadas(establecimiento, fechaInicio, fechaFin);
    }

    @Test
    void testObtenerFranjasDisponibles_EstablecimientoSinFranjas() {
        // Arrange
        LocalDate fecha = LocalDate.now().plusDays(1);
        establecimiento.setFranjasHorarias(new ArrayList<>());
        
        LocalDateTime inicioDelDia = fecha.atStartOfDay();
        LocalDateTime finDelDia = fecha.atTime(23, 59, 59);
        when(reservaRepo.findReservasByEstablecimientoAndFecha(establecimiento, inicioDelDia, finDelDia))
            .thenReturn(new ArrayList<>());

        // Act
        List<FranjaDisponibilidad> resultado = reservaService.obtenerFranjasDisponibles(establecimiento, fecha);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(reservaRepo).findReservasByEstablecimientoAndFecha(establecimiento, inicioDelDia, finDelDia);
    }

    @Test
    void testObtenerFranjasDisponibles_ConFranjas() {
        // Arrange
        LocalDate fecha = LocalDate.now().plusDays(1); // Asumiendo que es un día de la semana
        DayOfWeek diaSemana = fecha.getDayOfWeek();
        
        FranjaHoraria franja = new FranjaHoraria();
        franja.setDiaSemana(diaSemana);
        franja.setHoraInicio(LocalTime.of(9, 0));
        franja.setHoraFin(LocalTime.of(17, 0));
        
        establecimiento.setFranjasHorarias(Arrays.asList(franja));
        establecimiento.setAforo(5);
        
        LocalDateTime inicioDelDia = fecha.atStartOfDay();
        LocalDateTime finDelDia = fecha.atTime(23, 59, 59);
        when(reservaRepo.findReservasByEstablecimientoAndFecha(establecimiento, inicioDelDia, finDelDia))
            .thenReturn(new ArrayList<>());

        // Act
        List<FranjaDisponibilidad> resultado = reservaService.obtenerFranjasDisponibles(establecimiento, fecha);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(franja, resultado.get(0).getFranjaHoraria());
        assertTrue(resultado.get(0).isTieneDisponibilidad());
        verify(reservaRepo).findReservasByEstablecimientoAndFecha(establecimiento, inicioDelDia, finDelDia);
    }

    @Test
    void testObtenerFranjasDisponibles_AforoIlimitado() {
        // Arrange
        LocalDate fecha = LocalDate.now().plusDays(1);
        DayOfWeek diaSemana = fecha.getDayOfWeek();
        
        FranjaHoraria franja = new FranjaHoraria();
        franja.setDiaSemana(diaSemana);
        franja.setHoraInicio(LocalTime.of(9, 0));
        franja.setHoraFin(LocalTime.of(17, 0));
        
        establecimiento.setFranjasHorarias(Arrays.asList(franja));
        establecimiento.setAforo(null); // Aforo ilimitado
        
        LocalDateTime inicioDelDia = fecha.atStartOfDay();
        LocalDateTime finDelDia = fecha.atTime(23, 59, 59);
        when(reservaRepo.findReservasByEstablecimientoAndFecha(establecimiento, inicioDelDia, finDelDia))
            .thenReturn(Arrays.asList(reserva)); // Con reservas existentes

        // Act
        List<FranjaDisponibilidad> resultado = reservaService.obtenerFranjasDisponibles(establecimiento, fecha);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).isTieneDisponibilidad()); // Siempre disponible con aforo ilimitado
        verify(reservaRepo).findReservasByEstablecimientoAndFecha(establecimiento, inicioDelDia, finDelDia);
    }

    @Test
    void testObtenerFranjasDisponibles_ConAforoLimitadoYReservasSolapadas() {
        // Arrange
        Establecimiento establnto = new Establecimiento();
        establnto.setId(1);
        establnto.setAforo(2); // Aforo limitado
        
        FranjaHoraria franja = new FranjaHoraria();
        franja.setHoraInicio(LocalTime.of(10, 0));
        franja.setHoraFin(LocalTime.of(12, 0));
        franja.setDiaSemana(DayOfWeek.MONDAY);
        establnto.setFranjasHorarias(List.of(franja));
        
        LocalDate fecha = LocalDate.of(2024, 1, 15);
        
        // Crear reservas que se solapen parcialmente
        Reserva reserva1 = new Reserva();
        reserva1.setFechaReserva(LocalDateTime.of(fecha, LocalTime.of(10, 30)));
        reserva1.setHoraFin(LocalTime.of(11, 0));
        
        Reserva reserva2 = new Reserva();
        reserva2.setFechaReserva(LocalDateTime.of(fecha, LocalTime.of(10, 45)));
        reserva2.setHoraFin(LocalTime.of(11, 30));
        
        Reserva reserva3 = new Reserva();
        reserva3.setFechaReserva(LocalDateTime.of(fecha, LocalTime.of(11, 15)));
        reserva3.setHoraFin(LocalTime.of(11, 45));
        
        List<Reserva> reservasDelDia = List.of(reserva1, reserva2, reserva3);
        
        when(reservaRepo.findReservasByEstablecimientoAndFecha(
            eq(establnto), 
            any(LocalDateTime.class), 
            any(LocalDateTime.class)
        )).thenReturn(reservasDelDia);
        
        // Act
        List<FranjaDisponibilidad> resultado = reservaService.obtenerFranjasDisponibles(establnto, fecha);
        
        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        
        FranjaDisponibilidad franjaDisp = resultado.get(0);
        assertEquals(LocalTime.of(10, 0), franjaDisp.getFranjaHoraria().getHoraInicio());
        assertEquals(LocalTime.of(12, 0), franjaDisp.getFranjaHoraria().getHoraFin());
        
        // Debe tener períodos disponibles donde el aforo no esté completo
        assertFalse(franjaDisp.getPeriodosDisponibles().isEmpty());
    }
    
    @Test
    void testObtenerFranjasDisponibles_ConReservasSinHoraFin() {
        // Arrange - Test para ejercitar obtenerHoraFinReserva con hora por defecto
        Establecimiento estab = new Establecimiento();
        estab.setId(1);
        estab.setAforo(1);
        
        FranjaHoraria franja = new FranjaHoraria();
        franja.setHoraInicio(LocalTime.of(9, 0));
        franja.setHoraFin(LocalTime.of(11, 0));
        franja.setDiaSemana(DayOfWeek.MONDAY);
        estab.setFranjasHorarias(List.of(franja));
        
        LocalDate fecha = LocalDate.of(2024, 1, 15);
        
        // Reserva sin hora de fin (usará hora por defecto +1 hora)
        Reserva reservaSinHoraFin = new Reserva();
        reservaSinHoraFin.setFechaReserva(LocalDateTime.of(fecha, LocalTime.of(9, 30)));
        reservaSinHoraFin.setHoraFin(null); // Sin hora de fin
        
        List<Reserva> reservasDelDia = List.of(reservaSinHoraFin);
        
        when(reservaRepo.findReservasByEstablecimientoAndFecha(
            eq(estab), 
            any(LocalDateTime.class), 
            any(LocalDateTime.class)
        )).thenReturn(reservasDelDia);
        
        // Act
        List<FranjaDisponibilidad> resultado = reservaService.obtenerFranjasDisponibles(estab, fecha);
        
        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        
        FranjaDisponibilidad franjaDisp = resultado.get(0);
        // Con aforo 1 y una reserva sin hora fin, puede no haber períodos disponibles
        assertTrue(franjaDisp.getPeriodosDisponibles().isEmpty());
    }
    
    @Test
    void testVerificarDisponibilidad_ConReservasEnBordesDeIntervalo() {
        // Arrange - Test para ejercitar reservaOcupaIntervalo y estaEntreFranja
        Establecimiento estab = new Establecimiento();
        estab.setId(1);
        estab.setAforo(2);
        
        LocalDate fecha = LocalDate.of(2024, 1, 15);
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = LocalTime.of(11, 0);
        
        // Reserva que termina exactamente cuando empieza la nueva
        Reserva reservaAnterior = new Reserva();
        reservaAnterior.setFechaReserva(LocalDateTime.of(fecha, LocalTime.of(9, 0)));
        reservaAnterior.setHoraFin(LocalTime.of(10, 0));
        
        // Reserva que empieza exactamente cuando termina la nueva
        Reserva reservaPosterior = new Reserva();
        reservaPosterior.setFechaReserva(LocalDateTime.of(fecha, LocalTime.of(11, 0)));
        reservaPosterior.setHoraFin(LocalTime.of(12, 0));
        
        List<Reserva> reservasDelDia = List.of(reservaAnterior, reservaPosterior);
        
        when(reservaRepo.findReservasSolapadas(
            eq(estab), 
            eq(LocalDateTime.of(fecha, horaInicio)), 
            eq(LocalDateTime.of(fecha, horaFin))
        )).thenReturn(reservasDelDia);
        
        // Act
        boolean disponible = reservaService.verificarDisponibilidad(
            estab, fecha, horaInicio, horaFin, null
        );
        
        // Assert
        assertFalse(disponible); // El código considera solapamiento en los bordes exactos
    }
    
    @Test
    void testVerificarDisponibilidad_ConMultiplesReservasSolapadas() {
        // Arrange - Test para ejercitar contarReservasEnIntervalo
        Establecimiento estab = new Establecimiento();
        estab.setId(1);
        estab.setAforo(3);
        
        LocalDate fecha = LocalDate.of(2024, 1, 15);
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = LocalTime.of(12, 0);
        
        // Múltiples reservas que se solapan parcialmente
        Reserva reserva1 = new Reserva();
        reserva1.setFechaReserva(LocalDateTime.of(fecha, LocalTime.of(9, 30)));
        reserva1.setHoraFin(LocalTime.of(10, 30));
        
        Reserva reserva2 = new Reserva();
        reserva2.setFechaReserva(LocalDateTime.of(fecha, LocalTime.of(10, 15)));
        reserva2.setHoraFin(LocalTime.of(11, 15));
        
        Reserva reserva3 = new Reserva();
        reserva3.setFechaReserva(LocalDateTime.of(fecha, LocalTime.of(11, 0)));
        reserva3.setHoraFin(LocalTime.of(12, 30));
        
        List<Reserva> reservasDelDia = List.of(reserva1, reserva2, reserva3);
        
        when(reservaRepo.findReservasSolapadas(
            estab, 
            LocalDateTime.of(fecha, horaInicio), 
            LocalDateTime.of(fecha, horaFin)
        )).thenReturn(reservasDelDia);
        
        // Act
        boolean disponible = reservaService.verificarDisponibilidad(
            estab, fecha, horaInicio, horaFin, null
        );
        
        // Assert
        assertFalse(disponible); // Las 3 reservas se solapan en algunos momentos, superando el aforo
    }
    
    @Test
    void testObtenerFranjasDisponibles_ConPuntosDeTimeComplejos() {
        // Arrange - Test para ejercitar obtenerPuntosDeTiempoOrdenados y agregarPuntoSiEstaEnFranja
        Establecimiento estab = new Establecimiento();
        estab.setId(1);
        estab.setAforo(1);
        
        FranjaHoraria franja = new FranjaHoraria();
        franja.setHoraInicio(LocalTime.of(10, 0));
        franja.setHoraFin(LocalTime.of(14, 0));
        franja.setDiaSemana(DayOfWeek.MONDAY);
        estab.setFranjasHorarias(List.of(franja));
        
        LocalDate fecha = LocalDate.of(2024, 1, 15);
        
        // Reservas con puntos de tiempo que están fuera de la franja
        Reserva reservaAntes = new Reserva();
        reservaAntes.setFechaReserva(LocalDateTime.of(fecha, LocalTime.of(9, 0))); // Antes de la franja
        reservaAntes.setHoraFin(LocalTime.of(10, 30)); // Termina dentro de la franja
        
        Reserva reservaDespues = new Reserva();
        reservaDespues.setFechaReserva(LocalDateTime.of(fecha, LocalTime.of(13, 30))); // Dentro de la franja
        reservaDespues.setHoraFin(LocalTime.of(15, 0)); // Termina después de la franja
        
        Reserva reservaDentro = new Reserva();
        reservaDentro.setFechaReserva(LocalDateTime.of(fecha, LocalTime.of(11, 0)));
        reservaDentro.setHoraFin(LocalTime.of(12, 0));
        
        List<Reserva> reservasDelDia = List.of(reservaAntes, reservaDespues, reservaDentro);
        
        when(reservaRepo.findReservasByEstablecimientoAndFecha(
            eq(estab), 
            any(LocalDateTime.class), 
            any(LocalDateTime.class)
        )).thenReturn(reservasDelDia);
        
        // Act
        List<FranjaDisponibilidad> resultado = reservaService.obtenerFranjasDisponibles(estab, fecha);
        
        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        
        FranjaDisponibilidad franjaDisp = resultado.get(0);
        // Con las reservas complejas configuradas, no hay períodos disponibles
        assertTrue(franjaDisp.getPeriodosDisponibles().isEmpty());
    }

    @Test
    void testPeriodoDisponible_Constructor() {
        // Arrange
        LocalTime horaInicio = LocalTime.of(9, 0);
        LocalTime horaFin = LocalTime.of(10, 30);

        // Act
        PeriodoDisponible periodo = new PeriodoDisponible(horaInicio, horaFin);

        // Assert
        assertNotNull(periodo);
        assertEquals(horaInicio, periodo.getHoraInicio());
        assertEquals(horaFin, periodo.getHoraFin());
    }

    @Test
    void testPeriodoDisponible_ConstructorConHorasExtremas() {
        // Arrange
        LocalTime horaInicioMinima = LocalTime.of(0, 0);
        LocalTime horaFinMaxima = LocalTime.of(23, 59);

        // Act
        PeriodoDisponible periodo = new PeriodoDisponible(horaInicioMinima, horaFinMaxima);

        // Assert
        assertNotNull(periodo);
        assertEquals(horaInicioMinima, periodo.getHoraInicio());
        assertEquals(horaFinMaxima, periodo.getHoraFin());
    }

    @Test
    void testPeriodoDisponible_ConstructorConHorasIguales() {
        // Arrange
        LocalTime hora = LocalTime.of(12, 0);

        // Act
        PeriodoDisponible periodo = new PeriodoDisponible(hora, hora);

        // Assert
        assertNotNull(periodo);
        assertEquals(hora, periodo.getHoraInicio());
        assertEquals(hora, periodo.getHoraFin());
    }

    @Test
    void testPeriodoDisponible_GetHoraInicio() {
        // Arrange
        LocalTime horaInicio = LocalTime.of(14, 15);
        LocalTime horaFin = LocalTime.of(15, 45);
        PeriodoDisponible periodo = new PeriodoDisponible(horaInicio, horaFin);

        // Act
        LocalTime resultado = periodo.getHoraInicio();

        // Assert
        assertEquals(horaInicio, resultado);
        assertEquals(LocalTime.of(14, 15), resultado);
    }

    @Test
    void testPeriodoDisponible_GetHoraFin() {
        // Arrange
        LocalTime horaInicio = LocalTime.of(8, 30);
        LocalTime horaFin = LocalTime.of(17, 0);
        PeriodoDisponible periodo = new PeriodoDisponible(horaInicio, horaFin);

        // Act
        LocalTime resultado = periodo.getHoraFin();

        // Assert
        assertEquals(horaFin, resultado);
        assertEquals(LocalTime.of(17, 0), resultado);
    }

    @Test
    void testPeriodoDisponible_ToString() {
        // Arrange
        LocalTime horaInicio = LocalTime.of(9, 30);
        LocalTime horaFin = LocalTime.of(11, 15);
        PeriodoDisponible periodo = new PeriodoDisponible(horaInicio, horaFin);

        // Act
        String resultado = periodo.toString();

        // Assert
        assertNotNull(resultado);
        assertEquals("09:30 - 11:15", resultado);
        assertTrue(resultado.contains("09:30"));
        assertTrue(resultado.contains("11:15"));
        assertTrue(resultado.contains(" - "));
    }

    @Test
    void testPeriodoDisponible_ToStringConHorasEnPuntoEnMinutos() {
        // Arrange - Test con horas exactas (00 minutos)
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = LocalTime.of(12, 0);
        PeriodoDisponible periodo = new PeriodoDisponible(horaInicio, horaFin);

        // Act
        String resultado = periodo.toString();

        // Assert
        assertEquals("10:00 - 12:00", resultado);
    }

    @Test
    void testPeriodoDisponible_ToStringConHorasMedioDia() {
        // Arrange - Test con horas del mediodía
        LocalTime horaInicio = LocalTime.of(12, 0);
        LocalTime horaFin = LocalTime.of(13, 30);
        PeriodoDisponible periodo = new PeriodoDisponible(horaInicio, horaFin);

        // Act
        String resultado = periodo.toString();

        // Assert
        assertEquals("12:00 - 13:30", resultado);
    }

    @Test
    void testPeriodoDisponible_ToStringConHorasNocturnas() {
        // Arrange - Test con horas nocturnas/tardías
        LocalTime horaInicio = LocalTime.of(22, 45);
        LocalTime horaFin = LocalTime.of(23, 59);
        PeriodoDisponible periodo = new PeriodoDisponible(horaInicio, horaFin);

        // Act
        String resultado = periodo.toString();

        // Assert
        assertEquals("22:45 - 23:59", resultado);
    }

    @Test
    void testPeriodoDisponible_ToStringConHorasTempranas() {
        // Arrange - Test con horas muy tempranas
        LocalTime horaInicio = LocalTime.of(0, 0);
        LocalTime horaFin = LocalTime.of(1, 15);
        PeriodoDisponible periodo = new PeriodoDisponible(horaInicio, horaFin);

        // Act
        String resultado = periodo.toString();

        // Assert
        assertEquals("00:00 - 01:15", resultado);
    }

    @Test
    void testPeriodoDisponible_ToStringConPeriodoCorto() {
        // Arrange - Test con período muy corto (15 minutos)
        LocalTime horaInicio = LocalTime.of(14, 30);
        LocalTime horaFin = LocalTime.of(14, 45);
        PeriodoDisponible periodo = new PeriodoDisponible(horaInicio, horaFin);

        // Act
        String resultado = periodo.toString();

        // Assert
        assertEquals("14:30 - 14:45", resultado);
    }

    @Test
    void testPeriodoDisponible_ToStringConHorasIguales() {
        // Arrange - Test con horas iguales (período de duración cero)
        LocalTime hora = LocalTime.of(16, 0);
        PeriodoDisponible periodo = new PeriodoDisponible(hora, hora);

        // Act
        String resultado = periodo.toString();

        // Assert
        assertEquals("16:00 - 16:00", resultado);
    }

    @Test
    void testPeriodoDisponible_InmutabilidadHoraInicio() {
        // Arrange
        LocalTime horaInicioOriginal = LocalTime.of(10, 0);
        LocalTime horaFin = LocalTime.of(11, 0);
        PeriodoDisponible periodo = new PeriodoDisponible(horaInicioOriginal, horaFin);

        // Act - Obtener la hora de inicio
        LocalTime horaInicioObtenida = periodo.getHoraInicio();

        // Assert - Verificar que son la misma referencia o valor (LocalTime es inmutable)
        assertEquals(horaInicioOriginal, horaInicioObtenida);
        // Verificar que no se puede modificar el estado interno a través del getter
        // (LocalTime es inmutable por naturaleza, pero es bueno verificarlo)
        assertNotNull(horaInicioObtenida);
    }

    @Test
    void testPeriodoDisponible_InmutabilidadHoraFin() {
        // Arrange
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFinOriginal = LocalTime.of(11, 0);
        PeriodoDisponible periodo = new PeriodoDisponible(horaInicio, horaFinOriginal);

        // Act - Obtener la hora de fin
        LocalTime horaFinObtenida = periodo.getHoraFin();

        // Assert - Verificar que son la misma referencia o valor
        assertEquals(horaFinOriginal, horaFinObtenida);
        assertNotNull(horaFinObtenida);
    }

    @Test
    void testPeriodoDisponible_MultipleInstancias() {
        // Arrange - Crear múltiples instancias para verificar independencia
        PeriodoDisponible periodo1 = new PeriodoDisponible(LocalTime.of(9, 0), LocalTime.of(10, 0));
        PeriodoDisponible periodo2 = new PeriodoDisponible(LocalTime.of(14, 0), LocalTime.of(15, 0));
        PeriodoDisponible periodo3 = new PeriodoDisponible(LocalTime.of(9, 0), LocalTime.of(10, 0));

        // Act & Assert - Verificar que cada instancia mantiene su estado independiente
        assertEquals(LocalTime.of(9, 0), periodo1.getHoraInicio());
        assertEquals(LocalTime.of(10, 0), periodo1.getHoraFin());
        
        assertEquals(LocalTime.of(14, 0), periodo2.getHoraInicio());
        assertEquals(LocalTime.of(15, 0), periodo2.getHoraFin());
        
        assertEquals(LocalTime.of(9, 0), periodo3.getHoraInicio());
        assertEquals(LocalTime.of(10, 0), periodo3.getHoraFin());
        
        // Verificar toString independiente
        assertEquals("09:00 - 10:00", periodo1.toString());
        assertEquals("14:00 - 15:00", periodo2.toString());
        assertEquals("09:00 - 10:00", periodo3.toString());
    }
    
    @Test
    void testObtenerDisponibilidadDia_EstablecimientoCerrado() {
        // Arrange
        LocalDate fecha = LocalDate.of(2024, 1, 15); // Lunes
        
        // Establecimiento sin franjas horarias para ese día
        FranjaHoraria franjaTuesday = new FranjaHoraria();
        franjaTuesday.setDiaSemana(DayOfWeek.TUESDAY);
        franjaTuesday.setHoraInicio(LocalTime.of(9, 0));
        franjaTuesday.setHoraFin(LocalTime.of(17, 0));
        
        establecimiento.setFranjasHorarias(Arrays.asList(franjaTuesday));

        // Act
        DisponibilidadDia resultado = reservaService.obtenerDisponibilidadDia(establecimiento, fecha);

        // Assert
        assertNotNull(resultado);
        assertEquals(fecha, resultado.getFecha());
        assertFalse(resultado.isTieneHorarioApertura());
        assertFalse(resultado.isTieneDisponibilidad());
        assertTrue(resultado.getFranjasDisponibles().isEmpty());
        assertEquals("Cerrado", resultado.getResumen());
    }

    @Test
    void testObtenerDisponibilidadDia_ConDisponibilidad() {
        // Arrange
        LocalDate fecha = LocalDate.of(2024, 1, 15); // Lunes
        
        FranjaHoraria franja = new FranjaHoraria();
        franja.setDiaSemana(DayOfWeek.MONDAY);
        franja.setHoraInicio(LocalTime.of(9, 0));
        franja.setHoraFin(LocalTime.of(17, 0));
        
        establecimiento.setFranjasHorarias(Arrays.asList(franja));
        establecimiento.setAforo(5);
        
        LocalDateTime inicioDelDia = fecha.atStartOfDay();
        LocalDateTime finDelDia = fecha.atTime(23, 59, 59);
        when(reservaRepo.findReservasByEstablecimientoAndFecha(establecimiento, inicioDelDia, finDelDia))
            .thenReturn(new ArrayList<>());

        // Act
        DisponibilidadDia resultado = reservaService.obtenerDisponibilidadDia(establecimiento, fecha);

        // Assert
        assertNotNull(resultado);
        assertEquals(fecha, resultado.getFecha());
        assertTrue(resultado.isTieneHorarioApertura());
        assertTrue(resultado.isTieneDisponibilidad());
        assertEquals(1, resultado.getFranjasDisponibles().size());
        assertEquals("1 período disponible", resultado.getResumen());
        verify(reservaRepo).findReservasByEstablecimientoAndFecha(establecimiento, inicioDelDia, finDelDia);
    }

    @Test
    void testObtenerDisponibilidadDia_SinDisponibilidad() {
        // Arrange
        LocalDate fecha = LocalDate.of(2024, 1, 15); // Lunes
        
        FranjaHoraria franja = new FranjaHoraria();
        franja.setDiaSemana(DayOfWeek.MONDAY);
        franja.setHoraInicio(LocalTime.of(9, 0));
        franja.setHoraFin(LocalTime.of(10, 0));
        
        establecimiento.setFranjasHorarias(Arrays.asList(franja));
        establecimiento.setAforo(1); // Aforo limitado
        
        // Reserva que ocupa toda la franja
        Reserva reservaCompleta = new Reserva();
        reservaCompleta.setFechaReserva(LocalDateTime.of(fecha, LocalTime.of(9, 0)));
        reservaCompleta.setHoraFin(LocalTime.of(10, 0));
        
        LocalDateTime inicioDelDia = fecha.atStartOfDay();
        LocalDateTime finDelDia = fecha.atTime(23, 59, 59);
        when(reservaRepo.findReservasByEstablecimientoAndFecha(establecimiento, inicioDelDia, finDelDia))
            .thenReturn(Arrays.asList(reservaCompleta));

        // Act
        DisponibilidadDia resultado = reservaService.obtenerDisponibilidadDia(establecimiento, fecha);

        // Assert
        assertNotNull(resultado);
        assertEquals(fecha, resultado.getFecha());
        assertTrue(resultado.isTieneHorarioApertura());
        assertFalse(resultado.isTieneDisponibilidad());
        assertEquals(1, resultado.getFranjasDisponibles().size());
        assertEquals("Sin disponibilidad", resultado.getResumen());
        verify(reservaRepo).findReservasByEstablecimientoAndFecha(establecimiento, inicioDelDia, finDelDia);
    }

    @Test
    void testObtenerDisponibilidadDia_VariosPeriodosDisponibles() {
        // Arrange
        LocalDate fecha = LocalDate.of(2024, 1, 15); // Lunes
        
        FranjaHoraria franja1 = new FranjaHoraria();
        franja1.setDiaSemana(DayOfWeek.MONDAY);
        franja1.setHoraInicio(LocalTime.of(9, 0));
        franja1.setHoraFin(LocalTime.of(12, 0));
        
        FranjaHoraria franja2 = new FranjaHoraria();
        franja2.setDiaSemana(DayOfWeek.MONDAY);
        franja2.setHoraInicio(LocalTime.of(14, 0));
        franja2.setHoraFin(LocalTime.of(17, 0));
        
        establecimiento.setFranjasHorarias(Arrays.asList(franja1, franja2));
        establecimiento.setAforo(5);
        
        LocalDateTime inicioDelDia = fecha.atStartOfDay();
        LocalDateTime finDelDia = fecha.atTime(23, 59, 59);
        when(reservaRepo.findReservasByEstablecimientoAndFecha(establecimiento, inicioDelDia, finDelDia))
            .thenReturn(new ArrayList<>());

        // Act
        DisponibilidadDia resultado = reservaService.obtenerDisponibilidadDia(establecimiento, fecha);

        // Assert
        assertNotNull(resultado);
        assertEquals(fecha, resultado.getFecha());
        assertTrue(resultado.isTieneHorarioApertura());
        assertTrue(resultado.isTieneDisponibilidad());
        assertEquals(2, resultado.getFranjasDisponibles().size());
        assertEquals("2 períodos disponibles", resultado.getResumen());
        verify(reservaRepo).findReservasByEstablecimientoAndFecha(establecimiento, inicioDelDia, finDelDia);
    }

    // ================================
    // TESTS PARA obtenerDisponibilidadMensual
    // ================================

    @Test
    void testObtenerDisponibilidadMensual_MesCompleto() {
        // Arrange
        int año = 2024;
        int mes = 1; // Enero
        
        FranjaHoraria franja = new FranjaHoraria();
        franja.setDiaSemana(DayOfWeek.MONDAY);
        franja.setHoraInicio(LocalTime.of(9, 0));
        franja.setHoraFin(LocalTime.of(17, 0));
        
        establecimiento.setFranjasHorarias(Arrays.asList(franja));
        establecimiento.setAforo(5);
        
        LocalDateTime inicioMes = LocalDate.of(año, mes, 1).atStartOfDay();
        LocalDateTime finMes = LocalDate.of(año, mes, 31).atTime(23, 59, 59);
        when(reservaRepo.findReservasByEstablecimientoAndFecha(establecimiento, inicioMes, finMes))
            .thenReturn(new ArrayList<>());

        // Act
        Map<LocalDate, DisponibilidadDia> resultado = reservaService.obtenerDisponibilidadMensual(establecimiento, año, mes);

        // Assert
        assertNotNull(resultado);
        assertEquals(31, resultado.size()); // Enero tiene 31 días
        
        // Verificar algunos días específicos
        LocalDate primerLunes = LocalDate.of(2024, 1, 1); // 1 de enero es lunes
        DisponibilidadDia disponibilidadLunes = resultado.get(primerLunes);
        assertNotNull(disponibilidadLunes);
        assertTrue(disponibilidadLunes.isTieneHorarioApertura());
        assertTrue(disponibilidadLunes.isTieneDisponibilidad());
        
        LocalDate primerMartes = LocalDate.of(2024, 1, 2); // 2 de enero es martes
        DisponibilidadDia disponibilidadMartes = resultado.get(primerMartes);
        assertNotNull(disponibilidadMartes);
        assertFalse(disponibilidadMartes.isTieneHorarioApertura()); // No hay franja para martes
        assertEquals("Cerrado", disponibilidadMartes.getResumen());
        
        verify(reservaRepo).findReservasByEstablecimientoAndFecha(establecimiento, inicioMes, finMes);
    }

    @Test
    void testObtenerDisponibilidadMensual_ConReservasExistentes() {
        // Arrange
        int año = 2024;
        int mes = 2; // Febrero (mes más corto)
        
        FranjaHoraria franja = new FranjaHoraria();
        franja.setDiaSemana(DayOfWeek.THURSDAY);
        franja.setHoraInicio(LocalTime.of(10, 0));
        franja.setHoraFin(LocalTime.of(12, 0));
        
        establecimiento.setFranjasHorarias(Arrays.asList(franja));
        establecimiento.setAforo(1);
        
        // Crear una reserva para el primer jueves del mes
        LocalDate primerJueves = LocalDate.of(2024, 2, 1); // 1 de febrero es jueves
        Reserva reservaJueves = new Reserva();
        reservaJueves.setFechaReserva(LocalDateTime.of(primerJueves, LocalTime.of(10, 0)));
        reservaJueves.setHoraFin(LocalTime.of(12, 0));
        
        LocalDateTime inicioMes = LocalDate.of(año, mes, 1).atStartOfDay();
        LocalDateTime finMes = LocalDate.of(año, mes, 29).atTime(23, 59, 59); // 2024 es bisiesto
        when(reservaRepo.findReservasByEstablecimientoAndFecha(establecimiento, inicioMes, finMes))
            .thenReturn(Arrays.asList(reservaJueves));

        // Act
        Map<LocalDate, DisponibilidadDia> resultado = reservaService.obtenerDisponibilidadMensual(establecimiento, año, mes);

        // Assert
        assertNotNull(resultado);
        assertEquals(29, resultado.size()); // Febrero 2024 tiene 29 días (bisiesto)
        
        // Verificar el primer jueves (con reserva)
        DisponibilidadDia disponibilidadPrimerJueves = resultado.get(primerJueves);
        assertNotNull(disponibilidadPrimerJueves);
        assertTrue(disponibilidadPrimerJueves.isTieneHorarioApertura());
        assertFalse(disponibilidadPrimerJueves.isTieneDisponibilidad()); // Sin disponibilidad por la reserva
        assertEquals("Sin disponibilidad", disponibilidadPrimerJueves.getResumen());
        
        // Verificar el segundo jueves (sin reserva)
        LocalDate segundoJueves = LocalDate.of(2024, 2, 8);
        DisponibilidadDia disponibilidadSegundoJueves = resultado.get(segundoJueves);
        assertNotNull(disponibilidadSegundoJueves);
        assertTrue(disponibilidadSegundoJueves.isTieneHorarioApertura());
        assertTrue(disponibilidadSegundoJueves.isTieneDisponibilidad()); // Con disponibilidad
        assertEquals("1 período disponible", disponibilidadSegundoJueves.getResumen());
        
        verify(reservaRepo).findReservasByEstablecimientoAndFecha(establecimiento, inicioMes, finMes);
    }

    @Test
    void testObtenerDisponibilidadMensual_EstablecimientoSiempreCerrado() {
        // Arrange
        int año = 2024;
        int mes = 3; // Marzo
        
        // Establecimiento sin franjas horarias
        establecimiento.setFranjasHorarias(new ArrayList<>());
        
        LocalDateTime inicioMes = LocalDate.of(año, mes, 1).atStartOfDay();
        LocalDateTime finMes = LocalDate.of(año, mes, 31).atTime(23, 59, 59);
        when(reservaRepo.findReservasByEstablecimientoAndFecha(establecimiento, inicioMes, finMes))
            .thenReturn(new ArrayList<>());

        // Act
        Map<LocalDate, DisponibilidadDia> resultado = reservaService.obtenerDisponibilidadMensual(establecimiento, año, mes);

        // Assert
        assertNotNull(resultado);
        assertEquals(31, resultado.size()); // Marzo tiene 31 días
        
        // Verificar que todos los días están cerrados
        for (DisponibilidadDia disponibilidad : resultado.values()) {
            assertFalse(disponibilidad.isTieneHorarioApertura());
            assertFalse(disponibilidad.isTieneDisponibilidad());
            assertEquals("Cerrado", disponibilidad.getResumen());
        }
        
        verify(reservaRepo).findReservasByEstablecimientoAndFecha(establecimiento, inicioMes, finMes);
    }

    @Test
    void testObtenerDisponibilidadMensual_AforoIlimitado() {
        // Arrange
        int año = 2024;
        int mes = 4; // Abril
        
        FranjaHoraria franja = new FranjaHoraria();
        franja.setDiaSemana(DayOfWeek.WEDNESDAY);
        franja.setHoraInicio(LocalTime.of(8, 0));
        franja.setHoraFin(LocalTime.of(18, 0));
        
        establecimiento.setFranjasHorarias(Arrays.asList(franja));
        establecimiento.setAforo(null); // Aforo ilimitado
        
        // Crear múltiples reservas para un miércoles
        LocalDate primerMiercoles = LocalDate.of(2024, 4, 3);
        Reserva reserva1 = new Reserva();
        reserva1.setFechaReserva(LocalDateTime.of(primerMiercoles, LocalTime.of(9, 0)));
        reserva1.setHoraFin(LocalTime.of(10, 0));
        
        Reserva reserva2 = new Reserva();
        reserva2.setFechaReserva(LocalDateTime.of(primerMiercoles, LocalTime.of(11, 0)));
        reserva2.setHoraFin(LocalTime.of(12, 0));
        
        LocalDateTime inicioMes = LocalDate.of(año, mes, 1).atStartOfDay();
        LocalDateTime finMes = LocalDate.of(año, mes, 30).atTime(23, 59, 59);
        when(reservaRepo.findReservasByEstablecimientoAndFecha(establecimiento, inicioMes, finMes))
            .thenReturn(Arrays.asList(reserva1, reserva2));

        // Act
        Map<LocalDate, DisponibilidadDia> resultado = reservaService.obtenerDisponibilidadMensual(establecimiento, año, mes);

        // Assert
        assertNotNull(resultado);
        assertEquals(30, resultado.size()); // Abril tiene 30 días
        
        // Verificar el primer miércoles (con reservas pero aforo ilimitado)
        DisponibilidadDia disponibilidadMiercoles = resultado.get(primerMiercoles);
        assertNotNull(disponibilidadMiercoles);
        assertTrue(disponibilidadMiercoles.isTieneHorarioApertura());
        assertTrue(disponibilidadMiercoles.isTieneDisponibilidad()); // Siempre disponible con aforo ilimitado
        assertEquals("1 período disponible", disponibilidadMiercoles.getResumen());
        
        verify(reservaRepo).findReservasByEstablecimientoAndFecha(establecimiento, inicioMes, finMes);
    }

    // ================================
    // TESTS PARA calcularDisponibilidadDiaOptimizada (método privado testeado a través del público)
    // ================================

    @Test
    void testCalcularDisponibilidadDiaOptimizada_SinReservas() {
        // Arrange - Testeado a través de obtenerDisponibilidadMensual
        int año = 2024;
        int mes = 5; // Mayo
        
        FranjaHoraria franja = new FranjaHoraria();
        franja.setDiaSemana(DayOfWeek.FRIDAY);
        franja.setHoraInicio(LocalTime.of(13, 0));
        franja.setHoraFin(LocalTime.of(16, 0));
        
        establecimiento.setFranjasHorarias(Arrays.asList(franja));
        establecimiento.setAforo(3);
        
        LocalDateTime inicioMes = LocalDate.of(año, mes, 1).atStartOfDay();
        LocalDateTime finMes = LocalDate.of(año, mes, 31).atTime(23, 59, 59);
        when(reservaRepo.findReservasByEstablecimientoAndFecha(establecimiento, inicioMes, finMes))
            .thenReturn(new ArrayList<>());

        // Act
        Map<LocalDate, DisponibilidadDia> resultado = reservaService.obtenerDisponibilidadMensual(establecimiento, año, mes);

        // Assert - Verificar que calcularDisponibilidadDiaOptimizada funciona correctamente
        LocalDate primerViernes = LocalDate.of(2024, 5, 3);
        DisponibilidadDia disponibilidadViernes = resultado.get(primerViernes);
        
        assertNotNull(disponibilidadViernes);
        assertTrue(disponibilidadViernes.isTieneHorarioApertura());
        assertTrue(disponibilidadViernes.isTieneDisponibilidad());
        assertEquals(1, disponibilidadViernes.getFranjasDisponibles().size());
        assertEquals("1 período disponible", disponibilidadViernes.getResumen());
        
        // Verificar un día sin horario de apertura
        LocalDate primerSabado = LocalDate.of(2024, 5, 4);
        DisponibilidadDia disponibilidadSabado = resultado.get(primerSabado);
        
        assertNotNull(disponibilidadSabado);
        assertFalse(disponibilidadSabado.isTieneHorarioApertura());
        assertFalse(disponibilidadSabado.isTieneDisponibilidad());
        assertTrue(disponibilidadSabado.getFranjasDisponibles().isEmpty());
        assertEquals("Cerrado", disponibilidadSabado.getResumen());
        
        verify(reservaRepo).findReservasByEstablecimientoAndFecha(establecimiento, inicioMes, finMes);
    }

    @Test
    void testCalcularDisponibilidadDiaOptimizada_ConReservasDelDia() {
        // Arrange - Testeado a través de obtenerDisponibilidadMensual
        int año = 2024;
        int mes = 6; // Junio
        
        FranjaHoraria franja = new FranjaHoraria();
        franja.setDiaSemana(DayOfWeek.SATURDAY);
        franja.setHoraInicio(LocalTime.of(10, 0));
        franja.setHoraFin(LocalTime.of(14, 0));
        
        establecimiento.setFranjasHorarias(Arrays.asList(franja));
        establecimiento.setAforo(2);
        
        // Reservas para diferentes sábados del mes
        LocalDate primerSabado = LocalDate.of(2024, 6, 1);
        Reserva reservaSabado1 = new Reserva();
        reservaSabado1.setFechaReserva(LocalDateTime.of(primerSabado, LocalTime.of(10, 0)));
        reservaSabado1.setHoraFin(LocalTime.of(11, 0));
        
        Reserva reservaSabado2 = new Reserva();
        reservaSabado2.setFechaReserva(LocalDateTime.of(primerSabado, LocalTime.of(10, 30)));
        reservaSabado2.setHoraFin(LocalTime.of(11, 30));
        
        LocalDate segundoSabado = LocalDate.of(2024, 6, 8);
        Reserva reservaSegundoSabado = new Reserva();
        reservaSegundoSabado.setFechaReserva(LocalDateTime.of(segundoSabado, LocalTime.of(12, 0)));
        reservaSegundoSabado.setHoraFin(LocalTime.of(13, 0));
        
        LocalDateTime inicioMes = LocalDate.of(año, mes, 1).atStartOfDay();
        LocalDateTime finMes = LocalDate.of(año, mes, 30).atTime(23, 59, 59);
        when(reservaRepo.findReservasByEstablecimientoAndFecha(establecimiento, inicioMes, finMes))
            .thenReturn(Arrays.asList(reservaSabado1, reservaSabado2, reservaSegundoSabado));

        // Act
        Map<LocalDate, DisponibilidadDia> resultado = reservaService.obtenerDisponibilidadMensual(establecimiento, año, mes);

        // Assert - Verificar que las reservas se agrupan correctamente por fecha
        DisponibilidadDia disponibilidadPrimerSabado = resultado.get(primerSabado);
        assertNotNull(disponibilidadPrimerSabado);
        assertTrue(disponibilidadPrimerSabado.isTieneHorarioApertura());
        assertTrue(disponibilidadPrimerSabado.isTieneDisponibilidad()); // Debe tener algunos períodos disponibles
        
        DisponibilidadDia disponibilidadSegundoSabado = resultado.get(segundoSabado);
        assertNotNull(disponibilidadSegundoSabado);
        assertTrue(disponibilidadSegundoSabado.isTieneHorarioApertura());
        assertTrue(disponibilidadSegundoSabado.isTieneDisponibilidad()); // Con menos reservas, más disponibilidad
        
        verify(reservaRepo).findReservasByEstablecimientoAndFecha(establecimiento, inicioMes, finMes);
    }

    @Test
    void testCalcularDisponibilidadDiaOptimizada_MultiplesPeridosDisponibles() {
        // Arrange - Caso complejo con múltiples franjas
        int año = 2024;
        int mes = 7; // Julio
        
        FranjaHoraria franjaMañana = new FranjaHoraria();
        franjaMañana.setDiaSemana(DayOfWeek.SUNDAY);
        franjaMañana.setHoraInicio(LocalTime.of(8, 0));
        franjaMañana.setHoraFin(LocalTime.of(12, 0));
        
        FranjaHoraria franjaTarde = new FranjaHoraria();
        franjaTarde.setDiaSemana(DayOfWeek.SUNDAY);
        franjaTarde.setHoraInicio(LocalTime.of(14, 0));
        franjaTarde.setHoraFin(LocalTime.of(18, 0));
        
        establecimiento.setFranjasHorarias(Arrays.asList(franjaMañana, franjaTarde));
        establecimiento.setAforo(4);
        
        // Una reserva que no ocupa completamente ninguna franja
        LocalDate primerDomingo = LocalDate.of(2024, 7, 7);
        Reserva reservaDomingo = new Reserva();
        reservaDomingo.setFechaReserva(LocalDateTime.of(primerDomingo, LocalTime.of(10, 0)));
        reservaDomingo.setHoraFin(LocalTime.of(11, 0));
        
        LocalDateTime inicioMes = LocalDate.of(año, mes, 1).atStartOfDay();
        LocalDateTime finMes = LocalDate.of(año, mes, 31).atTime(23, 59, 59);
        when(reservaRepo.findReservasByEstablecimientoAndFecha(establecimiento, inicioMes, finMes))
            .thenReturn(Arrays.asList(reservaDomingo));

        // Act
        Map<LocalDate, DisponibilidadDia> resultado = reservaService.obtenerDisponibilidadMensual(establecimiento, año, mes);

        // Assert
        DisponibilidadDia disponibilidadDomingo = resultado.get(primerDomingo);
        assertNotNull(disponibilidadDomingo);
        assertTrue(disponibilidadDomingo.isTieneHorarioApertura());
        assertTrue(disponibilidadDomingo.isTieneDisponibilidad());
        assertEquals(2, disponibilidadDomingo.getFranjasDisponibles().size());
        
        // Debe tener múltiples períodos disponibles
        long totalPeriodos = disponibilidadDomingo.getFranjasDisponibles().stream()
            .mapToLong(franja -> franja.getPeriodosDisponibles().size())
            .sum();
        assertTrue(totalPeriodos > 1);
        assertTrue(disponibilidadDomingo.getResumen().contains("períodos disponibles"));
        
        verify(reservaRepo).findReservasByEstablecimientoAndFecha(establecimiento, inicioMes, finMes);
    }
}
