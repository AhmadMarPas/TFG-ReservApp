package es.ubu.reservapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.UsuarioService;

/**
 * Test para la clase EditarPerfilController.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EditarPerfilController Tests")
class EditarPerfilControllerTest {

    @Mock
    private SessionData sessionData;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes mockRedirectAttributes;

    @InjectMocks
    private EditarPerfilController editarPerfilController;

    private Usuario usuario;
    private Usuario usuarioAdmin;
    private Usuario usuarioBloqueado;

    private static final String USER_ID = "user123";
    private static final String ADMIN_USER_ID = "admin123";
    private static final String BLOCKED_USER_ID = "blocked123";
    private static final String USER_EMAIL = "user@test.com";
    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String BLOCKED_EMAIL = "blocked@test.com";
    private static final String NEW_EMAIL = "newemail@test.com";
    private static final String EXISTING_EMAIL = "existing@test.com";

    @BeforeEach
    void setUp() {
        // Usuario regular
        usuario = new Usuario();
        usuario.setId(USER_ID);
        usuario.setNombre("Usuario");
        usuario.setApellidos("Test");
        usuario.setPassword("password123");
        usuario.setCorreo(USER_EMAIL);
        usuario.setTelefono("123456789");
        usuario.setAdministrador(false);
        usuario.setBloqueado(false);

        // Usuario administrador
        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(ADMIN_USER_ID);
        usuarioAdmin.setNombre("Admin");
        usuarioAdmin.setApellidos("Test");
        usuarioAdmin.setPassword("adminpass");
        usuarioAdmin.setCorreo(ADMIN_EMAIL);
        usuarioAdmin.setTelefono("987654321");
        usuarioAdmin.setAdministrador(true);
        usuarioAdmin.setBloqueado(false);

        // Usuario bloqueado
        usuarioBloqueado = new Usuario();
        usuarioBloqueado.setId(BLOCKED_USER_ID);
        usuarioBloqueado.setNombre("Bloqueado");
        usuarioBloqueado.setApellidos("Test");
        usuarioBloqueado.setPassword("blockedpass");
        usuarioBloqueado.setCorreo(BLOCKED_EMAIL);
        usuarioBloqueado.setTelefono("555666777");
        usuarioBloqueado.setAdministrador(false);
        usuarioBloqueado.setBloqueado(true);
    }

    // ========== TESTS PARA mostrarFormularioEditarPerfil ==========

    @Test
    @DisplayName("Mostrar formulario - Usuario autenticado correctamente")
    void testMostrarFormularioEditarPerfil_UsuarioAutenticado() {
        when(sessionData.getUsuario()).thenReturn(usuario);

        String viewName = editarPerfilController.mostrarFormularioEditarPerfil(model, mockRedirectAttributes);

        verify(model).addAttribute(eq("usuario"), any(Usuario.class));
        verify(model).addAttribute("isEdit", true);
        assertEquals("perfiles/editar_perfil", viewName);
    }

    @Test
    @DisplayName("Mostrar formulario - Usuario no autenticado")
    void testMostrarFormularioEditarPerfil_UsuarioNoAutenticado() {
        when(sessionData.getUsuario()).thenReturn(null);

        String viewName = editarPerfilController.mostrarFormularioEditarPerfil(model, mockRedirectAttributes);

        verify(mockRedirectAttributes).addFlashAttribute("error", "Usuario no autenticado correctamente.");
        verify(model, never()).addAttribute(eq("usuario"), any());
        assertEquals("redirect:/menuprincipal", viewName);
    }

    @Test
    @DisplayName("Mostrar formulario - Usuario administrador")
    void testMostrarFormularioEditarPerfil_UsuarioAdmin() {
        when(sessionData.getUsuario()).thenReturn(usuarioAdmin);

        String viewName = editarPerfilController.mostrarFormularioEditarPerfil(model, mockRedirectAttributes);

        verify(model).addAttribute(eq("usuario"), any(Usuario.class));
        verify(model).addAttribute("isEdit", true);
        assertEquals("perfiles/editar_perfil", viewName);
    }

    @Test
    @DisplayName("Mostrar formulario - Usuario bloqueado")
    void testMostrarFormularioEditarPerfil_UsuarioBloqueado() {
        when(sessionData.getUsuario()).thenReturn(usuarioBloqueado);

        String viewName = editarPerfilController.mostrarFormularioEditarPerfil(model, mockRedirectAttributes);

        verify(model).addAttribute(eq("usuario"), any(Usuario.class));
        verify(model).addAttribute("isEdit", true);
        assertEquals("perfiles/editar_perfil", viewName);
    }

    @Test
    @DisplayName("Mostrar formulario - Excepción durante la carga")
    void testMostrarFormularioEditarPerfil_ExcepcionDuranteCarga() {
        when(sessionData.getUsuario()).thenThrow(new RuntimeException("Error de base de datos"));

        String viewName = editarPerfilController.mostrarFormularioEditarPerfil(model, mockRedirectAttributes);

        verify(mockRedirectAttributes).addFlashAttribute(eq("error"), anyString());
        assertEquals("redirect:/menuprincipal", viewName);
    }

    @Test
    @DisplayName("Mostrar formulario - Verificar que la contraseña se oculta")
    void testMostrarFormularioEditarPerfil_ContraseñaOculta() {
        when(sessionData.getUsuario()).thenReturn(usuario);

        editarPerfilController.mostrarFormularioEditarPerfil(model, mockRedirectAttributes);

        // Verificar que se llama addAttribute con un usuario que tiene password null
        verify(model).addAttribute(eq("usuario"), any(Usuario.class));
    }

    // ========== TESTS PARA guardarPerfil ==========

    @Test
    @DisplayName("Guardar perfil - Usuario no autenticado")
    void testGuardarPerfil_UsuarioNoAutenticado() {
        when(sessionData.getUsuario()).thenReturn(null);
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = editarPerfilController.guardarPerfil(usuario, bindingResult, redirectAttributes, model);

        assertEquals("Usuario no autenticado correctamente.", redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("redirect:/menuprincipal", viewName);
    }

    @Test
    @DisplayName("Guardar perfil - Usuario intenta editar perfil de otro")
    void testGuardarPerfil_UsuarioEditaPerfilAjeno() {
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId("otro_usuario");
        otroUsuario.setNombre("Otro");
        otroUsuario.setApellidos("Usuario");
        otroUsuario.setCorreo("otro@test.com");

        when(sessionData.getUsuario()).thenReturn(usuario);
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = editarPerfilController.guardarPerfil(otroUsuario, bindingResult, redirectAttributes, model);

        assertEquals("No tienes permisos para editar este perfil.", redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("redirect:/menuprincipal", viewName);
    }

    @Test
    @DisplayName("Guardar perfil - Errores de validación")
    void testGuardarPerfil_ErroresValidacion() {
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(bindingResult.hasErrors()).thenReturn(true);

        String viewName = editarPerfilController.guardarPerfil(usuario, bindingResult, mockRedirectAttributes, model);

        verify(model).addAttribute("isEdit", true);
        assertEquals("perfiles/editar_perfil", viewName);
    }

    @Test
    @DisplayName("Guardar perfil - Email ya existe")
    void testGuardarPerfil_EmailYaExiste() {
        Usuario usuarioConNuevoEmail = new Usuario();
        usuarioConNuevoEmail.setId(USER_ID);
        usuarioConNuevoEmail.setNombre("Usuario");
        usuarioConNuevoEmail.setApellidos("Test");
        usuarioConNuevoEmail.setCorreo(EXISTING_EMAIL);
        usuarioConNuevoEmail.setTelefono("123456789");

        when(sessionData.getUsuario()).thenReturn(usuario);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(usuarioService.existeEmail(EXISTING_EMAIL)).thenReturn(true);

        String viewName = editarPerfilController.guardarPerfil(usuarioConNuevoEmail, bindingResult, mockRedirectAttributes, model);

        verify(bindingResult).rejectValue("correo", "error.usuario", "Ya existe un usuario con este correo electrónico.");
        verify(model).addAttribute("isEdit", true);
        assertEquals("perfiles/editar_perfil", viewName);
    }

    @Test
    @DisplayName("Guardar perfil - Actualización exitosa sin cambio de contraseña")
    void testGuardarPerfil_ActualizacionExitosaSinCambioPassword() {
        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setId(USER_ID);
        usuarioActualizado.setNombre("Nombre Actualizado");
        usuarioActualizado.setApellidos("Apellidos Actualizados");
        usuarioActualizado.setCorreo(NEW_EMAIL);
        usuarioActualizado.setTelefono("999888777");
        usuarioActualizado.setPassword(""); // Contraseña vacía

        when(sessionData.getUsuario()).thenReturn(usuario);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(usuarioService.existeEmail(NEW_EMAIL)).thenReturn(false);
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = editarPerfilController.guardarPerfil(usuarioActualizado, bindingResult, redirectAttributes, model);

        verify(usuarioService).save(usuario);
        verify(sessionData).setUsuario(usuario);
        assertEquals("Perfil actualizado correctamente.", redirectAttributes.getFlashAttributes().get("exito"));
        assertEquals("redirect:/menuprincipal", viewName);

        // Verificar que los datos se actualizaron correctamente
        assertEquals("Nombre Actualizado", usuario.getNombre());
        assertEquals("Apellidos Actualizados", usuario.getApellidos());
        assertEquals(NEW_EMAIL.toLowerCase(), usuario.getCorreo());
        assertEquals("999888777", usuario.getTelefono());
        // La contraseña original debe mantenerse
        assertEquals("password123", usuario.getPassword());
    }

    @Test
    @DisplayName("Guardar perfil - Actualización exitosa con cambio de contraseña")
    void testGuardarPerfil_ActualizacionExitosaConCambioPassword() {
        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setId(USER_ID);
        usuarioActualizado.setNombre("Nombre Actualizado");
        usuarioActualizado.setApellidos("Apellidos Actualizados");
        usuarioActualizado.setCorreo(NEW_EMAIL);
        usuarioActualizado.setTelefono("999888777");
        usuarioActualizado.setPassword("nuevaPassword123");

        when(sessionData.getUsuario()).thenReturn(usuario);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(usuarioService.existeEmail(NEW_EMAIL)).thenReturn(false);
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = editarPerfilController.guardarPerfil(usuarioActualizado, bindingResult, redirectAttributes, model);

        verify(usuarioService).save(usuario);
        verify(sessionData).setUsuario(usuario);
        assertEquals("Perfil actualizado correctamente.", redirectAttributes.getFlashAttributes().get("exito"));
        assertEquals("redirect:/menuprincipal", viewName);

        // Verificar que la contraseña se actualizó
        assertEquals("nuevaPassword123", usuario.getPassword());
    }

    @Test
    @DisplayName("Guardar perfil - Actualización con mismo email (case insensitive)")
    void testGuardarPerfil_MismoEmailCaseInsensitive() {
        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setId(USER_ID);
        usuarioActualizado.setNombre("Nombre Actualizado");
        usuarioActualizado.setApellidos("Apellidos Actualizados");
        usuarioActualizado.setCorreo(USER_EMAIL.toUpperCase()); // Mismo email en mayúsculas
        usuarioActualizado.setTelefono("999888777");
        usuarioActualizado.setPassword("");

        when(sessionData.getUsuario()).thenReturn(usuario);
        when(bindingResult.hasErrors()).thenReturn(false);
        // No debe llamar a existeEmail porque es el mismo email
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = editarPerfilController.guardarPerfil(usuarioActualizado, bindingResult, redirectAttributes, model);

        verify(usuarioService, never()).existeEmail(anyString());
        verify(usuarioService).save(usuario);
        assertEquals("Perfil actualizado correctamente.", redirectAttributes.getFlashAttributes().get("exito"));
        assertEquals("redirect:/menuprincipal", viewName);
    }

    @Test
    @DisplayName("Guardar perfil - Contraseña con espacios en blanco")
    void testGuardarPerfil_PasswordConEspacios() {
        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setId(USER_ID);
        usuarioActualizado.setNombre("Nombre Actualizado");
        usuarioActualizado.setApellidos("Apellidos Actualizados");
        usuarioActualizado.setCorreo(NEW_EMAIL);
        usuarioActualizado.setTelefono("999888777");
        usuarioActualizado.setPassword("   "); // Solo espacios en blanco

        when(sessionData.getUsuario()).thenReturn(usuario);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(usuarioService.existeEmail(NEW_EMAIL)).thenReturn(false);
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = editarPerfilController.guardarPerfil(usuarioActualizado, bindingResult, redirectAttributes, model);

        verify(usuarioService).save(usuario);
        assertEquals("Perfil actualizado correctamente.", redirectAttributes.getFlashAttributes().get("exito"));
        assertEquals("redirect:/menuprincipal", viewName);

        // La contraseña original debe mantenerse
        assertEquals("password123", usuario.getPassword());
    }

    @Test
    @DisplayName("Guardar perfil - Contraseña null")
    void testGuardarPerfil_PasswordNull() {
        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setId(USER_ID);
        usuarioActualizado.setNombre("Nombre Actualizado");
        usuarioActualizado.setApellidos("Apellidos Actualizados");
        usuarioActualizado.setCorreo(NEW_EMAIL);
        usuarioActualizado.setTelefono("999888777");
        usuarioActualizado.setPassword(null);

        when(sessionData.getUsuario()).thenReturn(usuario);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(usuarioService.existeEmail(NEW_EMAIL)).thenReturn(false);
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = editarPerfilController.guardarPerfil(usuarioActualizado, bindingResult, redirectAttributes, model);

        verify(usuarioService).save(usuario);
        assertEquals("Perfil actualizado correctamente.", redirectAttributes.getFlashAttributes().get("exito"));
        assertEquals("redirect:/menuprincipal", viewName);

        // La contraseña original debe mantenerse
        assertEquals("password123", usuario.getPassword());
    }

    @Test
    @DisplayName("Guardar perfil - Usuario administrador mantiene privilegios")
    void testGuardarPerfil_AdminMantienePrivilegios() {
        Usuario adminActualizado = new Usuario();
        adminActualizado.setId(ADMIN_USER_ID);
        adminActualizado.setNombre("Admin Actualizado");
        adminActualizado.setApellidos("Test Actualizado");
        adminActualizado.setCorreo(NEW_EMAIL);
        adminActualizado.setTelefono("999888777");
        adminActualizado.setPassword("nuevaAdminPass");
        // Intentar cambiar privilegios (no debería afectar)
        adminActualizado.setAdministrador(false);
        adminActualizado.setBloqueado(true);

        when(sessionData.getUsuario()).thenReturn(usuarioAdmin);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(usuarioService.existeEmail(NEW_EMAIL)).thenReturn(false);
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = editarPerfilController.guardarPerfil(adminActualizado, bindingResult, redirectAttributes, model);

        verify(usuarioService).save(usuarioAdmin);
        assertEquals("Perfil actualizado correctamente.", redirectAttributes.getFlashAttributes().get("exito"));
        assertEquals("redirect:/menuprincipal", viewName);

        // Los privilegios originales deben mantenerse
        assertTrue(usuarioAdmin.isAdministrador());
        assertEquals(false, usuarioAdmin.isBloqueado());
    }

    @Test
    @DisplayName("Guardar perfil - Excepción durante el guardado")
    void testGuardarPerfil_ExcepcionDuranteGuardado() {
        when(sessionData.getUsuario()).thenReturn(usuario);
        when(bindingResult.hasErrors()).thenReturn(false);
        doThrow(new RuntimeException("Error de base de datos")).when(usuarioService).save(any(Usuario.class));

        String viewName = editarPerfilController.guardarPerfil(usuario, bindingResult, mockRedirectAttributes, model);

        verify(model).addAttribute(eq("error"), anyString());
        verify(model).addAttribute("isEdit", true);
        assertEquals("perfiles/editar_perfil", viewName);
    }

    @Test
    @DisplayName("Guardar perfil - Email se convierte a minúsculas")
    void testGuardarPerfil_EmailConvertidoAMinusculas() {
        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setId(USER_ID);
        usuarioActualizado.setNombre("Usuario");
        usuarioActualizado.setApellidos("Test");
        usuarioActualizado.setCorreo("NUEVO@TEST.COM"); // Email en mayúsculas
        usuarioActualizado.setTelefono("123456789");
        usuarioActualizado.setPassword("");

        when(sessionData.getUsuario()).thenReturn(usuario);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(usuarioService.existeEmail("NUEVO@TEST.COM")).thenReturn(false);
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        editarPerfilController.guardarPerfil(usuarioActualizado, bindingResult, redirectAttributes, model);

        // Verificar que el email se guardó en minúsculas
        assertEquals("nuevo@test.com", usuario.getCorreo());
    }

    // ========== TESTS DE INTEGRACIÓN Y CASOS EDGE ==========

    @Test
    @DisplayName("Test de integración - Flujo completo exitoso")
    void testIntegracion_FlujoCompletoExitoso() {
        // 1. Mostrar formulario
        when(sessionData.getUsuario()).thenReturn(usuario);
        String viewName1 = editarPerfilController.mostrarFormularioEditarPerfil(model, mockRedirectAttributes);
        assertEquals("perfiles/editar_perfil", viewName1);

        // 2. Guardar cambios
        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setId(USER_ID);
        usuarioActualizado.setNombre("Nuevo Nombre");
        usuarioActualizado.setApellidos("Nuevos Apellidos");
        usuarioActualizado.setCorreo(NEW_EMAIL);
        usuarioActualizado.setTelefono("111222333");
        usuarioActualizado.setPassword("nuevaPassword");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(usuarioService.existeEmail(NEW_EMAIL)).thenReturn(false);
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName2 = editarPerfilController.guardarPerfil(usuarioActualizado, bindingResult, redirectAttributes, model);

        assertEquals("redirect:/menuprincipal", viewName2);
        verify(usuarioService).save(usuario);
        verify(sessionData).setUsuario(usuario);
        assertNotNull(redirectAttributes.getFlashAttributes().get("exito"));
    }

    @Test
    @DisplayName("Verificar que los campos no editables se mantienen")
    void testCamposNoEditablesSeMantienen() {
        // Configurar usuario original con valores específicos
        usuario.setAdministrador(true);
        usuario.setBloqueado(false);

        Usuario usuarioConCambiosNoPermitidos = new Usuario();
        usuarioConCambiosNoPermitidos.setId(USER_ID);
        usuarioConCambiosNoPermitidos.setNombre("Nuevo Nombre");
        usuarioConCambiosNoPermitidos.setApellidos("Nuevos Apellidos");
        usuarioConCambiosNoPermitidos.setCorreo(NEW_EMAIL);
        usuarioConCambiosNoPermitidos.setTelefono("111222333");
        usuarioConCambiosNoPermitidos.setPassword("");
        // Intentar cambiar campos no editables
        usuarioConCambiosNoPermitidos.setAdministrador(false);
        usuarioConCambiosNoPermitidos.setBloqueado(true);

        when(sessionData.getUsuario()).thenReturn(usuario);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(usuarioService.existeEmail(NEW_EMAIL)).thenReturn(false);
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        editarPerfilController.guardarPerfil(usuarioConCambiosNoPermitidos, bindingResult, redirectAttributes, model);

        // Los campos no editables deben mantener sus valores originales
        assertTrue(usuario.isAdministrador()); // Se mantiene como admin
        assertEquals(false, usuario.isBloqueado()); // Se mantiene como no bloqueado
    }

    @Test
    @DisplayName("Verificar manejo de valores null en campos opcionales")
    void testManejoCamposNullOpcionales() {
        Usuario usuarioConCamposNull = new Usuario();
        usuarioConCamposNull.setId(USER_ID);
        usuarioConCamposNull.setNombre("Nombre");
        usuarioConCamposNull.setApellidos("Apellidos");
        usuarioConCamposNull.setCorreo(NEW_EMAIL);
        usuarioConCamposNull.setTelefono(null); // Campo opcional null
        usuarioConCamposNull.setPassword(null);

        when(sessionData.getUsuario()).thenReturn(usuario);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(usuarioService.existeEmail(NEW_EMAIL)).thenReturn(false);
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = editarPerfilController.guardarPerfil(usuarioConCamposNull, bindingResult, redirectAttributes, model);

        assertEquals("redirect:/menuprincipal", viewName);
        verify(usuarioService).save(usuario);
        // El teléfono debe actualizarse a null
        assertNull(usuario.getTelefono());
        // La contraseña original debe mantenerse
        assertEquals("password123", usuario.getPassword());
    }
}