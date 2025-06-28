package es.ubu.reservapp.model.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.repositories.UsuarioRepo;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;


/**
 * Test para la clase SessionData
 * 
 * @author Ahmad Mareie Pascual
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
        SessionData sessionDataVacia = new SessionData();
        
        assertNotNull(sessionDataVacia);
    }

    @Test
    void testConstructorConParametros() {
        assertNotNull(sessionData);
    }

    @Test
    void registroUsuarioEntrada_SuccessfulCase() {
        // Given
        when(usuarioRepo.findById("test123")).thenReturn(Optional.of(usuario));
        when(usuarioRepo.save(any(Usuario.class))).thenReturn(usuario);

        // When
        sessionData.registroUsuarioEntrada("test123");

        // Then
        verify(usuarioRepo).findById("test123");
        assertEquals(usuario, sessionData.getUsuario());
    }

    @Test
    void registroUsuarioEntrada_UserNotFound() {
        // Given
        when(usuarioRepo.findById("nonexistent")).thenReturn(Optional.empty());

        // When
        sessionData.registroUsuarioEntrada("nonexistent");

        // Then
        verify(usuarioRepo).findById("nonexistent");
        verify(usuarioRepo, never()).save(any());
        assertNull(sessionData.getUsuario());
    }

    @Test
    void registroUsuarioEntrada_FindByIdThrowsException() {
        // Given
        when(usuarioRepo.findById("test123")).thenThrow(new RuntimeException("Database error"));

        // When
        sessionData.registroUsuarioEntrada("test123");

        // Then
        verify(usuarioRepo).findById("test123");
        verify(usuarioRepo, never()).save(any());
        assertNull(sessionData.getUsuario());
    }

    @Test
    void registroUsuarioEntrada_SaveThrowsConstraintViolationException() {
        // Given
        when(usuarioRepo.findById("test123")).thenReturn(Optional.of(usuario));
        Set<ConstraintViolation<Usuario>> violations = new HashSet<>();
        ConstraintViolationException exception = new ConstraintViolationException("Validation failed", violations);
        when(usuarioRepo.save(any(Usuario.class))).thenThrow(exception);

        // When
        sessionData.registroUsuarioEntrada("test123");

        // Then
        verify(usuarioRepo).findById("test123");
        verify(usuarioRepo).save(any());
        assertEquals(usuario, sessionData.getUsuario());
    }

    @Test
    void registroUsuarioEntrada_SaveThrowsGenericException() {
        // Given
        when(usuarioRepo.findById("test123")).thenReturn(Optional.of(usuario));
        when(usuarioRepo.save(any(Usuario.class))).thenThrow(new RuntimeException("Save error"));

        // When
        sessionData.registroUsuarioEntrada("test123");

        // Then
        verify(usuarioRepo).findById("test123");
        verify(usuarioRepo).save(any());
        assertEquals(usuario, sessionData.getUsuario());
    }

    @Test
    void testConstructors() {
        // Test no-args constructor
        SessionData emptySessionData = new SessionData();
        assertNotNull(emptySessionData);

        // Test constructor with UsuarioRepo
        SessionData repoSessionData = new SessionData(usuarioRepo);
        assertNotNull(repoSessionData);
    }

    @Test
    void testGettersAndSetters() {
        // Test usuario getter/setter
        sessionData.setUsuario(usuario);
        assertEquals(usuario, sessionData.getUsuario());

        // Test lstEstablecimientoUsuario getter/setter
        List<Establecimiento> establecimientos = new ArrayList<>();
        sessionData.setLstEstablecimientoUsuario(establecimientos);
        assertEquals(establecimientos, sessionData.getLstEstablecimientoUsuario());

        // Test lstRolesUsuario getter/setter
        List<Integer> roles = new ArrayList<>();
        sessionData.setLstRolesUsuario(roles);
        assertEquals(roles, sessionData.getLstRolesUsuario());
    }
    
}