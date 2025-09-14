package es.ubu.reservapp.model.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import es.ubu.reservapp.ReservApplication;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.UsuarioService;

/**
 * Tests unitarios para UsuarioRepo.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepoTest {

    @Autowired
    private UsuarioRepo usuarioRepo;

    @MockitoBean
    private ReservApplication reservApplication;
    
    @MockitoBean
    private SessionData sessionData;
    
    @MockitoBean
    private UsuarioService usuarioService;
    
    @MockitoBean
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        // Mock de usuario
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId("test");

        when(sessionData.getUsuario()).thenReturn(usuarioMock);
    }

    @Test
    void testSaveAndFindById() {
        // Given
        Usuario usuario = new Usuario();
        usuario.setId("testuser");
        usuario.setPassword("password123");
        usuario.setCorreo("test@example.com");
        usuario.setNombre("Test");
        usuario.setApellidos("User");
        usuario.setTelefono("123456789");
        
        // When
        Usuario savedUsuario = usuarioRepo.save(usuario);
        
        // Then
        assertThat(savedUsuario.getId()).isEqualTo("testuser");
        
        Usuario foundUsuario = usuarioRepo.findById("testuser").orElse(null);
        assertThat(foundUsuario).isNotNull();
        assertThat(foundUsuario.getId()).isEqualTo("testuser");
        assertThat(foundUsuario.getCorreo()).isEqualTo("test@example.com");
        assertThat(foundUsuario.getNombre()).isEqualTo("Test");
        assertThat(foundUsuario.getApellidos()).isEqualTo("User");
    }

    @Test
    void testFindUsuarioByIdAndPassword_ValidCredentials() {
        // Given
        Usuario usuario = new Usuario();
        usuario.setId("validuser");
        usuario.setPassword("validpass");
        usuario.setCorreo("valid@example.com");
        usuario.setNombre("Valid");
        usuario.setApellidos("User");
        usuario.setTelefono("987654321");
        
        usuarioRepo.save(usuario);
        
        // When
        Usuario foundUsuario = usuarioRepo.findUsuarioByIdAndPassword("validuser", "validpass");
        
        // Then
        assertThat(foundUsuario).isNotNull();
        assertThat(foundUsuario.getId()).isEqualTo("validuser");
        assertThat(foundUsuario.getPassword()).isEqualTo("validpass");
    }

    @Test
    void testFindUsuarioByIdAndPassword_InvalidPassword() {
        // Given
        Usuario usuario = new Usuario();
        usuario.setId("testuser");
        usuario.setPassword("correctpass");
        usuario.setCorreo("test@example.com");
        usuario.setNombre("Test");
        usuario.setApellidos("User");
        usuario.setTelefono("123456789");
        
        usuarioRepo.save(usuario);
        
        // When
        Usuario foundUsuario = usuarioRepo.findUsuarioByIdAndPassword("testuser", "wrongpass");
        
        // Then
        assertThat(foundUsuario).isNull();
    }

    @Test
    void testFindUsuarioByIdAndPassword_InvalidUser() {
        // When
        Usuario foundUsuario = usuarioRepo.findUsuarioByIdAndPassword("nonexistent", "anypass");
        
        // Then
        assertThat(foundUsuario).isNull();
    }

    @Test
    void testFindUsuarioById_ExistingUser() {
        // Given
        Usuario usuario = new Usuario();
        usuario.setId("existuser");
        usuario.setPassword("password");
        usuario.setCorreo("existing@example.com");
        usuario.setNombre("Existing");
        usuario.setApellidos("User");
        usuario.setTelefono("111222333");
        
        usuarioRepo.save(usuario);
        
        // When
        Usuario foundUsuario = usuarioRepo.findUsuarioById("existuser");
        
        // Then
        assertThat(foundUsuario).isNotNull();
        assertThat(foundUsuario.getId()).isEqualTo("existuser");
        assertThat(foundUsuario.getCorreo()).isEqualTo("existing@example.com");
    }

    @Test
    void testFindUsuarioById_NonExistingUser() {
        // When
        Usuario foundUsuario = usuarioRepo.findUsuarioById("nonexistent");
        
        // Then
        assertThat(foundUsuario).isNull();
    }

    @Test
    void testFindByCorreo_ExistingEmail() {
        // Given
        Usuario usuario = new Usuario();
        usuario.setId("emailuser");
        usuario.setPassword("password");
        usuario.setCorreo("unique@example.com");
        usuario.setNombre("Email");
        usuario.setApellidos("User");
        usuario.setTelefono("444555666");
        
        usuarioRepo.save(usuario);
        
        // When
        Optional<Usuario> foundUsuario = usuarioRepo.findByCorreo("unique@example.com");
        
        // Then
        assertThat(foundUsuario).isPresent();
        assertThat(foundUsuario.get().getId()).isEqualTo("emailuser");
        assertThat(foundUsuario.get().getCorreo()).isEqualTo("unique@example.com");
    }

    @Test
    void testFindByCorreo_NonExistingEmail() {
        // When
        Optional<Usuario> foundUsuario = usuarioRepo.findByCorreo("nonexistent@example.com");
        
        // Then
        assertThat(foundUsuario).isEmpty();
    }

    @Test
    void testFindByCorreo_CaseSensitive() {
        // Given
        Usuario usuario = new Usuario();
        usuario.setId("caseuser");
        usuario.setPassword("password");
        usuario.setCorreo("Case@Example.com");
        usuario.setNombre("Case");
        usuario.setApellidos("User");
        usuario.setTelefono("777888999");
        
        usuarioRepo.save(usuario);
        
        // When
        Optional<Usuario> foundUsuario = usuarioRepo.findByCorreo("case@examp1e.com");
        
        // Then
        assertThat(foundUsuario).isEmpty(); // Case sensitive
    }

    @Test
    void testFindAll() {
        // Given
        Usuario usuario1 = new Usuario();
        usuario1.setId("user1");
        usuario1.setPassword("pass1");
        usuario1.setCorreo("user1@example.com");
        usuario1.setNombre("User1");
        usuario1.setApellidos("Test");
        usuario1.setTelefono("111111111");
        
        Usuario usuario2 = new Usuario();
        usuario2.setId("user2");
        usuario2.setPassword("pass2");
        usuario2.setCorreo("user2@example.com");
        usuario2.setNombre("User2");
        usuario2.setApellidos("Test");
        usuario2.setTelefono("222222222");
        
        usuarioRepo.save(usuario1);
        usuarioRepo.save(usuario2);
        
        // When
        var usuarios = usuarioRepo.findAll();
        
        // Then
        assertThat(usuarios).hasSize(2);
        assertThat(usuarios).extracting(Usuario::getId)
            .containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    void testDeleteById() {
        // Given
        Usuario usuario = new Usuario();
        usuario.setId("deleteuser");
        usuario.setPassword("password");
        usuario.setCorreo("delete@example.com");
        usuario.setNombre("Delete");
        usuario.setApellidos("User");
        usuario.setTelefono("999888777");
        
        usuarioRepo.save(usuario);
        
        // When
        usuarioRepo.deleteById("deleteuser");
        
        // Then
        assertThat(usuarioRepo.findById("deleteuser")).isEmpty();
        assertThat(usuarioRepo.findUsuarioById("deleteuser")).isNull();
        assertThat(usuarioRepo.findByCorreo("delete@example.com")).isEmpty();
    }

    @Test
    void testExistsById() {
        // Given
        Usuario usuario = new Usuario();
        usuario.setId("existsuser");
        usuario.setPassword("password");
        usuario.setCorreo("exists@example.com");
        usuario.setNombre("Exists");
        usuario.setApellidos("User");
        usuario.setTelefono("666777888");
        
        usuarioRepo.save(usuario);
        
        // When & Then
        assertThat(usuarioRepo.existsById("existsuser")).isTrue();
        assertThat(usuarioRepo.existsById("nonexistent")).isFalse();
    }
}