package es.ubu.reservapp.model.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.repositories.UsuarioRepo;
import jakarta.validation.ConstraintViolationException;

/**
 * Test para la clase SessionData
 * 
 * @author Test Generator
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class SessionDataTest {

    @Mock
    private UsuarioRepo usuarioRepo;

    private SessionData sessionData;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId("user1");
        usuario.setNombre("Usuario Test");
        usuario.setApellidos("Apellidos Test");
        usuario.setCorreo("test@example.com");
        
        sessionData = new SessionData(usuarioRepo);
    }

    @Test
    void testConstructorVacio() {
        // Arrange & Act
        SessionData sessionDataVacia = new SessionData();
        
        // Assert
        assertNotNull(sessionDataVacia);
    }

    @Test
    void testConstructorConParametros() {
        // Arrange & Act & Assert
        assertNotNull(sessionData);
    }

    @Test
    void testGettersAndSetters() {
        // Arrange
        List<Establecimiento> establecimientos = new ArrayList<>();
        Establecimiento establecimiento = new Establecimiento();
        establecimiento.setId(1);
        establecimiento.setNombre("Establecimiento Test");
        establecimientos.add(establecimiento);
        
        List<Integer> roles = new ArrayList<>();
        roles.add(1);
        roles.add(2);
        
        // Act
        sessionData.setUsuario(usuario);
        sessionData.setLstEstablecimientoUsuario(establecimientos);
        sessionData.setLstRolesUsuario(roles);
        
        // Assert
        assertEquals(usuario, sessionData.getUsuario());
        assertEquals(establecimientos, sessionData.getLstEstablecimientoUsuario());
        assertEquals(roles, sessionData.getLstRolesUsuario());
    }

    @Test
    void testRegistroUsuarioEntradaExitoso() {
        // Arrange
        when(usuarioRepo.findById("user1")).thenReturn(Optional.of(usuario));
        
        // Act
        sessionData.registroUsuarioEntrada("user1");
        
        // Assert
        assertEquals(usuario, sessionData.getUsuario());
        assertNotNull(usuario.getFechaUltimoAcceso());
        verify(usuarioRepo, times(1)).save(usuario);
    }

    @Test
    void testRegistroUsuarioEntradaUsuarioNoEncontrado() {
        // Arrange
        when(usuarioRepo.findById("userNoExiste")).thenReturn(Optional.empty());
        
        // Act
        sessionData.registroUsuarioEntrada("userNoExiste");
        
        // Assert
        assertEquals(null, sessionData.getUsuario());
        verify(usuarioRepo, times(0)).save(any(Usuario.class));
    }

    @Test
    void testRegistroUsuarioEntradaConExcepcion() {
        // Arrange
        when(usuarioRepo.findById("user1")).thenReturn(Optional.of(usuario));
        when(usuarioRepo.save(usuario)).thenThrow(new RuntimeException("Error al guardar"));
        
        // Act - No debería lanzar excepción, se maneja internamente
        sessionData.registroUsuarioEntrada("user1");
        
        // Assert
        assertEquals(usuario, sessionData.getUsuario());
        assertNotNull(usuario.getFechaUltimoAcceso());
    }

    @Test
    void testRegistroUsuarioEntradaConConstraintViolationException() {
        // Arrange
        ConstraintViolationException mockException = mock(ConstraintViolationException.class);
        when(usuarioRepo.findById("user1")).thenReturn(Optional.of(usuario));
        when(usuarioRepo.save(usuario)).thenThrow(mockException);
        
        // Act - No debería lanzar excepción, se maneja internamente
        sessionData.registroUsuarioEntrada("user1");
        
        // Assert
        assertEquals(usuario, sessionData.getUsuario());
        assertNotNull(usuario.getFechaUltimoAcceso());
    }
}