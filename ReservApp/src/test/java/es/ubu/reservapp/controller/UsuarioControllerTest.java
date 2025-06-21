package es.ubu.reservapp.controller;

import es.ubu.reservapp.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test para el controlador UsuarioController
 */
@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {
	
	private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UsuarioController usuarioController;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
//        when(securityContext.getAuthentication()).thenReturn(authentication);
        
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();
    }

    @Test
    void testGetUserInfo_ReturnsUserInfoSuccessfully() throws Exception {
        String username = "testUser";
        Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        when(authentication.getAuthorities()).thenAnswer(invocation -> authorities); // Usar thenAnswer para evitar problemas con tipos genéricos

        mockMvc.perform(get("/users/account")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.info").value("Basic Auth"))
                .andExpect(jsonPath("$.authorities[0].authority").value("ROLE_USER"));
    }
    
    @Test
    void testGetUserInfo_WithMultipleAuthorities() throws Exception {
        String username = "multiRoleUser";
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        when(authentication.getName()).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenAnswer(invocation -> authorities);

        mockMvc.perform(get("/users/account")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.info").value("Basic Auth"))
                .andExpect(jsonPath("$.authorities[0].authority").value("ROLE_USER"))
                .andExpect(jsonPath("$.authorities[1].authority").value("ROLE_ADMIN"));
    }
    
    @Test
    void testGetUserInfo() {
        when(authentication.getName()).thenReturn("testuser");
        
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("USER"));
        authorities.add(new SimpleGrantedAuthority("ADMIN"));
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        
        ResponseEntity<Map<String, Object>> response = usuarioController.getUserInfo();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertEquals("Basic Auth", body.get("info"));
        assertEquals("testuser", body.get("username"));
        
        Collection<?> responseAuthorities = (Collection<?>) body.get("authorities");
        assertEquals(2, responseAuthorities.size());
        assertTrue(responseAuthorities.containsAll(authorities));
    }

    @Test
    void testAdminData() {
        String viewName = usuarioController.admindata();
        
        assertEquals("admin page", viewName);
    }

    @Test
    void testUserPage() {
        String viewName = usuarioController.userpage();
        
        assertEquals("user page", viewName);
    }
}