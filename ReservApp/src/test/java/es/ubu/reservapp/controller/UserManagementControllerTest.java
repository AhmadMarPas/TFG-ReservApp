package es.ubu.reservapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import es.ubu.reservapp.exception.UserNotFoundException;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.UsuarioService;

/**
 * Test para el controlador UserManagementController
 */
@ExtendWith(MockitoExtension.class)
class UserManagementControllerTest {

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
    private UserManagementController userManagementController;

    private Usuario usuario;
    private List<Usuario> usuarios;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId("usuario1");
        usuario.setNombre("Usuario");
        usuario.setApellidos("Test");
        usuario.setPassword("password");
        usuario.setCorreo("usuario@test.com");
        usuario.setAdministrador(false);
        usuario.setBloqueado(false);

        Usuario admin = new Usuario();
        admin.setId("admin");
        admin.setNombre("Admin");
        admin.setApellidos("Test");
        admin.setPassword("password");
        admin.setCorreo("admin@test.com");
        admin.setAdministrador(true);
        admin.setBloqueado(false);

        Usuario bloqueado = new Usuario();
        bloqueado.setId("bloqueado");
        bloqueado.setNombre("Bloqueado");
        bloqueado.setApellidos("Test");
        bloqueado.setPassword("password");
        bloqueado.setCorreo("bloqueado@test.com");
        bloqueado.setAdministrador(false);
        bloqueado.setBloqueado(true);

