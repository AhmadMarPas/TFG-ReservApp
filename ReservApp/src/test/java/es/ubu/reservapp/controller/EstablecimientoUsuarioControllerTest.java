package es.ubu.reservapp.controller;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test para el controlador EstablecimientoUsuarioController
 */
@ExtendWith(MockitoExtension.class)
class EstablecimientoUsuarioControllerTest {

    @Mock
    private SessionData sessionData;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private EstablecimientoUsuarioController establecimientoUsuarioController;

    private Usuario usuario;
    private List<Establecimiento> establecimientos;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId("usuario1");
        usuario.setNombre("Usuario Test");
        usuario.setApellidos("Apellidos Test");
        usuario.setCorreo("test@example.com");
        
        Establecimiento establecimiento1 = new Establecimiento();
        establecimiento1.setId(1);
        establecimiento1.setNombre("Establecimiento 1");
        establecimiento1.setDireccion("Dirección 1");
        establecimiento1.setCapacidad(10);
        establecimiento1.setActivo(true);
        
        Establecimiento establecimiento2 = new Establecimiento();
        establecimiento2.setId(2);
        establecimiento2.setNombre("Establecimiento 2");
        establecimiento2.setDireccion("Dirección 2");
        establecimiento2.setCapacidad(15);
        establecimiento2.setActivo(true);
        
        establecimientos = new ArrayList<>();
        establecimientos.add(establecimiento1);
        establecimientos.add(establecimiento2);
        
        usuario.setLstEstablecimientos(establecimientos);
    }

    @Test
    void testListarEstablecimientosUsuarioSuccess() {
        when(sessionData.getUsuario()).thenReturn(usuario);
        
        String viewName = establecimientoUsuarioController.listarEstablecimientosUsuario(model, redirectAttributes);
        
        verify(model).addAttribute("establecimientos", establecimientos);
        assertEquals("establecimientos/listado_usuario", viewName);
    }

    @Test
    void testListarEstablecimientosUsuarioSinEstablecimientos() {
        usuario.setLstEstablecimientos(new ArrayList<>());
        when(sessionData.getUsuario()).thenReturn(usuario);
        
        String viewName = establecimientoUsuarioController.listarEstablecimientosUsuario(model, redirectAttributes);
        
        verify(model).addAttribute("establecimientos", new ArrayList<>());
        assertEquals("establecimientos/listado_usuario", viewName);
    }

    @Test
    void testListarEstablecimientosUsuarioNoAutenticado() {
        when(sessionData.getUsuario()).thenReturn(null);
        
        String viewName = establecimientoUsuarioController.listarEstablecimientosUsuario(model, redirectAttributes);
        
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        assertEquals("redirect:/", viewName);
    }

    @Test
    void testListarEstablecimientosUsuarioConExcepcion() {
        when(sessionData.getUsuario()).thenThrow(new RuntimeException("Error de prueba"));
        
        String viewName = establecimientoUsuarioController.listarEstablecimientosUsuario(model, redirectAttributes);
        
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        assertEquals("redirect:/menuprincipal", viewName);
    }

    @Test
    void testListarEstablecimientosUsuarioConEstablecimientosNulos() {
        usuario.setLstEstablecimientos(null);
        when(sessionData.getUsuario()).thenReturn(usuario);
        
        String viewName = establecimientoUsuarioController.listarEstablecimientosUsuario(model, redirectAttributes);
        
        verify(model).addAttribute("establecimientos", null);
        assertEquals("establecimientos/listado_usuario", viewName);
    }
}