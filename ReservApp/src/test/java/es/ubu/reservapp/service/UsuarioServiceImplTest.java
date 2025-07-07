package es.ubu.reservapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
        verify(usuarioRepo).save(argThat(Usuario::isBloqueado));
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

    @Test
    void deleteById_WhenUserExists_ShouldDeleteUser() {
        String userId = "testUser";
        Usuario user = new Usuario();
        user.setId(userId);
        when(usuarioRepo.findById(userId)).thenReturn(java.util.Optional.of(user));
        
        assertDoesNotThrow(() -> {
            usuarioService.deleteById(userId);
        });
        
        verify(usuarioRepo, times(1)).deleteById(userId);
    }

    @Test
    void deleteById_WhenUserDoesNotExist_ShouldThrowException() {
        String userId = "nonExistentUser";
        when(usuarioRepo.findById(userId)).thenReturn(java.util.Optional.empty());
        
        assertThrows(UserNotFoundException.class, () -> {
            usuarioService.deleteById(userId);
        });
        
        verify(usuarioRepo, never()).deleteById(userId);
    }

    @Test
    void save_WithNullPassword_ShouldNotEncrypt() {
        // Given
        Usuario userWithNullPassword = new Usuario();
        userWithNullPassword.setId("user1");
        userWithNullPassword.setPassword(null);
        
        // When
        usuarioService.save(userWithNullPassword);
        
        // Then
        verify(usuarioRepo).save(userWithNullPassword);
        assertNull(userWithNullPassword.getPassword());
    }

    @Test
    void save_WithEmptyPassword_ShouldNotEncrypt() {
        // Given
        Usuario userWithEmptyPassword = new Usuario();
        userWithEmptyPassword.setId("user1");
        userWithEmptyPassword.setPassword("");
        
        // When
        usuarioService.save(userWithEmptyPassword);
        
        // Then
        verify(usuarioRepo).save(userWithEmptyPassword);
        assertEquals("", userWithEmptyPassword.getPassword());
    }

    @Test
    void save_WithBcryptVersionB_ShouldNotReencrypt() {
        // Given
        String bcryptB = "$2b$10$N9qo8uLOickgx2ZMRZoMye/Ci/BABjFWfuaXvI6FUFmox2MIk.FlS";
        Usuario userWithBcryptB = new Usuario();
        userWithBcryptB.setPassword(bcryptB);
        
        // When
        usuarioService.save(userWithBcryptB);
        
        // Then
        verify(usuarioRepo).save(userWithBcryptB);
        assertEquals(bcryptB, userWithBcryptB.getPassword());
    }

    @Test
    void save_WithBcryptVersionY_ShouldNotReencrypt() {
        // Given
        String bcryptY = "$2y$10$N9qo8uLOickgx2ZMRZoMye/Ci/BABjFWfuaXvI6FUFmox2MIk.FlS";
        Usuario userWithBcryptY = new Usuario();
        userWithBcryptY.setPassword(bcryptY);
        
        // When
        usuarioService.save(userWithBcryptY);
        
        // Then
        verify(usuarioRepo).save(userWithBcryptY);
        assertEquals(bcryptY, userWithBcryptY.getPassword());
    }

    @Test
    void validateAuthentication_WithUserHavingNullPassword_ShouldReturnNull() {
        // Given
        usuario.setPassword(null);
        when(usuarioRepo.findUsuarioById("test123")).thenReturn(usuario);
        
        // When
        Usuario result = usuarioService.validateAuthentication("test123", "password");
        
        // Then
        assertNull(result);
    }

    @Test
    void validateAuthentication_WithInvalidPassword_ShouldReturnNull() {
        // Given
        usuario.setPassword(ENCRYPTED_PASSWORD);
        when(usuarioRepo.findUsuarioById("test123")).thenReturn(usuario);
        
        // When
        Usuario result = usuarioService.validateAuthentication("test123", "wrongpassword");
        
        // Then
        assertNull(result);
    }

    @Test
    void validateAuthentication_WithNonExistingUser_ShouldReturnNull() {
        // Given
        when(usuarioRepo.findUsuarioById("nonexistent")).thenReturn(null);
        
        // When
        Usuario result = usuarioService.validateAuthentication("nonexistent", "password");
        
        // Then
        assertNull(result);
    }

    @Test
    void existeEmail_WhenNotExists_ShouldReturnFalse() {
        // Given
        when(usuarioRepo.findByCorreo("nonexistent@test.com")).thenReturn(Optional.empty());
        
        // When
        boolean result = usuarioService.existeEmail("nonexistent@test.com");
        
        // Then
        assertFalse(result);
    }

    @Test
    void existeId_WithNonExistingId_ShouldReturnFalse() {
        // Given
        when(usuarioRepo.findById("nonexistent")).thenReturn(Optional.empty());
        
        // When
        boolean result = usuarioService.existeId("nonexistent");
        
        // Then
        assertFalse(result);
    }

    @Test
    void findUsuarioByCorreo_WhenNotExists_ShouldReturnNull() {
        // Given
        when(usuarioRepo.findByCorreo("nonexistent@test.com")).thenReturn(Optional.empty());
        
        // When
        Usuario result = usuarioService.findUsuarioByCorreo("nonexistent@test.com");
        
        // Then
        assertNull(result);
    }
}

