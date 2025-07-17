package es.ubu.reservapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.EstablecimientoService;
import es.ubu.reservapp.service.UsuarioService;

/**
 * Test para la clase EstablecimientoAsignacionController.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EstablecimientoAsignacionController Tests")
class EstablecimientoAsignacionControllerTest {

    @Mock
    private SessionData sessionData;

    @Mock
    private EstablecimientoService establecimientoService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private EstablecimientoAsignacionController controller;

    private Usuario usuario;
    private Usuario usuarioAdmin;
    private List<Establecimiento> establecimientos;
    private List<Integer> establecimientosIds;
    
    private static final String USER_ID = "user123";
    private static final String ADMIN_USER_ID = "admin123";
    private static final String NONEXISTENT_USER_ID = "nonexistent";

    @BeforeEach
    void setUp() {
        // Crear usuario normal
        usuario = new Usuario();
        usuario.setId(USER_ID);
        usuario.setNombre("Usuario");
        usuario.setApellidos("Test");
        usuario.setCorreo("usuario@test.com");
        usuario.setLstEstablecimientos(new ArrayList<>());

        // Crear usuario admin
        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(ADMIN_USER_ID);
        usuarioAdmin.setNombre("Admin");
        usuarioAdmin.setApellidos("Test");
        usuarioAdmin.setCorreo("admin@test.com");
        usuarioAdmin.setAdministrador(true);
        usuarioAdmin.setLstEstablecimientos(new ArrayList<>());

        // Crear establecimientos
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

        establecimientos = Arrays.asList(establecimiento1, establecimiento2);
        establecimientosIds = Arrays.asList(1, 2);
    }

    @Test
    @DisplayName("Constructor - Debe inicializar correctamente el controlador")
    void constructor_ShouldInitializeControllerCorrectly() {
        // Given & When
        EstablecimientoAsignacionController newController = new EstablecimientoAsignacionController(
            sessionData, establecimientoService, usuarioService);
        
        // Then
        assertNotNull(newController);
    }

    @Nested
    @DisplayName("Tests para mostrarAsignacionEstablecimientos")
    class MostrarAsignacionEstablecimientosTests {

        @Test
        @DisplayName("Debe mostrar asignación de establecimientos para usuario actual")
        void mostrarAsignacionEstablecimientos_WithValidUser_ShouldReturnAsignacionView() {
            // Given
            when(sessionData.getUsuario()).thenReturn(usuario);
            when(usuarioService.findUsuarioById(USER_ID)).thenReturn(usuario);
            when(usuarioService.recuperarEstablecimientosUsuario(USER_ID, model)).thenReturn(model);
            when(establecimientoService.findAll()).thenReturn(establecimientos);

            // When
            String result = controller.mostrarAsignacionEstablecimientos(model);

            // Then
            assertEquals("establecimientos/asignacion", result);
            verify(model).addAttribute("usuario", usuario);
            verify(model).addAttribute("establecimientos", establecimientos);
            verify(model).addAttribute("origen", "usuario");
            verify(usuarioService).recuperarEstablecimientosUsuario(USER_ID, model);
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException cuando usuario actual es null")
        void mostrarAsignacionEstablecimientos_WithNullUser_ShouldThrowNullPointerException() {
            // Given
            when(sessionData.getUsuario()).thenReturn(null);

            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
                controller.mostrarAsignacionEstablecimientos(model);
            });
            verify(usuarioService, never()).findUsuarioById(anyString());
        }
    }

    @Nested
    @DisplayName("Tests para mostrarAsignacionEstablecimientosAdmin")
    class MostrarAsignacionEstablecimientosAdminTests {

        @Test
        @DisplayName("Debe mostrar asignación de establecimientos para usuario específico (admin)")
        void mostrarAsignacionEstablecimientosAdmin_WithValidUserId_ShouldReturnAsignacionView() {
            // Given
            when(usuarioService.findUsuarioById(USER_ID)).thenReturn(usuario);
            when(usuarioService.recuperarEstablecimientosUsuario(USER_ID, model)).thenReturn(model);
            when(establecimientoService.findAll()).thenReturn(establecimientos);

            // When
            String result = controller.mostrarAsignacionEstablecimientosAdmin(USER_ID, model);

            // Then
            assertEquals("establecimientos/asignacion", result);
            verify(model).addAttribute("usuario", usuario);
            verify(model).addAttribute("establecimientos", establecimientos);
            verify(model).addAttribute("origen", "admin");
            verify(usuarioService).recuperarEstablecimientosUsuario(USER_ID, model);
        }

        @Test
        @DisplayName("Debe redirigir a error cuando usuario no existe")
        void mostrarAsignacionEstablecimientosAdmin_WithNonexistentUser_ShouldRedirectToError() {
            // Given
            when(usuarioService.findUsuarioById(NONEXISTENT_USER_ID)).thenReturn(null);

            // When
            String result = controller.mostrarAsignacionEstablecimientosAdmin(NONEXISTENT_USER_ID, model);

            // Then
            assertEquals("redirect:/error", result);
            verify(usuarioService).findUsuarioById(NONEXISTENT_USER_ID);
            verify(model, never()).addAttribute(anyString(), any());
        }

        @Test
        @DisplayName("Debe manejar lista vacía de establecimientos")
        void mostrarAsignacionEstablecimientosAdmin_WithEmptyEstablecimientos_ShouldWork() {
            // Given
            when(usuarioService.findUsuarioById(USER_ID)).thenReturn(usuario);
            when(usuarioService.recuperarEstablecimientosUsuario(USER_ID, model)).thenReturn(model);
            when(establecimientoService.findAll()).thenReturn(new ArrayList<>());

            // When
            String result = controller.mostrarAsignacionEstablecimientosAdmin(USER_ID, model);

            // Then
            assertEquals("establecimientos/asignacion", result);
            verify(model).addAttribute("establecimientos", new ArrayList<>());
        }
    }

    @Nested
    @DisplayName("Tests para guardarAsignacionEstablecimientos")
    class GuardarAsignacionEstablecimientosTests {

        @Test
        @DisplayName("Debe guardar asignación exitosamente para usuario actual")
        void guardarAsignacionEstablecimientos_WithValidData_ShouldSaveAndRedirect() {
            // Given
            when(sessionData.getUsuario()).thenReturn(usuario);
            when(usuarioService.findUsuarioById(USER_ID)).thenReturn(usuario);
            when(establecimientoService.findAllById(establecimientosIds)).thenReturn(establecimientos);
            when(usuarioService.asignarEstablecimientos(usuario, establecimientos)).thenReturn(usuario);

            // When
            String result = controller.guardarAsignacionEstablecimientos(establecimientosIds, redirectAttributes);

            // Then
            assertEquals("redirect:/menuprincipal", result);
            verify(usuarioService).save(usuario);
            verify(redirectAttributes).addFlashAttribute("exito", "Establecimientos asignados correctamente.");
        }

        @Test
        @DisplayName("Debe manejar lista vacía de establecimientos")
        void guardarAsignacionEstablecimientos_WithEmptyList_ShouldSaveAndRedirect() {
            // Given
            when(sessionData.getUsuario()).thenReturn(usuario);
            when(usuarioService.findUsuarioById(USER_ID)).thenReturn(usuario);

            // When
            String result = controller.guardarAsignacionEstablecimientos(new ArrayList<>(), redirectAttributes);

            // Then
            assertEquals("redirect:/menuprincipal", result);
            verify(usuarioService).save(usuario);
            verify(usuarioService, never()).asignarEstablecimientos(any(), anyList());
            verify(redirectAttributes).addFlashAttribute("exito", "Establecimientos asignados correctamente.");
        }

        @Test
        @DisplayName("Debe manejar lista null de establecimientos")
        void guardarAsignacionEstablecimientos_WithNullList_ShouldSaveAndRedirect() {
            // Given
            when(sessionData.getUsuario()).thenReturn(usuario);
            when(usuarioService.findUsuarioById(USER_ID)).thenReturn(usuario);

            // When
            String result = controller.guardarAsignacionEstablecimientos(null, redirectAttributes);

            // Then
            assertEquals("redirect:/menuprincipal", result);
            verify(usuarioService).save(usuario);
            verify(usuarioService, never()).asignarEstablecimientos(any(), anyList());
            verify(redirectAttributes).addFlashAttribute("exito", "Establecimientos asignados correctamente.");
        }

        @Test
        @DisplayName("Debe manejar usuario no encontrado")
        void guardarAsignacionEstablecimientos_WithNonexistentUser_ShouldRedirectWithError() {
            // Given
            Usuario usuarioNulo = new Usuario();
            usuarioNulo.setId(NONEXISTENT_USER_ID);
            when(sessionData.getUsuario()).thenReturn(usuarioNulo);
            when(usuarioService.findUsuarioById(NONEXISTENT_USER_ID)).thenReturn(null);

            // When
            String result = controller.guardarAsignacionEstablecimientos(establecimientosIds, redirectAttributes);

            // Then
            assertEquals("redirect:/menuprincipal", result);
            verify(redirectAttributes).addFlashAttribute("error", "Usuario no encontrado.");
            verify(usuarioService, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException cuando usuario de sesión es null")
        void guardarAsignacionEstablecimientos_WithNullSessionUser_ShouldThrowNullPointerException() {
            // Given
            when(sessionData.getUsuario()).thenReturn(null);

            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
                controller.guardarAsignacionEstablecimientos(establecimientosIds, redirectAttributes);
            });
            verify(usuarioService, never()).findUsuarioById(anyString());
            verify(usuarioService, never()).save(any());
        }

        @Test
        @DisplayName("Debe manejar excepción durante el guardado")
        void guardarAsignacionEstablecimientos_WithException_ShouldRedirectWithError() {
            // Given
            when(sessionData.getUsuario()).thenReturn(usuario);
            when(usuarioService.findUsuarioById(USER_ID)).thenReturn(usuario);
            when(establecimientoService.findAllById(establecimientosIds)).thenReturn(establecimientos);
            when(usuarioService.asignarEstablecimientos(usuario, establecimientos)).thenReturn(usuario);
            doThrow(new RuntimeException("Error de prueba")).when(usuarioService).save(usuario);

            // When
            String result = controller.guardarAsignacionEstablecimientos(establecimientosIds, redirectAttributes);

            // Then
            assertEquals("redirect:/menuprincipal", result);
            verify(redirectAttributes).addFlashAttribute("error", "Error al asignar establecimientos.");
        }
    }

    @Nested
    @DisplayName("Tests para guardarAsignacionEstablecimientosAdmin")
    class GuardarAsignacionEstablecimientosAdminTests {

        @Test
        @DisplayName("Debe guardar asignación exitosamente para usuario específico (admin)")
        void guardarAsignacionEstablecimientosAdmin_WithValidData_ShouldSaveAndRedirect() {
            // Given
            when(usuarioService.findUsuarioById(USER_ID)).thenReturn(usuario);
            when(establecimientoService.findAllById(establecimientosIds)).thenReturn(establecimientos);
            when(usuarioService.asignarEstablecimientos(usuario, establecimientos)).thenReturn(usuario);

            // When
            String result = controller.guardarAsignacionEstablecimientosAdmin(USER_ID, establecimientosIds, redirectAttributes);

            // Then
            assertEquals("redirect:/admin/usuarios", result);
            verify(usuarioService).save(usuario);
            verify(redirectAttributes).addFlashAttribute("exito", "Establecimientos asignados correctamente.");
        }

        @Test
        @DisplayName("Debe manejar usuario no encontrado en modo admin")
        void guardarAsignacionEstablecimientosAdmin_WithNonexistentUser_ShouldRedirectWithError() {
            // Given
            when(usuarioService.findUsuarioById(NONEXISTENT_USER_ID)).thenReturn(null);

            // When
            String result = controller.guardarAsignacionEstablecimientosAdmin(NONEXISTENT_USER_ID, establecimientosIds, redirectAttributes);

            // Then
            assertEquals("redirect:/admin/usuarios", result);
            verify(redirectAttributes).addFlashAttribute("error", "Usuario no encontrado.");
            verify(usuarioService, never()).save(any());
        }

        @Test
        @DisplayName("Debe manejar excepción durante el guardado en modo admin")
        void guardarAsignacionEstablecimientosAdmin_WithException_ShouldRedirectWithError() {
            // Given
            when(usuarioService.findUsuarioById(USER_ID)).thenReturn(usuario);
            when(establecimientoService.findAllById(establecimientosIds)).thenReturn(establecimientos);
            when(usuarioService.asignarEstablecimientos(usuario, establecimientos)).thenReturn(usuario);
            doThrow(new RuntimeException("Error de prueba")).when(usuarioService).save(usuario);

            // When
            String result = controller.guardarAsignacionEstablecimientosAdmin(USER_ID, establecimientosIds, redirectAttributes);

            // Then
            assertEquals("redirect:/admin/usuarios", result);
            verify(redirectAttributes).addFlashAttribute("error", "Error al asignar establecimientos.");
        }

        @Test
        @DisplayName("Debe manejar lista vacía en modo admin")
        void guardarAsignacionEstablecimientosAdmin_WithEmptyList_ShouldSaveAndRedirect() {
            // Given
            when(usuarioService.findUsuarioById(USER_ID)).thenReturn(usuario);

            // When
            String result = controller.guardarAsignacionEstablecimientosAdmin(USER_ID, new ArrayList<>(), redirectAttributes);

            // Then
            assertEquals("redirect:/admin/usuarios", result);
            verify(usuarioService).save(usuario);
            verify(usuarioService, never()).asignarEstablecimientos(any(), anyList());
            verify(redirectAttributes).addFlashAttribute("exito", "Establecimientos asignados correctamente.");
        }

        @Test
        @DisplayName("Debe manejar lista null en modo admin")
        void guardarAsignacionEstablecimientosAdmin_WithNullList_ShouldSaveAndRedirect() {
            // Given
            when(usuarioService.findUsuarioById(USER_ID)).thenReturn(usuario);

            // When
            String result = controller.guardarAsignacionEstablecimientosAdmin(USER_ID, null, redirectAttributes);

            // Then
            assertEquals("redirect:/admin/usuarios", result);
            verify(usuarioService).save(usuario);
            verify(usuarioService, never()).asignarEstablecimientos(any(), anyList());
            verify(redirectAttributes).addFlashAttribute("exito", "Establecimientos asignados correctamente.");
        }
    }

    @Nested
    @DisplayName("Tests de integración y casos edge")
    class IntegrationAndEdgeCasesTests {

        @Test
        @DisplayName("Debe verificar que se llaman todos los servicios correctamente")
        void integration_ShouldCallAllServicesCorrectly() {
            // Given
            when(sessionData.getUsuario()).thenReturn(usuario);
            when(usuarioService.findUsuarioById(USER_ID)).thenReturn(usuario);
            when(usuarioService.recuperarEstablecimientosUsuario(USER_ID, model)).thenReturn(model);
            when(establecimientoService.findAll()).thenReturn(establecimientos);

            // When
            controller.mostrarAsignacionEstablecimientos(model);

            // Then
            verify(sessionData, times(1)).getUsuario();
            verify(usuarioService, times(1)).findUsuarioById(USER_ID);
            verify(usuarioService, times(1)).recuperarEstablecimientosUsuario(USER_ID, model);
            verify(establecimientoService, times(1)).findAll();
        }

        @Test
        @DisplayName("Debe manejar usuario con establecimientos ya asignados")
        void integration_WithUserWithExistingEstablecimientos_ShouldWork() {
            // Given
            usuario.setLstEstablecimientos(establecimientos);
            when(sessionData.getUsuario()).thenReturn(usuario);
            when(usuarioService.findUsuarioById(USER_ID)).thenReturn(usuario);
            when(establecimientoService.findAllById(establecimientosIds)).thenReturn(establecimientos);
            when(usuarioService.asignarEstablecimientos(usuario, establecimientos)).thenReturn(usuario);

            // When
            String result = controller.guardarAsignacionEstablecimientos(establecimientosIds, redirectAttributes);

            // Then
            assertEquals("redirect:/menuprincipal", result);
            verify(usuarioService).save(usuario);
            verify(redirectAttributes).addFlashAttribute("exito", "Establecimientos asignados correctamente.");
        }

        @Test
        @DisplayName("Debe manejar flujo completo: mostrar y guardar")
        void integration_CompleteFlow_ShouldWork() {
            // Given - Mostrar
            when(sessionData.getUsuario()).thenReturn(usuario);
            when(usuarioService.findUsuarioById(USER_ID)).thenReturn(usuario);
            when(usuarioService.recuperarEstablecimientosUsuario(USER_ID, model)).thenReturn(model);
            when(establecimientoService.findAll()).thenReturn(establecimientos);

            // When - Mostrar
            String showResult = controller.mostrarAsignacionEstablecimientos(model);

            // Then - Mostrar
            assertEquals("establecimientos/asignacion", showResult);

            // Given - Guardar
            when(establecimientoService.findAllById(establecimientosIds)).thenReturn(establecimientos);
            when(usuarioService.asignarEstablecimientos(usuario, establecimientos)).thenReturn(usuario);

            // When - Guardar
            String saveResult = controller.guardarAsignacionEstablecimientos(establecimientosIds, redirectAttributes);

            // Then - Guardar
            assertEquals("redirect:/menuprincipal", saveResult);
            verify(usuarioService).save(usuario);
        }
    }
}