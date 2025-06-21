package es.ubu.reservapp.controller;

import es.ubu.reservapp.exception.UserNotFoundException;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test para el controlador UserManagementController
 */
@ExtendWith(MockitoExtension.class)
class UserManagementControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

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
        
        String viewName = userManagementController.showEditUserForm("nonexistent", model, redirectAttributes);
        
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        
        assertEquals("redirect:/admin/usuarios", viewName);
    }

    @Test
    void testShowEditUserFormSuccess() {
        when(usuarioService.findUsuarioById("usuario1")).thenReturn(usuario);
        
        String viewName = userManagementController.showEditUserForm("usuario1", model, redirectAttributes);
        
        verify(model).addAttribute("usuario", usuario);
        verify(model).addAttribute("isEdit", true);
        
        assertNull(usuario.getPassword());
        
        assertEquals("admin/user_form", viewName);
    }

    @Test
    void testSaveOrUpdateUserWithValidationErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);
//        when(bindingResult.getErrorCount()).thenReturn(2);
        
        String viewName = userManagementController.saveOrUpdateUser(usuario, bindingResult, model, redirectAttributes);
        
        verify(model).addAttribute("isEdit", false);
        
        assertEquals("admin/user_form", viewName);
    }

    @Test
    void testSaveOrUpdateNewUserWithExistingId() {
        usuario.setId(null);
        
        when(bindingResult.hasErrors()).thenReturn(false);
        
        String viewName = userManagementController.saveOrUpdateUser(usuario, bindingResult, model, redirectAttributes);
        
        verify(bindingResult).rejectValue(eq("id"), anyString(), anyString());
        
//        verify(model).addAttribute("isEdit", true);
        
        assertEquals("redirect:/admin/usuarios", viewName);
    }

    @Test
    void testSaveOrUpdateNewUserWithExistingEmail() {
        Usuario newUser = new Usuario();
        newUser.setId("newuser");
        newUser.setCorreo("usuario@test.com"); // Email que ya existe
        newUser.setPassword("password");
        
        when(bindingResult.hasErrors()).thenReturn(false);
        
        when(usuarioService.existeId("newuser")).thenReturn(false);
        when(usuarioService.findUsuarioByCorreo("usuario@test.com")).thenReturn(usuario);
        
        String viewName = userManagementController.saveOrUpdateUser(newUser, bindingResult, model, redirectAttributes);
        
        verify(bindingResult).rejectValue(eq("correo"), anyString(), anyString());
        
//        verify(model).addAttribute("isEdit", true);
        
        assertEquals("redirect:/admin/usuarios", viewName);
    }

    @Test
    void testSaveOrUpdateNewUserSuccess() {
        Usuario newUser = new Usuario();
        newUser.setId("newuser");
        newUser.setNombre("Nuevo");
        newUser.setApellidos("Usuario");
        newUser.setPassword("password");
        newUser.setCorreo("nuevo@test.com");
        
        when(bindingResult.hasErrors()).thenReturn(false);
        
        when(usuarioService.existeId("newuser")).thenReturn(false);
        when(usuarioService.findUsuarioByCorreo("nuevo@test.com")).thenReturn(null);
        
        String viewName = userManagementController.saveOrUpdateUser(newUser, bindingResult, model, redirectAttributes);
        
        verify(usuarioService).save(newUser);
        
        verify(redirectAttributes).addFlashAttribute(eq("exito"), anyString());
        
        assertEquals("redirect:/admin/usuarios", viewName);
    }

    @Test
    void testSaveOrUpdateExistingUserSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);
        
        when(usuarioService.existeId("usuario1")).thenReturn(true);
//        when(usuarioService.findUsuarioById("usuario1")).thenReturn(usuario);
        
        String viewName = userManagementController.saveOrUpdateUser(usuario, bindingResult, model, redirectAttributes);
        
        verify(usuarioService).save(usuario);
        
        verify(redirectAttributes).addFlashAttribute(eq("exito"), anyString());
        
        assertEquals("redirect:/admin/usuarios", viewName);
    }

    @Test
    void testBlockUser() throws UserNotFoundException {
        String viewName = userManagementController.blockUser("usuario1", redirectAttributes);
        
        verify(usuarioService).blockUser("usuario1");
        
        verify(redirectAttributes).addFlashAttribute(eq("exito"), anyString());
        
        assertEquals("redirect:/admin/usuarios", viewName);
    }

    @Test
    void testUnblockUser() throws UserNotFoundException {
        String viewName = userManagementController.unblockUser("usuario1", redirectAttributes);
        
        verify(usuarioService).unblockUser("usuario1");
        
        verify(redirectAttributes).addFlashAttribute(eq("exito"), anyString());
        
        assertEquals("redirect:/admin/usuarios", viewName);
    }
    
    private void assertNull(Object obj) {
        assertEquals(null, obj);
    }
}