package es.ubu.reservapp.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
 * @author Ahmad Mareie Pascual
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

			@Override
			public EntidadPK<Integer> copia() {
				return null;
			}
        };
        entidadInfo.setId(1);
    }

    @Test
    void testGuardar() {
        assertNotNull(entidadInfo);
        when(sessionData.getUsuario()).thenReturn(usuario);
        
        interceptor.guardar(entidadInfo);
        
        assertNotNull(entidadInfo.getFechaCreaReg());
        assertEquals("testUser", entidadInfo.getUsuarioCreaReg());
    }

    @Test
    void testActualizar() {
        assertNotNull(entidadInfo);
        when(sessionData.getUsuario()).thenReturn(usuario);
        
        interceptor.actualizar(entidadInfo);
        
        assertNotNull(entidadInfo.getFechaModReg());
        assertEquals("testUser", entidadInfo.getUsuarioModReg());
        assertNotNull(entidadInfo.getFechaCreaReg());
        assertEquals("testUser", entidadInfo.getUsuarioCreaReg());
    }

    @Test
    void testActualizarConFechaCreacionExistente() {
        LocalDateTime fechaCreacion = LocalDateTime.now().minusDays(1);
        entidadInfo.setFechaCreaReg(fechaCreacion);
        entidadInfo.setUsuarioCreaReg("oldUser");
        when(sessionData.getUsuario()).thenReturn(usuario);
        
        interceptor.actualizar(entidadInfo);
        
        assertNotNull(entidadInfo.getFechaModReg());
        assertEquals("testUser", entidadInfo.getUsuarioModReg());
        assertEquals(fechaCreacion, entidadInfo.getFechaCreaReg());
        assertEquals("oldUser", entidadInfo.getUsuarioCreaReg());
    }

    @Test
    void testActualizarConSessionDataMock() {
        SessionData mockSessionData = mock(SessionData.class);
        Usuario mockUsuario = new Usuario();
        mockUsuario.setId("dynamicUser");
        when(mockSessionData.getUsuario()).thenReturn(mockUsuario);
        
        LocalDateTime fechaCreacion = LocalDateTime.now().minusDays(1);
        entidadInfo.setFechaCreaReg(fechaCreacion);
        entidadInfo.setUsuarioCreaReg("oldUser");
        
        EntidadInfoInterceptor mockInterceptor = new EntidadInfoInterceptor(mockSessionData);
        
        mockInterceptor.actualizar(entidadInfo);
        
        assertNotNull(entidadInfo.getFechaModReg());
        assertEquals("dynamicUser", entidadInfo.getUsuarioModReg());
        assertEquals(fechaCreacion, entidadInfo.getFechaCreaReg());
        assertEquals("oldUser", entidadInfo.getUsuarioCreaReg());
    }

    @Test
    void testGuardarConSessionDataMock() {
        SessionData mockSessionData = mock(SessionData.class);
        Usuario mockUsuario = new Usuario();
        mockUsuario.setId("dynamicUser");
        when(mockSessionData.getUsuario()).thenReturn(mockUsuario);
        
        EntidadInfoInterceptor mockInterceptor = new EntidadInfoInterceptor(mockSessionData);
        
        mockInterceptor.guardar(entidadInfo);
        
        assertEquals("dynamicUser", entidadInfo.getUsuarioCreaReg());
    }
    
    @Test
    void testGetSessionData_whenSessionDataIsNull_attemptsCdiLookup() {
        // Crear interceptor con sessionData null para activar la búsqueda CDI
        EntidadInfoInterceptor interceptorWithNullSession = new EntidadInfoInterceptor(null);

        // Preparar un objeto EntidadInfo dummy para el test
        EntidadInfo<Integer> dummyEntidadInfo = new EntidadInfo<Integer>() {
            private Integer id;
            private static final long serialVersionUID = 1L;
            @Override public Integer getId() { return id; }
            @Override public void setId(Integer id) { this.id = id; }
            @Override public EntidadPK<Integer> copia() { return null; }
        };
        dummyEntidadInfo.setId(2);

        Exception exception = assertThrows(Exception.class, () -> {
            interceptorWithNullSession.guardar(dummyEntidadInfo);
        }, "Debería lanzar una excepción cuando la búsqueda CDI falla.");

        // Verificar si la excepción es uno de los tipos esperados de CDI
        assertTrue(exception instanceof IllegalStateException || 
                   exception instanceof jakarta.enterprise.inject.UnsatisfiedResolutionException,
                   "Se esperaba IllegalStateException o UnsatisfiedResolutionException cuando CDI no está disponible o el bean no se encuentra, pero se obtuvo " + 
                   exception.getClass().getName() + ": " + exception.getMessage());
    }
}