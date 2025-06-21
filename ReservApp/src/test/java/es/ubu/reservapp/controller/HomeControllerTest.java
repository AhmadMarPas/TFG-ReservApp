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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Test para el controlador HomeController
 */
@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private SessionData sessionData;

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
        usuario.setEstablecimiento(new ArrayList<>());
    }

    @Test
    void testHome() {
        String viewName = homeController.home(model);
        
        verify(model).addAttribute(eq("usuario"), any(Usuario.class));
        
        assertEquals("login", viewName);
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
        
        usuario.setEstablecimiento(establecimientos);
        when(sessionData.getUsuario()).thenReturn(usuario);
        
        String viewName = homeController.misReservas(model);
        
        verify(model).addAttribute("establecimientos", establecimientos);
        
        assertEquals("reservas/misreservas", viewName);
    }
}