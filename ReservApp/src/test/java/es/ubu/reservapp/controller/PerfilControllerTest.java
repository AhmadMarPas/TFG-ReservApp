package es.ubu.reservapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import es.ubu.reservapp.model.entities.Perfil;
import es.ubu.reservapp.service.PerfilService;

/**
 * Clase de test para PerfilController.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class PerfilControllerTest {

    @Mock
    private PerfilService perfilService;

    @InjectMocks
    private PerfilController perfilController;

    private MockMvc mockMvc;
    private Perfil perfil;
    private List<Perfil> perfiles;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(perfilController).build();
        
        perfil = new Perfil();
        perfil.setId(1);
        perfil.setNombre("Test Perfil");
        
        Perfil perfil2 = new Perfil();
        perfil2.setId(2);
        perfil2.setNombre("Otro Perfil");
        
        perfiles = Arrays.asList(perfil, perfil2);
    }

    @Test
    void testListarPerfiles_Success() throws Exception {
        when(perfilService.findAll()).thenReturn(perfiles);

        mockMvc.perform(get("/admin/perfiles"))
                .andExpect(status().isOk())
                .andExpect(view().name("perfiles/listado"))
                .andExpect(model().attribute("perfiles", perfiles))
                .andExpect(model().attribute("perfilActivoCount", 2L));

        verify(perfilService, times(1)).findAll();
    }

    @Test
    void testListarPerfiles_WithSlash() throws Exception {
        when(perfilService.findAll()).thenReturn(perfiles);

        mockMvc.perform(get("/admin/perfiles/"))
                .andExpect(status().isOk())
                .andExpect(view().name("perfiles/listado"))
                .andExpect(model().attribute("perfiles", perfiles))
                .andExpect(model().attribute("perfilActivoCount", 2L));

        verify(perfilService, times(1)).findAll();
    }

    @Test
    void testListarPerfiles_WithListado() throws Exception {
        when(perfilService.findAll()).thenReturn(perfiles);

        mockMvc.perform(get("/admin/perfiles/listado"))
                .andExpect(status().isOk())
                .andExpect(view().name("perfiles/listado"))
                .andExpect(model().attribute("perfiles", perfiles))
                .andExpect(model().attribute("perfilActivoCount", 2L));

        verify(perfilService, times(1)).findAll();
    }

    @Test
    void testListarPerfiles_WithException() throws Exception {
        when(perfilService.findAll()).thenThrow(new RuntimeException("Error de base de datos"));

        mockMvc.perform(get("/admin/perfiles"))
                .andExpect(status().isOk())
                .andExpect(view().name("perfiles/listado"))
                .andExpect(model().attribute("error", "Error al cargar la lista de perfiles: Error de base de datos"));

        verify(perfilService, times(1)).findAll();
    }

    @Test
    void testMostrarFormularioNuevoPerfil() throws Exception {
        mockMvc.perform(get("/admin/perfiles/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("perfiles/formulario"))
                .andExpect(model().attributeExists("perfil"))
                .andExpect(model().attribute("isEdit", false));
    }

    @Test
    void testMostrarFormularioEditarPerfil_Success() throws Exception {
        when(perfilService.findById(1)).thenReturn(Optional.of(perfil));

        mockMvc.perform(get("/admin/perfiles/editar/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("perfiles/formulario"))
                .andExpect(model().attribute("perfil", perfil))
                .andExpect(model().attribute("isEdit", true));

        verify(perfilService, times(1)).findById(1);
    }

    @Test
    void testMostrarFormularioEditarPerfil_NotFound() throws Exception {
        when(perfilService.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/perfiles/editar/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/perfiles/listado"))
                .andExpect(flash().attribute("error", "Perfil no encontrado"));

        verify(perfilService, times(1)).findById(1);
    }

    @Test
    void testMostrarFormularioEditarPerfil_WithException() throws Exception {
        when(perfilService.findById(1)).thenThrow(new RuntimeException("Error de base de datos"));

        mockMvc.perform(get("/admin/perfiles/editar/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/perfiles/listado"))
                .andExpect(flash().attribute("error", "Error al cargar el perfil: Error de base de datos"));

        verify(perfilService, times(1)).findById(1);
    }

    @Test
    void testGuardarPerfil_ValidationErrors() throws Exception {
        mockMvc.perform(post("/admin/perfiles/guardar")
                .param("nombre", "")) // Nombre vacío para provocar error de validación
                .andExpect(status().isOk())
                .andExpect(view().name("perfiles/formulario"))
                .andExpect(model().attribute("isEdit", false));

        verify(perfilService, never()).save(any(Perfil.class));
    }

    @Test
    void testGuardarPerfil_ValidationErrors_EditMode() throws Exception {
        mockMvc.perform(post("/admin/perfiles/guardar")
                .param("id", "1")
                .param("nombre", "")) // Nombre vacío para provocar error de validación
                .andExpect(status().isOk())
                .andExpect(view().name("perfiles/formulario"))
                .andExpect(model().attribute("isEdit", true));

        verify(perfilService, never()).save(any(Perfil.class));
    }

    @Test
    void testGuardarPerfil_NewPerfil_Success() throws Exception {
        when(perfilService.save(any(Perfil.class))).thenReturn(perfil);

        mockMvc.perform(post("/admin/perfiles/guardar")
                .param("nombre", "Nuevo Perfil"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/perfiles/listado"))
                .andExpect(flash().attribute("exito", "Perfil creado correctamente"));

        verify(perfilService, times(1)).save(any(Perfil.class));
    }

    @Test
    void testGuardarPerfil_UpdatePerfil_Success() throws Exception {
        when(perfilService.findById(1)).thenReturn(Optional.of(perfil));
        when(perfilService.save(any(Perfil.class))).thenReturn(perfil);

        mockMvc.perform(post("/admin/perfiles/guardar")
                .param("id", "1")
                .param("nombre", "Perfil Actualizado"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/perfiles/listado"))
                .andExpect(flash().attribute("exito", "Perfil actualizado correctamente"));

        verify(perfilService, times(1)).findById(1);
        verify(perfilService, times(1)).save(any(Perfil.class));
    }

    @Test
    void testGuardarPerfil_UpdatePerfil_NotFound() throws Exception {
        when(perfilService.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(post("/admin/perfiles/guardar")
                .param("id", "1")
                .param("nombre", "Perfil Actualizado"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/perfiles/listado"))
                .andExpect(flash().attribute("error", "Perfil no encontrado para actualizar"));

        verify(perfilService, times(1)).findById(1);
        verify(perfilService, never()).save(any(Perfil.class));
    }

    @Test
    void testGuardarPerfil_UpdatePerfil_IdMismatch() throws Exception {
        Perfil existingPerfil = new Perfil();
        existingPerfil.setId(2); // ID diferente
        existingPerfil.setNombre("Perfil Existente");
        
        when(perfilService.findById(1)).thenReturn(Optional.of(existingPerfil));

        mockMvc.perform(post("/admin/perfiles/guardar")
                .param("id", "1")
                .param("nombre", "Perfil Actualizado"))
                .andExpect(status().isOk())
                .andExpect(view().name("perfiles/formulario"));

        verify(perfilService, times(1)).findById(1);
        verify(perfilService, never()).save(any(Perfil.class));
    }

    @Test
    void testGuardarPerfil_NewPerfil_WithException() throws Exception {
        when(perfilService.save(any(Perfil.class))).thenThrow(new RuntimeException("Error de base de datos"));

        mockMvc.perform(post("/admin/perfiles/guardar")
                .param("nombre", "Nuevo Perfil"))
                .andExpect(status().isOk())
                .andExpect(view().name("perfiles/formulario"))
                .andExpect(model().attribute("error", "Error al guardar el perfil: Error de base de datos"))
                .andExpect(model().attribute("isEdit", false));

        verify(perfilService, times(1)).save(any(Perfil.class));
    }

    @Test
    void testGuardarPerfil_UpdatePerfil_WithException() throws Exception {
        when(perfilService.findById(1)).thenReturn(Optional.of(perfil));
        when(perfilService.save(any(Perfil.class))).thenThrow(new RuntimeException("Error de base de datos"));

        mockMvc.perform(post("/admin/perfiles/guardar")
                .param("id", "1")
                .param("nombre", "Perfil Actualizado"))
                .andExpect(status().isOk())
                .andExpect(view().name("perfiles/formulario"))
                .andExpect(model().attribute("error", "Error al guardar el perfil: Error de base de datos"))
                .andExpect(model().attribute("isEdit", true));

        verify(perfilService, times(1)).findById(1);
        verify(perfilService, times(1)).save(any(Perfil.class));
    }

    @Test
    void testEliminarPerfil_Success() throws Exception {
        when(perfilService.findById(1)).thenReturn(Optional.of(perfil));

        mockMvc.perform(post("/admin/perfiles/eliminar/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/perfiles/listado"))
                .andExpect(flash().attribute("exito", "Perfil eliminado correctamente."));

        verify(perfilService, times(1)).findById(1);
        verify(perfilService, times(1)).deleteById(1);
    }

    @Test
    void testEliminarPerfil_AdminProfile() throws Exception {
        Perfil adminPerfil = new Perfil();
        adminPerfil.setId(1);
        adminPerfil.setNombre("ADMIN");
        
        when(perfilService.findById(1)).thenReturn(Optional.of(adminPerfil));

        mockMvc.perform(post("/admin/perfiles/eliminar/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/perfiles/listado"))
                .andExpect(flash().attribute("error", "El perfil 'ADMIN' no puede ser eliminado."));

        verify(perfilService, times(1)).findById(1);
        verify(perfilService, never()).deleteById(anyInt());
    }

    @Test
    void testEliminarPerfil_AdminProfile_CaseInsensitive() throws Exception {
        Perfil adminPerfil = new Perfil();
        adminPerfil.setId(1);
        adminPerfil.setNombre("admin"); // Minúsculas
        
        when(perfilService.findById(1)).thenReturn(Optional.of(adminPerfil));

        mockMvc.perform(post("/admin/perfiles/eliminar/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/perfiles/listado"))
                .andExpect(flash().attribute("error", "El perfil 'ADMIN' no puede ser eliminado."));

        verify(perfilService, times(1)).findById(1);
        verify(perfilService, never()).deleteById(anyInt());
    }

    @Test
    void testEliminarPerfil_NotFound() throws Exception {
        when(perfilService.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(post("/admin/perfiles/eliminar/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/perfiles/listado"))
                .andExpect(flash().attribute("error", "Perfil no encontrado con ID: 1"));

        verify(perfilService, times(1)).findById(1);
        verify(perfilService, never()).deleteById(anyInt());
    }

    @Test
    void testEliminarPerfil_WithException() throws Exception {
        when(perfilService.findById(1)).thenReturn(Optional.of(perfil));
        doThrow(new RuntimeException("Error de base de datos")).when(perfilService).deleteById(1);

        mockMvc.perform(post("/admin/perfiles/eliminar/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/perfiles/listado"))
                .andExpect(flash().attribute("error", "Error al eliminar el perfil. Es posible que esté asignado a usuarios."));

        verify(perfilService, times(1)).findById(1);
        verify(perfilService, times(1)).deleteById(1);
    }
}