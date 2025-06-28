package es.ubu.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.userdetails.DaoAuthenticationConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Clase de test para SecurityConfig.
 */
@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private CustomUserDetailsService mockUserDetailsService;
    
    @Mock
    private HttpSecurity mockHttpSecurity;
    
    @Mock
    private AuthenticationManagerBuilder mockAuthBuilder;
    
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(mockUserDetailsService);
    }

    /**
     * Test del constructor de SecurityConfig.
     * Verifica que el CustomUserDetailsService se inyecta correctamente.
     */
    @Test
    void testConstructor() {
        // Given
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
        
        // When
        SecurityConfig config = new SecurityConfig(userDetailsService);
        
        // Then
        assertNotNull(config);
        Object injectedService = ReflectionTestUtils.getField(config, "userDetailsService");
        assertEquals(userDetailsService, injectedService);
    }

    /**
     * Test del constructor con parámetro null.
     * Verifica que se puede crear la instancia incluso con null.
     */
    @Test
    void testConstructorWithNull() {
        // When
        SecurityConfig config = new SecurityConfig(null);
        
        // Then
        assertNotNull(config);
        Object injectedService = ReflectionTestUtils.getField(config, "userDetailsService");
        assertNull(injectedService);
    }

    /**
     * Test del bean PasswordEncoder.
     * Verifica que se crea correctamente una instancia de BCryptPasswordEncoder.
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
     * Verifica que el encoder funciona correctamente para codificar y verificar contraseñas.
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
     * Test del bean SecurityFilterChain.
     * Verifica que se crea correctamente la cadena de filtros de seguridad.
     */
    @Test
    void testFilterChain() throws Exception {
        // Given
        HttpSecurity httpSecurity = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        
        // Configurar mocks para el fluent API
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.formLogin(any())).thenReturn(httpSecurity);
        when(httpSecurity.logout(any())).thenReturn(httpSecurity);
        when(httpSecurity.build()).thenReturn(mock(DefaultSecurityFilterChain.class));
        
        // When
        SecurityFilterChain filterChain = securityConfig.filterChain(httpSecurity);
        
        // Then
        assertNotNull(filterChain);
        verify(httpSecurity).authorizeHttpRequests(any());
        verify(httpSecurity).formLogin(any());
        verify(httpSecurity).logout(any());
        verify(httpSecurity).build();
    }

    /**
     * Test específico para las configuraciones de autorización HTTP.
     * Verifica que se ejecutan las configuraciones de requestMatchers.
     */
    @Test
    @SuppressWarnings("unchecked")
    void testFilterChainAuthorizationConfiguration() throws Exception {
        // Given
        HttpSecurity httpSecurity = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authRegistry = 
            mock(AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class);
        
        // Crear mocks para las diferentes URLs
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl publicUrlsAuth = 
            mock(AuthorizeHttpRequestsConfigurer.AuthorizedUrl.class);
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl adminUrlsAuth = 
            mock(AuthorizeHttpRequestsConfigurer.AuthorizedUrl.class);
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl anyRequestUrl =
            mock(AuthorizeHttpRequestsConfigurer.AuthorizedUrl.class);
        
        // Configurar el comportamiento base de HttpSecurity
        when(httpSecurity.authorizeHttpRequests(any(Customizer.class))).thenAnswer(invocation -> {
            Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> customizer = 
                invocation.getArgument(0);
            customizer.customize(authRegistry);
            return httpSecurity;
        });
        when(httpSecurity.formLogin(any())).thenReturn(httpSecurity);
        when(httpSecurity.logout(any())).thenReturn(httpSecurity);
        when(httpSecurity.build()).thenReturn(mock(DefaultSecurityFilterChain.class));
        
        // Configurar comportamiento para URLs públicas
        when(authRegistry.requestMatchers("/login", "/registro", "/css/**", "/js/**", "/images/**"))
            .thenReturn(publicUrlsAuth);
        when(publicUrlsAuth.permitAll()).thenReturn(authRegistry);
        
        // Configurar comportamiento para URLs de admin
        when(authRegistry.requestMatchers("/admin/**")).thenReturn(adminUrlsAuth);
        when(adminUrlsAuth.hasAuthority("ADMIN")).thenReturn(authRegistry);
        
        // Configurar comportamiento para anyRequest
        when(authRegistry.anyRequest()).thenReturn(anyRequestUrl);
        when(anyRequestUrl.authenticated()).thenReturn(authRegistry);
        
        // When
        SecurityFilterChain filterChain = securityConfig.filterChain(httpSecurity);
        
        // Then
        assertNotNull(filterChain);
        
        // Verificar el orden de las configuraciones
        var inOrder = Mockito.inOrder(authRegistry, publicUrlsAuth, adminUrlsAuth, anyRequestUrl);
        
        // Verificar URLs públicas
        inOrder.verify(authRegistry).requestMatchers("/login", "/registro", "/css/**", "/js/**", "/images/**");
        inOrder.verify(publicUrlsAuth).permitAll();
        
        // Verificar URLs de admin
        inOrder.verify(authRegistry).requestMatchers("/admin/**");
        inOrder.verify(adminUrlsAuth).hasAuthority("ADMIN");
        
        // Verificar configuración de anyRequest
        inOrder.verify(authRegistry).anyRequest();
        inOrder.verify(anyRequestUrl).authenticated();
    }


    /**
     * Test específico para las configuraciones de form login.
     * Verifica que se ejecutan las configuraciones de loginPage, loginProcessingUrl, etc.
     */
    @Test
    @SuppressWarnings("unchecked")
    void testFilterChainFormLoginConfiguration() throws Exception {
        // Given
        HttpSecurity httpSecurity = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        FormLoginConfigurer<HttpSecurity> formLoginConfigurer = mock(FormLoginConfigurer.class);
        
        // Configurar comportamiento base de HttpSecurity
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.formLogin(any(Customizer.class))).thenAnswer(invocation -> {
            Customizer<FormLoginConfigurer<HttpSecurity>> customizer = invocation.getArgument(0);
            customizer.customize(formLoginConfigurer);
            return httpSecurity;
        });

        when(httpSecurity.logout(any())).thenReturn(httpSecurity);
        when(httpSecurity.build()).thenReturn(mock(DefaultSecurityFilterChain.class));
        
        // Configurar comportamiento del formLoginConfigurer con retorno fluido
        when(formLoginConfigurer.loginPage(anyString())).thenReturn(formLoginConfigurer);
        when(formLoginConfigurer.loginProcessingUrl(anyString())).thenReturn(formLoginConfigurer);
        when(formLoginConfigurer.usernameParameter(anyString())).thenReturn(formLoginConfigurer);
        when(formLoginConfigurer.passwordParameter(anyString())).thenReturn(formLoginConfigurer);
        when(formLoginConfigurer.defaultSuccessUrl(anyString(), any(Boolean.class))).thenReturn(formLoginConfigurer);
        when(formLoginConfigurer.failureUrl(anyString())).thenReturn(formLoginConfigurer);
        when(formLoginConfigurer.permitAll()).thenReturn(formLoginConfigurer);
        
        // When
        SecurityFilterChain filterChain = securityConfig.filterChain(httpSecurity);
        
        // Then
        assertNotNull(filterChain);
        
        // Verificar el orden de las configuraciones de form login
        var inOrder = Mockito.inOrder(formLoginConfigurer);
        
        inOrder.verify(formLoginConfigurer).loginPage("/login");
        inOrder.verify(formLoginConfigurer).loginProcessingUrl("/authenticate");
        inOrder.verify(formLoginConfigurer).usernameParameter("username");
        inOrder.verify(formLoginConfigurer).passwordParameter("password");
        inOrder.verify(formLoginConfigurer).defaultSuccessUrl("/menuprincipal", true);
        inOrder.verify(formLoginConfigurer).failureUrl("/login?error=true");
        inOrder.verify(formLoginConfigurer).permitAll();
        
        // Verificar que se llamaron los métodos principales de HttpSecurity
        verify(httpSecurity).authorizeHttpRequests(any());
        verify(httpSecurity).formLogin(any(Customizer.class));
        verify(httpSecurity).logout(any());
        verify(httpSecurity).build();
    }


    /**
     * Test específico para las configuraciones de logout.
     * Verifica que se ejecutan las configuraciones de logoutUrl y logoutSuccessUrl.
     */
    @Test
    @SuppressWarnings("unchecked")
    void testFilterChainLogoutConfiguration() throws Exception {
        // Given
        HttpSecurity httpSecurity = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        LogoutConfigurer<HttpSecurity> logoutConfigurer = mock(LogoutConfigurer.class);
        
        // Configurar mocks
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.formLogin(any())).thenReturn(httpSecurity);
        when(httpSecurity.logout(any(Customizer.class))).thenAnswer(invocation -> {
            Customizer<LogoutConfigurer<HttpSecurity>> customizer = invocation.getArgument(0);
            customizer.customize(logoutConfigurer);
            return httpSecurity;
        });
        when(httpSecurity.build()).thenReturn(mock(DefaultSecurityFilterChain.class));
        
        when(logoutConfigurer.logoutUrl(anyString())).thenReturn(logoutConfigurer);
        when(logoutConfigurer.logoutSuccessUrl(anyString())).thenReturn(logoutConfigurer);
        when(logoutConfigurer.permitAll()).thenReturn(logoutConfigurer);
        
        // When
        SecurityFilterChain filterChain = securityConfig.filterChain(httpSecurity);
        
        // Then
        assertNotNull(filterChain);
        verify(logoutConfigurer).logoutUrl("/logout");
        verify(logoutConfigurer).logoutSuccessUrl("/login?logout=true");
        verify(logoutConfigurer).permitAll();
    }

    /**
     * Test del método configureGlobal.
     * Verifica que se configura correctamente el AuthenticationManagerBuilder.
     */
    @Test
    void testConfigureGlobal() throws Exception {
        // Given
        AuthenticationManagerBuilder authBuilder = mock(AuthenticationManagerBuilder.class);
        DaoAuthenticationConfigurer<AuthenticationManagerBuilder, CustomUserDetailsService> daoConfigurer = mock(DaoAuthenticationConfigurer.class);
        
        when(authBuilder.userDetailsService(any(CustomUserDetailsService.class))).thenReturn(daoConfigurer);
        when(daoConfigurer.passwordEncoder(any(PasswordEncoder.class))).thenReturn(daoConfigurer);
        
        // When
        securityConfig.configureGlobal(authBuilder);
        
        // Then
        verify(authBuilder).userDetailsService(mockUserDetailsService);
        verify(daoConfigurer).passwordEncoder(any(BCryptPasswordEncoder.class));
    }

    /**
     * Test del método configureGlobal con excepción.
     * Verifica que las excepciones se propagan correctamente.
     */
    @Test
    void testConfigureGlobalWithException() throws Exception {
        // Given
        AuthenticationManagerBuilder authBuilder = mock(AuthenticationManagerBuilder.class);
        when(authBuilder.userDetailsService(any())).thenThrow(new RuntimeException("Test exception"));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            securityConfig.configureGlobal(authBuilder);
        });
    }

    /**
     * Test de configuración con AuthenticationManagerBuilder null.
     * Verifica el comportamiento cuando se pasa null al método configureGlobal.
     */
    @Test
    void testConfigureGlobalWithNullBuilder() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            securityConfig.configureGlobal(null);
        });
    }

    /**
     * Test de configuración con HttpSecurity null.
     * Verifica el comportamiento cuando se pasa null al método filterChain.
     */
    @Test
    void testFilterChainWithNullHttpSecurity() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            securityConfig.filterChain(null);
        });
    }

    /**
     * Test de integración del PasswordEncoder con diferentes contraseñas.
     * Verifica el comportamiento con contraseñas vacías y especiales.
     */
    @Test
    void testPasswordEncoderEdgeCases() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        
        // Test con contraseña vacía
        String emptyPassword = "";
        String encodedEmpty = passwordEncoder.encode(emptyPassword);
        assertNotNull(encodedEmpty);
        assertTrue(passwordEncoder.matches(emptyPassword, encodedEmpty));
        
        // Test con contraseña con caracteres especiales
        String specialPassword = "!@#$%^&*()_+-=[]{}|;':,.<>?";
        String encodedSpecial = passwordEncoder.encode(specialPassword);
        assertNotNull(encodedSpecial);
        assertTrue(passwordEncoder.matches(specialPassword, encodedSpecial));
        
        // Test con contraseña muy larga
        String longPassword = "a".repeat(50);
        String encodedLong = passwordEncoder.encode(longPassword);
        assertNotNull(encodedLong);
        assertTrue(passwordEncoder.matches(longPassword, encodedLong));
    }

    /**
     * Test de múltiples instancias del PasswordEncoder.
     * Verifica que cada llamada al método passwordEncoder() devuelve una nueva instancia.
     */
    @Test
    void testMultiplePasswordEncoderInstances() {
        // When
        PasswordEncoder encoder1 = securityConfig.passwordEncoder();
        PasswordEncoder encoder2 = securityConfig.passwordEncoder();
        
        // Then
        assertNotNull(encoder1);
        assertNotNull(encoder2);
        assertNotSame(encoder1, encoder2); // Diferentes instancias
        assertEquals(encoder1.getClass(), encoder2.getClass()); // Mismo tipo
    }

    /**
     * Test de consistencia del PasswordEncoder.
     * Verifica que diferentes instancias del encoder pueden verificar contraseñas codificadas por otras.
     */
    @Test
    void testPasswordEncoderConsistency() {
        // Given
        PasswordEncoder encoder1 = securityConfig.passwordEncoder();
        PasswordEncoder encoder2 = securityConfig.passwordEncoder();
        String password = "testPassword";
        
        // When
        String encoded = encoder1.encode(password);
        
        // Then
        assertTrue(encoder2.matches(password, encoded));
        assertTrue(encoder1.matches(password, encoded));
    }

    /**
     * Test del userDetailsService inyectado.
     * Verifica que el servicio se mantiene correctamente en la instancia.
     */
    @Test
    void testUserDetailsServiceInjection() {
        // Given
        CustomUserDetailsService customService = mock(CustomUserDetailsService.class);
        SecurityConfig config = new SecurityConfig(customService);
        
        // When
        Object injectedService = ReflectionTestUtils.getField(config, "userDetailsService");
        
        // Then
        assertSame(customService, injectedService);
    }

    /**
     * Test de la anotación @Configuration.
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
        assertTrue(clazz.getMethod("filterChain", HttpSecurity.class).isAnnotationPresent(org.springframework.context.annotation.Bean.class));
    }

    /**
     * Test para verificar la anotación @Autowired en configureGlobal.
     * Verifica que el método tiene la anotación correcta.
     */
    @Test
    void testConfigureGlobalAutowiredAnnotation() throws NoSuchMethodException {
        // Given
        Class<SecurityConfig> clazz = SecurityConfig.class;
        
        // When & Then
        assertTrue(clazz.getMethod("configureGlobal", AuthenticationManagerBuilder.class)
            .isAnnotationPresent(org.springframework.beans.factory.annotation.Autowired.class));
    }

    /**
     * Test para verificar la anotación @EnableMethodSecurity con parámetros.
     * Verifica que la anotación tiene los parámetros correctos.
     */
    @Test
    void testEnableMethodSecurityAnnotation() {
        // Given
        Class<SecurityConfig> clazz = SecurityConfig.class;
        
        // When
        org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity annotation = 
            clazz.getAnnotation(org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity.class);
        
        // Then
        assertNotNull(annotation);
        assertTrue(annotation.securedEnabled());
    }
}