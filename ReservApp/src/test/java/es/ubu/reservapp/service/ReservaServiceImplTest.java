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
import es.ubu.reservapp.service.ReservaService.FranjaDisponibilidad;
import es.ubu.reservapp.model.repositories.ReservaRepo;
import es.ubu.reservapp.model.repositories.UsuarioRepo;

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
        Establecimiento establecimiento = new Establecimiento();
        establecimiento.setId(1);
        establecimiento.setAforo(2); // Aforo limitado
        
        FranjaHoraria franja = new FranjaHoraria();
        franja.setHoraInicio(LocalTime.of(10, 0));
        franja.setHoraFin(LocalTime.of(12, 0));
        franja.setDiaSemana(DayOfWeek.MONDAY);
        establecimiento.setFranjasHorarias(List.of(franja));
        
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
            eq(establecimiento), 
            any(LocalDateTime.class), 
            any(LocalDateTime.class)
        )).thenReturn(reservasDelDia);
        
        // Act
        List<FranjaDisponibilidad> resultado = reservaService.obtenerFranjasDisponibles(establecimiento, fecha);
        
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
        Establecimiento establecimiento = new Establecimiento();
        establecimiento.setId(1);
        establecimiento.setAforo(1);
        
        FranjaHoraria franja = new FranjaHoraria();
        franja.setHoraInicio(LocalTime.of(9, 0));
        franja.setHoraFin(LocalTime.of(11, 0));
        franja.setDiaSemana(DayOfWeek.MONDAY);
        establecimiento.setFranjasHorarias(List.of(franja));
        
        LocalDate fecha = LocalDate.of(2024, 1, 15);
        
        // Reserva sin hora de fin (usará hora por defecto +1 hora)
        Reserva reservaSinHoraFin = new Reserva();
        reservaSinHoraFin.setFechaReserva(LocalDateTime.of(fecha, LocalTime.of(9, 30)));
        reservaSinHoraFin.setHoraFin(null); // Sin hora de fin
        
        List<Reserva> reservasDelDia = List.of(reservaSinHoraFin);
        
        when(reservaRepo.findReservasByEstablecimientoAndFecha(
            eq(establecimiento), 
            any(LocalDateTime.class), 
            any(LocalDateTime.class)
        )).thenReturn(reservasDelDia);
        
        // Act
        List<FranjaDisponibilidad> resultado = reservaService.obtenerFranjasDisponibles(establecimiento, fecha);
        
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
        Establecimiento establecimiento = new Establecimiento();
        establecimiento.setId(1);
        establecimiento.setAforo(2);
        
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
            eq(establecimiento), 
            eq(LocalDateTime.of(fecha, horaInicio)), 
            eq(LocalDateTime.of(fecha, horaFin))
        )).thenReturn(reservasDelDia);
        
        // Act
        boolean disponible = reservaService.verificarDisponibilidad(
            establecimiento, fecha, horaInicio, horaFin, null
        );
        
        // Assert
        assertFalse(disponible); // El código considera solapamiento en los bordes exactos
    }
    
    @Test
    void testVerificarDisponibilidad_ConMultiplesReservasSolapadas() {
        // Arrange - Test para ejercitar contarReservasEnIntervalo
        Establecimiento establecimiento = new Establecimiento();
        establecimiento.setId(1);
        establecimiento.setAforo(3);
        
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
            eq(establecimiento), 
            eq(LocalDateTime.of(fecha, horaInicio)), 
            eq(LocalDateTime.of(fecha, horaFin))
        )).thenReturn(reservasDelDia);
        
        // Act
        boolean disponible = reservaService.verificarDisponibilidad(
            establecimiento, fecha, horaInicio, horaFin, null
        );
        
        // Assert
        assertFalse(disponible); // Las 3 reservas se solapan en algunos momentos, superando el aforo
    }
    
    @Test
    void testObtenerFranjasDisponibles_ConPuntosDeTimeComplejos() {
        // Arrange - Test para ejercitar obtenerPuntosDeTiempoOrdenados y agregarPuntoSiEstaEnFranja
        Establecimiento establecimiento = new Establecimiento();
        establecimiento.setId(1);
        establecimiento.setAforo(1);
        
        FranjaHoraria franja = new FranjaHoraria();
        franja.setHoraInicio(LocalTime.of(10, 0));
        franja.setHoraFin(LocalTime.of(14, 0));
        franja.setDiaSemana(DayOfWeek.MONDAY);
        establecimiento.setFranjasHorarias(List.of(franja));
        
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
            eq(establecimiento), 
            any(LocalDateTime.class), 
            any(LocalDateTime.class)
        )).thenReturn(reservasDelDia);
        
        // Act
        List<FranjaDisponibilidad> resultado = reservaService.obtenerFranjasDisponibles(establecimiento, fecha);
        
        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        
        FranjaDisponibilidad franjaDisp = resultado.get(0);
        // Con las reservas complejas configuradas, no hay períodos disponibles
        assertTrue(franjaDisp.getPeriodosDisponibles().isEmpty());
    }
}