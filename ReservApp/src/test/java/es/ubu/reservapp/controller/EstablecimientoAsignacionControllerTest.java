package es.ubu.reservapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.repositories.UsuarioRepo;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.ConvocatoriaService;
import es.ubu.reservapp.service.EmailService;
import es.ubu.reservapp.service.EstablecimientoService;
import es.ubu.reservapp.service.PerfilService;
import es.ubu.reservapp.service.ReservaService;
import es.ubu.reservapp.service.UsuarioService;

@WebMvcTest(EstablecimientoAsignacionController.class)
class EstablecimientoAsignacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstablecimientoService establecimientoService;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private SessionData sessionData;
        
    @MockitoBean
    private ReservaService reservaService;

    @MockitoBean
    private ConvocatoriaService convocatoriaService;

    @MockitoBean
    private PerfilService perfilService;
    
    @MockitoBean
    private UsuarioRepo usrRepo;
    
    @MockitoBean
    private EmailService mailSender;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private Usuario usuarioLogueado;
    private Usuario usuarioAdminTarget;
    private Establecimiento establecimiento1;
    private Establecimiento establecimiento2;

    private static final String USER_ID_LOGUEADO = "user123";
    private static final String USER_ID_ADMIN_TARGET = "user456";
    private static final String ERROR_ATTR = "error";
    private static final String EXITO_ATTR = "exito";

    @BeforeEach
    void setUp() {
        // Para evitar problemas con el contexto de seguridad en los tests cuando no es necesario
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        usuarioLogueado = new Usuario();
        usuarioLogueado.setId(USER_ID_LOGUEADO);
        usuarioLogueado.setNombre("Usuario Logueado");
        usuarioLogueado.setLstEstablecimientos(new ArrayList<>());

        usuarioAdminTarget = new Usuario();
        usuarioAdminTarget.setId(USER_ID_ADMIN_TARGET);
        usuarioAdminTarget.setNombre("Usuario Target");
        usuarioAdminTarget.setLstEstablecimientos(new ArrayList<>());

        establecimiento1 = new Establecimiento(1, "Establecimiento 1", "Descripción 1", 10, 20, 30, 40, "Gimnasio 1", "Direccion 1", "987654321", "email1@test.com", true, new ArrayList<>(), new ArrayList<>());
        establecimiento2 = new Establecimiento(2, "Establecimiento 2", "Descripción 2", 10, 20, 30, 40, "Gimnasio 2", "Direccion 2", "987654322", "email2@test.com", true, new ArrayList<>(), new ArrayList<>());
        
        when(sessionData.getUsuario()).thenReturn(usuarioLogueado);
    }

    // Constantes para URLs y Vistas
    private static final String URL_ASIGNAR_USER = "/establecimientos/asignar";
    private static final String URL_ASIGNAR_ADMIN = "/admin/usuarios/" + USER_ID_ADMIN_TARGET + "/establecimientos";
    private static final String VIEW_ASIGNACION = "establecimientos/asignacion";
    private static final String MENU_PRINCIPAL = "/menuprincipal";
    private static final String ADMIN_USUARIOS = "/admin/usuarios";

    // Tests para mostrarAsignacionEstablecimientos (Usuario logueado)
    @Test
    void mostrarAsignacionEstablecimientos_usuarioLogueado_ok() throws Exception {
        when(usuarioService.findUsuarioById(USER_ID_LOGUEADO)).thenReturn(usuarioLogueado);
        when(establecimientoService.findAll()).thenReturn(Arrays.asList(establecimiento1, establecimiento2));

        mockMvc.perform(get(URL_ASIGNAR_USER))
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_ASIGNACION))
                .andExpect(model().attributeExists("usuario", "establecimientos", "establecimientosAsignados", "origen"))
                .andExpect(model().attribute("usuario", usuarioLogueado))
                .andExpect(model().attribute("establecimientos", Arrays.asList(establecimiento1, establecimiento2)))
                .andExpect(model().attribute("establecimientosAsignados", Collections.emptySet()))
                .andExpect(model().attribute("origen", "usuario"));

        verify(usuarioService).findUsuarioById(USER_ID_LOGUEADO);
        verify(establecimientoService).findAll();
    }
    
    @Test
    void mostrarAsignacionEstablecimientos_usuarioLogueado_conEstablecimientosAsignados() throws Exception {
        usuarioLogueado.getLstEstablecimientos().add(establecimiento1);
        when(usuarioService.findUsuarioById(USER_ID_LOGUEADO)).thenReturn(usuarioLogueado);
        when(establecimientoService.findAll()).thenReturn(Arrays.asList(establecimiento1, establecimiento2));

        mockMvc.perform(get(URL_ASIGNAR_USER))
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_ASIGNACION))
                .andExpect(model().attribute("establecimientosAsignados", Set.of(establecimiento1.getId())));
    }

    // Tests para mostrarAsignacionEstablecimientosAdmin (Admin)
    @Test
    void mostrarAsignacionEstablecimientosAdmin_usuarioExiste_ok() throws Exception {
        when(usuarioService.findUsuarioById(USER_ID_ADMIN_TARGET)).thenReturn(usuarioAdminTarget);
        when(establecimientoService.findAll()).thenReturn(Arrays.asList(establecimiento1, establecimiento2));

        mockMvc.perform(get(URL_ASIGNAR_ADMIN))
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_ASIGNACION))
                .andExpect(model().attributeExists("usuario", "establecimientos", "establecimientosAsignados", "origen"))
                .andExpect(model().attribute("usuario", usuarioAdminTarget))
                .andExpect(model().attribute("establecimientos", Arrays.asList(establecimiento1, establecimiento2)))
                .andExpect(model().attribute("establecimientosAsignados", Collections.emptySet()))
                .andExpect(model().attribute("origen", "admin"));

        verify(usuarioService).findUsuarioById(USER_ID_ADMIN_TARGET);
        verify(establecimientoService).findAll();
    }

    @Test
    void mostrarAsignacionEstablecimientosAdmin_usuarioNoExiste_redirigeAError() throws Exception {
        when(usuarioService.findUsuarioById(USER_ID_ADMIN_TARGET)).thenReturn(null);

        mockMvc.perform(get(URL_ASIGNAR_ADMIN))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error")); // Asumiendo REDIRECT_ERROR es "redirect:/error"

        verify(usuarioService).findUsuarioById(USER_ID_ADMIN_TARGET);
    }
    
    // Tests para guardarAsignacionEstablecimientos (Usuario logueado)
    @Test
    void guardarAsignacionEstablecimientos_usuarioLogueado_ok() throws Exception {
        List<Integer> idsSeleccionados = Arrays.asList(establecimiento1.getId());
        when(usuarioService.findUsuarioById(USER_ID_LOGUEADO)).thenReturn(usuarioLogueado);
        when(establecimientoService.findAllById(idsSeleccionados)).thenReturn(Collections.singletonList(establecimiento1));
        doNothing().when(usuarioService).save(any(Usuario.class));

        mockMvc.perform(post(URL_ASIGNAR_USER)
                .param("establecimientosIds", establecimiento1.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(MENU_PRINCIPAL))
                .andExpect(flash().attributeExists(EXITO_ATTR));

        verify(usuarioService).findUsuarioById(USER_ID_LOGUEADO);
        verify(establecimientoService).findAllById(idsSeleccionados);
        verify(usuarioService).save(usuarioLogueado);
        assert(usuarioLogueado.getLstEstablecimientos().contains(establecimiento1));
    }

    @Test
    void guardarAsignacionEstablecimientos_usuarioLogueado_sinSeleccionados_ok() throws Exception {
        usuarioLogueado.getLstEstablecimientos().add(establecimiento1); // Pre-asignar uno para ver si se limpia
        when(usuarioService.findUsuarioById(USER_ID_LOGUEADO)).thenReturn(usuarioLogueado);
        doNothing().when(usuarioService).save(any(Usuario.class));

        mockMvc.perform(post(URL_ASIGNAR_USER)) // No se envían establecimientosIds
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(MENU_PRINCIPAL))
                .andExpect(flash().attributeExists(EXITO_ATTR));

        verify(usuarioService).findUsuarioById(USER_ID_LOGUEADO);
        verify(establecimientoService, times(0)).findAllById(anyList()); // No debe llamarse si no hay IDs
        verify(usuarioService).save(usuarioLogueado);
        assert(usuarioLogueado.getLstEstablecimientos().isEmpty());
    }
    
    @Test
    void guardarAsignacionEstablecimientos_usuarioLogueado_establecimientosNulos_ok() throws Exception {
        usuarioLogueado.getLstEstablecimientos().add(establecimiento1); // Pre-asignar uno para ver si se limpia
        when(usuarioService.findUsuarioById(USER_ID_LOGUEADO)).thenReturn(usuarioLogueado);
        doNothing().when(usuarioService).save(any(Usuario.class));

        mockMvc.perform(post(URL_ASIGNAR_USER)
                .param("establecimientosIds", (String) null)) // Enviar parametro nulo
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(MENU_PRINCIPAL))
                .andExpect(flash().attributeExists(EXITO_ATTR));

        verify(usuarioService).findUsuarioById(USER_ID_LOGUEADO);
        verify(establecimientoService, times(0)).findAllById(anyList());
        verify(usuarioService).save(usuarioLogueado);
        assert(usuarioLogueado.getLstEstablecimientos().isEmpty());
    }

    @Test
    void guardarAsignacionEstablecimientos_usuarioLogueado_errorEnServicio() throws Exception {
        List<Integer> idsSeleccionados = Arrays.asList(establecimiento1.getId());
        when(usuarioService.findUsuarioById(USER_ID_LOGUEADO)).thenReturn(usuarioLogueado);
        when(establecimientoService.findAllById(idsSeleccionados)).thenThrow(new RuntimeException("Error de servicio"));

        mockMvc.perform(post(URL_ASIGNAR_USER)
                .param("establecimientosIds", establecimiento1.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(MENU_PRINCIPAL))
                .andExpect(flash().attributeExists(ERROR_ATTR));
        
        verify(usuarioService).findUsuarioById(USER_ID_LOGUEADO);
        verify(establecimientoService).findAllById(idsSeleccionados);
        verify(usuarioService, times(0)).save(any(Usuario.class)); // No se debe llamar a save si hay error antes
    }

    // Tests para guardarAsignacionEstablecimientosAdmin (Admin)
    @Test
    void guardarAsignacionEstablecimientosAdmin_usuarioExiste_ok() throws Exception {
        List<Integer> idsSeleccionados = Arrays.asList(establecimiento2.getId());
        when(usuarioService.findUsuarioById(USER_ID_ADMIN_TARGET)).thenReturn(usuarioAdminTarget);
        when(establecimientoService.findAllById(idsSeleccionados)).thenReturn(Collections.singletonList(establecimiento2));
        doNothing().when(usuarioService).save(any(Usuario.class));

        mockMvc.perform(post(URL_ASIGNAR_ADMIN)
                .param("establecimientosIds", establecimiento2.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ADMIN_USUARIOS))
                .andExpect(flash().attributeExists(EXITO_ATTR));

        verify(usuarioService).findUsuarioById(USER_ID_ADMIN_TARGET);
        verify(establecimientoService).findAllById(idsSeleccionados);
        verify(usuarioService).save(usuarioAdminTarget);
        assert(usuarioAdminTarget.getLstEstablecimientos().contains(establecimiento2));
    }

    @Test
    void guardarAsignacionEstablecimientosAdmin_usuarioNoExiste_redirigeYMensajeError() throws Exception {
        when(usuarioService.findUsuarioById(USER_ID_ADMIN_TARGET)).thenReturn(null);

        mockMvc.perform(post(URL_ASIGNAR_ADMIN)
                .param("establecimientosIds", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ADMIN_USUARIOS))
                .andExpect(flash().attributeExists(ERROR_ATTR))
                .andExpect(flash().attribute(ERROR_ATTR, "Usuario no encontrado."));

        verify(usuarioService).findUsuarioById(USER_ID_ADMIN_TARGET);
        verify(establecimientoService, times(0)).findAllById(anyList());
        verify(usuarioService, times(0)).save(any(Usuario.class));
    }
    
    @Test
    void guardarAsignacionEstablecimientosAdmin_errorEnServicio() throws Exception {
        List<Integer> idsSeleccionados = Arrays.asList(establecimiento1.getId());
        when(usuarioService.findUsuarioById(USER_ID_ADMIN_TARGET)).thenReturn(usuarioAdminTarget);
        when(establecimientoService.findAllById(idsSeleccionados)).thenThrow(new RuntimeException("Error de servicio establecimientos"));

        mockMvc.perform(post(URL_ASIGNAR_ADMIN)
                .param("establecimientosIds", establecimiento1.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ADMIN_USUARIOS))
                .andExpect(flash().attributeExists(ERROR_ATTR))
                .andExpect(flash().attribute(ERROR_ATTR, "Error al asignar establecimientos."));
        
        verify(usuarioService).findUsuarioById(USER_ID_ADMIN_TARGET);
        verify(establecimientoService).findAllById(idsSeleccionados);
        verify(usuarioService, times(0)).save(any(Usuario.class));
    }
}