        usuarios = new ArrayList<>();
        usuarios.add(usuario);
        usuarios.add(admin);
        usuarios.add(bloqueado);
    }

    @Test
    void testListUsers() {
        when(usuarioService.findAll()).thenReturn(usuarios);
        
        String viewName = userManagementController.listUsers(model);
        
        verify(model).addAttribute("usuarios", usuarios);
        verify(model).addAttribute("adminCount", 1L);
        verify(model).addAttribute("blockedCount", 1L);
        
        assertEquals("admin/user_management", viewName);
    }

    @Test
    void testShowCreateUserForm() {
        String viewName = userManagementController.showCreateUserForm(model);
        
        verify(model).addAttribute(eq("usuario"), any(Usuario.class));
        verify(model).addAttribute("isEdit", false);
        
        assertEquals("admin/user_form", viewName);
    }

    @Test
    void testShowEditUserFormUserNotFound() {
        when(usuarioService.findUsuarioById("nonexistent")).thenReturn(null);
        
        String viewName = userManagementController.showEditUserForm("nonexistent", model, mockRedirectAttributes);
        
        verify(mockRedirectAttributes).addFlashAttribute(eq("error"), anyString());
        
        assertEquals("redirect:/admin/usuarios", viewName);
    }

    @Test
    void testShowEditUserFormSuccess() {
        when(usuarioService.findUsuarioById("usuario1")).thenReturn(usuario);
        
        String viewName = userManagementController.showEditUserForm("usuario1", model, mockRedirectAttributes);
        
        verify(model).addAttribute("usuario", usuario);
        verify(model).addAttribute("isEdit", true);
        
        assertNull(usuario.getPassword());
        
        assertEquals("admin/user_form", viewName);
    }

    @Test
    void testSaveOrUpdateUserWithValidationErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);
        
        String viewName = userManagementController.saveOrUpdateUser(usuario, bindingResult, false, model, mockRedirectAttributes);
        
        verify(model).addAttribute("isEdit", false);
        
        assertEquals("admin/user_form", viewName);
    }

    @Test
    void testSaveOrUpdateNewUserWithExistingId() {
        usuario.setId(null);
        
        String viewName = userManagementController.saveOrUpdateUser(usuario, bindingResult, true, model, mockRedirectAttributes);
        
        verify(bindingResult).rejectValue("id", "error.usuario", "No se puede modificar el usuario. El ID no existe en el sistema.");
        
        verify(model).addAttribute("isEdit", true);
        assertEquals("admin/user_form", viewName);
    }

    @Test
    void testSaveOrUpdateNewUserWithExistingEmail() {
        Usuario newUser = new Usuario();
        newUser.setId("newuser");
        newUser.setCorreo("usuario@test.com");
        newUser.setPassword("password");
        
        when(usuarioService.existeId("newuser")).thenReturn(false);
        when(usuarioService.findUsuarioByCorreo("usuario@test.com")).thenReturn(usuario);
        
        String viewName = userManagementController.saveOrUpdateUser(newUser, bindingResult, false, model, mockRedirectAttributes);
        
        verify(bindingResult).rejectValue("correo", "error.usuario", "El email ya está registrado.");
        verify(model).addAttribute("isEdit", false);
        
        assertEquals("admin/user_form", viewName);
    }

    @Test
    void testSaveOrUpdateNewUserSuccess() {
        Usuario newUser = new Usuario();
        newUser.setId("newuser");
        newUser.setNombre("Nuevo");
        newUser.setApellidos("Usuario");
        newUser.setPassword("password");
        newUser.setCorreo("nuevo@test.com");
    	RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        when(bindingResult.hasErrors()).thenReturn(false);
        
        when(usuarioService.existeId("newuser")).thenReturn(false);
        when(usuarioService.findUsuarioByCorreo("nuevo@test.com")).thenReturn(null);
        
        String viewName = userManagementController.saveOrUpdateUser(newUser, bindingResult, false, model, redirectAttributes);
        
        verify(usuarioService).save(newUser);
        
        assertNotNull(redirectAttributes.getFlashAttributes().get("exito"));
        assertEquals("Usuario creado correctamente.", redirectAttributes.getFlashAttributes().get("exito"));
        assertTrue(redirectAttributes.getFlashAttributes().containsValue("Usuario creado correctamente."));

        assertEquals("redirect:/admin/usuarios", viewName);
    }

    @Test
    void testSaveOrUpdateExistingUserSuccess() {
    	RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        when(bindingResult.hasErrors()).thenReturn(false);
        
        when(usuarioService.existeId("usuario1")).thenReturn(true);
        
        String viewName = userManagementController.saveOrUpdateUser(usuario, bindingResult, true, model, redirectAttributes);
        
        verify(usuarioService).save(usuario);
        
        assertNotNull(redirectAttributes.getFlashAttributes().get("exito"));
        assertEquals("Usuario actualizado correctamente.", redirectAttributes.getFlashAttributes().get("exito"));
        assertTrue(redirectAttributes.getFlashAttributes().containsValue("Usuario actualizado correctamente."));

        assertEquals("redirect:/admin/usuarios", viewName);
    }

    @Test
    void testBlockUser() throws UserNotFoundException {
    	RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
    	
        String viewName = userManagementController.blockUser("usuario1", redirectAttributes);
        
        verify(usuarioService).blockUser("usuario1");
        
        assertNotNull(redirectAttributes.getFlashAttributes().get("exito"));
        assertEquals("Usuario bloqueado correctamente.", redirectAttributes.getFlashAttributes().get("exito"));
        assertTrue(redirectAttributes.getFlashAttributes().containsValue("Usuario bloqueado correctamente."));
        
        assertEquals("redirect:/admin/usuarios", viewName);
    }

    @Test
    void testUnblockUser() throws UserNotFoundException {
    	RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
    	
        String viewName = userManagementController.unblockUser("usuario1", redirectAttributes);
        
        verify(usuarioService).unblockUser("usuario1");
        
        assertNotNull(redirectAttributes.getFlashAttributes().get("exito"));
        assertEquals("Usuario desbloqueado correctamente.", redirectAttributes.getFlashAttributes().get("exito"));
        assertTrue(redirectAttributes.getFlashAttributes().containsValue("Usuario desbloqueado correctamente."));
        
        assertEquals("redirect:/admin/usuarios", viewName);
    }

    @Test
    void testBlockUserNotFound() throws UserNotFoundException {
    	RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
    	
        doThrow(new UserNotFoundException("Usuario no encontrado")).when(usuarioService).blockUser("nonexistent");
        
        String viewName = userManagementController.blockUser("nonexistent", redirectAttributes);
        
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Usuario no encontrado o ya bloqueado.", redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(redirectAttributes.getFlashAttributes().containsValue("Usuario no encontrado o ya bloqueado."));
        
        assertEquals("redirect:/admin/usuarios", viewName);
    }

    @Test
    void testUnblockUserNotFound() throws UserNotFoundException {
    	RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
    	
        doThrow(new UserNotFoundException("Usuario no encontrado")).when(usuarioService).unblockUser("nonexistent");
        
        String viewName = userManagementController.unblockUser("nonexistent", redirectAttributes);
        
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Usuario no encontrado o ya desbloqueado.", redirectAttributes.getFlashAttributes().get("error"));
        assertTrue(redirectAttributes.getFlashAttributes().containsValue("Usuario no encontrado o ya desbloqueado."));
        
        assertEquals("redirect:/admin/usuarios", viewName);
    }

    @Test
    void testSaveOrUpdateNewUserWithoutPassword() {
        Usuario newUser = new Usuario();
        newUser.setId("newuser");
        newUser.setNombre("Nuevo");
        newUser.setApellidos("Usuario");
        newUser.setPassword(""); // Password vacío
        newUser.setCorreo("nuevo@test.com");
        
        when(usuarioService.existeId("newuser")).thenReturn(false);
        
        String viewName = userManagementController.saveOrUpdateUser(newUser, bindingResult, false, model, mockRedirectAttributes);
        
        verify(bindingResult).rejectValue("password", "error.usuario", "La contraseña es obligatoria para nuevos usuarios.");
        verify(model).addAttribute("isEdit", false);
        
        assertEquals("admin/user_form", viewName);
    }

    @Test
    void testSaveOrUpdateExistingUserWithExistingEmailFromOtherUser() {
        Usuario otherUser = new Usuario();
        otherUser.setId("otheruser");
        otherUser.setCorreo("usuario@test.com");
        
        usuario.setCorreo("usuario@test.com");
        
        when(usuarioService.existeId("usuario1")).thenReturn(true);
        when(usuarioService.findUsuarioByCorreo("usuario@test.com")).thenReturn(otherUser);
        
        String viewName = userManagementController.saveOrUpdateUser(usuario, bindingResult, true, model, mockRedirectAttributes);
        
        verify(bindingResult).rejectValue("correo", "error.usuario", "El email ya está registrado por otro usuario.");
        verify(model).addAttribute("isEdit", true);
        
        assertEquals("admin/user_form", viewName);
    }

    @Test
    void testSaveOrUpdateExistingUserWithEmptyPassword() {
        Usuario existingUser = new Usuario();
        existingUser.setId("usuario1");
        existingUser.setPassword("hashedpassword");
        
        usuario.setPassword(""); // Password vacío para usuario existente
        
        when(bindingResult.hasErrors()).thenReturn(false);
        when(usuarioService.existeId("usuario1")).thenReturn(true);
        when(usuarioService.findUsuarioByCorreo("usuario@test.com")).thenReturn(null);
        when(usuarioService.findUsuarioById("usuario1")).thenReturn(existingUser);
        
        String viewName = userManagementController.saveOrUpdateUser(usuario, bindingResult, true, model, mockRedirectAttributes);
        
        verify(usuarioService).save(usuario);
        verify(mockRedirectAttributes).addFlashAttribute("exito", "Usuario actualizado correctamente.");
        
        assertEquals("redirect:/admin/usuarios", viewName);
        assertEquals("hashedpassword", usuario.getPassword());
    }

    @Test
    void testSaveOrUpdateUserWithEmailToLowerCase() {
        Usuario newUser = new Usuario();
        newUser.setId("newuser");
        newUser.setNombre("Nuevo");
        newUser.setApellidos("Usuario");
        newUser.setPassword("password");
        newUser.setCorreo("NUEVO@TEST.COM"); // Email en mayúsculas
        
        when(bindingResult.hasErrors()).thenReturn(false);
        when(usuarioService.existeId("newuser")).thenReturn(false);
        
        String viewName = userManagementController.saveOrUpdateUser(newUser, bindingResult, false, model, mockRedirectAttributes);
        
        verify(usuarioService).save(newUser);
        verify(mockRedirectAttributes).addFlashAttribute("exito", "Usuario creado correctamente.");
        assertEquals("nuevo@test.com", newUser.getCorreo());
        
        assertEquals("redirect:/admin/usuarios", viewName);
    }
    
    @Test
    void testSaveNewUserSuccess() {
        // Arrange
        Usuario usuarioTest = new Usuario();
        usuarioTest.setId("newUser");
        usuarioTest.setCorreo("test@example.com");
        usuarioTest.setPassword("password123");
        
        when(bindingResult.hasErrors()).thenReturn(false);
        when(usuarioService.existeId("newUser")).thenReturn(false);
        when(usuarioService.findUsuarioByCorreo("test@example.com")).thenReturn(null);
        
        // Act
        String result = userManagementController.saveOrUpdateUser(usuarioTest, bindingResult, false, model, mockRedirectAttributes);
        
        // Assert
        assertEquals("redirect:/admin/usuarios", result);
        verify(mockRedirectAttributes).addFlashAttribute("exito", "Usuario creado correctamente.");
    }

    @Test
    void testUpdateExistingUserSuccess() {
        // Arrange
        Usuario usuarioTest = new Usuario();
        usuarioTest.setId("existingUser");
        usuarioTest.setCorreo("test@example.com");
        usuarioTest.setPassword("");
        
        Usuario existingUser = new Usuario();
        existingUser.setPassword("encodedPassword");
        
        when(bindingResult.hasErrors()).thenReturn(false);
        when(usuarioService.existeId("existingUser")).thenReturn(true);
        when(usuarioService.findUsuarioById("existingUser")).thenReturn(existingUser);
        when(usuarioService.findUsuarioByCorreo("test@example.com")).thenReturn(null);
        
        // Act
        String result = userManagementController.saveOrUpdateUser(usuarioTest, bindingResult, true, model, mockRedirectAttributes);
        
        // Assert
        assertEquals("redirect:/admin/usuarios", result);
        verify(mockRedirectAttributes).addFlashAttribute("exito", "Usuario actualizado correctamente.");
    }

    @Test
    void testSaveNewUserWithDuplicateId() {
        // Arrange
        Usuario usuarioTest = new Usuario();
        usuarioTest.setId("existingId");
        usuarioTest.setCorreo("test@example.com");
        usuarioTest.setPassword("password123");
        
        when(usuarioService.existeId("existingId")).thenReturn(true);
        
        // Act
        String result = userManagementController.saveOrUpdateUser(usuarioTest, bindingResult, false, model, mockRedirectAttributes);
        
        // Assert
        verify(bindingResult).rejectValue("id", "error.usuario", "No se puede crear un nuevo usuario. El ID ya existe en el sistema.");
        verify(model).addAttribute("isEdit", false);
        assertEquals("admin/user_form", result);
    }
    
    @Test
    void testSaveModifiedUserWithDuplicateId() {
        // Arrange
        Usuario usuarioTest = new Usuario();
        usuarioTest.setId("existingId");
        usuarioTest.setCorreo("test@example.com");
        usuarioTest.setPassword("password123");
        
        when(usuarioService.existeId("existingId")).thenReturn(true);
        
        // Act
        String result = userManagementController.saveOrUpdateUser(usuarioTest, bindingResult, true, model, mockRedirectAttributes);
        
        // Assert
        verify(mockRedirectAttributes).addFlashAttribute("exito", "Usuario actualizado correctamente.");
        assertEquals("redirect:/admin/usuarios", result);
    }


    @Test
    void testSaveNewUserWithDuplicateEmail() {
        // Arrange
        Usuario usuarioTest = new Usuario();
        usuarioTest.setId("newUser");
        usuarioTest.setCorreo("existing@example.com");
        usuarioTest.setPassword("password123");
        
        Usuario existingUser = new Usuario();
        existingUser.setCorreo("existing@example.com");
        
        when(usuarioService.existeId("newUser")).thenReturn(false);
        when(usuarioService.findUsuarioByCorreo("existing@example.com")).thenReturn(existingUser);
        
        // Act
        String result = userManagementController.saveOrUpdateUser(usuarioTest, bindingResult, false, model, mockRedirectAttributes);
        
        // Assert
        verify(bindingResult).rejectValue("correo", "error.usuario", "El email ya está registrado.");
        verify(model).addAttribute("isEdit", false);
        assertEquals("admin/user_form", result);
    }

    @Test
    void testUpdateUserWithEmptyPassword() {
        // Arrange
        Usuario usuarioTest = new Usuario();
        usuarioTest.setId("existingUser");
        usuarioTest.setCorreo("test@example.com");
        usuarioTest.setPassword("");
        
        Usuario existingUser = new Usuario();
        existingUser.setPassword("encodedPassword");
        
        when(bindingResult.hasErrors()).thenReturn(false);
        when(usuarioService.existeId("existingUser")).thenReturn(true);
        when(usuarioService.findUsuarioById("existingUser")).thenReturn(existingUser);
        
        // Act
        String result = userManagementController.saveOrUpdateUser(usuarioTest, bindingResult, true, model, mockRedirectAttributes);
        
        // Assert
        assertEquals("redirect:/admin/usuarios", result);
        assertEquals("encodedPassword", usuarioTest.getPassword());
        
        verify(mockRedirectAttributes).addFlashAttribute("exito", "Usuario actualizado correctamente.");
    }

    @Test
    void testSaveNewUserWithEmptyId() {
        // Arrange
        Usuario usuarioTest = new Usuario();
        usuarioTest.setId("");
        usuarioTest.setCorreo("test@example.com");
        usuarioTest.setPassword("password123");
        
        // Act
        String result = userManagementController.saveOrUpdateUser(usuarioTest, bindingResult, true, model, mockRedirectAttributes);
        
        // Assert
        verify(model).addAttribute("isEdit", true);
        verify(bindingResult).rejectValue("id", "error.usuario", "No se puede modificar el usuario. El ID no existe en el sistema.");
        assertEquals("admin/user_form", result);
    }

    
    
    @Test
    void testSaveOrUpdateUser_ValidBindingResult() {
        // Configurar usuario y parámetros
        Usuario usr = new Usuario();
        usr.setId("testId");
        usr.setCorreo("test@example.com");
        usr.setPassword("password");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(usuarioService.existeId("testId")).thenReturn(false);

        // Llamar al método
        String result = userManagementController.saveOrUpdateUser(usr, bindingResult, false, model, mockRedirectAttributes);

        // Verificar resultados
        verify(usuarioService).save(usr);
        verify(mockRedirectAttributes).addFlashAttribute("exito", "Usuario creado correctamente.");
        assertEquals("redirect:/admin/usuarios", result);
    }

    @Test
    void testSaveOrUpdateUser_InvalidBindingResult() {
        // Configurar usuario y parámetros
        Usuario usr = new Usuario();
        usr.setId("testId");
        usr.setCorreo("test@example.com");
        usr.setPassword("password");

        when(bindingResult.hasErrors()).thenReturn(true);

        // Llamar al método
        String result = userManagementController.saveOrUpdateUser(usr, bindingResult, false, model, mockRedirectAttributes);

        // Verificar resultados
        verify(model).addAttribute("isEdit", false);
        assertEquals("admin/user_form", result);
    }

    
    
    
    
    @Test
    void testDeleteUserSuccess() throws UserNotFoundException {
        // Arrange
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        Usuario usr	= new Usuario();
        usr.setId("usuario");
        when(sessionData.getUsuario()).thenReturn(usr);
        
        // Act
        String viewName = userManagementController.deleteUser("usuario1", redirectAttributes);
        
        // Assert
        verify(usuarioService).deleteById("usuario1");
        assertNotNull(redirectAttributes.getFlashAttributes().get("exito"));
        assertEquals("Usuario eliminado correctamente.", redirectAttributes.getFlashAttributes().get("exito"));
        assertEquals("redirect:/admin/usuarios", viewName);
    }

    @Test
    void testDeleteUserNotFound() throws UserNotFoundException {
        // Arrange
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        Usuario usr	= new Usuario();
        usr.setId("usuario");
        when(sessionData.getUsuario()).thenReturn(usr);
        doThrow(new UserNotFoundException("Usuario no encontrado")).when(usuarioService).deleteById("nonexistent");
        
        // Act
        String viewName = userManagementController.deleteUser("nonexistent", redirectAttributes);
        
        // Assert
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("Usuario no encontrado o ya eliminado.", redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("redirect:/admin/usuarios", viewName);
    }

    @Test
    void testDeleteCurrentUser() {
        // Arrange
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        Usuario usr	= new Usuario();
        usr.setId("usuario1");
        when(sessionData.getUsuario()).thenReturn(usr);
        
        // Act
        String viewName = userManagementController.deleteUser("usuario1", redirectAttributes);
        
        // Assert
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("No puedes eliminar tu propio usuario.", redirectAttributes.getFlashAttributes().get("error"));
        assertEquals("redirect:/admin/usuarios", viewName);
    }

    
    private void assertNull(Object obj) {
        assertEquals(null, obj);
    }
}