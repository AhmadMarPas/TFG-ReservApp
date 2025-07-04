package es.ubu.reservapp.controller;

import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import es.ubu.config.SecurityConfig;
import es.ubu.reservapp.model.entities.Perfil;
import es.ubu.reservapp.model.repositories.EstablecimientoRepo;
import es.ubu.reservapp.model.repositories.ReservaRepo;
import es.ubu.reservapp.model.repositories.UsuarioRepo;
import es.ubu.reservapp.service.PerfilService;

@WebMvcTest(PerfilController.class)
@Import({SecurityConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PerfilControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PerfilService perfilService;
    
    @MockitoBean
    private EstablecimientoRepo estRepo;
    
    @MockitoBean
    private UsuarioRepo usrRepo;
    
    @MockitoBean
    private ReservaRepo reservaRepo;

    private Perfil perfilAdmin;
    private Perfil perfilUser;

    private static final String BASE_URL = "/admin/perfiles";
    private static final String LISTADO_URL = BASE_URL + "/listado";
    private static final String NUEVO_URL = BASE_URL + "/nuevo";
    private static final String EDITAR_URL = BASE_URL + "/editar/";
    private static final String GUARDAR_URL = BASE_URL + "/guardar";
    private static final String ELIMINAR_URL = BASE_URL + "/eliminar/";

    private static final String LISTADO_VIEW = "perfiles/listado";
    private static final String FORMULARIO_VIEW = "perfiles/formulario";

    private static final String PERFIL_ATTRIBUTE = "perfil";
    private static final String ERROR_ATTRIBUTE = "error";
    private static final String EXITO_ATTRIBUTE = "exito";
    private static final String EDIT_MODE_ATTRIBUTE = "isEdit";

    @BeforeEach
    void setUp() {
        perfilAdmin = new Perfil(1, "ADMIN", new ArrayList<>());
        perfilUser = new Perfil(2, "USER", new ArrayList<>());
    }

    // Helper para simular usuario ADMIN
    private static RequestPostProcessor adminUser() {
        return user("admin").authorities(new SimpleGrantedAuthority("ADMIN"));
    }

    @Test
    void listarPerfiles_conPerfiles_devuelveVistaYModeloCorrectos() throws Exception {
        List<Perfil> perfiles = List.of(perfilAdmin, perfilUser);
        when(perfilService.findAll()).thenReturn(perfiles);

        mockMvc.perform(get(LISTADO_URL).with(adminUser()))
                .andExpect(status().isOk())
                .andExpect(view().name(LISTADO_VIEW))
                .andExpect(model().attribute("perfiles", perfiles))
                .andExpect(model().attribute("perfilActivoCount", 2L));

        verify(perfilService).findAll();
    }

    @Test
    void listarPerfiles_listaVacia_devuelveVistaYModeloCorrectos() throws Exception {
        when(perfilService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(LISTADO_URL).with(adminUser()))
                .andExpect(status().isOk())
                .andExpect(view().name(LISTADO_VIEW))
                .andExpect(model().attribute("perfiles", Collections.emptyList()))
                .andExpect(model().attribute("perfilActivoCount", 0L));
        
        verify(perfilService).findAll();
    }

    @Test
    @Disabled("Revisar comportamiento")
    void listarPerfiles_servicioLanzaExcepcion_devuelveVistaConError() throws Exception {
        when(perfilService.findAll()).thenThrow(new RuntimeException("Error de servicio"));

        mockMvc.perform(get(LISTADO_URL).with(adminUser()))
                .andExpect(status().isOk())
                .andExpect(view().name(LISTADO_VIEW))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, "Error al cargar la lista de perfiles: Error de servicio"));
        
        verify(perfilService).findAll();
    }
    
    @Test
    void mostrarFormularioNuevoPerfil_devuelveVistaYModeloCorrectos() throws Exception {
        mockMvc.perform(get(NUEVO_URL).with(adminUser()))
                .andExpect(status().isOk())
                .andExpect(view().name(FORMULARIO_VIEW))
                .andExpect(model().attributeExists(PERFIL_ATTRIBUTE))
                .andExpect(model().attribute(PERFIL_ATTRIBUTE, instanceOf(Perfil.class)))
                .andExpect(model().attribute(EDIT_MODE_ATTRIBUTE, false));
    }

    @Test
    void mostrarFormularioEditarPerfil_perfilExiste_devuelveVistaYModeloCorrectos() throws Exception {
        when(perfilService.findById(perfilUser.getId())).thenReturn(Optional.of(perfilUser));

        mockMvc.perform(get(EDITAR_URL + perfilUser.getId()).with(adminUser()))
                .andExpect(status().isOk())
                .andExpect(view().name(FORMULARIO_VIEW))
                .andExpect(model().attribute(PERFIL_ATTRIBUTE, perfilUser))
                .andExpect(model().attribute(EDIT_MODE_ATTRIBUTE, true));

        verify(perfilService).findById(perfilUser.getId());
    }

    @Test
    void mostrarFormularioEditarPerfil_perfilNoExiste_redirigeAListadoConError() throws Exception {
        when(perfilService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get(EDITAR_URL + 99).with(adminUser()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(LISTADO_URL))
                .andExpect(flash().attribute(ERROR_ATTRIBUTE, "Perfil no encontrado"));
        
        verify(perfilService).findById(99);
    }

    @Test
    void mostrarFormularioEditarPerfil_servicioLanzaExcepcion_redirigeAListadoConError() throws Exception {
        when(perfilService.findById(perfilUser.getId())).thenThrow(new RuntimeException("Error de servicio"));

        mockMvc.perform(get(EDITAR_URL + perfilUser.getId()).with(adminUser()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(LISTADO_URL))
                .andExpect(flash().attribute(ERROR_ATTRIBUTE, "Error al cargar el perfil: Error de servicio"));
        
        verify(perfilService).findById(perfilUser.getId());
    }
    
    // Tests para guardarPerfil - Creación
    @Test
    void guardarPerfil_creacion_exito() throws Exception {
//        doNothing().when(perfilService).save(any(Perfil.class));
    	when(perfilService.save(any(Perfil.class))).thenReturn(perfilUser);

        mockMvc.perform(post(GUARDAR_URL).with(adminUser()).with(csrf())
                .param("nombre", "NUEVO_PERFIL"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(LISTADO_URL))
                .andExpect(flash().attribute(EXITO_ATTRIBUTE, "Perfil creado correctamente"));

        verify(perfilService).save(argThat(p -> "NUEVO_PERFIL".equals(p.getNombre()) && p.getId() == null));
    }

    @Test
    void guardarPerfil_creacion_errorValidacion() throws Exception {
        mockMvc.perform(post(GUARDAR_URL).with(adminUser()).with(csrf())
                // Sin el parámetro "nombre", que es requerido por @Valid si Perfil lo tiene anotado como NotBlank/NotEmpty
                .param("nombre", "")) 
                .andExpect(status().isOk())
                .andExpect(view().name(FORMULARIO_VIEW))
                .andExpect(model().attributeHasFieldErrors(PERFIL_ATTRIBUTE, "nombre"))
                .andExpect(model().attribute(EDIT_MODE_ATTRIBUTE, false));

        verify(perfilService, never()).save(any(Perfil.class));
    }
    
    @Test
    void guardarPerfil_creacion_servicioLanzaExcepcion() throws Exception {
        when(perfilService.save(any(Perfil.class))).thenThrow(new RuntimeException("Error de guardado"));

        mockMvc.perform(post(GUARDAR_URL).with(adminUser()).with(csrf())
                .param("nombre", "OTRO_PERFIL"))
                .andExpect(status().isOk())
                .andExpect(view().name(FORMULARIO_VIEW))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, "Error al guardar el perfil: Error de guardado"))
                .andExpect(model().attribute(EDIT_MODE_ATTRIBUTE, false));
        
        verify(perfilService).save(any(Perfil.class));
    }

    // Tests para guardarPerfil - Actualización
    @Test
    void guardarPerfil_actualizacion_exito() throws Exception {
        when(perfilService.findById(perfilUser.getId())).thenReturn(Optional.of(perfilUser));
        when(perfilService.save(any(Perfil.class))).thenReturn(perfilUser);
//        doNothing().when(perfilService).save(any(Perfil.class));
        
        mockMvc.perform(post(GUARDAR_URL).with(adminUser()).with(csrf())
                .param("id", perfilUser.getId().toString())
                .param("nombre", "USER_MODIFICADO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(LISTADO_URL))
                .andExpect(flash().attribute(EXITO_ATTRIBUTE, "Perfil actualizado correctamente"));

        verify(perfilService).findById(perfilUser.getId());
        verify(perfilService).save(perfilUser);
        verify(perfilService).save(argThat(p -> "USER_MODIFICADO".equals(p.getNombre()) && perfilUser.getId().equals(p.getId())));
    }
    
    @Test
    void guardarPerfil_actualizacion_errorValidacion() throws Exception {
         mockMvc.perform(post(GUARDAR_URL).with(adminUser()).with(csrf())
                .param("id", perfilUser.getId().toString())
                .param("nombre", "")) // Nombre vacío para provocar error de validación
                .andExpect(status().isOk())
                .andExpect(view().name(FORMULARIO_VIEW))
                .andExpect(model().attributeHasFieldErrors(PERFIL_ATTRIBUTE, "nombre"))
                .andExpect(model().attribute(EDIT_MODE_ATTRIBUTE, true));

        verify(perfilService, never()).save(any(Perfil.class));
    }

    @Test
    void guardarPerfil_actualizacion_perfilNoEncontrado() throws Exception {
        when(perfilService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(post(GUARDAR_URL).with(adminUser()).with(csrf())
                .param("id", "99")
                .param("nombre", "NO_EXISTE_MOD"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(LISTADO_URL))
                .andExpect(flash().attribute(ERROR_ATTRIBUTE, "Perfil no encontrado para actualizar"));
        
        verify(perfilService).findById(99);
        verify(perfilService, never()).save(any(Perfil.class));
    }
    
    @Test
    void guardarPerfil_actualizacion_nombreDuplicado_logicaActual() throws Exception {
        Perfil perfilExistenteConOtroId = new Perfil(3, "OTRO_NOMBRE", new ArrayList<>()); // Perfil que se encuentra en BBDD
        when(perfilService.findById(perfilUser.getId())).thenReturn(Optional.of(perfilExistenteConOtroId)); // Al buscar por perfilUser.id, devolvemos uno con ID 3
        
        mockMvc.perform(post(GUARDAR_URL).with(adminUser()).with(csrf())
                .param("id", perfilUser.getId().toString()) // ID del formulario es 2
                .param("nombre", "USER_INTENTANDO_ACTUALIZAR"))
                .andExpect(status().isOk())
                .andExpect(view().name(FORMULARIO_VIEW))
                .andExpect(model().attributeHasFieldErrors(PERFIL_ATTRIBUTE, "nombre")); // Esperamos el error "nombre"
//                .andExpect(model().attribute(EDIT_MODE_ATTRIBUTE, true)); // Se mantiene en modo edición

        verify(perfilService).findById(perfilUser.getId());
        verify(perfilService, never()).save(any(Perfil.class));
    }

    @Test
    void guardarPerfil_actualizacion_servicioLanzaExcepcion() throws Exception {
        when(perfilService.findById(perfilUser.getId())).thenReturn(Optional.of(perfilUser));
        when(perfilService.save(any(Perfil.class))).thenThrow(new RuntimeException("Error de guardado actualizando"));

        mockMvc.perform(post(GUARDAR_URL).with(adminUser()).with(csrf())
                .param("id", perfilUser.getId().toString())
                .param("nombre", "USER_MODIFICADO_ERROR"))
                .andExpect(status().isOk())
                .andExpect(view().name(FORMULARIO_VIEW))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, "Error al guardar el perfil: Error de guardado actualizando"))
                .andExpect(model().attribute(EDIT_MODE_ATTRIBUTE, true));
        
        verify(perfilService).findById(perfilUser.getId());
        verify(perfilService).save(any(Perfil.class));
    }
    
    // Tests para eliminarPerfil
    @Test
    void eliminarPerfil_exito() throws Exception {
        when(perfilService.findById(perfilUser.getId())).thenReturn(Optional.of(perfilUser));
        doNothing().when(perfilService).deleteById(perfilUser.getId());

        mockMvc.perform(post(ELIMINAR_URL + perfilUser.getId()).with(adminUser()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(LISTADO_URL))
                .andExpect(flash().attribute(EXITO_ATTRIBUTE, "Perfil eliminado correctamente."));

        verify(perfilService).findById(perfilUser.getId());
        verify(perfilService).deleteById(perfilUser.getId());
    }

    @Test
    void eliminarPerfil_intentoEliminarAdmin() throws Exception {
        when(perfilService.findById(perfilAdmin.getId())).thenReturn(Optional.of(perfilAdmin));

        mockMvc.perform(post(ELIMINAR_URL + perfilAdmin.getId()).with(adminUser()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(LISTADO_URL))
                .andExpect(flash().attribute(ERROR_ATTRIBUTE, "El perfil 'ADMIN' no puede ser eliminado."));

        verify(perfilService).findById(perfilAdmin.getId());
        verify(perfilService, never()).deleteById(anyInt());
    }
    
    @Test
    void eliminarPerfil_perfilNoEncontrado() throws Exception {
        when(perfilService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(post(ELIMINAR_URL + 99).with(adminUser()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(LISTADO_URL))
                .andExpect(flash().attribute(ERROR_ATTRIBUTE, "Perfil no encontrado con ID: 99"));
        
        verify(perfilService).findById(99);
        verify(perfilService, never()).deleteById(anyInt());
    }

    @Test
    void eliminarPerfil_servicioLanzaExcepcion() throws Exception {
        when(perfilService.findById(perfilUser.getId())).thenReturn(Optional.of(perfilUser));
        doThrow(new DataIntegrityViolationException("Perfil asignado")).when(perfilService).deleteById(perfilUser.getId());

        mockMvc.perform(post(ELIMINAR_URL + perfilUser.getId()).with(adminUser()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(LISTADO_URL))
                .andExpect(flash().attribute(ERROR_ATTRIBUTE, "Error al eliminar el perfil. Es posible que esté asignado a usuarios."));
        
        verify(perfilService).findById(perfilUser.getId());
        verify(perfilService).deleteById(perfilUser.getId());
    }
}
