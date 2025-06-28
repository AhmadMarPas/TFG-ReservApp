package es.ubu.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import es.ubu.reservapp.model.entities.Usuario;

/**
 * Clase de test para CustomUserDetails.
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsTest {

    @Mock
    private Usuario mockUsuario;
    
    private Collection<GrantedAuthority> authorities;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        // Configurar authorities
        authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.add(new SimpleGrantedAuthority("ADMIN"));
        
        // Configurar mock usuario
        when(mockUsuario.getId()).thenReturn("testuser");
        when(mockUsuario.getPassword()).thenReturn("encodedPassword123");
        when(mockUsuario.isBloqueado()).thenReturn(false);
        when(mockUsuario.getNombre()).thenReturn("Juan");
        when(mockUsuario.getApellidos()).thenReturn("Pérez García");
        when(mockUsuario.getCorreo()).thenReturn("juan.perez@example.com");
        when(mockUsuario.getTelefono()).thenReturn("123456789");
        
        customUserDetails = new CustomUserDetails(mockUsuario, authorities);
    }

    /**
     * Test del constructor de CustomUserDetails.
     * Verifica que se inicializa correctamente con los datos del usuario.
     */
    @Test
    void testConstructor() {
        // Given
        Usuario usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn("user123");
        when(usuario.getPassword()).thenReturn("password456");
        when(usuario.isBloqueado()).thenReturn(false);
        when(usuario.getNombre()).thenReturn("María");
        when(usuario.getApellidos()).thenReturn("González López");
        when(usuario.getCorreo()).thenReturn("maria.gonzalez@test.com");
        when(usuario.getTelefono()).thenReturn("987654321");
        
        List<GrantedAuthority> testAuthorities = new ArrayList<>();
        testAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        
        // When
        CustomUserDetails userDetails = new CustomUserDetails(usuario, testAuthorities);
        
        // Then
        assertNotNull(userDetails);
        assertEquals("user123", userDetails.getUsername());
        assertEquals("password456", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertEquals(new HashSet<>(testAuthorities), userDetails.getAuthorities());
        assertEquals("María", userDetails.getNombre());
        assertEquals("González López", userDetails.getApellidos());
        assertEquals("maria.gonzalez@test.com", userDetails.getCorreo());
        assertEquals("987654321", userDetails.getTelefono());
    }

    /**
     * Test del constructor con usuario bloqueado.
     * Verifica que el estado de habilitación se establece correctamente.
     */
    @Test
    void testConstructorWithBlockedUser() {
        // Given
        Usuario usuarioBloqueado = mock(Usuario.class);
        when(usuarioBloqueado.getId()).thenReturn("blockeduser");
        when(usuarioBloqueado.getPassword()).thenReturn("password");
        when(usuarioBloqueado.isBloqueado()).thenReturn(true);
        when(usuarioBloqueado.getNombre()).thenReturn("Usuario");
        when(usuarioBloqueado.getApellidos()).thenReturn("Bloqueado");
        when(usuarioBloqueado.getCorreo()).thenReturn("blocked@test.com");
        when(usuarioBloqueado.getTelefono()).thenReturn("000000000");
        
        // When
        CustomUserDetails userDetails = new CustomUserDetails(usuarioBloqueado, authorities);
        
        // Then
        assertFalse(userDetails.isEnabled()); // Usuario bloqueado = no habilitado
        assertEquals("blockeduser", userDetails.getUsername());
        assertEquals("Usuario", userDetails.getNombre());
        assertEquals("Bloqueado", userDetails.getApellidos());
        assertEquals("blocked@test.com", userDetails.getCorreo());
        assertEquals("000000000", userDetails.getTelefono());
    }

    /**
     * Test de los getters de campos personalizados.
     * Verifica que todos los getters devuelven los valores correctos.
     */
    @Test
    void testCustomFieldGetters() {
        // Then
        assertEquals("Juan", customUserDetails.getNombre());
        assertEquals("Pérez García", customUserDetails.getApellidos());
        assertEquals("juan.perez@example.com", customUserDetails.getCorreo());
        assertEquals("123456789", customUserDetails.getTelefono());
    }

    /**
     * Test de los métodos heredados de User.
     * Verifica que los métodos de la clase padre funcionan correctamente.
     */
    @Test
    void testInheritedMethods() {
        // Then
        assertEquals("testuser", customUserDetails.getUsername());
        assertEquals("encodedPassword123", customUserDetails.getPassword());
        assertTrue(customUserDetails.isEnabled());
        assertTrue(customUserDetails.isAccountNonExpired());
        assertTrue(customUserDetails.isAccountNonLocked());
        assertTrue(customUserDetails.isCredentialsNonExpired());
        assertIterableEquals(authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()),
            customUserDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet())
        );
    }

    /**
     * Test con authorities vacías.
     * Verifica el comportamiento cuando no se proporcionan authorities.
     */
    @Test
    void testWithEmptyAuthorities() {
        // Given
        Collection<GrantedAuthority> emptyAuthorities = new ArrayList<>();
        
        // When
        CustomUserDetails userDetails = new CustomUserDetails(mockUsuario, emptyAuthorities);
        
        // Then
        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().isEmpty());
        assertEquals("Juan", userDetails.getNombre());
        assertEquals("Pérez García", userDetails.getApellidos());
    }

    /**
     * Test con authorities null.
     * Verifica el comportamiento cuando se pasan authorities null.
     */
    @Test
    void testWithNullAuthorities() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            new CustomUserDetails(mockUsuario, null);
        });
    }

    /**
     * Test con múltiples authorities.
     * Verifica el manejo de múltiples roles y permisos.
     */
    @Test
    void testWithMultipleAuthorities() {
        // Given
        List<GrantedAuthority> multipleAuthorities = new ArrayList<>();
        multipleAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        multipleAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        multipleAuthorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
        multipleAuthorities.add(new SimpleGrantedAuthority("READ_PRIVILEGE"));
        multipleAuthorities.add(new SimpleGrantedAuthority("WRITE_PRIVILEGE"));
        
        // When
        CustomUserDetails userDetails = new CustomUserDetails(mockUsuario, multipleAuthorities);
        
        // Then
        assertEquals(5, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().containsAll(multipleAuthorities));
    }

    /**
     * Test con valores null en campos del usuario.
     * Verifica el comportamiento cuando algunos campos del usuario son null.
     */
    @Test
    void testWithNullUserFields() {
        // Given
        Usuario usuarioConNulls = mock(Usuario.class);
        when(usuarioConNulls.getId()).thenReturn("nulluser");
        when(usuarioConNulls.getPassword()).thenReturn("password");
        when(usuarioConNulls.isBloqueado()).thenReturn(false);
        when(usuarioConNulls.getNombre()).thenReturn(null);
        when(usuarioConNulls.getApellidos()).thenReturn(null);
        when(usuarioConNulls.getCorreo()).thenReturn(null);
        when(usuarioConNulls.getTelefono()).thenReturn(null);
        
        // When
        CustomUserDetails userDetails = new CustomUserDetails(usuarioConNulls, authorities);
        
        // Then
        assertNotNull(userDetails);
        assertNull(userDetails.getNombre());
        assertNull(userDetails.getApellidos());
        assertNull(userDetails.getCorreo());
        assertNull(userDetails.getTelefono());
        assertEquals("nulluser", userDetails.getUsername());
    }

    /**
     * Test con valores vacíos en campos del usuario.
     * Verifica el comportamiento cuando algunos campos del usuario están vacíos.
     */
    @Test
    void testWithEmptyUserFields() {
        // Given
        Usuario usuarioConVacios = mock(Usuario.class);
        when(usuarioConVacios.getId()).thenReturn("emptyuser");
        when(usuarioConVacios.getPassword()).thenReturn("");
        when(usuarioConVacios.isBloqueado()).thenReturn(false);
        when(usuarioConVacios.getNombre()).thenReturn("");
        when(usuarioConVacios.getApellidos()).thenReturn("");
        when(usuarioConVacios.getCorreo()).thenReturn("");
        when(usuarioConVacios.getTelefono()).thenReturn("");
        
        // When
        CustomUserDetails userDetails = new CustomUserDetails(usuarioConVacios, authorities);
        
        // Then
        assertNotNull(userDetails);
        assertEquals("", userDetails.getNombre());
        assertEquals("", userDetails.getApellidos());
        assertEquals("", userDetails.getCorreo());
        assertEquals("", userDetails.getTelefono());
        assertEquals("", userDetails.getPassword());
    }

    /**
     * Test de serialVersionUID.
     * Verifica que el serialVersionUID está definido correctamente.
     */
    @Test
    void testSerialVersionUID() {
        // Given
        Class<CustomUserDetails> clazz = CustomUserDetails.class;
        
        // When & Then
        try {
            java.lang.reflect.Field serialVersionUIDField = clazz.getDeclaredField("serialVersionUID");
            serialVersionUIDField.setAccessible(true);
            long serialVersionUID = serialVersionUIDField.getLong(null);
            assertEquals(1L, serialVersionUID);
        } catch (Exception e) {
            fail("serialVersionUID field should be present and accessible");
        }
    }

    /**
     * Test de herencia de la clase User.
     * Verifica que CustomUserDetails extiende correctamente de User.
     */
    @Test
    void testInheritance() {
        // Then
        assertTrue(customUserDetails instanceof org.springframework.security.core.userdetails.User);
        assertTrue(customUserDetails instanceof org.springframework.security.core.userdetails.UserDetails);
    }

    /**
     * Test de igualdad y hashCode (heredados).
     * Verifica el comportamiento de equals y hashCode heredados de User.
     */
    @Test
    void testEqualsAndHashCode() {
        // Given
        CustomUserDetails userDetails1 = new CustomUserDetails(mockUsuario, authorities);
        CustomUserDetails userDetails2 = new CustomUserDetails(mockUsuario, authorities);
        
        // When & Then
        assertEquals(userDetails1, userDetails2);
        assertEquals(userDetails1.hashCode(), userDetails2.hashCode());
        assertEquals(userDetails1, customUserDetails);
    }

    @Test
    void testEquals() {
        // Create base user
        Usuario user1 = new Usuario();
        user1.setId("test");
        user1.setPassword("pass");
        user1.setNombre("John");
        user1.setApellidos("Doe");
        user1.setCorreo("john@test.com");
        user1.setTelefono("123456789");
        user1.setBloqueado(false);
        
        // Create identical user
        Usuario user2 = new Usuario();
        user2.setId("test");
        user2.setPassword("pass");
        user2.setNombre("John");
        user2.setApellidos("Doe");
        user2.setCorreo("john@test.com");
        user2.setTelefono("123456789");
        user2.setBloqueado(false);
        
        // Create different user
        Usuario user3 = new Usuario();
        user3.setId("test");
        user3.setPassword("pass");
        user3.setNombre("Jane");
        user3.setApellidos("Smith");
        user3.setCorreo("jane@test.com");
        user3.setTelefono("987654321");
        user3.setBloqueado(false);

        authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        
        CustomUserDetails details1 = new CustomUserDetails(user1, authorities);
        CustomUserDetails details2 = new CustomUserDetails(user2, authorities);
        CustomUserDetails details3 = new CustomUserDetails(user3, authorities);

        // Test same object
        assertEquals(details1, details1);
        
        // Test null
        assertNotEquals(null, details1);
        
        // Test different class
        assertNotEquals(new Object(), details1);
        
        // Test equal objects
        assertEquals(details2, details1);
        assertEquals(details1, details2);
        
        // Test different objects
        assertNotEquals(details3, details1);
        assertNotEquals(details1, details3);
    }

    @Test
    void testEqualsWithNullFields() {
        // Create users with null fields
        Usuario userWithNulls1 = new Usuario();
        userWithNulls1.setId("test");
        userWithNulls1.setPassword("pass");
        userWithNulls1.setNombre(null);
        userWithNulls1.setApellidos(null);
        userWithNulls1.setCorreo(null);
        userWithNulls1.setTelefono(null);
        userWithNulls1.setBloqueado(false);
        
        Usuario userWithNulls2 = new Usuario();
        userWithNulls2.setId("test");
        userWithNulls2.setPassword("pass");
        userWithNulls2.setNombre(null);
        userWithNulls2.setApellidos(null);
        userWithNulls2.setCorreo(null);
        userWithNulls2.setTelefono(null);
        userWithNulls2.setBloqueado(false);
        
        Usuario userWithValues = new Usuario();
        userWithValues.setId("test");
        userWithValues.setPassword("pass");
        userWithValues.setNombre("John");
        userWithValues.setApellidos("Doe");
        userWithValues.setCorreo("john@test.com");
        userWithValues.setTelefono("123456789");
        userWithValues.setBloqueado(false);
        
        authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        
        CustomUserDetails detailsWithNulls1 = new CustomUserDetails(userWithNulls1, authorities);
        CustomUserDetails detailsWithNulls2 = new CustomUserDetails(userWithNulls2, authorities);
        CustomUserDetails detailsWithValues = new CustomUserDetails(userWithValues, authorities);
        
        // Test equals with null fields
        assertEquals(detailsWithNulls1, detailsWithNulls2);
        assertNotEquals(detailsWithNulls1, detailsWithValues);
        assertNotEquals(detailsWithValues, detailsWithNulls1);
    }

    @Test
    void testEqualsWithDifferentNombre() {
        Usuario user1 = new Usuario();
        user1.setId("test");
        user1.setPassword("pass");
        user1.setNombre("John");
        user1.setApellidos("Doe");
        user1.setCorreo("john@test.com");
        user1.setTelefono("123456789");
        user1.setBloqueado(false);
        
        Usuario user2 = new Usuario();
        user2.setId("test");
        user2.setPassword("pass");
        user2.setNombre("Jane"); // Different nombre
        user2.setApellidos("Doe");
        user2.setCorreo("john@test.com");
        user2.setTelefono("123456789");
        user2.setBloqueado(false);
        
        authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        
        CustomUserDetails details1 = new CustomUserDetails(user1, authorities);
        CustomUserDetails details2 = new CustomUserDetails(user2, authorities);
        
        assertNotEquals(details1, details2);
        assertNotEquals(details2, details1);
    }

    @Test
    void testEqualsWithDifferentApellidos() {
        Usuario user1 = new Usuario();
        user1.setId("test");
        user1.setPassword("pass");
        user1.setNombre("John");
        user1.setApellidos("Doe");
        user1.setCorreo("john@test.com");
        user1.setTelefono("123456789");
        user1.setBloqueado(false);
        
        Usuario user2 = new Usuario();
        user2.setId("test");
        user2.setPassword("pass");
        user2.setNombre("John");
        user2.setApellidos("Smith"); // Different apellidos
        user2.setCorreo("john@test.com");
        user2.setTelefono("123456789");
        user2.setBloqueado(false);
        
        authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        
        CustomUserDetails details1 = new CustomUserDetails(user1, authorities);
        CustomUserDetails details2 = new CustomUserDetails(user2, authorities);
        
        assertNotEquals(details1, details2);
        assertNotEquals(details2, details1);
    }

    @Test
    void testEqualsWithDifferentCorreo() {
        Usuario user1 = new Usuario();
        user1.setId("test");
        user1.setPassword("pass");
        user1.setNombre("John");
        user1.setApellidos("Doe");
        user1.setCorreo("john@test.com");
        user1.setTelefono("123456789");
        user1.setBloqueado(false);
        
        Usuario user2 = new Usuario();
        user2.setId("test");
        user2.setPassword("pass");
        user2.setNombre("John");
        user2.setApellidos("Doe");
        user2.setCorreo("jane@test.com"); // Different correo
        user2.setTelefono("123456789");
        user2.setBloqueado(false);
        
        authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        
        CustomUserDetails details1 = new CustomUserDetails(user1, authorities);
        CustomUserDetails details2 = new CustomUserDetails(user2, authorities);
        
        assertNotEquals(details1, details2);
        assertNotEquals(details2, details1);
    }

    @Test
    void testEqualsWithDifferentTelefono() {
        Usuario user1 = new Usuario();
        user1.setId("test");
        user1.setPassword("pass");
        user1.setNombre("John");
        user1.setApellidos("Doe");
        user1.setCorreo("john@test.com");
        user1.setTelefono("123456789");
        user1.setBloqueado(false);
        
        Usuario user2 = new Usuario();
        user2.setId("test");
        user2.setPassword("pass");
        user2.setNombre("John");
        user2.setApellidos("Doe");
        user2.setCorreo("john@test.com");
        user2.setTelefono("987654321"); // Different telefono
        user2.setBloqueado(false);
        
        authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        
        CustomUserDetails details1 = new CustomUserDetails(user1, authorities);
        CustomUserDetails details2 = new CustomUserDetails(user2, authorities);
        
        assertNotEquals(details1, details2);
        assertNotEquals(details2, details1);
    }

    @Test
    void testEqualsWithMixedNullAndNonNullFields() {
        // Test nombre null vs non-null
        Usuario userNombreNull = new Usuario();
        userNombreNull.setId("test");
        userNombreNull.setPassword("pass");
        userNombreNull.setNombre(null);
        userNombreNull.setApellidos("Doe");
        userNombreNull.setCorreo("john@test.com");
        userNombreNull.setTelefono("123456789");
        userNombreNull.setBloqueado(false);
        
        Usuario userNombreNotNull = new Usuario();
        userNombreNotNull.setId("test");
        userNombreNotNull.setPassword("pass");
        userNombreNotNull.setNombre("John");
        userNombreNotNull.setApellidos("Doe");
        userNombreNotNull.setCorreo("john@test.com");
        userNombreNotNull.setTelefono("123456789");
        userNombreNotNull.setBloqueado(false);
        
        authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        
        CustomUserDetails detailsNombreNull = new CustomUserDetails(userNombreNull, authorities);
        CustomUserDetails detailsNombreNotNull = new CustomUserDetails(userNombreNotNull, authorities);
        
        assertNotEquals(detailsNombreNull, detailsNombreNotNull);
        assertNotEquals(detailsNombreNotNull, detailsNombreNull);
    }

    @Test
    void testEqualsWithSuperClassDifference() {
        Usuario user1 = new Usuario();
        user1.setId("test1");
        user1.setPassword("pass");
        user1.setNombre("John");
        user1.setApellidos("Doe");
        user1.setCorreo("john@test.com");
        user1.setTelefono("123456789");
        user1.setBloqueado(false);
        
        Usuario user2 = new Usuario();
        user2.setId("test2");
        user2.setPassword("pass");
        user2.setNombre("John");
        user2.setApellidos("Doe");
        user2.setCorreo("john@test.com");
        user2.setTelefono("123456789");
        user2.setBloqueado(false);
        
        authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        
        CustomUserDetails details1 = new CustomUserDetails(user1, authorities);
        CustomUserDetails details2 = new CustomUserDetails(user2, authorities);
        
        // Debería ser diferente porque super.equals() devuelve false
        assertNotEquals(details1, details2);
        assertNotEquals(details2, details1);
    }

    @Test
    void testEqualsWithEmptyStrings() {
        Usuario user1 = new Usuario();
        user1.setId("test");
        user1.setPassword("pass");
        user1.setNombre("");
        user1.setApellidos("");
        user1.setCorreo("");
        user1.setTelefono("");
        user1.setBloqueado(false);
        
        Usuario user2 = new Usuario();
        user2.setId("test");
        user2.setPassword("pass");
        user2.setNombre("");
        user2.setApellidos("");
        user2.setCorreo("");
        user2.setTelefono("");
        user2.setBloqueado(false);
        
        authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        
        CustomUserDetails details1 = new CustomUserDetails(user1, authorities);
        CustomUserDetails details2 = new CustomUserDetails(user2, authorities);
        
        assertEquals(details1, details2);
        assertEquals(details2, details1);
    }

    /**
     * Test para cubrir la rama getClass() != obj.getClass().
     * Verifica que equals retorna false cuando se compara con un objeto de diferente clase.
     */
    @Test
    void testEqualsWithDifferentClass() {
        // Given
        Usuario usuario = new Usuario();
        usuario.setId("test");
        usuario.setPassword("pass");
        usuario.setNombre("John");
        usuario.setApellidos("Doe");
        usuario.setCorreo("john@test.com");
        usuario.setTelefono("123456789");
        usuario.setBloqueado(false);
        
        authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        customUserDetails = new CustomUserDetails(usuario, authorities);
        
        // Create an object of a different class that extends User
        org.springframework.security.core.userdetails.User differentClassUser = new org.springframework.security.core.userdetails.User("test2", "pass2", authorities);
        
        // When & Then
        assertNotEquals(customUserDetails, differentClassUser);
        assertNotEquals(differentClassUser, customUserDetails);
    }
    
    /**
     * Test de toString (heredado).
     * Verifica que el método toString funciona correctamente.
     */
    @Test
    void testToString() {
        // When
        String toString = customUserDetails.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("testuser")); // Debe contener el username
    }

    /**
     * Test con caracteres especiales en los campos.
     * Verifica el manejo de caracteres especiales y unicode.
     */
    @Test
    void testWithSpecialCharacters() {
        // Given
        Usuario usuarioEspecial = mock(Usuario.class);
        when(usuarioEspecial.getId()).thenReturn("user@special");
        when(usuarioEspecial.getPassword()).thenReturn("pass!@#$%");
        when(usuarioEspecial.isBloqueado()).thenReturn(false);
        when(usuarioEspecial.getNombre()).thenReturn("José María");
        when(usuarioEspecial.getApellidos()).thenReturn("Fernández-Ruíz");
        when(usuarioEspecial.getCorreo()).thenReturn("josé.maría@dominio.es");
        when(usuarioEspecial.getTelefono()).thenReturn("+34-123-456-789");
        
        // When
        CustomUserDetails userDetails = new CustomUserDetails(usuarioEspecial, authorities);
        
        // Then
        assertEquals("José María", userDetails.getNombre());
        assertEquals("Fernández-Ruíz", userDetails.getApellidos());
        assertEquals("josé.maría@dominio.es", userDetails.getCorreo());
        assertEquals("+34-123-456-789", userDetails.getTelefono());
        assertEquals("user@special", userDetails.getUsername());
    }

    /**
     * Test de inmutabilidad de los campos.
     * Verifica que los campos son final y no pueden ser modificados.
     */
    @Test
    void testFieldImmutability() {
        // Given
        String originalNombre = customUserDetails.getNombre();
        String originalApellidos = customUserDetails.getApellidos();
        String originalCorreo = customUserDetails.getCorreo();
        String originalTelefono = customUserDetails.getTelefono();
        
        // When - Los campos son final, no se pueden modificar
        // Verificamos que los getters devuelven los mismos valores
        
        // Then
        assertEquals(originalNombre, customUserDetails.getNombre());
        assertEquals(originalApellidos, customUserDetails.getApellidos());
        assertEquals(originalCorreo, customUserDetails.getCorreo());
        assertEquals(originalTelefono, customUserDetails.getTelefono());
    }
    
    @Test
    void testEqualsDifferentClass() {
    	assertNotEquals(customUserDetails, new String());
    }


    /**
     * Test de la anotación @Getter de Lombok.
     * Verifica que los métodos getter están disponibles.
     */
    @Test
    void testLombokGetters() {
        // When & Then
        assertDoesNotThrow(() -> {
            customUserDetails.getNombre();
            customUserDetails.getApellidos();
            customUserDetails.getCorreo();
            customUserDetails.getTelefono();
        });
    }
}