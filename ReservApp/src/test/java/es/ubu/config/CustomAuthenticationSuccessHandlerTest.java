package es.ubu.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.UsuarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * Test para CustomAuthenticationSuccessHandler.
 * 
 * @author UBU
 */
@ExtendWith(MockitoExtension.class)
class CustomAuthenticationSuccessHandlerTest {

    @Mock
    private UsuarioService usuarioService;
    
    @Mock
    private SessionData sessionData;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private Authentication authentication;
    
    @Mock
    private UserDetails userDetails;
    
    @InjectMocks
    private CustomAuthenticationSuccessHandler successHandler;
    
    private Usuario usuario;
    
    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId("testUser");
        usuario.setNombre("Test User");
    }
    
    /**
     * Test del constructor.
     * Verifica que se inyectan correctamente las dependencias.
     */
    @Test
    void testConstructor() {
        // When
        CustomAuthenticationSuccessHandler handler = new CustomAuthenticationSuccessHandler(sessionData, usuarioService);
        
        // Then
        assertNotNull(handler);
    }
    
    /**
     * Test del constructor con parámetros null.
     * Verifica que se pueden inyectar dependencias null.
     */
    @Test
    void testConstructorWithNullParams() {
        // When
        CustomAuthenticationSuccessHandler handler = new CustomAuthenticationSuccessHandler(null, null);
        
        // Then
        assertNotNull(handler);
    }
    
    /**
     * Test de onAuthenticationSuccess con usuario encontrado.
     * Verifica que se establece correctamente el usuario en SessionData,
     * se actualiza la fecha de último acceso y se guarda el usuario.
     */
    @Test
    void testOnAuthenticationSuccessWithUserFound() throws IOException, ServletException {
        // Given
        String username = "testUser";
        LocalDateTime fechaAnterior = LocalDateTime.now().minusDays(1);
        usuario.setFechaUltimoAcceso(fechaAnterior);
        
        when(authentication.getName()).thenReturn(username);
        when(usuarioService.findUsuarioById(username)).thenReturn(usuario);
        
        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);
        
        // Then
        verify(usuarioService).findUsuarioById(username);
        verify(usuarioService).save(argThat(savedUser -> 
            savedUser.getFechaUltimoAcceso() != null && 
            savedUser.getFechaUltimoAcceso().isAfter(fechaAnterior)
        ));
        verify(sessionData).setUsuario(usuario);
        verify(response).sendRedirect("/menuprincipal");
    }
    
    /**
     * Test de onAuthenticationSuccess con usuario no encontrado.
     * Verifica que se maneja correctamente cuando el usuario no existe.
     */
    @Test
    void testOnAuthenticationSuccessWithUserNotFound() throws IOException, ServletException {
        // Given
        String username = "nonExistentUser";
        when(authentication.getName()).thenReturn(username);
        when(usuarioService.findUsuarioById(username)).thenReturn(null);
        
        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);
        
        // Then
        verify(usuarioService).findUsuarioById(username);
        verify(usuarioService, never()).save(any());
        verify(sessionData, never()).setUsuario(any());
        verify(response).sendRedirect("/menuprincipal");
    }
    
    /**
     * Test de onAuthenticationSuccess con excepción en usuarioService.
     * Verifica que se propagan las excepciones correctamente.
     */
    @Test
    void testOnAuthenticationSuccessWithServiceException() {
        // Given
        String username = "testUser";
        when(authentication.getName()).thenReturn(username);
        when(usuarioService.findUsuarioById(username)).thenThrow(new RuntimeException("Service error"));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            successHandler.onAuthenticationSuccess(request, response, authentication);
        });
        
        verify(usuarioService).findUsuarioById(username);
        verify(usuarioService, never()).save(any());
        verify(sessionData, never()).setUsuario(any());
    }
    
    /**
     * Test de onAuthenticationSuccess con excepción en response.sendRedirect.
     * Verifica que se propagan las excepciones de IO correctamente.
     */
    @Test
    void testOnAuthenticationSuccessWithIOException() throws IOException {
        // Given
        String username = "testUser";
        when(authentication.getName()).thenReturn(username);
        when(usuarioService.findUsuarioById(username)).thenReturn(usuario);
        doThrow(new IOException("Redirect error")).when(response).sendRedirect(anyString());
        
        // When & Then
        assertThrows(IOException.class, () -> {
            successHandler.onAuthenticationSuccess(request, response, authentication);
        });
        
        verify(usuarioService).findUsuarioById(username);
        verify(usuarioService).save(any());
        verify(sessionData).setUsuario(usuario);
    }
    
    /**
     * Test de onAuthenticationSuccess con authentication null.
     * Verifica el comportamiento con parámetros null.
     */
    @Test
    void testOnAuthenticationSuccessWithNullAuthentication() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            successHandler.onAuthenticationSuccess(request, response, null);
        });
    }
    
    /**
     * Test de onAuthenticationSuccess con response null.
     * Verifica el comportamiento con response null.
     */
    @Test
    void testOnAuthenticationSuccessWithNullResponse() {
        // Given
        when(authentication.getName()).thenReturn("testUser");
        
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            successHandler.onAuthenticationSuccess(request, null, authentication);
        });
    }
    
    /**
     * Test para verificar que la fecha de último acceso se actualiza correctamente.
     * Verifica que se establece una fecha de último acceso posterior a la fecha anterior.
     */
    @Test
    void testFechaUltimoAccesoUpdate() throws IOException, ServletException {
        // Given
        String username = "testUser";
        LocalDateTime fechaAnterior = LocalDateTime.now().minusHours(1);
        usuario.setFechaUltimoAcceso(fechaAnterior);
        
        when(authentication.getName()).thenReturn(username);
        when(usuarioService.findUsuarioById(username)).thenReturn(usuario);
        
        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);
        
        // Then
        verify(usuarioService).save(argThat(savedUser -> {
            LocalDateTime fechaUltimoAcceso = savedUser.getFechaUltimoAcceso();
            return fechaUltimoAcceso != null && fechaUltimoAcceso.isAfter(fechaAnterior);
        }));
    }
    
    /**
     * Test para verificar que se establece fecha de último acceso cuando el usuario no la tenía.
     */
    @Test
    void testFechaUltimoAccesoFirstTime() throws IOException, ServletException {
        // Given
        String username = "testUser";
        usuario.setFechaUltimoAcceso(null); // Primera vez que se logea
        
        when(authentication.getName()).thenReturn(username);
        when(usuarioService.findUsuarioById(username)).thenReturn(usuario);
        
        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);
        
        // Then
        verify(usuarioService).save(argThat(savedUser -> 
            savedUser.getFechaUltimoAcceso() != null
        ));
    }
}