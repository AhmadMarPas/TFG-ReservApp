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
import es.ubu.reservapp.model.entities.Perfil;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.UsuarioService;

/**
 * Tests unitarios para PerfilRepo.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
class PerfilRepoTest {

    @Autowired
    private PerfilRepo perfilRepo;

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
        Perfil perfil = new Perfil();
        perfil.setNombre("ADMIN");
        
        // When
        Perfil savedPerfil = perfilRepo.save(perfil);
        
        // Then
        assertThat(savedPerfil.getId()).isNotNull();
        
        Perfil foundPerfil = perfilRepo.findById(savedPerfil.getId()).orElse(null);
        assertThat(foundPerfil).isNotNull();
        assertThat(foundPerfil.getNombre()).isEqualTo("ADMIN");
    }

    @Test
    void testFindByNombre_ExistingProfile() {
        // Given
        Perfil perfil = new Perfil();
        perfil.setNombre("USER");
        
        perfilRepo.save(perfil);
        
        // When
        Optional<Perfil> foundPerfil = perfilRepo.findByNombre("USER");
        
        // Then
        assertThat(foundPerfil).isPresent();
        assertThat(foundPerfil.get().getNombre()).isEqualTo("USER");
    }

    @Test
    void testFindByNombre_NonExistingProfile() {
        // When
        Optional<Perfil> foundPerfil = perfilRepo.findByNombre("NONEXISTENT");
        
        // Then
        assertThat(foundPerfil).isEmpty();
    }

    @Test
    void testFindByNombre_CaseSensitive() {
        // Given
        Perfil perfil = new Perfil();
        perfil.setNombre("Admin");
        
        perfilRepo.save(perfil);
        
        // When
        Optional<Perfil> foundPerfil = perfilRepo.findByNombre("admin");
        
        // Then
        assertThat(foundPerfil).isEmpty(); // Case sensitive
    }

    @Test
    void testFindAll() {
        // Given
        Perfil perfil1 = new Perfil();
        perfil1.setNombre("ADMIN");
        
        Perfil perfil2 = new Perfil();
        perfil2.setNombre("USER");
        
        perfilRepo.save(perfil1);
        perfilRepo.save(perfil2);
        
        // When
        var perfiles = perfilRepo.findAll();
        
        // Then
        assertThat(perfiles).hasSize(2);
        assertThat(perfiles).extracting(Perfil::getNombre)
            .containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    void testDeleteById() {
        // Given
        Perfil perfil = new Perfil();
        perfil.setNombre("TEMP");
        
        Perfil savedPerfil = perfilRepo.save(perfil);
        
        // When
        perfilRepo.deleteById(savedPerfil.getId());
        
        // Then
        assertThat(perfilRepo.findById(savedPerfil.getId())).isEmpty();
        assertThat(perfilRepo.findByNombre("TEMP")).isEmpty();
    }

    @Test
    void testExistsById() {
        // Given
        Perfil perfil = new Perfil();
        perfil.setNombre("TEST");
        
        Perfil savedPerfil = perfilRepo.save(perfil);
        
        // When & Then
        assertThat(perfilRepo.existsById(savedPerfil.getId())).isTrue();
        assertThat(perfilRepo.existsById(999)).isFalse();
    }

    @Test
    void testUpdatePerfil() {
        // Given
        Perfil perfil = new Perfil();
        perfil.setNombre("ORIGINAL");
        
        Perfil savedPerfil = perfilRepo.save(perfil);
        
        // When
        Optional<Perfil> foundPerfil = perfilRepo.findById(savedPerfil.getId());
        assertThat(foundPerfil).isPresent();
        
        Perfil updatedPerfil = perfilRepo.save(foundPerfil.get());
        
        // Then
        assertThat(updatedPerfil.getNombre()).isEqualTo("ORIGINAL");
    }
}