package es.ubu.reservapp.model.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import es.ubu.reservapp.ReservApplication;
import es.ubu.reservapp.model.entities.Menu;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.UsuarioService;

/**
 * Tests unitarios para MenuRepo.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@DataJpaTest
class MenuRepoTest {

    @Autowired
    private MenuRepo menuRepo;

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
        Menu menu = new Menu();
        menu.setNombre("Menú Principal");
        menu.setIdPadre(null);
        
        // When
        Menu savedMenu = menuRepo.save(menu);
        
        // Then
        assertThat(savedMenu.getId()).isNotNull();
        
        Menu foundMenu = menuRepo.findById(savedMenu.getId()).orElse(null);
        assertThat(foundMenu).isNotNull();
        assertThat(foundMenu.getNombre()).isEqualTo("Menú Principal");
        assertThat(foundMenu.getIdPadre()).isNull();
    }

    @Test
    void testSaveMenuWithParent() {
        // Given
        Menu menuPadre = new Menu();
        menuPadre.setNombre("Menú Padre");
        menuPadre.setIdPadre(null);
        
        Menu savedMenuPadre = menuRepo.save(menuPadre);
        
        Menu menuHijo = new Menu();
        menuHijo.setNombre("Menú Hijo");
        menuHijo.setIdPadre(savedMenuPadre.getId());
        
        // When
        Menu savedMenuHijo = menuRepo.save(menuHijo);
        
        // Then
        assertThat(savedMenuHijo.getId()).isNotNull();
        assertThat(savedMenuHijo.getIdPadre()).isEqualTo(savedMenuPadre.getId());
        
        Menu foundMenuHijo = menuRepo.findById(savedMenuHijo.getId()).orElse(null);
        assertThat(foundMenuHijo).isNotNull();
        assertThat(foundMenuHijo.getNombre()).isEqualTo("Menú Hijo");
        assertThat(foundMenuHijo.getIdPadre()).isEqualTo(savedMenuPadre.getId());
    }

    @Test
    void testFindAll() {
        // Given
        Menu menu1 = new Menu();
        menu1.setNombre("Menú 1");
        
        Menu menu2 = new Menu();
        menu2.setNombre("Menú 2");
        
        menuRepo.save(menu1);
        menuRepo.save(menu2);
        
        // When
        var menus = menuRepo.findAll();
        
        // Then
        assertThat(menus).hasSize(2);
        assertThat(menus).extracting(Menu::getNombre).containsExactlyInAnyOrder("Menú 1", "Menú 2");
    }

    @Test
    void testDeleteById() {
        // Given
        Menu menu = new Menu();
        menu.setNombre("Menú Test");
        
        Menu savedMenu = menuRepo.save(menu);
        
        // When
        menuRepo.deleteById(savedMenu.getId());
        
        // Then
        assertThat(menuRepo.findById(savedMenu.getId())).isEmpty();
    }

    @Test
    void testExistsById() {
        // Given
        Menu menu = new Menu();
        menu.setNombre("Menú Test");
        
        Menu savedMenu = menuRepo.save(menu);
        
        // When & Then
        assertThat(menuRepo.existsById(savedMenu.getId())).isTrue();
        assertThat(menuRepo.existsById(999)).isFalse();
    }

    @Test
    void testCount() {
        // Given
        Menu menu1 = new Menu();
        menu1.setNombre("Menú 1");
        
        Menu menu2 = new Menu();
        menu2.setNombre("Menú 2");
        
        menuRepo.save(menu1);
        menuRepo.save(menu2);
        
        // When & Then
        assertThat(menuRepo.count()).isEqualTo(2);
    }
}