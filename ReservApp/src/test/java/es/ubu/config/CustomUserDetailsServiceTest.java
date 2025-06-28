package es.ubu.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import es.ubu.reservapp.model.entities.Perfil;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.repositories.UsuarioRepo;

/**
 * Clase de test para CustomUserDetailsService.
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepo usuarioRepo;
    
    @Mock
    private Usuario mockUsuario;
    
    @Mock
    private Perfil mockPerfil1;
    
    @Mock
    private Perfil mockPerfil2;
    
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        customUserDetailsService = new CustomUserDetailsService(usuarioRepo);
    }

    /**
     * Test del constructor de CustomUserDetailsService.
     * Verifica que se inicializa correctamente con el repositorio.
     */
    @Test
    void testConstructor() {
        // Given
        UsuarioRepo repo = mock(UsuarioRepo.class);
        
        // When
        CustomUserDetailsService service = new CustomUserDetailsService(repo);
        
        // Then
        assertNotNull(service);
    }

    /**
     * Test del constructor con repositorio null.
     * Verifica que acepta repositorio null sin lanzar excepción.
     */
    @Test
    void testConstructorWithNullRepo() {
        // When & Then
        assertDoesNotThrow(() -> new CustomUserDetailsService(null));
    }

    /**
     * Test de loadUserByUsername con usuario administrador.
     * Verifica que se asigna correctamente el rol ADMIN.
     */
    @Test
    void testLoadUserByUsernameWithAdminUser() {
        // Given
        String username = "admin";
        when(usuarioRepo.findById(username)).thenReturn(Optional.of(mockUsuario));
        when(mockUsuario.isAdministrador()).thenReturn(true);
        when(mockUsuario.getId()).thenReturn(username);
        when(mockUsuario.getPassword()).thenReturn("encodedPassword");
        when(mockUsuario.isBloqueado()).thenReturn(false);
        when(mockUsuario.getNombre()).thenReturn("Admin");
        when(mockUsuario.getApellidos()).thenReturn("User");
        when(mockUsuario.getCorreo()).thenReturn("admin@test.com");
        when(mockUsuario.getTelefono()).thenReturn("123456789");
        when(mockUsuario.getPerfil()).thenReturn(null);
        
        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        
        // Then
        assertNotNull(userDetails);
        assertTrue(userDetails instanceof CustomUserDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")));
        assertEquals(1, userDetails.getAuthorities().size());
        
        CustomUserDetails customDetails = (CustomUserDetails) userDetails;
        assertEquals("Admin", customDetails.getNombre());
        assertEquals("User", customDetails.getApellidos());
        assertEquals("admin@test.com", customDetails.getCorreo());
        assertEquals("123456789", customDetails.getTelefono());
    }

    /**
     * Test de loadUserByUsername con usuario no administrador.
     * Verifica que se asigna correctamente el rol USER.
     */
    @Test
    void testLoadUserByUsernameWithRegularUser() {
        // Given
        String username = "user";
        when(usuarioRepo.findById(username)).thenReturn(Optional.of(mockUsuario));
        when(mockUsuario.isAdministrador()).thenReturn(false);
        when(mockUsuario.getId()).thenReturn(username);
        when(mockUsuario.getPassword()).thenReturn("userPassword");
        when(mockUsuario.isBloqueado()).thenReturn(false);
        when(mockUsuario.getNombre()).thenReturn("Regular");
        when(mockUsuario.getApellidos()).thenReturn("User");
        when(mockUsuario.getCorreo()).thenReturn("user@test.com");
        when(mockUsuario.getTelefono()).thenReturn("987654321");
        when(mockUsuario.getPerfil()).thenReturn(null);
        
        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        
        // Then
        assertNotNull(userDetails);
        assertTrue(userDetails instanceof CustomUserDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("userPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("USER")));
        assertEquals(1, userDetails.getAuthorities().size());
        
        CustomUserDetails customDetails = (CustomUserDetails) userDetails;
        assertEquals("Regular", customDetails.getNombre());
        assertEquals("User", customDetails.getApellidos());
        assertEquals("user@test.com", customDetails.getCorreo());
        assertEquals("987654321", customDetails.getTelefono());
    }

    /**
     * Test de loadUserByUsername con usuario bloqueado.
     * Verifica que el usuario bloqueado no está habilitado.
     */
    @Test
    void testLoadUserByUsernameWithBlockedUser() {
        // Given
        String username = "blockeduser";
        when(usuarioRepo.findById(username)).thenReturn(Optional.of(mockUsuario));
        when(mockUsuario.isAdministrador()).thenReturn(false);
        when(mockUsuario.getId()).thenReturn(username);
        when(mockUsuario.getPassword()).thenReturn("password");
        when(mockUsuario.isBloqueado()).thenReturn(true);
        when(mockUsuario.getNombre()).thenReturn("Blocked");
        when(mockUsuario.getApellidos()).thenReturn("User");
        when(mockUsuario.getCorreo()).thenReturn("blocked@test.com");
        when(mockUsuario.getTelefono()).thenReturn("000000000");
        when(mockUsuario.getPerfil()).thenReturn(null);
        
        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        
        // Then
        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled()); // Usuario bloqueado = no habilitado
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("USER")));
    }

    /**
     * Test de loadUserByUsername con usuario que tiene perfiles.
     * Verifica que se añaden correctamente los roles de los perfiles.
     */
    @Test
    void testLoadUserByUsernameWithUserProfiles() {
        // Given
        String username = "userWithProfiles";
        List<Perfil> perfiles = new ArrayList<>();
        perfiles.add(mockPerfil1);
        perfiles.add(mockPerfil2);
        
        when(usuarioRepo.findById(username)).thenReturn(Optional.of(mockUsuario));
        when(mockUsuario.isAdministrador()).thenReturn(false);
        when(mockUsuario.getId()).thenReturn(username);
        when(mockUsuario.getPassword()).thenReturn("password");
        when(mockUsuario.isBloqueado()).thenReturn(false);
        when(mockUsuario.getNombre()).thenReturn("User");
        when(mockUsuario.getApellidos()).thenReturn("WithProfiles");
        when(mockUsuario.getCorreo()).thenReturn("userprofiles@test.com");
        when(mockUsuario.getTelefono()).thenReturn("111111111");
        when(mockUsuario.getPerfil()).thenReturn(perfiles);
        when(mockPerfil1.getNombre()).thenReturn("ROLE_MANAGER");
        when(mockPerfil2.getNombre()).thenReturn("ROLE_EDITOR");
        
        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        
        // Then
        assertNotNull(userDetails);
        assertEquals(3, userDetails.getAuthorities().size()); // USER + 2 perfiles
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("USER")));
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER")));
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EDITOR")));
    }

    /**
     * Test de loadUserByUsername con administrador que tiene perfiles.
     * Verifica que se añaden tanto el rol ADMIN como los roles de los perfiles.
     */
    @Test
    void testLoadUserByUsernameWithAdminAndProfiles() {
        // Given
        String username = "adminWithProfiles";
        List<Perfil> perfiles = new ArrayList<>();
        perfiles.add(mockPerfil1);
        
        when(usuarioRepo.findById(username)).thenReturn(Optional.of(mockUsuario));
        when(mockUsuario.isAdministrador()).thenReturn(true);
        when(mockUsuario.getId()).thenReturn(username);
        when(mockUsuario.getPassword()).thenReturn("adminPassword");
        when(mockUsuario.isBloqueado()).thenReturn(false);
        when(mockUsuario.getNombre()).thenReturn("Admin");
        when(mockUsuario.getApellidos()).thenReturn("WithProfiles");
        when(mockUsuario.getCorreo()).thenReturn("adminprofiles@test.com");
        when(mockUsuario.getTelefono()).thenReturn("222222222");
        when(mockUsuario.getPerfil()).thenReturn(perfiles);
        when(mockPerfil1.getNombre()).thenReturn("ROLE_SUPER_ADMIN");
        
        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        
        // Then
        assertNotNull(userDetails);
        assertEquals(2, userDetails.getAuthorities().size()); // ADMIN + 1 perfil
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")));
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
    }

    /**
     * Test de loadUserByUsername con lista de perfiles vacía.
     * Verifica que se maneja correctamente una lista vacía de perfiles.
     */
    @Test
    void testLoadUserByUsernameWithEmptyProfiles() {
        // Given
        String username = "userEmptyProfiles";
        List<Perfil> perfilesVacios = new ArrayList<>();
        
        when(usuarioRepo.findById(username)).thenReturn(Optional.of(mockUsuario));
        when(mockUsuario.isAdministrador()).thenReturn(false);
        when(mockUsuario.getId()).thenReturn(username);
        when(mockUsuario.getPassword()).thenReturn("password");
        when(mockUsuario.isBloqueado()).thenReturn(false);
        when(mockUsuario.getNombre()).thenReturn("User");
        when(mockUsuario.getApellidos()).thenReturn("EmptyProfiles");
        when(mockUsuario.getCorreo()).thenReturn("empty@test.com");
        when(mockUsuario.getTelefono()).thenReturn("333333333");
        when(mockUsuario.getPerfil()).thenReturn(perfilesVacios);
        
        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        
        // Then
        assertNotNull(userDetails);
        assertEquals(1, userDetails.getAuthorities().size()); // Solo USER
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("USER")));
    }

    /**
     * Test de loadUserByUsername con usuario no encontrado.
     * Verifica que se lanza UsernameNotFoundException cuando el usuario no existe.
     */
    @Test
    void testLoadUserByUsernameUserNotFound() {
        // Given
        String username = "nonexistent";
        when(usuarioRepo.findById(username)).thenReturn(Optional.empty());
        
        // When & Then
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername(username)
        );
        
        assertEquals("Usuario no encontrado: " + username, exception.getMessage());
    }

    /**
     * Test de loadUserByUsername con username null.
     * Verifica el comportamiento cuando se pasa un username null.
     */
    @Test
    void testLoadUserByUsernameWithNullUsername() {
        // Given
        String username = null;
        when(usuarioRepo.findById(username)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername(username)
        );
    }

    /**
     * Test de loadUserByUsername con username vacío.
     * Verifica el comportamiento cuando se pasa un username vacío.
     */
    @Test
    void testLoadUserByUsernameWithEmptyUsername() {
        // Given
        String username = "";
        when(usuarioRepo.findById(username)).thenReturn(Optional.empty());
        
        // When & Then
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername(username)
        );
        
        assertEquals("Usuario no encontrado: ", exception.getMessage());
    }

    /**
     * Test de loadUserByUsername con caracteres especiales en username.
     * Verifica el manejo de usernames con caracteres especiales.
     */
    @Test
    void testLoadUserByUsernameWithSpecialCharacters() {
        // Given
        String username = "user@special";
        when(usuarioRepo.findById(username)).thenReturn(Optional.of(mockUsuario));
        when(mockUsuario.isAdministrador()).thenReturn(false);
        when(mockUsuario.getId()).thenReturn(username);
        when(mockUsuario.getPassword()).thenReturn("password");
        when(mockUsuario.isBloqueado()).thenReturn(false);
        when(mockUsuario.getNombre()).thenReturn("Special");
        when(mockUsuario.getApellidos()).thenReturn("User");
        when(mockUsuario.getCorreo()).thenReturn("special@test.com");
        when(mockUsuario.getTelefono()).thenReturn("444444444");
        when(mockUsuario.getPerfil()).thenReturn(null);
        
        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        
        // Then
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("USER")));
    }

    /**
     * Test de loadUserByUsername con múltiples perfiles.
     * Verifica el manejo de usuarios con múltiples perfiles.
     */
    @Test
    void testLoadUserByUsernameWithMultipleProfiles() {
        // Given
        String username = "multiProfileUser";
        List<Perfil> multiplePerfiles = new ArrayList<>();
        Perfil perfil3 = mock(Perfil.class);
        Perfil perfil4 = mock(Perfil.class);
        Perfil perfil5 = mock(Perfil.class);
        
        multiplePerfiles.add(mockPerfil1);
        multiplePerfiles.add(mockPerfil2);
        multiplePerfiles.add(perfil3);
        multiplePerfiles.add(perfil4);
        multiplePerfiles.add(perfil5);
        
        when(usuarioRepo.findById(username)).thenReturn(Optional.of(mockUsuario));
        when(mockUsuario.isAdministrador()).thenReturn(true);
        when(mockUsuario.getId()).thenReturn(username);
        when(mockUsuario.getPassword()).thenReturn("password");
        when(mockUsuario.isBloqueado()).thenReturn(false);
        when(mockUsuario.getNombre()).thenReturn("Multi");
        when(mockUsuario.getApellidos()).thenReturn("Profile");
        when(mockUsuario.getCorreo()).thenReturn("multi@test.com");
        when(mockUsuario.getTelefono()).thenReturn("555555555");
        when(mockUsuario.getPerfil()).thenReturn(multiplePerfiles);
        when(mockPerfil1.getNombre()).thenReturn("ROLE_MANAGER");
        when(mockPerfil2.getNombre()).thenReturn("ROLE_EDITOR");
        when(perfil3.getNombre()).thenReturn("ROLE_VIEWER");
        when(perfil4.getNombre()).thenReturn("ROLE_ANALYST");
        when(perfil5.getNombre()).thenReturn("ROLE_REPORTER");
        
        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        
        // Then
        assertNotNull(userDetails);
        assertEquals(6, userDetails.getAuthorities().size()); // ADMIN + 5 perfiles
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")));
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER")));
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EDITOR")));
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_VIEWER")));
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ANALYST")));
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_REPORTER")));
    }

    /**
     * Test de loadUserByUsername con campos null en usuario.
     * Verifica el manejo de usuarios con campos null.
     */
    @Test
    void testLoadUserByUsernameWithNullUserFields() {
        // Given
        String username = "nullFieldsUser";
        when(usuarioRepo.findById(username)).thenReturn(Optional.of(mockUsuario));
        when(mockUsuario.isAdministrador()).thenReturn(false);
        when(mockUsuario.getId()).thenReturn(username);
        when(mockUsuario.getPassword()).thenReturn("password");
        when(mockUsuario.isBloqueado()).thenReturn(false);
        when(mockUsuario.getNombre()).thenReturn(null);
        when(mockUsuario.getApellidos()).thenReturn(null);
        when(mockUsuario.getCorreo()).thenReturn(null);
        when(mockUsuario.getTelefono()).thenReturn(null);
        when(mockUsuario.getPerfil()).thenReturn(null);
        
        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        
        // Then
        assertNotNull(userDetails);
        assertTrue(userDetails instanceof CustomUserDetails);
        CustomUserDetails customDetails = (CustomUserDetails) userDetails;
        assertNull(customDetails.getNombre());
        assertNull(customDetails.getApellidos());
        assertNull(customDetails.getCorreo());
        assertNull(customDetails.getTelefono());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("USER")));
    }

    /**
     * Test de verificación de implementación de UserDetailsService.
     * Verifica que la clase implementa correctamente la interfaz.
     */
    @Test
    void testImplementsUserDetailsService() {
        // Then
        assertTrue(customUserDetailsService instanceof org.springframework.security.core.userdetails.UserDetailsService);
    }

    /**
     * Test de verificación de anotación @Service.
     * Verifica que la clase tiene la anotación @Service.
     */
    @Test
    void testServiceAnnotation() {
        // Given
        Class<CustomUserDetailsService> clazz = CustomUserDetailsService.class;
        
        // When & Then
        assertTrue(clazz.isAnnotationPresent(org.springframework.stereotype.Service.class));
    }

    /**
     * Test de interacción con el repositorio.
     * Verifica que se llama correctamente al repositorio.
     */
    @Test
    void testRepositoryInteraction() {
        // Given
        String username = "testuser";
        when(usuarioRepo.findById(username)).thenReturn(Optional.of(mockUsuario));
        when(mockUsuario.isAdministrador()).thenReturn(false);
        when(mockUsuario.getId()).thenReturn(username);
        when(mockUsuario.getPassword()).thenReturn("password");
        when(mockUsuario.isBloqueado()).thenReturn(false);
        when(mockUsuario.getNombre()).thenReturn("Test");
        when(mockUsuario.getApellidos()).thenReturn("User");
        when(mockUsuario.getCorreo()).thenReturn("test@test.com");
        when(mockUsuario.getTelefono()).thenReturn("123456789");
        when(mockUsuario.getPerfil()).thenReturn(null);
        
        // When
        customUserDetailsService.loadUserByUsername(username);
        
        // Then
        verify(usuarioRepo, times(1)).findById(username);
    }

    /**
     * Test de manejo de excepción del repositorio.
     * Verifica el comportamiento cuando el repositorio lanza una excepción.
     */
    @Test
    void testRepositoryException() {
        // Given
        String username = "erroruser";
        when(usuarioRepo.findById(username)).thenThrow(new RuntimeException("Database error"));
        
        // When & Then
        assertThrows(
            RuntimeException.class,
            () -> customUserDetailsService.loadUserByUsername(username)
        );
    }
}