package es.ubu.reservapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import es.ubu.reservapp.model.entities.Convocatoria;
import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;
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

    @InjectMocks
    private ReservaServiceImpl reservaService;

    private Reserva reserva;
    private Usuario usuario;
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
        reserva.setConvocatorias(new ArrayList<>());
    }

    @Test
    void testCrearReservaConConvocatorias_ConUsuariosConvocados_Success() throws UserNotFoundException {
        // Arrange
        List<String> idUsuariosConvocados = Arrays.asList("convocado1", "convocado2");
        String enlaceReunion = "https://meet.google.com/abc-def-ghi";
        String observaciones = "Reunión importante";
        reserva.setEnlace(enlaceReunion);
        reserva.setObservaciones(observaciones);

        when(reservaRepo.save(any(Reserva.class))).thenReturn(reserva);
        when(usuarioRepo.findById("convocado1")).thenReturn(Optional.of(usuarioConvocado1));
        when(usuarioRepo.findById("convocado2")).thenReturn(Optional.of(usuarioConvocado2));
        when(convocatoriaService.save(any(Convocatoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Reserva resultado = reservaService.crearReservaConConvocatorias(reserva, usuario, idUsuariosConvocados);

        // Assert
        assertNotNull(resultado);
        assertEquals(usuario, resultado.getUsuario());
        assertEquals(2, resultado.getConvocatorias().size());
        verify(reservaRepo).save(reserva);
        verify(usuarioRepo).findById("convocado1");
        verify(usuarioRepo).findById("convocado2");
        verify(convocatoriaService, times(2)).save(any(Convocatoria.class));
    }

    @Test
    void testCrearReservaConConvocatorias_SinUsuariosConvocados_Success() throws UserNotFoundException {
        // Arrange
        List<String> idUsuariosConvocados = new ArrayList<>();
        String enlaceReunion = "https://meet.google.com/abc-def-ghi";
        String observaciones = "Sin convocados";
        reserva.setEnlace(enlaceReunion);
        reserva.setObservaciones(observaciones);

        when(reservaRepo.save(any(Reserva.class))).thenReturn(reserva);

        // Act
        Reserva resultado = reservaService.crearReservaConConvocatorias(reserva, usuario, idUsuariosConvocados);

        // Assert
        assertNotNull(resultado);
        assertEquals(usuario, resultado.getUsuario());
        assertEquals(0, resultado.getConvocatorias().size());
        verify(reservaRepo).save(reserva);
        verify(usuarioRepo, never()).findById(anyString());
        verify(convocatoriaService, never()).save(any(Convocatoria.class));
    }

    @Test
    void testCrearReservaConConvocatorias_ListaUsuariosNull_Success() throws UserNotFoundException {
        // Arrange
        List<String> idUsuariosConvocados = null;
        String enlaceReunion = "https://meet.google.com/abc-def-ghi";
        String observaciones = "Lista null";
        reserva.setEnlace(enlaceReunion);
        reserva.setObservaciones(observaciones);

        when(reservaRepo.save(any(Reserva.class))).thenReturn(reserva);

        // Act
        Reserva resultado = reservaService.crearReservaConConvocatorias(reserva, usuario, idUsuariosConvocados);

        // Assert
        assertNotNull(resultado);
        assertEquals(usuario, resultado.getUsuario());
        assertEquals(0, resultado.getConvocatorias().size());
        verify(reservaRepo).save(reserva);
        verify(usuarioRepo, never()).findById(anyString());
        verify(convocatoriaService, never()).save(any(Convocatoria.class));
    }

    @Test
    void testCrearReservaConConvocatorias_UsuarioNoEncontrado_ThrowsException() {
        // Arrange
        List<String> idUsuariosConvocados = Arrays.asList("usuarioInexistente");
        String enlaceReunion = "https://meet.google.com/abc-def-ghi";
        String observaciones = "Usuario no existe";
        reserva.setEnlace(enlaceReunion);
        reserva.setObservaciones(observaciones);

        when(reservaRepo.save(any(Reserva.class))).thenReturn(reserva);
        when(usuarioRepo.findById("usuarioInexistente")).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            reservaService.crearReservaConConvocatorias(reserva, usuario, idUsuariosConvocados);
        });

        assertEquals("El Usuario con ID usuarioInexistente no fue encontrado.", exception.getMessage());
        verify(reservaRepo).save(reserva);
        verify(usuarioRepo).findById("usuarioInexistente");
        verify(convocatoriaService, never()).save(any(Convocatoria.class));
    }

    @Test
    void testCrearReservaConConvocatorias_AlgunosUsuariosNoEncontrados_ThrowsException() {
        // Arrange
        List<String> idUsuariosConvocados = Arrays.asList("convocado1", "usuarioInexistente");
        String enlaceReunion = "https://meet.google.com/abc-def-ghi";
        String observaciones = "Algunos usuarios no existen";
        reserva.setEnlace(enlaceReunion);
        reserva.setObservaciones(observaciones);

        when(reservaRepo.save(any(Reserva.class))).thenReturn(reserva);
        when(usuarioRepo.findById("convocado1")).thenReturn(Optional.of(usuarioConvocado1));
        when(usuarioRepo.findById("usuarioInexistente")).thenReturn(Optional.empty());
        when(convocatoriaService.save(any(Convocatoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            reservaService.crearReservaConConvocatorias(reserva, usuario, idUsuariosConvocados);
        });

        assertEquals("El Usuario con ID usuarioInexistente no fue encontrado.", exception.getMessage());
        verify(reservaRepo).save(reserva);
        verify(usuarioRepo).findById("convocado1");
        verify(usuarioRepo).findById("usuarioInexistente");
        verify(convocatoriaService, times(1)).save(any(Convocatoria.class));
    }

    @Test
    void testCrearReservaConConvocatorias_ConEnlaceYObservacionesNull() throws UserNotFoundException {
        // Arrange
        List<String> idUsuariosConvocados = Arrays.asList("convocado1");
        String enlaceReunion = null;
        String observaciones = null;
        reserva.setEnlace(enlaceReunion);
        reserva.setObservaciones(observaciones);

        when(reservaRepo.save(any(Reserva.class))).thenReturn(reserva);
        when(usuarioRepo.findById("convocado1")).thenReturn(Optional.of(usuarioConvocado1));
        when(convocatoriaService.save(any(Convocatoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Reserva resultado = reservaService.crearReservaConConvocatorias(reserva, usuario, idUsuariosConvocados);

        // Assert
        assertNotNull(resultado);
        assertEquals(usuario, resultado.getUsuario());
        assertEquals(1, resultado.getConvocatorias().size());
        verify(reservaRepo).save(reserva);
        verify(usuarioRepo).findById("convocado1");
        verify(convocatoriaService).save(any(Convocatoria.class));
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
        when(reservaRepo.findById(id)).thenReturn(Optional.of(reservaTest));

        // Act
        Optional<Reserva> resultado = reservaService.findById(id);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isPresent());
        assertEquals(reservaTest, resultado.get());

        // Verify
        verify(reservaRepo).findById(id);
    }
}