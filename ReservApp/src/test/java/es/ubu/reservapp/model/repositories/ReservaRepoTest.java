package es.ubu.reservapp.model.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import es.ubu.reservapp.ReservApplication;
import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.UsuarioService;

/**
 * Tests unitarios para ReservaRepo.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
class ReservaRepoTest {

    @Autowired
    private ReservaRepo reservaRepo;
    
    @Autowired
    private UsuarioRepo usuarioRepo;
    
    @Autowired
    private EstablecimientoRepo establecimientoRepo;

    @MockitoBean
    private ReservApplication reservApplication;
    
    @MockitoBean
    private SessionData sessionData;
    
    @MockitoBean
    private UsuarioService usuarioService;
    
    @MockitoBean
    private JavaMailSender mailSender;

    private Usuario usuario;
    private Establecimiento establecimiento;
    private LocalDateTime fechaActual;
    private LocalDateTime fechaPasada;
    private LocalDateTime fechaFutura;

    @BeforeEach
    void setUp() {
        // Mock de usuario
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId("test");

        when(sessionData.getUsuario()).thenReturn(usuarioMock);
        
        // Crear usuario de prueba
        usuario = new Usuario();
        usuario.setId("testuser");
        usuario.setPassword("password");
        usuario.setCorreo("test@example.com");
        usuario.setNombre("Test");
        usuario.setApellidos("User");
        usuario.setTelefono("123456789");
        usuario = usuarioRepo.save(usuario);

        // Crear establecimiento de prueba
        establecimiento = new Establecimiento();
        establecimiento.setNombre("Restaurante Test");
        establecimiento.setDireccion("Calle Test 123");
        establecimiento.setTelefono("987654321");
        establecimiento.setCapacidad(50);
        establecimiento.setAforo(5);
        establecimiento.setDescripcion("Descripción test");
        establecimiento = establecimientoRepo.save(establecimiento);

        // Configurar fechas
        fechaActual = LocalDateTime.now().withNano(0);
        fechaPasada = fechaActual.minusDays(1);
        fechaFutura = fechaActual.plusDays(1);
    }

    @Test
    void testSaveAndFindById() {
        // Given
        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setEstablecimiento(establecimiento);
        reserva.setFechaReserva(fechaFutura);
        
        // When
        Reserva savedReserva = reservaRepo.save(reserva);
        
        // Then
        assertThat(savedReserva.getId()).isNotNull();
        
        Reserva foundReserva = reservaRepo.findById(savedReserva.getId()).orElse(null);
        assertThat(foundReserva).isNotNull();
        assertThat(foundReserva.getUsuario().getId()).isEqualTo("testuser");
        assertThat(foundReserva.getEstablecimiento().getId()).isEqualTo(establecimiento.getId());
    }

    @Test
    void testFindByUsuarioAndEstablecimientoAndFechaReservaBefore() {
        // Given
        Reserva reservaPasada = new Reserva();
        reservaPasada.setUsuario(usuario);
        reservaPasada.setEstablecimiento(establecimiento);
        reservaPasada.setFechaReserva(fechaPasada);
        
        Reserva reservaFutura = new Reserva();
        reservaFutura.setUsuario(usuario);
        reservaFutura.setEstablecimiento(establecimiento);
        reservaFutura.setFechaReserva(fechaFutura);
        
        reservaRepo.save(reservaPasada);
        reservaRepo.save(reservaFutura);
        
        // When
        List<Reserva> reservasPasadas = reservaRepo.findByUsuarioAndEstablecimientoAndFechaReservaBefore(
            usuario, establecimiento, fechaActual);
        
        // Then
        assertThat(reservasPasadas).hasSize(1);
        assertThat(reservasPasadas.get(0).getFechaReserva()).isBefore(fechaActual);
    }

    @Test
    void testFindByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual() {
        // Given
        Reserva reservaPasada = new Reserva();
        reservaPasada.setUsuario(usuario);
        reservaPasada.setEstablecimiento(establecimiento);
        reservaPasada.setFechaReserva(fechaPasada);
        
        Reserva reservaFutura = new Reserva();
        reservaFutura.setUsuario(usuario);
        reservaFutura.setEstablecimiento(establecimiento);
        reservaFutura.setFechaReserva(fechaFutura);
        
        Reserva reservaActual = new Reserva();
        reservaActual.setUsuario(usuario);
        reservaActual.setEstablecimiento(establecimiento);
        reservaActual.setFechaReserva(fechaActual);
        
        reservaRepo.save(reservaPasada);
        reservaRepo.save(reservaFutura);
        reservaRepo.save(reservaActual);
        
        // When
        List<Reserva> reservasFuturas = reservaRepo.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(
            usuario, establecimiento, fechaActual);
        
        // Then
        assertThat(reservasFuturas).hasSize(2);
    }

    @Test
    void testFindByEstablecimientoAndFechaReservaBetween() {
        // Given
        LocalDateTime fechaInicio = fechaActual.minusHours(2);
        LocalDateTime fechaFin = fechaActual.plusHours(2);
        
        Reserva reservaDentroRango1 = new Reserva();
        reservaDentroRango1.setUsuario(usuario);
        reservaDentroRango1.setEstablecimiento(establecimiento);
        reservaDentroRango1.setFechaReserva(fechaActual.minusHours(1));
        
        Reserva reservaDentroRango2 = new Reserva();
        reservaDentroRango2.setUsuario(usuario);
        reservaDentroRango2.setEstablecimiento(establecimiento);
        reservaDentroRango2.setFechaReserva(fechaActual.plusHours(1));
        
        Reserva reservaFueraRango = new Reserva();
        reservaFueraRango.setUsuario(usuario);
        reservaFueraRango.setEstablecimiento(establecimiento);
        reservaFueraRango.setFechaReserva(fechaActual.plusHours(3));
        
        reservaRepo.save(reservaDentroRango1);
        reservaRepo.save(reservaDentroRango2);
        reservaRepo.save(reservaFueraRango);
        
        // When
        List<Reserva> reservasEnRango = reservaRepo.findByEstablecimientoAndFechaReservaBetween(
            establecimiento, fechaInicio, fechaFin);
        
        // Then
        assertThat(reservasEnRango).hasSize(2);
    }

    @Test
    void testFindByEstablecimientoAndFechaReservaBetween_EmptyResult() {
        // Given
        LocalDateTime fechaInicio = fechaActual.plusDays(10);
        LocalDateTime fechaFin = fechaActual.plusDays(11);
        
        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setEstablecimiento(establecimiento);
        reserva.setFechaReserva(fechaActual);
        
        reservaRepo.save(reserva);
        
        // When
        List<Reserva> reservasEnRango = reservaRepo.findByEstablecimientoAndFechaReservaBetween(establecimiento, fechaInicio, fechaFin);
        
        // Then
        assertThat(reservasEnRango).isEmpty();
    }

    @Test
    void testFindAll() {
        // Given
        Reserva reserva1 = new Reserva();
        reserva1.setUsuario(usuario);
        reserva1.setEstablecimiento(establecimiento);
        reserva1.setFechaReserva(fechaFutura);
        
        Reserva reserva2 = new Reserva();
        reserva2.setUsuario(usuario);
        reserva2.setEstablecimiento(establecimiento);
        reserva2.setFechaReserva(fechaFutura.plusHours(1));
        
        reservaRepo.save(reserva1);
        reservaRepo.save(reserva2);
        
        // When
        var reservas = reservaRepo.findAll();
        
        // Then
        assertThat(reservas).hasSize(2);
    }

    @Test
    void testDeleteById() {
        // Given
        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setEstablecimiento(establecimiento);
        reserva.setFechaReserva(fechaFutura);
        
        Reserva savedReserva = reservaRepo.save(reserva);
        
        // When
        reservaRepo.deleteById(savedReserva.getId());
        
        // Then
        assertThat(reservaRepo.findById(savedReserva.getId())).isEmpty();
    }

    @Test
    void testExistsById() {
        // Given
        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setEstablecimiento(establecimiento);
        reserva.setFechaReserva(fechaFutura);
        
        Reserva savedReserva = reservaRepo.save(reserva);
        
        // When & Then
        assertThat(reservaRepo.existsById(savedReserva.getId())).isTrue();
        assertThat(reservaRepo.existsById(999)).isFalse();
    }

    @Test
    void testQueryMethodsWithDifferentUsers() {
        // Given
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId("otrouser");
        otroUsuario.setPassword("password");
        otroUsuario.setCorreo("otro@example.com");
        otroUsuario.setNombre("Otro");
        otroUsuario.setApellidos("Usuario");
        otroUsuario.setTelefono("987654321");
        otroUsuario = usuarioRepo.save(otroUsuario);
        
        Reserva reservaUsuario1 = new Reserva();
        reservaUsuario1.setUsuario(usuario);
        reservaUsuario1.setEstablecimiento(establecimiento);
        reservaUsuario1.setFechaReserva(fechaFutura);
        
        Reserva reservaUsuario2 = new Reserva();
        reservaUsuario2.setUsuario(otroUsuario);
        reservaUsuario2.setEstablecimiento(establecimiento);
        reservaUsuario2.setFechaReserva(fechaFutura);
        
        reservaRepo.save(reservaUsuario1);
        reservaRepo.save(reservaUsuario2);
        
        // When
        List<Reserva> reservasUsuario1 = reservaRepo.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(
            usuario, establecimiento, fechaActual);
        List<Reserva> reservasUsuario2 = reservaRepo.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(
            otroUsuario, establecimiento, fechaActual);
        
        // Then
        assertThat(reservasUsuario1).hasSize(1);
        
        assertThat(reservasUsuario2).hasSize(1);
    }
}