package es.ubu.reservapp.controller;

import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test para el controlador LoginController
 */
@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private SessionData sessionData;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private LoginController loginController;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId("usuario1");
        usuario.setNombre("Usuario");
        usuario.setApellidos("Test");
        usuario.setPassword("password");
        usuario.setCorreo("usuario@test.com");
    }

    @Test
    void testLoginPage() {
        String viewName = loginController.loginPage(model, null, null);
        
        verify(model).addAttribute(eq("usuario"), any(Usuario.class));
        
        assertEquals("login", viewName);
    }
    
    @Test
    void testLoginPageWithError() {
        String viewName = loginController.loginPage(model, "true", null);
        
        verify(model).addAttribute("error", "Usuario o contraseña incorrectos.");
        verify(model).addAttribute(eq("usuario"), any(Usuario.class));
        
        assertEquals("login", viewName);
    }
    
    @Test
    void testLoginPageWithLogout() {
        String viewName = loginController.loginPage(model, null, "true");
        
        verify(model).addAttribute(eq("logout"), eq("Has cerrado sesión correctamente."));
        verify(model).addAttribute(eq("usuario"), any(Usuario.class));
        
        assertEquals("login", viewName);
    }

    @Test
    void testRegisterForm() {
        String viewName = loginController.registerForm(model);
        
        verify(model).addAttribute(eq("usuario"), any(Usuario.class));
        
        assertEquals("registro", viewName);
    }

    @Test
    void testRegistroWithValidationErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);
        
        String viewName = loginController.registro(usuario, bindingResult, "password", redirectAttributes);
        
        assertEquals("registro", viewName);
    }

    @Test
    void testRegistroWithExistingUsername() {
        when(bindingResult.hasErrors()).thenReturn(false);
        
        when(usuarioService.existeId("usuario1")).thenReturn(true);
        
        String viewName = loginController.registro(usuario, bindingResult, "password", redirectAttributes);
        
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        
        assertEquals("redirect:/registro", viewName);
    }

    @Test
    void testRegistroWithPasswordMismatch() {
        when(bindingResult.hasErrors()).thenReturn(false);
        
        when(usuarioService.existeId("usuario1")).thenReturn(false);
        
        String viewName = loginController.registro(usuario, bindingResult, "differentpassword", redirectAttributes);
        
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        
        assertEquals("redirect:/registro", viewName);
    }

    @Test
    void testRegistroWithExistingEmail() {
        when(bindingResult.hasErrors()).thenReturn(false);
        
        when(usuarioService.existeId("usuario1")).thenReturn(false);
        when(usuarioService.existeEmail("usuario@test.com")).thenReturn(true);
        
        String viewName = loginController.registro(usuario, bindingResult, "password", redirectAttributes);
        
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        
        assertEquals("redirect:/registro", viewName);
    }

    @Test
    void testRegistroSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);
        
        when(usuarioService.existeId("usuario1")).thenReturn(false);
        when(usuarioService.existeEmail("usuario@test.com")).thenReturn(false);
        
        String viewName = loginController.registro(usuario, bindingResult, "password", redirectAttributes);
        
        verify(usuarioService).save(any(Usuario.class));
        verify(sessionData).setUsuario(any(Usuario.class));
        
        verify(redirectAttributes).addFlashAttribute(eq("exito"), anyString());
        
        assertEquals("redirect:/menuprincipal", viewName);
    }

    @Test
    void testLogoutGet() {
        String viewName = loginController.logout(session);
        
        verify(session).invalidate();
        
        assertEquals("redirect:/login", viewName);
    }

    @Test
    void testLogoutPost() {
        when(request.getSession(false)).thenReturn(session);
        
        String viewName = loginController.logout(request);
        
        verify(session).invalidate();
        
        assertEquals("redirect:/login", viewName);
    }
}