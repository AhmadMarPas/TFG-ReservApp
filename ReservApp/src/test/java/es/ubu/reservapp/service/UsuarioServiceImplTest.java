package es.ubu.reservapp.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import es.ubu.reservapp.exception.UserNotFoundException;
import es.ubu.reservapp.model.entities.Establecimiento;
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
    
    @Mock
    private Model model;

    private UsuarioServiceImpl usuarioService;
    private Usuario usuario;
    private Establecimiento establecimiento1;
    private Establecimiento establecimiento2;
    private List<Establecimiento> establecimientos;
    private static final String ENCRYPTED_PASSWORD = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG";

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioServiceImpl(usuarioRepo);
        usuario = new Usuario();
        usuario.setId("test123");
        usuario.setCorreo("test@test.com");
        usuario.setPassword("password");
        usuario.setLstEstablecimientos(new ArrayList<>());
        
        establecimiento1 = new Establecimiento();
        establecimiento1.setId(1);
        establecimiento1.setNombre("Establecimiento 1");
        
        establecimiento2 = new Establecimiento();
        establecimiento2.setId(2);
        establecimiento2.setNombre("Establecimiento 2");
        
        establecimientos = Arrays.asList(establecimiento1, establecimiento2);
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
    
    @Test
    void recuperarEstablecimientosUsuario_WhenUserExists_ShouldAddAttributesToModel() {
        // Given
        usuario.setLstEstablecimientos(establecimientos);
        when(usuarioRepo.findById("test123")).thenReturn(Optional.of(usuario));
        when(model.addAttribute("usuario", usuario)).thenReturn(model);
        when(model.addAttribute(any(String.class), any(Set.class))).thenReturn(model);
        
        // When
        Model result = usuarioService.recuperarEstablecimientosUsuario("test123", model);
        
        // Then
        assertNotNull(result);
        verify(model).addAttribute("usuario", usuario);
        verify(model).addAttribute(any(String.class), any(Set.class));
    }
    
    @Test
    void recuperarEstablecimientosUsuario_WhenUserNotExists_ShouldReturnModel() {
        // Given
        when(usuarioRepo.findById("nonexistent")).thenReturn(Optional.empty());
        
        // When
        Model result = usuarioService.recuperarEstablecimientosUsuario("nonexistent", model);
        
        // Then
        assertNotNull(result);
        verify(model, never()).addAttribute(any(String.class), any());
    }
    
    @Test
    void asignarEstablecimientos_WhenUserExists_ShouldAssignEstablecimientos() {
        // Given
        when(usuarioRepo.findById("test123")).thenReturn(Optional.of(usuario));
        
        // When
        Usuario result = usuarioService.asignarEstablecimientos(usuario, establecimientos);
        
        // Then
        assertNotNull(result);
        assertEquals(establecimientos, result.getLstEstablecimientos());
    }
    
    @Test
    void asignarEstablecimientos_WhenUserNotExists_ShouldReturnNull() {
        // Given
        when(usuarioRepo.findById("test123")).thenReturn(Optional.empty());
        
        // When
        Usuario result = usuarioService.asignarEstablecimientos(usuario, establecimientos);
        
        // Then
        assertNull(result);
    }
    
    @Test
    void obtenerEstablecimientosUsuario_WhenUserExists_ShouldAddAttributesToModel() {
        // Given
        usuario.setLstEstablecimientos(establecimientos);
        when(usuarioRepo.findById("test123")).thenReturn(Optional.of(usuario));
        when(model.addAttribute("usuario", usuario)).thenReturn(model);
        when(model.addAttribute("establecimientos", establecimientos)).thenReturn(model);
        
        // When
        Model result = usuarioService.obtenerEstablecimientosUsuario(usuario, model);
        
        // Then
        assertNotNull(result);
        verify(model).addAttribute("usuario", usuario);
        verify(model).addAttribute("establecimientos", establecimientos);
    }
    
    @Test
    void obtenerEstablecimientosUsuario_WhenUserNotExists_ShouldReturnModel() {
        // Given
        when(usuarioRepo.findById("test123")).thenReturn(Optional.empty());
        
        // When
        Model result = usuarioService.obtenerEstablecimientosUsuario(usuario, model);
        
        // Then
        assertNotNull(result);
        verify(model, never()).addAttribute(any(String.class), any());
    }
    
    @Test
    void establecimientoAsignado_WhenUserExistsAndEstablecimientoAssigned_ShouldReturnTrue() {
        // Given
        usuario.setLstEstablecimientos(establecimientos);
        when(usuarioRepo.findById("test123")).thenReturn(Optional.of(usuario));
        
        // When
        boolean result = usuarioService.establecimientoAsignado(usuario, establecimiento1);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void establecimientoAsignado_WhenUserExistsAndEstablecimientoNotAssigned_ShouldReturnFalse() {
        // Given
        Establecimiento establecimiento3 = new Establecimiento();
        establecimiento3.setId(3);
        usuario.setLstEstablecimientos(establecimientos);
        when(usuarioRepo.findById("test123")).thenReturn(Optional.of(usuario));
        
        // When
        boolean result = usuarioService.establecimientoAsignado(usuario, establecimiento3);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void establecimientoAsignado_WhenUserNotExists_ShouldReturnFalse() {
        // Given
        when(usuarioRepo.findById("test123")).thenReturn(Optional.empty());
        
        // When
        boolean result = usuarioService.establecimientoAsignado(usuario, establecimiento1);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void constructor_ShouldInitializeFields() {
        // When
        UsuarioServiceImpl service = new UsuarioServiceImpl(usuarioRepo);
        
        // Then
        assertNotNull(service);
    }

    // ================================
    // TESTS PARA buscarUsuarioSegunQuery
    // ================================

    @Test
    void buscarUsuarioSegunQuery_WithValidQuery_ShouldReturnMatchingUsers() {
        // Given
        Usuario usuario1 = new Usuario();
        usuario1.setId("test123");
        usuario1.setNombre("Test User");
        
        Usuario usuario2 = new Usuario();
        usuario2.setId("user456");
        usuario2.setCorreo("test@example.com");
        
        List<Usuario> expectedUsers = Arrays.asList(usuario1, usuario2);
        when(usuarioRepo.findByIdContainingIgnoreCaseOrNombreContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrCorreoContainingIgnoreCase(
            "test", "test", "test", "test")).thenReturn(expectedUsers);

        // When
        List<Usuario> result = usuarioService.buscarUsuarioSegunQuery("test");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedUsers, result);
        verify(usuarioRepo).findByIdContainingIgnoreCaseOrNombreContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrCorreoContainingIgnoreCase(
            "test", "test", "test", "test");
    }

    @Test
    void buscarUsuarioSegunQuery_WithEmptyQuery_ShouldCallRepositoryWithEmptyString() {
        // Given
        List<Usuario> expectedUsers = new ArrayList<>();
        when(usuarioRepo.findByIdContainingIgnoreCaseOrNombreContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrCorreoContainingIgnoreCase(
            "", "", "", "")).thenReturn(expectedUsers);

        // When
        List<Usuario> result = usuarioService.buscarUsuarioSegunQuery("");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(usuarioRepo).findByIdContainingIgnoreCaseOrNombreContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrCorreoContainingIgnoreCase(
            "", "", "", "");
    }

    @Test
    void buscarUsuarioSegunQuery_WithNullQuery_ShouldCallRepositoryWithNull() {
        // Given
        List<Usuario> expectedUsers = new ArrayList<>();
        when(usuarioRepo.findByIdContainingIgnoreCaseOrNombreContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrCorreoContainingIgnoreCase(
            null, null, null, null)).thenReturn(expectedUsers);

        // When
        List<Usuario> result = usuarioService.buscarUsuarioSegunQuery(null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(usuarioRepo).findByIdContainingIgnoreCaseOrNombreContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrCorreoContainingIgnoreCase(
            null, null, null, null);
    }

    @Test
    void buscarUsuarioSegunQuery_WithSpecialCharacters_ShouldHandleCorrectly() {
        // Given
        String query = "test@domain.com";
        Usuario usuario1 = new Usuario();
        usuario1.setCorreo("test@domain.com");
        
        List<Usuario> expectedUsers = Arrays.asList(usuario1);
        when(usuarioRepo.findByIdContainingIgnoreCaseOrNombreContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrCorreoContainingIgnoreCase(
            query, query, query, query)).thenReturn(expectedUsers);

        // When
        List<Usuario> result = usuarioService.buscarUsuarioSegunQuery(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedUsers, result);
        verify(usuarioRepo).findByIdContainingIgnoreCaseOrNombreContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrCorreoContainingIgnoreCase(
            query, query, query, query);
    }

    @Test
    void buscarUsuarioSegunQuery_WithWhitespaceQuery_ShouldCallRepositoryWithWhitespace() {
        // Given
        String query = "   ";
        List<Usuario> expectedUsers = new ArrayList<>();
        when(usuarioRepo.findByIdContainingIgnoreCaseOrNombreContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrCorreoContainingIgnoreCase(
            query, query, query, query)).thenReturn(expectedUsers);

        // When
        List<Usuario> result = usuarioService.buscarUsuarioSegunQuery(query);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(usuarioRepo).findByIdContainingIgnoreCaseOrNombreContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrCorreoContainingIgnoreCase(
            query, query, query, query);
    }

    @Test
    void buscarUsuarioSegunQuery_WithNoResults_ShouldReturnEmptyList() {
        // Given
        String query = "nonexistent";
        List<Usuario> expectedUsers = new ArrayList<>();
        when(usuarioRepo.findByIdContainingIgnoreCaseOrNombreContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrCorreoContainingIgnoreCase(
            query, query, query, query)).thenReturn(expectedUsers);

        // When
        List<Usuario> result = usuarioService.buscarUsuarioSegunQuery(query);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(usuarioRepo).findByIdContainingIgnoreCaseOrNombreContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrCorreoContainingIgnoreCase(
            query, query, query, query);
    }

    // ================================
    // TESTS PARA findUsuariosByIds
    // ================================

    @Test
    void findUsuariosByIds_WithValidIds_ShouldReturnMatchingUsers() {
        // Given
        List<String> ids = Arrays.asList("user1", "user2", "user3");
        
        Usuario usuario1 = new Usuario();
        usuario1.setId("user1");
        usuario1.setNombre("Usuario 1");
        
        Usuario usuario2 = new Usuario();
        usuario2.setId("user2");
        usuario2.setNombre("Usuario 2");
        
        Usuario usuario3 = new Usuario();
        usuario3.setId("user3");
        usuario3.setNombre("Usuario 3");
        
        Usuario usuario4 = new Usuario();
        usuario4.setId("user4");
        usuario4.setNombre("Usuario 4");
        
        List<Usuario> allUsers = Arrays.asList(usuario1, usuario2, usuario3, usuario4);
        when(usuarioRepo.findAll()).thenReturn(allUsers);

        // When
        List<Usuario> result = usuarioService.findUsuariosByIds(ids);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(usuario1));
        assertTrue(result.contains(usuario2));
        assertTrue(result.contains(usuario3));
        assertFalse(result.contains(usuario4));
        verify(usuarioRepo).findAll();
    }

    @Test
    void findUsuariosByIds_WithEmptyIdsList_ShouldReturnEmptyList() {
        // Given
        List<String> ids = new ArrayList<>();
        List<Usuario> allUsers = Arrays.asList(usuario);
        when(usuarioRepo.findAll()).thenReturn(allUsers);

        // When
        List<Usuario> result = usuarioService.findUsuariosByIds(ids);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(usuarioRepo).findAll();
    }

    @Test
    void findUsuariosByIds_WithNonExistentIds_ShouldReturnEmptyList() {
        // Given
        List<String> ids = Arrays.asList("nonexistent1", "nonexistent2");
        
        Usuario usuario1 = new Usuario();
        usuario1.setId("existing1");
        
        Usuario usuario2 = new Usuario();
        usuario2.setId("existing2");
        
        List<Usuario> allUsers = Arrays.asList(usuario1, usuario2);
        when(usuarioRepo.findAll()).thenReturn(allUsers);

        // When
        List<Usuario> result = usuarioService.findUsuariosByIds(ids);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(usuarioRepo).findAll();
    }

    @Test
    void findUsuariosByIds_WithMixedExistentAndNonExistentIds_ShouldReturnOnlyExistentUsers() {
        // Given
        List<String> ids = Arrays.asList("user1", "nonexistent", "user3", "alsoNonexistent");
        
        Usuario usuario1 = new Usuario();
        usuario1.setId("user1");
        usuario1.setNombre("Usuario 1");
        
        Usuario usuario2 = new Usuario();
        usuario2.setId("user2");
        usuario2.setNombre("Usuario 2");
        
        Usuario usuario3 = new Usuario();
        usuario3.setId("user3");
        usuario3.setNombre("Usuario 3");
        
        List<Usuario> allUsers = Arrays.asList(usuario1, usuario2, usuario3);
        when(usuarioRepo.findAll()).thenReturn(allUsers);

        // When
        List<Usuario> result = usuarioService.findUsuariosByIds(ids);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(usuario1));
        assertFalse(result.contains(usuario2));
        assertTrue(result.contains(usuario3));
        verify(usuarioRepo).findAll();
    }

    @Test
    void findUsuariosByIds_WithDuplicateIds_ShouldReturnUniqueUsers() {
        // Given
        List<String> ids = Arrays.asList("user1", "user1", "user2", "user1");
        
        Usuario usuario1 = new Usuario();
        usuario1.setId("user1");
        usuario1.setNombre("Usuario 1");
        
        Usuario usuario2 = new Usuario();
        usuario2.setId("user2");
        usuario2.setNombre("Usuario 2");
        
        List<Usuario> allUsers = Arrays.asList(usuario1, usuario2);
        when(usuarioRepo.findAll()).thenReturn(allUsers);

        // When
        List<Usuario> result = usuarioService.findUsuariosByIds(ids);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(usuario1));
        assertTrue(result.contains(usuario2));
        // Verificar que cada usuario aparece solo una vez
        long user1Count = result.stream().filter(u -> "user1".equals(u.getId())).count();
        long user2Count = result.stream().filter(u -> "user2".equals(u.getId())).count();
        assertEquals(1, user1Count);
        assertEquals(1, user2Count);
        verify(usuarioRepo).findAll();
    }

    @Test
    void findUsuariosByIds_WithSingleId_ShouldReturnSingleUser() {
        // Given
        List<String> ids = Arrays.asList("user1");
        
        Usuario usuario1 = new Usuario();
        usuario1.setId("user1");
        usuario1.setNombre("Usuario 1");
        
        Usuario usuario2 = new Usuario();
        usuario2.setId("user2");
        usuario2.setNombre("Usuario 2");
        
        List<Usuario> allUsers = Arrays.asList(usuario1, usuario2);
        when(usuarioRepo.findAll()).thenReturn(allUsers);

        // When
        List<Usuario> result = usuarioService.findUsuariosByIds(ids);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(usuario1, result.get(0));
        verify(usuarioRepo).findAll();
    }

    @Test
    void findUsuariosByIds_WithEmptyRepositoryResult_ShouldReturnEmptyList() {
        // Given
        List<String> ids = Arrays.asList("user1", "user2");
        List<Usuario> allUsers = new ArrayList<>();
        when(usuarioRepo.findAll()).thenReturn(allUsers);

        // When
        List<Usuario> result = usuarioService.findUsuariosByIds(ids);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(usuarioRepo).findAll();
    }
}