package es.ubu.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Test para SecurityConfig.
 */
@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private CustomUserDetailsService mockUserDetailsService;
    
    @Mock
    private CustomAuthenticationSuccessHandler mockSuccessHandler;
    
    private SecurityConfig securityConfig;
    
    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(mockUserDetailsService, mockSuccessHandler);
    }
    
    /**
     * Test del constructor.
     * Verifica que se inyectan correctamente las dependencias.
     */
    @Test
    void testConstructor() {
        // Given
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
        CustomAuthenticationSuccessHandler successHandler = mock(CustomAuthenticationSuccessHandler.class);
        
        // When
        SecurityConfig config = new SecurityConfig(userDetailsService, successHandler);
        
        // Then
        assertNotNull(config);
        Object injectedService = ReflectionTestUtils.getField(config, "customUserDetailsService");
        Object injectedHandler = ReflectionTestUtils.getField(config, "successHandler");
        assertEquals(userDetailsService, injectedService);
        assertEquals(successHandler, injectedHandler);
    }
    
    /**
     * Test del constructor con parámetros null.
     * Verifica que se pueden inyectar dependencias null.
     */
    @Test
    void testConstructorWithNull() {
        // When
        SecurityConfig config = new SecurityConfig(null, null);
        
        // Then
        assertNotNull(config);
        Object injectedService = ReflectionTestUtils.getField(config, "customUserDetailsService");
        Object injectedHandler = ReflectionTestUtils.getField(config, "successHandler");
        assertNull(injectedService);
        assertNull(injectedHandler);
    }
    
    /**
     * Test del bean PasswordEncoder.
     * Verifica que se crea correctamente el encoder de contraseñas.
     */
    @Test
    void testPasswordEncoder() {
        // When
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        
        // Then
        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder);
    }
    
    /**
     * Test de funcionalidad del PasswordEncoder.
     * Verifica que el encoder funciona correctamente.
     */
    @Test
    void testPasswordEncoderFunctionality() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";
        
        // When
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // Then
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        assertFalse(passwordEncoder.matches("wrongPassword", encodedPassword));
    }
    
    /**
     * Test de consistencia del PasswordEncoder.
     * Verifica que diferentes codificaciones de la misma contraseña son diferentes.
     */
    @Test
    void testPasswordEncoderConsistency() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";
        
        // When
        String encoded1 = passwordEncoder.encode(rawPassword);
        String encoded2 = passwordEncoder.encode(rawPassword);
        
        // Then
        assertNotEquals(encoded1, encoded2); // BCrypt genera diferentes hashes
        assertTrue(passwordEncoder.matches(rawPassword, encoded1));
        assertTrue(passwordEncoder.matches(rawPassword, encoded2));
    }
    
    /**
     * Test del bean DaoAuthenticationProvider.
     * Verifica que se crea correctamente el proveedor de autenticación.
     */
    @Test
    void testAuthenticationProvider() {
        // When
        DaoAuthenticationProvider authProvider = securityConfig.authenticationProvider();
        
        // Then
        assertNotNull(authProvider);
        assertTrue(authProvider instanceof DaoAuthenticationProvider);
    }
    
    /**
     * Test de configuración del DaoAuthenticationProvider.
     * Verifica que se configuran correctamente el UserDetailsService y PasswordEncoder.
     */
    @Test
    void testAuthenticationProviderConfiguration() {
        // When
        DaoAuthenticationProvider authProvider = securityConfig.authenticationProvider();
        
        // Then
        assertNotNull(authProvider);
        // Verificar que se han establecido las dependencias
        Object userDetailsService = ReflectionTestUtils.getField(authProvider, "userDetailsService");
        Object passwordEncoder = ReflectionTestUtils.getField(authProvider, "passwordEncoder");
        assertEquals(mockUserDetailsService, userDetailsService);
        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder);
    }
    
    /**
     * Test del bean AuthenticationManager.
     * Verifica que se crea correctamente el gestor de autenticación.
     */
    @Test
    void testAuthenticationManager() throws Exception {
        // Given
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(authManager);
        
        // When
        AuthenticationManager result = securityConfig.authenticationManager(authConfig);
        
        // Then
        assertNotNull(result);
        assertEquals(authManager, result);
        verify(authConfig).getAuthenticationManager();
    }
    
    /**
     * Test del bean AuthenticationManager con excepción.
     * Verifica que se propagan las excepciones correctamente.
     */
    @Test
    void testAuthenticationManagerWithException() throws Exception {
        // Given
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        when(authConfig.getAuthenticationManager()).thenThrow(new RuntimeException("Auth error"));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            securityConfig.authenticationManager(authConfig);
        });
        
        verify(authConfig).getAuthenticationManager();
    }
    
    /**
     * Test del bean SecurityFilterChain.
     * Verifica que se crea correctamente la cadena de filtros de seguridad.
     */
    @Test
    void testFilterChain() throws Exception {
        // Given
        HttpSecurity httpSecurity = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        SecurityFilterChain filterChain = mock(DefaultSecurityFilterChain.class);
        
        // Configurar mocks para el flujo completo
        when(httpSecurity.authenticationProvider(any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.formLogin(any())).thenReturn(httpSecurity);
        when(httpSecurity.logout(any())).thenReturn(httpSecurity);
        when(httpSecurity.httpBasic(any())).thenReturn(httpSecurity);
        when(httpSecurity.build()).thenReturn((DefaultSecurityFilterChain) filterChain);
        
        // When
        SecurityFilterChain result = securityConfig.filterChain(httpSecurity);
        
        // Then
        assertNotNull(result);
        assertEquals(filterChain, result);
        
        // Verificar que se llamaron todos los métodos de configuración
        verify(httpSecurity).authenticationProvider(any(DaoAuthenticationProvider.class));
        verify(httpSecurity).authorizeHttpRequests(any());
        verify(httpSecurity).formLogin(any());
        verify(httpSecurity).logout(any());
        verify(httpSecurity).httpBasic(any());
        verify(httpSecurity).build();
    }
    
    /**
     * Test de configuración de autorización HTTP.
     * Verifica que se configuran correctamente las reglas de autorización.
     */
    @Test
    void testFilterChainAuthorizationConfiguration() throws Exception {
        // Given
        HttpSecurity httpSecurity = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        
        when(httpSecurity.authenticationProvider(any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenAnswer(invocation -> {
            // Simular la configuración de autorización
            return httpSecurity;
        });
        when(httpSecurity.formLogin(any())).thenReturn(httpSecurity);
        when(httpSecurity.logout(any())).thenReturn(httpSecurity);
        when(httpSecurity.httpBasic(any())).thenReturn(httpSecurity);
        when(httpSecurity.build()).thenReturn(mock(DefaultSecurityFilterChain.class));
        
        // When
        SecurityFilterChain result = securityConfig.filterChain(httpSecurity);
        
        // Then
        assertNotNull(result);
        verify(httpSecurity).authorizeHttpRequests(any());
    }
    
    /**
     * Test de configuración de form login.
     * Verifica que se configuran correctamente los parámetros de login.
     */
    @Test
    void testFilterChainFormLoginConfiguration() throws Exception {
        // Given
        HttpSecurity httpSecurity = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        
        when(httpSecurity.authenticationProvider(any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.formLogin(any())).thenAnswer(invocation -> {
            // Simular la configuración de form login
            return httpSecurity;
        });
        when(httpSecurity.logout(any())).thenReturn(httpSecurity);
        when(httpSecurity.httpBasic(any())).thenReturn(httpSecurity);
        when(httpSecurity.build()).thenReturn(mock(DefaultSecurityFilterChain.class));
        
        // When
        SecurityFilterChain result = securityConfig.filterChain(httpSecurity);
        
        // Then
        assertNotNull(result);
        verify(httpSecurity).formLogin(any());
    }
    
    /**
     * Test de configuración de logout.
     * Verifica que se configuran correctamente los parámetros de logout.
     */
    @Test
    void testFilterChainLogoutConfiguration() throws Exception {
        // Given
        HttpSecurity httpSecurity = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        
        when(httpSecurity.authenticationProvider(any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.formLogin(any())).thenReturn(httpSecurity);
        when(httpSecurity.logout(any())).thenAnswer(invocation -> {
            // Simular la configuración de logout
            return httpSecurity;
        });
        when(httpSecurity.httpBasic(any())).thenReturn(httpSecurity);
        when(httpSecurity.build()).thenReturn(mock(DefaultSecurityFilterChain.class));
        
        // When
        SecurityFilterChain result = securityConfig.filterChain(httpSecurity);
        
        // Then
        assertNotNull(result);
        verify(httpSecurity).logout(any());
    }
    
    /**
     * Test de configuración de HTTP Basic.
     * Verifica que se deshabilita correctamente HTTP Basic Auth.
     */
    @Test
    void testFilterChainHttpBasicConfiguration() throws Exception {
        // Given
        HttpSecurity httpSecurity = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        
        when(httpSecurity.authenticationProvider(any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.formLogin(any())).thenReturn(httpSecurity);
        when(httpSecurity.logout(any())).thenReturn(httpSecurity);
        when(httpSecurity.httpBasic(any())).thenAnswer(invocation -> {
            // Simular la configuración de HTTP Basic
            return httpSecurity;
        });
        when(httpSecurity.build()).thenReturn(mock(DefaultSecurityFilterChain.class));
        
        // When
        SecurityFilterChain result = securityConfig.filterChain(httpSecurity);
        
        // Then
        assertNotNull(result);
        verify(httpSecurity).httpBasic(any());
    }
    
    /**
     * Test de configuración del authentication provider en filter chain.
     * Verifica que se establece correctamente el proveedor de autenticación.
     */
    @Test
    void testFilterChainAuthenticationProviderConfiguration() throws Exception {
        // Given
        HttpSecurity httpSecurity = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        
        when(httpSecurity.authenticationProvider(any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.formLogin(any())).thenReturn(httpSecurity);
        when(httpSecurity.logout(any())).thenReturn(httpSecurity);
        when(httpSecurity.httpBasic(any())).thenReturn(httpSecurity);
        when(httpSecurity.build()).thenReturn(mock(DefaultSecurityFilterChain.class));
        
        // When
        securityConfig.filterChain(httpSecurity);
        
        // Then
        verify(httpSecurity).authenticationProvider(any(DaoAuthenticationProvider.class));
    }
    
    /**
     * Test de excepción en filterChain.
     * Verifica que se propagan las excepciones correctamente.
     */
    @Test
    void testFilterChainWithException() {
        // Given
        HttpSecurity httpSecurity = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        when(httpSecurity.authenticationProvider(any())).thenThrow(new RuntimeException("Config error"));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            securityConfig.filterChain(httpSecurity);
        });
        
        verify(httpSecurity).authenticationProvider(any());
    }
    
    /**
     * Test de las anotaciones de la clase.
     * Verifica que la clase tiene las anotaciones correctas.
     */
    @Test
    void testClassAnnotations() {
        // When
        Class<SecurityConfig> clazz = SecurityConfig.class;
        
        // Then
        assertTrue(clazz.isAnnotationPresent(org.springframework.context.annotation.Configuration.class));
        assertTrue(clazz.isAnnotationPresent(org.springframework.security.config.annotation.web.configuration.EnableWebSecurity.class));
        assertTrue(clazz.isAnnotationPresent(org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity.class));
        
        // Verificar configuración de EnableMethodSecurity
        var enableMethodSecurity = clazz.getAnnotation(org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity.class);
        assertTrue(enableMethodSecurity.securedEnabled());
    }
    
    /**
     * Test de los métodos @Bean.
     * Verifica que los métodos tienen las anotaciones @Bean correctas.
     */
    @Test
    void testBeanAnnotations() throws NoSuchMethodException {
        // Given
        Class<SecurityConfig> clazz = SecurityConfig.class;
        
        // When & Then
        assertTrue(clazz.getMethod("passwordEncoder").isAnnotationPresent(org.springframework.context.annotation.Bean.class));
        assertTrue(clazz.getMethod("authenticationProvider").isAnnotationPresent(org.springframework.context.annotation.Bean.class));
        assertTrue(clazz.getMethod("authenticationManager", AuthenticationConfiguration.class).isAnnotationPresent(org.springframework.context.annotation.Bean.class));
        assertTrue(clazz.getMethod("filterChain", HttpSecurity.class).isAnnotationPresent(org.springframework.context.annotation.Bean.class));
    }
    
    /**
     * Test de instanciación múltiple de beans.
     * Verifica que cada llamada a los métodos @Bean devuelve nuevas instancias.
     */
    @Test
    void testBeanInstantiation() {
        // When
        PasswordEncoder encoder1 = securityConfig.passwordEncoder();
        PasswordEncoder encoder2 = securityConfig.passwordEncoder();
        DaoAuthenticationProvider provider1 = securityConfig.authenticationProvider();
        DaoAuthenticationProvider provider2 = securityConfig.authenticationProvider();
        
        // Then
        assertNotNull(encoder1);
        assertNotNull(encoder2);
        assertNotSame(encoder1, encoder2); // Diferentes instancias
        
        assertNotNull(provider1);
        assertNotNull(provider2);
        assertNotSame(provider1, provider2); // Diferentes instancias
    }
    
    /**
     * Test de integración de componentes.
     * Verifica que los componentes se integran correctamente.
     */
    @Test
    void testComponentIntegration() {
        // When
        DaoAuthenticationProvider authProvider = securityConfig.authenticationProvider();
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        
        // Then
        assertNotNull(authProvider);
        assertNotNull(passwordEncoder);
        
        // Verificar que el provider usa el mismo tipo de encoder
        Object providerEncoder = ReflectionTestUtils.getField(authProvider, "passwordEncoder");
        assertTrue(providerEncoder instanceof BCryptPasswordEncoder);
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder);
    }
}