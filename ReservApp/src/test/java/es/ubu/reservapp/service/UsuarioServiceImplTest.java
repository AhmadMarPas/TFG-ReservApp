package es.ubu.reservapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.ubu.reservapp.exception.UserNotFoundException;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.repositories.UsuarioRepo;

/**
 * * Test para la clase UsuarioServiceImpl.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepo usuarioRepo;

    private UsuarioServiceImpl usuarioService;
    private Usuario usuario;
    private static final String ENCRYPTED_PASSWORD = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG";

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioServiceImpl(usuarioRepo);
        usuario = new Usuario();
        usuario.setId("test123");
        usuario.setCorreo("test@test.com");
        usuario.setPassword("password");
    }

    @Test
    void save_WithPlainPassword_ShouldEncryptPassword() {
        // Given
        Usuario newUser = new Usuario();
        newUser.setId("new123");
        newUser.setPassword("plainPassword");
        when(usuarioRepo.save(any(Usuario.class))).thenReturn(newUser);

        // When
        usuarioService.save(newUser);

        // Then
        verify(usuarioRepo).save(argThat(savedUser -> 
            savedUser.getPassword() != null && 
            savedUser.getPassword().startsWith("$2a$")
        ));
    }

    @Test
    void save_WithEncryptedPassword_ShouldNotReencrypt() {
        // Given
        usuario.setPassword(ENCRYPTED_PASSWORD);
        when(usuarioRepo.save(usuario)).thenReturn(usuario);

        // When
        usuarioService.save(usuario);

        // Then
        verify(usuarioRepo).save(argThat(savedUser -> 
            savedUser.getPassword().equals(ENCRYPTED_PASSWORD)
        ));
    }

    @Test
    void findUsuarioById_WhenExists_ShouldReturnUsuario() {
        // Given
        when(usuarioRepo.findById("test123")).thenReturn(Optional.of(usuario));

        // When
        Usuario result = usuarioService.findUsuarioById("test123");

        // Then
        assertNotNull(result);
        assertEquals(usuario, result);
    }

    @Test
    void findUsuarioById_WhenNotExists_ShouldReturnNull() {
        // Given
        when(usuarioRepo.findById("nonexistent")).thenReturn(Optional.empty());

        // When
        Usuario result = usuarioService.findUsuarioById("nonexistent");

        // Then
        assertNull(result);
    }

    @Test
    void validateAuthentication_WithValidCredentials_ShouldReturnUsuario() {
        // Given
        usuario.setPassword(ENCRYPTED_PASSWORD);
        when(usuarioRepo.findUsuarioById("test123")).thenReturn(usuario);

        // When
        Usuario result = usuarioService.validateAuthentication("test123", "password");

        // Then
        assertNotNull(result);
        assertEquals(usuario, result);
    }

    @Test
    void validateAuthentication_WithInvalidCredentials_ShouldReturnNull() {
        // When
        Usuario result = usuarioService.validateAuthentication("test123", "wrongpassword");

        // Then
        assertNull(result);
    }

    @Test
    void validateAuthentication_WithNullCredentials_ShouldReturnNull() {
        // When
        Usuario result = usuarioService.validateAuthentication(null, null);

        // Then
        assertNull(result);
    }
    
    @Test
    void validateAuthentication_WithNullCredentials_ShouldReturnNull2() {
        // When
        Usuario result = usuarioService.validateAuthentication("test123", null);

        // Then
        assertNull(result);
    }
    
    @Test
    void validateAuthentication_WithNullCredentials_ShouldReturnNull3() {
        // When
        Usuario result = usuarioService.validateAuthentication(null, "pass");

        // Then
        assertNull(result);
    }

    @Test
    void existeEmail_WhenExists_ShouldReturnTrue() {
        // Given
        when(usuarioRepo.findByCorreo("test@test.com")).thenReturn(Optional.of(usuario));

        // When
        boolean result = usuarioService.existeEmail("test@test.com");

        // Then
        assertTrue(result);
    }

    @Test
    void existeId_WithValidId_ShouldReturnTrue() {
        // Given
        when(usuarioRepo.findById("test123")).thenReturn(Optional.of(usuario));

        // When
        boolean result = usuarioService.existeId("test123");

        // Then
        assertTrue(result);
    }

    @Test
    void existeId_WithNullOrEmptyId_ShouldReturnFalse() {
        // When & Then
        assertFalse(usuarioService.existeId(null));
        assertFalse(usuarioService.existeId(""));
        assertFalse(usuarioService.existeId("   "));
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Given
        List<Usuario> expectedList = Arrays.asList(usuario);
        when(usuarioRepo.findAll()).thenReturn(expectedList);

        // When
        List<Usuario> result = usuarioService.findAll();

        // Then
        assertEquals(expectedList, result);
    }

    @Test
    void findUsuarioByCorreo_WhenExists_ShouldReturnUsuario() {
        // Given
        when(usuarioRepo.findByCorreo("test@test.com")).thenReturn(Optional.of(usuario));

        // When
        Usuario result = usuarioService.findUsuarioByCorreo("test@test.com");

        // Then
        assertNotNull(result);
        assertEquals(usuario, result);
    }

    @Test
    void blockUser_WhenUserExists_ShouldBlockUser() throws UserNotFoundException {
        // Given
        when(usuarioRepo.findById("test123")).thenReturn(Optional.of(usuario));

        // When
        usuarioService.blockUser("test123");

        // Then
        verify(usuarioRepo).save(argThat(user -> user.isBloqueado()));
    }

    @Test
    void blockUser_WhenUserNotExists_ShouldThrowException() {
        // Given
        when(usuarioRepo.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> 
            usuarioService.blockUser("nonexistent")
        );
    }

    @Test
    void unblockUser_WhenUserExists_ShouldUnblockUser() throws UserNotFoundException {
        // Given
        usuario.setBloqueado(true);
        when(usuarioRepo.findById("test123")).thenReturn(Optional.of(usuario));

        // When
        usuarioService.unblockUser("test123");

        // Then
        verify(usuarioRepo).save(argThat(user -> !user.isBloqueado()));
    }

    @Test
    void unblockUser_WhenUserNotExists_ShouldThrowException() {
        // Given
        when(usuarioRepo.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> 
            usuarioService.unblockUser("nonexistent")
        );
    }
}
