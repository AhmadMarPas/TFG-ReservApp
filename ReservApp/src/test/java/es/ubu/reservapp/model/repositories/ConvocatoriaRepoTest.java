package es.ubu.reservapp.model.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import es.ubu.reservapp.ReservApplication;
import es.ubu.reservapp.model.entities.Convocatoria;
import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.UsuarioService;

/**
 * Tests unitarios para ConvocatoriaRepo.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
class ConvocatoriaRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ConvocatoriaRepo convocatoriaRepo;

    @MockitoBean
    private ReservApplication reservApplication;
    
    @MockitoBean
    private SessionData sessionData;
    
    @MockitoBean
    private UsuarioService usuarioService;
    
    @MockitoBean
    private JavaMailSender mailSender;
    
    private Reserva reserva;
    private Convocatoria convocatoria;

    @BeforeEach
    void setUp() {
        // Mock de usuario
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId("test");
        usuarioMock.setNombre("Test User");
        usuarioMock.setApellidos("Test Apellidos");
        usuarioMock.setTelefono("123456789");
        usuarioMock.setPassword("password");
        usuarioMock.setCorreo("test@test.com");

        when(sessionData.getUsuario()).thenReturn(usuarioMock);
        
        // Persist usuario
        usuarioMock = entityManager.persistAndFlush(usuarioMock);
        
        // Create and persist Establecimiento
        Establecimiento establecimiento = new Establecimiento();
        establecimiento.setNombre("Test Establecimiento");
        establecimiento.setDireccion("Test Direccion");
        establecimiento.setDescripcion("Test Descripcion");
        establecimiento.setCapacidad(10);
        establecimiento.setActivo(true);
        establecimiento.setAforo(30);
        establecimiento = entityManager.persistAndFlush(establecimiento);
        
        // Create and persist Reserva
        reserva = new Reserva();
        reserva.setFechaReserva(LocalDateTime.now().plusDays(1));
        reserva.setUsuario(usuarioMock);
        reserva.setEstablecimiento(establecimiento);
        reserva = entityManager.persistAndFlush(reserva);

        // Create and persist Convocatoria
        convocatoria = new Convocatoria();
        convocatoria.setReserva(reserva);
        convocatoria.setEnlace("http://test.com");
        convocatoria.setObservaciones("Test convocatoria");
        convocatoria.setConvocados(new ArrayList<>());
        convocatoria = entityManager.persistAndFlush(convocatoria);
    }

    @Test
    void testFindConvocatoriaByReserva() {
        // When
        List<Convocatoria> result = convocatoriaRepo.findConvocatoriaByReserva(reserva);

        // Then
        assertThat(result).isNotEmpty().hasSize(1);
        assertThat(result.get(0).getReserva().getId()).isEqualTo(reserva.getId());
    }

    @Test
    void testFindConvocatoriaByReservaNotFound() {
        // Given - crear una reserva sin convocatoria asociada
        Usuario usuario2 = new Usuario();
        usuario2.setId("test2");
        usuario2.setNombre("Test User 2");
        usuario2.setCorreo("test2@test.com");
        usuario2.setApellidos("Test Apellidos");
        usuario2.setTelefono("123456789");
        usuario2.setPassword("password");
        usuario2 = entityManager.persistAndFlush(usuario2);
        
        Establecimiento establecimiento2 = new Establecimiento();
        establecimiento2.setNombre("Test Establecimiento 2");
        establecimiento2.setDireccion("Test Direccion 2");
        establecimiento2.setDescripcion("Test Descripcion");
        establecimiento2.setAforo(30);
        establecimiento2.setCapacidad(5);
        establecimiento2.setActivo(true);
        establecimiento2 = entityManager.persistAndFlush(establecimiento2);
        
        Reserva reservaSinConvocatoria = new Reserva();
        reservaSinConvocatoria.setFechaReserva(LocalDateTime.now().plusDays(2));
        reservaSinConvocatoria.setUsuario(usuario2);
        reservaSinConvocatoria.setEstablecimiento(establecimiento2);
        reservaSinConvocatoria = entityManager.persistAndFlush(reservaSinConvocatoria);

        // When
        List<Convocatoria> result = convocatoriaRepo.findConvocatoriaByReserva(reservaSinConvocatoria);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testDeleteByReserva() {
        // Given - verificar que existe la convocatoria
        assertThat(convocatoriaRepo.findConvocatoriaByReserva(reserva)).isNotEmpty();

        // When
        convocatoriaRepo.deleteByReserva(reserva);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(convocatoriaRepo.findConvocatoriaByReserva(reserva)).isEmpty();
    }

    @Test
    void testFindByIdReservaIgnoringValido() {
        // When
        Optional<Convocatoria> result = convocatoriaRepo.findByIdReservaIgnoringValido(reserva.getId());

        // Then
        assertTrue(result.isPresent());
        assertThat(result.get().getReserva().getId()).isEqualTo(reserva.getId());
    }

    @Test
    void testFindByIdReservaIgnoringValidoNotFound() {
        // Given
        Integer nonExistentId = 99999;

        // When
        Optional<Convocatoria> result = convocatoriaRepo.findByIdReservaIgnoringValido(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testSave() {
        // Given
        Convocatoria newConvocatoria = new Convocatoria();
        newConvocatoria.setReserva(reserva);
        newConvocatoria.setEnlace("http://nueva-convocatoria.com");
        newConvocatoria.setObservaciones("Nueva convocatoria de prueba");
        newConvocatoria.setConvocados(new ArrayList<>());

        // When
        Convocatoria saved = convocatoriaRepo.save(newConvocatoria);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEnlace()).isEqualTo("http://nueva-convocatoria.com");
        assertThat(saved.getObservaciones()).isEqualTo("Nueva convocatoria de prueba");
    }

    @Test
    void testFindById() {
        // When
        Optional<Convocatoria> found = convocatoriaRepo.findById(convocatoria.getId());

        // Then
        assertTrue(found.isPresent());
        assertThat(found.get().getId()).isEqualTo(convocatoria.getId());
        assertThat(found.get().getReserva().getId()).isEqualTo(reserva.getId());
    }

    @Test
    void testFindByIdNotFound() {
        // Given
        Integer nonExistentId = 99999;

        // When
        Optional<Convocatoria> found = convocatoriaRepo.findById(nonExistentId);

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        // When
        List<Convocatoria> all = convocatoriaRepo.findAll();

        // Then
        assertThat(all).isNotEmpty().hasSizeGreaterThanOrEqualTo(1).anyMatch(c -> c.getId().equals(convocatoria.getId()));
    }

    @Test
    void testDelete() {
        // Given
        Integer convocatoriaId = convocatoria.getId();
        assertTrue(convocatoriaRepo.findById(convocatoriaId).isPresent());

        // When
        convocatoriaRepo.delete(convocatoria);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Convocatoria> deleted = convocatoriaRepo.findById(convocatoriaId);
        assertFalse(deleted.isPresent());
    }

    @Test
    void testDeleteById() {
        // Given
        Integer convocatoriaId = convocatoria.getId();
        assertTrue(convocatoriaRepo.findById(convocatoriaId).isPresent());

        // When
        convocatoriaRepo.deleteById(convocatoriaId);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Convocatoria> deleted = convocatoriaRepo.findById(convocatoriaId);
        assertFalse(deleted.isPresent());
    }

    @Test
    void testExistsById() {
        // When & Then
        assertTrue(convocatoriaRepo.existsById(convocatoria.getId()));
        assertFalse(convocatoriaRepo.existsById(99999));
    }

    @Test
    void testCount() {
        // When
        long count = convocatoriaRepo.count();

        // Then
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    void testSaveAll() {
        // Given
        Convocatoria conv1 = new Convocatoria();
        conv1.setReserva(reserva);
        conv1.setEnlace("http://conv1.com");
        conv1.setObservaciones("Convocatoria 1");
        conv1.setConvocados(new ArrayList<>());
        
        Convocatoria conv2 = new Convocatoria();
        conv2.setReserva(reserva);
        conv2.setEnlace("http://conv2.com");
        conv2.setObservaciones("Convocatoria 2");
        conv2.setConvocados(new ArrayList<>());
        
        List<Convocatoria> convocatorias = List.of(conv1, conv2);

        // When
        List<Convocatoria> saved = convocatoriaRepo.saveAll(convocatorias);

        // Then
        assertThat(saved).hasSize(2).allMatch(c -> c.getId() != null);
    }

    @Test
    void testDeleteAll() {
        // Given
        long initialCount = convocatoriaRepo.count();
        assertThat(initialCount).isGreaterThan(0);

        // When
        convocatoriaRepo.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(convocatoriaRepo.count()).isZero();
    }

    @Test
    void testDeleteAllById() {
        // Given
        Convocatoria conv1 = new Convocatoria();
        conv1.setReserva(reserva);
        conv1.setEnlace("http://conv1.com");
        conv1.setObservaciones("Convocatoria para eliminar 1");
        conv1.setConvocados(new ArrayList<>());
        conv1 = convocatoriaRepo.save(conv1);
        
        Convocatoria conv2 = new Convocatoria();
        conv2.setReserva(reserva);
        conv2.setEnlace("http://conv2.com");
        conv2.setObservaciones("Convocatoria para eliminar 2");
        conv2.setConvocados(new ArrayList<>());
        conv2 = convocatoriaRepo.save(conv2);
        
        List<Integer> idsToDelete = List.of(conv1.getId(), conv2.getId());

        // When
        convocatoriaRepo.deleteAllById(idsToDelete);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertFalse(convocatoriaRepo.existsById(conv1.getId()));
        assertFalse(convocatoriaRepo.existsById(conv2.getId()));
    }

    @Test
    @Disabled("Para revisar")
    void testFindAllById() {
        // Given
        Convocatoria conv1 = new Convocatoria();
        Reserva reserva2 = new Reserva();
        reserva2.setId(2);
        reserva2.setConvocatoria(conv1);
        conv1.setReserva(reserva2);
        conv1.setEnlace("http://conv1.com");
        conv1.setObservaciones("Convocatoria 1");
        conv1.setConvocados(new ArrayList<>());
        conv1 = convocatoriaRepo.save(conv1);
        
        List<Integer> ids = List.of(convocatoria.getId(), conv1.getId());

        // When
        List<Convocatoria> found = convocatoriaRepo.findAllById(ids);

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Convocatoria::getId).containsExactlyInAnyOrder(convocatoria.getId(), conv1.getId());
    }
}
