package es.ubu.reservapp.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test para la clase EntidadInfo
 * 
 * @author Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
class EntidadInfoTest {

    /**
     * Implementación concreta de EntidadInfo para pruebas
     */
    private static class EntidadInfoImpl extends EntidadInfo<Integer> {
        private static final long serialVersionUID = 1L;
		private Integer id;

        public EntidadInfoImpl(EntidadInfoImpl entidadInfoImpl) {
			this.setId(entidadInfoImpl.getId());
		}

		public EntidadInfoImpl() {
			super();
		}

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
			return new EntidadInfoImpl(this);
		}
    }

    private EntidadInfoImpl entidad;

    @BeforeEach
    void setUp() {
        entidad = new EntidadInfoImpl();
        entidad.setId(1);
    }

    @Test
    void testOrden() {
        assertNull(entidad.getOrden());
        
        entidad.setOrden(5);
        
        assertEquals(5, entidad.getOrden());
    }

    @Test
    void testUsuarioModReg() {
        assertNull(entidad.getUsuarioModReg());
        
        entidad.setUsuarioModReg("user1");
        
        assertEquals("user1", entidad.getUsuarioModReg());
    }

    @Test
    void testUsuarioCreaReg() {
        assertNull(entidad.getUsuarioCreaReg());
        
        entidad.setUsuarioCreaReg("user2");
        
        assertEquals("user2", entidad.getUsuarioCreaReg());
    }

    @Test
    void testFechaModReg() {
        assertNull(entidad.getFechaModReg());
        
        LocalDateTime now = LocalDateTime.now();
        entidad.setFechaModReg(now);
        
        assertEquals(now, entidad.getFechaModReg());
    }

    @Test
    void testFechaCreaReg() {
        assertNull(entidad.getFechaCreaReg());
        
        LocalDateTime now = LocalDateTime.now();
        entidad.setFechaCreaReg(now);
        
        assertEquals(now, entidad.getFechaCreaReg());
    }

    @Test
    void testToString() {
        LocalDateTime now = LocalDateTime.now();
        entidad.setFechaCreaReg(now);
        entidad.setUsuarioCreaReg("user1");
        
        String result = entidad.toString();
        
        // Verificamos que el toString contiene la información básica
        assertTrue(result.contains("EntidadInfoImpl"));
        assertTrue(result.contains("id=1"));
    }
    
    private void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Assertion failed");
        }
    }
}