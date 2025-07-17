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
// Test class for EntidadInfoInterceptor
@ExtendWith(MockitoExtension.class)
class EntidadInfoInterceptorSegundaFromaTest {

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
        // Instantiate interceptor with null sessionData to trigger CDI lookup path
        EntidadInfoInterceptor interceptorWithNullSession = new EntidadInfoInterceptor(null);

        // Prepare a dummy EntidadInfo object for the test, similar to one in setUp
        EntidadInfo<Integer> dummyEntidadInfo = new EntidadInfo<Integer>() {
            private Integer id;
            private static final long serialVersionUID = 1L;
            @Override public Integer getId() { return id; }
            @Override public void setId(Integer id) { this.id = id; }
            @Override public EntidadPK<Integer> copia() { return null; }
        };
        dummyEntidadInfo.setId(2); // Using a different ID for clarity

        // We expect that calling CDI.current().select(SessionData.class).get()
        // will fail in a test environment where CDI is not initialized
        // or SessionData is not a discoverable bean.
        // This typically results in IllegalStateException or a CDI-specific UnsatisfiedResolutionException.
        Exception exception = assertThrows(Exception.class, () -> {
            interceptorWithNullSession.guardar(dummyEntidadInfo);
        }, "Should throw an exception when CDI lookup fails.");

        // Check if the exception is one of the expected types from CDI
        // jakarta.enterprise.inject.UnsatisfiedResolutionException is common if bean not found by CDI
        assertTrue(exception instanceof IllegalStateException || exception instanceof jakarta.enterprise.inject.UnsatisfiedResolutionException,
                   "Expected IllegalStateException or UnsatisfiedResolutionException when CDI is not available or bean not found, but got " + exception.getClass().getName() + ": " + exception.getMessage());
    }
}