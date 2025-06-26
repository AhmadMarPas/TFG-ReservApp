package es.ubu.reservapp.model.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.repositories.UsuarioRepo;
import jakarta.validation.ConstraintViolationException;

/**
 * Test para la clase SessionData
 * 
 * @author Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class SessionDataTest {

    @Mock
    private UsuarioRepo usuarioRepo;

    private SessionData sessionData;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId("user1");
        usuario.setNombre("Usuario Test");
        usuario.setApellidos("Apellidos Test");
        usuario.setCorreo("test@example.com");
        
        sessionData = new SessionData(usuarioRepo);
    }

    @Test
    void testConstructorVacio() {
        SessionData sessionDataVacia = new SessionData();
        
        assertNotNull(sessionDataVacia);
    }

    @Test
    void testConstructorConParametros() {
        assertNotNull(sessionData);
    }

    @Test
    void testGettersAndSetters() {
        List<Establecimiento> establecimientos = new ArrayList<>();
        Establecimiento establecimiento = new Establecimiento();
        establecimiento.setId(1);
        establecimiento.setNombre("Establecimiento Test");
        establecimientos.add(establecimiento);
        
        List<Integer> roles = new ArrayList<>();
        roles.add(1);
        roles.add(2);
        
        sessionData.setUsuario(usuario);
        sessionData.setLstEstablecimientoUsuario(establecimientos);
        sessionData.setLstRolesUsuario(roles);
        
        assertEquals(usuario, sessionData.getUsuario());
        assertEquals(establecimientos, sessionData.getLstEstablecimientoUsuario());
        assertEquals(roles, sessionData.getLstRolesUsuario());
    }

    @Test
    void testRegistroUsuarioEntradaExitoso() {
        when(usuarioRepo.findById("user1")).thenReturn(Optional.of(usuario));
        
        sessionData.registroUsuarioEntrada("user1");
        
        assertEquals(usuario, sessionData.getUsuario());
        assertNotNull(usuario.getFechaUltimoAcceso());
        verify(usuarioRepo, times(1)).save(usuario);
    }

    @Test
    void testRegistroUsuarioEntradaUsuarioNoEncontrado() {
        when(usuarioRepo.findById("userNoExiste")).thenReturn(Optional.empty());
        
        sessionData.registroUsuarioEntrada("userNoExiste");
        
        assertEquals(null, sessionData.getUsuario());
        verify(usuarioRepo, times(0)).save(any(Usuario.class));
    }

    @Test
    void testRegistroUsuarioEntradaConExcepcion() {
        when(usuarioRepo.findById("user1")).thenReturn(Optional.of(usuario));
        when(usuarioRepo.save(usuario)).thenThrow(new RuntimeException("Error al guardar"));
        
        // No debería lanzar excepción, se maneja internamente
        sessionData.registroUsuarioEntrada("user1");
        
        assertEquals(usuario, sessionData.getUsuario());
        assertNotNull(usuario.getFechaUltimoAcceso());
    }

    @Test
    void testRegistroUsuarioEntradaConConstraintViolationException() {
        ConstraintViolationException mockException = mock(ConstraintViolationException.class);
        when(usuarioRepo.findById("user1")).thenReturn(Optional.of(usuario));
        when(usuarioRepo.save(usuario)).thenThrow(mockException);
        
        // No debería lanzar excepción, se maneja internamente
        sessionData.registroUsuarioEntrada("user1");
        
        assertEquals(usuario, sessionData.getUsuario());
        assertNotNull(usuario.getFechaUltimoAcceso());
    }
}