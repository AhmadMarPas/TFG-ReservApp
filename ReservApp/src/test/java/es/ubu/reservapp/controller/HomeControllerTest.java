package es.ubu.reservapp.controller;

import es.ubu.reservapp.model.entities.Establecimiento;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Test para el controlador HomeController
 */
@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private SessionData sessionData;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private Model model;

    @InjectMocks
    private HomeController homeController;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId("usuario1");
        usuario.setNombre("Usuario");
        usuario.setApellidos("Test");
        usuario.setLstEstablecimientos(new ArrayList<>());
    }

    @Test
    void testConstructor() {
        // Arrange & Act
        HomeController controller = new HomeController(sessionData, usuarioService);
        
        // Assert
        assertNotNull(controller);
    }

    @Test
    void testHome() {
        String viewName = homeController.home();
        
        assertEquals("redirect:/menuprincipal", viewName);
    }

    @Test
    void testMenuPrincipal() {
        String viewName = homeController.menuprincipal();
        
        assertEquals("menuprincipal", viewName);
    }

    @Test
    void testMisReservas() {
        List<Establecimiento> establecimientos = new ArrayList<>();
        Establecimiento est1 = new Establecimiento();
        est1.setId(1);
        est1.setNombre("Establecimiento 1");
        establecimientos.add(est1);
        
        usuario.setLstEstablecimientos(establecimientos);
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(usuarioService.obtenerEstablecimientosUsuario(usuario, model)).thenReturn(model);
        
        String viewName = homeController.misReservas(model);
        
        verify(usuarioService).obtenerEstablecimientosUsuario(usuario, model);
        
        assertEquals("reservas/misreservas", viewName);
    }

    @Test
    void testMisReservasUsuarioNoAutenticado() {
        when(sessionData.getUsuario()).thenReturn(null);
        
        String viewName = homeController.misReservas(model);
        
        verify(model, never()).addAttribute(anyString(), any());
        
        assertEquals("redirect:/login", viewName);
    }

    @Test
    void testMisReservasSinEstablecimientos() {
        usuario.setLstEstablecimientos(new ArrayList<>());
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(usuarioService.obtenerEstablecimientosUsuario(usuario, model)).thenReturn(model);
        
        String viewName = homeController.misReservas(model);
        
        verify(usuarioService).obtenerEstablecimientosUsuario(usuario, model);
        
        assertEquals("reservas/misreservas", viewName);
    }

    @Test
    void testMisReservasConEstablecimientosNulos() {
        usuario.setLstEstablecimientos(null);
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(usuarioService.obtenerEstablecimientosUsuario(usuario, model)).thenReturn(model);
        
        String viewName = homeController.misReservas(model);
        
        verify(usuarioService).obtenerEstablecimientosUsuario(usuario, model);
        
        assertEquals("reservas/misreservas", viewName);
    }
}