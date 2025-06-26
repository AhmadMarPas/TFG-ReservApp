package es.ubu.reservapp.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.ubu.reservapp.model.shared.SessionData;

/**
 * Test para la clase EntidadInfoInterceptor
 * 
 * @author Test Generator
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class EntidadInfoInterceptorTest {

    @Mock
    private SessionData sessionData;

    private EntidadInfoInterceptor interceptor;
    private EntidadInfo<Integer> entidadInfo;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId("testUser");
        
        interceptor = new EntidadInfoInterceptor(sessionData);
        
        // Crear una implementación concreta de EntidadInfo para pruebas
        entidadInfo = new EntidadInfo<Integer>() {
            private Integer id;
            private static final long serialVersionUID = 1L;

            @Override
            public Integer getId() {
                return id;
            }

            @Override
            public void setId(Integer id) {
                this.id = id;
            }
        };
        entidadInfo.setId(1);
    }

    @Test
    void testGuardar() {
        // Arrange
        assertNotNull(entidadInfo);
        when(sessionData.getUsuario()).thenReturn(usuario);
        
        // Act
        interceptor.guardar(entidadInfo);
        
        // Assert
        assertNotNull(entidadInfo.getFechaCreaReg());
        assertEquals("testUser", entidadInfo.getUsuarioCreaReg());
    }

    @Test
    void testActualizar() {
        // Arrange
        assertNotNull(entidadInfo);
        when(sessionData.getUsuario()).thenReturn(usuario);
        
        // Act
        interceptor.actualizar(entidadInfo);
        
        // Assert
        assertNotNull(entidadInfo.getFechaModReg());
        assertEquals("testUser", entidadInfo.getUsuarioModReg());
        assertNotNull(entidadInfo.getFechaCreaReg());
        assertEquals("testUser", entidadInfo.getUsuarioCreaReg());
    }

    @Test
    void testActualizarConFechaCreacionExistente() {
        // Arrange
        LocalDateTime fechaCreacion = LocalDateTime.now().minusDays(1);
        entidadInfo.setFechaCreaReg(fechaCreacion);
        entidadInfo.setUsuarioCreaReg("oldUser");
        when(sessionData.getUsuario()).thenReturn(usuario);
        
        // Act
        interceptor.actualizar(entidadInfo);
        
        // Assert
        assertNotNull(entidadInfo.getFechaModReg());
        assertEquals("testUser", entidadInfo.getUsuarioModReg());
        assertEquals(fechaCreacion, entidadInfo.getFechaCreaReg());
        assertEquals("oldUser", entidadInfo.getUsuarioCreaReg());
    }

    @Test
    void testGetSessionDataWhenNull() {
        // Arrange
        SessionData mockSessionData = mock(SessionData.class);
        Usuario mockUsuario = new Usuario();
        mockUsuario.setId("dynamicUser");
        when(mockSessionData.getUsuario()).thenReturn(mockUsuario);
        
        // Crear un interceptor con el mock de SessionData
        EntidadInfoInterceptor mockInterceptor = new EntidadInfoInterceptor(mockSessionData);
        
        // Act
        mockInterceptor.guardar(entidadInfo);
        
        // Assert
        assertEquals("dynamicUser", entidadInfo.getUsuarioCreaReg());
    }
}