package es.ubu.reservapp.model.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import es.ubu.reservapp.ReservApplication;
import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.UsuarioService;

@DataJpaTest
class EstablecimientoRepoTest {

    @Autowired
    private EstablecimientoRepo establecimientoRepo;
    
    @MockitoBean
    private ReservApplication reservApplication;
    
    @MockitoBean
    private SessionData sessionData;
    
    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    void testGuardarYBuscarEstablecimiento() {
        // Mock de usuario
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId("test"); // Ajusta el tipo de ID según tu entidad

        when(sessionData.getUsuario()).thenReturn(usuarioMock);
        
        // Crear establecimiento de prueba
        Establecimiento est = new Establecimiento();
        est.setNombre("Bar Ejemplo");
        est.setDescripcion("desc");
        est.setCapacidad(100);
        est.setAforo(10);
        // Añade aquí otros setters si tienes más atributos obligatorios

        // Guardar
        Establecimiento guardado = establecimientoRepo.save(est);
        assertThat(guardado.getId()).isNotNull();

        // Buscar por ID
        Establecimiento encontrado = establecimientoRepo.findById(guardado.getId()).orElse(null);
        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getNombre()).isEqualTo("Bar Ejemplo");
    }
}