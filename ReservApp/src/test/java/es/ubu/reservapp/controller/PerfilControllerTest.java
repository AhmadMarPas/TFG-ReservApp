package es.ubu.reservapp.controller;

import es.ubu.reservapp.model.entities.Perfil;
import es.ubu.reservapp.service.PerfilService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerfilControllerTest {

    @Mock
    private PerfilService perfilService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PerfilController perfilController;

    private Perfil perfilAdmin;
    private Perfil perfilUsuario;
    private List<Perfil> perfiles;

    @BeforeEach
    void setUp() {
        // Configurar perfiles de prueba
        perfilAdmin = new Perfil();
        perfilAdmin.setId(1);
        perfilAdmin.setNombre("ADMIN");

        perfilUsuario = new Perfil();
        perfilUsuario.setId(2);
        perfilUsuario.setNombre("USUARIO");

        perfiles = new ArrayList<>();
        perfiles.add(perfilAdmin);
        perfiles.add(perfilUsuario);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testListarPerfiles_Exito() {
        when(perfilService.findAll()).thenReturn(perfiles);

        String viewName = perfilController.listarPerfiles(model);

        assertEquals("perfiles/listado", viewName);
        verify(model).addAttribute("perfiles", perfiles);
        verify(model).addAttribute("perfilActivoCount", 2L);
    }

    @Test
    void testListarPerfiles_Error() {
        when(perfilService.findAll()).thenThrow(new RuntimeException("Error de prueba"));

        String viewName = perfilController.listarPerfiles(model);

        assertEquals("perfiles/listado", viewName);
        verify(model).addAttribute(eq("error"), anyString());
    }

    @Test
    void testMostrarFormularioNuevoPerfil() {
        String viewName = perfilController.mostrarFormularioNuevoPerfil(model);

        assertEquals("perfiles/formulario", viewName);
        verify(model).addAttribute(eq("perfil"), any(Perfil.class));
        verify(model).addAttribute("isEdit", false);
    }

    @Test
    void testMostrarFormularioEditarPerfil_Exito() {
        when(perfilService.findById(1)).thenReturn(Optional.of(perfilAdmin));
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = perfilController.mostrarFormularioEditarPerfil(1, model, redirectAttributes);

        assertEquals("perfiles/formulario", viewName);
        verify(model).addAttribute("perfil", perfilAdmin);
        verify(model).addAttribute("isEdit", true);
        assertTrue(redirectAttributes.getFlashAttributes().isEmpty());
    }

    @Test
    void testMostrarFormularioEditarPerfil_PerfilNoEncontrado() {
        when(perfilService.findById(999)).thenReturn(Optional.empty());
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = perfilController.mostrarFormularioEditarPerfil(999, model, redirectAttributes);

        assertEquals("redirect:/admin/perfiles/listado", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Perfil no encontrado", redirectAttributes.getFlashAttributes().get("error"));
    }

    @Test
    void testMostrarFormularioEditarPerfil_Error() {
        when(perfilService.findById(1)).thenThrow(new RuntimeException("Error de prueba"));
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = perfilController.mostrarFormularioEditarPerfil(1, model, redirectAttributes);

        assertEquals("redirect:/admin/perfiles/listado", viewName);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("Error al cargar el perfil"));
    }

    @Test
    void testGuardarPerfil_ErroresValidacion() {
        Perfil perfilInvalido = new Perfil();
        when(bindingResult.hasErrors()).thenReturn(true);

        String viewName = perfilController.guardarPerfil(perfilInvalido, bindingResult, new RedirectAttributesModelMap(), model);

        assertEquals("perfiles/formulario", viewName);
        verify(model).addAttribute("isEdit", false); // ID es null
        verify(perfilService, never()).save(any(Perfil.class));
    }

    @Test
    void testGuardarPerfil_CreacionExitosa() {
        Perfil nuevoPerfil = new Perfil();
        nuevoPerfil.setNombre("NUEVO_PERFIL");
        when(bindingResult.hasErrors()).thenReturn(false);
        when(perfilService.save(nuevoPerfil)).thenReturn(nuevoPerfil);
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = perfilController.guardarPerfil(nuevoPerfil, bindingResult, redirectAttributes, model);

        assertEquals("redirect:/admin/perfiles/listado", viewName);
        verify(perfilService).save(nuevoPerfil);
        assertNotNull(redirectAttributes.getFlashAttributes().get("exito"));
        assertEquals("Perfil creado correctamente", redirectAttributes.getFlashAttributes().get("exito"));
    }

    @Test
    void testGuardarPerfil_ActualizacionExitosa() {
        Perfil perfilExistente = new Perfil();
        perfilExistente.setId(2);
        perfilExistente.setNombre("PERFIL_ACTUALIZADO");
        when(bindingResult.hasErrors()).thenReturn(false);
        when(perfilService.findById(2)).thenReturn(Optional.of(perfilUsuario));
        when(perfilService.save(perfilExistente)).thenReturn(perfilExistente);
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = perfilController.guardarPerfil(perfilExistente, bindingResult, redirectAttributes, model);

        assertEquals("redirect:/admin/perfiles/listado", viewName);
        verify(perfilService).save(perfilExistente);
        assertNotNull(redirectAttributes.getFlashAttributes().get("exito"));
        assertEquals("Perfil actualizado correctamente", redirectAttributes.getFlashAttributes().get("exito"));
    }

    @Test
    void testGuardarPerfil_PerfilNoEncontradoParaActualizar() {
        Perfil perfilInexistente = new Perfil();
        perfilInexistente.setId(999);
        perfilInexistente.setNombre("PERFIL_INEXISTENTE");
        when(bindingResult.hasErrors()).thenReturn(false);
        when(perfilService.findById(999)).thenReturn(Optional.empty());
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = perfilController.guardarPerfil(perfilInexistente, bindingResult, redirectAttributes, model);

        assertEquals("redirect:/admin/perfiles/listado", viewName);
        verify(perfilService, never()).save(any(Perfil.class));
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Perfil no encontrado para actualizar", redirectAttributes.getFlashAttributes().get("error"));
    }

    @Test
    void testGuardarPerfil_Error() {
        Perfil perfil = new Perfil();
        perfil.setNombre("PERFIL_ERROR");
        when(bindingResult.hasErrors()).thenReturn(false);
        when(perfilService.save(perfil)).thenThrow(new RuntimeException("Error de prueba"));

        String viewName = perfilController.guardarPerfil(perfil, bindingResult, new RedirectAttributesModelMap(), model);

        assertEquals("perfiles/formulario", viewName);
        verify(model).addAttribute(eq("error"), anyString());
        verify(model).addAttribute("isEdit", false);
    }

    @Test
    void testEliminarPerfil_Exito() {
        when(perfilService.findById(2)).thenReturn(Optional.of(perfilUsuario));
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = perfilController.eliminarPerfil(2, redirectAttributes);

        assertEquals("redirect:/admin/perfiles/listado", viewName);
        verify(perfilService).deleteById(2);
        assertNotNull(redirectAttributes.getFlashAttributes().get("exito"));
        assertEquals("Perfil eliminado correctamente.", redirectAttributes.getFlashAttributes().get("exito"));
    }

    @Test
    void testEliminarPerfil_PerfilAdmin() {
        when(perfilService.findById(1)).thenReturn(Optional.of(perfilAdmin));
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = perfilController.eliminarPerfil(1, redirectAttributes);

        assertEquals("redirect:/admin/perfiles/listado", viewName);
        verify(perfilService, never()).deleteById(anyInt());
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("El perfil 'ADMIN' no puede ser eliminado.", redirectAttributes.getFlashAttributes().get("error"));
    }

    @Test
    void testEliminarPerfil_PerfilNoEncontrado() {
        when(perfilService.findById(999)).thenReturn(Optional.empty());
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = perfilController.eliminarPerfil(999, redirectAttributes);

        assertEquals("redirect:/admin/perfiles/listado", viewName);
        verify(perfilService, never()).deleteById(anyInt());
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("Perfil no encontrado"));
    }

    @Test
    void testEliminarPerfil_Error() {
        when(perfilService.findById(2)).thenReturn(Optional.of(perfilUsuario));
        doThrow(new RuntimeException("Error al eliminar")).when(perfilService).deleteById(2);
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = perfilController.eliminarPerfil(2, redirectAttributes);

        assertEquals("redirect:/admin/perfiles/listado", viewName);
        verify(perfilService).deleteById(2);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(((String)redirectAttributes.getFlashAttributes().get("error")).contains("Error al eliminar el perfil"));
    }
}