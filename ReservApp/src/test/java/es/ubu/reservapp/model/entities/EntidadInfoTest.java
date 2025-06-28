package es.ubu.reservapp.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

    private EntidadInfoImpl entidad1;
    private EntidadInfoImpl entidad2;

    @BeforeEach
    void setUp() {
        entidad1 = new EntidadInfoImpl();
        entidad1.setId(1);
        entidad2 = new EntidadInfoImpl();
        entidad2.setId(2);
    }

    @Test
    void testOrden() {
        assertNull(entidad1.getOrden());
        
        entidad1.setOrden(5);
        
        assertEquals(5, entidad1.getOrden());
    }

    @Test
    void testUsuarioModReg() {
        assertNull(entidad1.getUsuarioModReg());
        
        entidad1.setUsuarioModReg("user1");
        
        assertEquals("user1", entidad1.getUsuarioModReg());
    }

    @Test
    void testUsuarioCreaReg() {
        assertNull(entidad1.getUsuarioCreaReg());
        
        entidad1.setUsuarioCreaReg("user2");
        
        assertEquals("user2", entidad1.getUsuarioCreaReg());
    }

    @Test
    void testFechaModReg() {
        assertNull(entidad1.getFechaModReg());
        
        LocalDateTime now = LocalDateTime.now();
        entidad1.setFechaModReg(now);
        
        assertEquals(now, entidad1.getFechaModReg());
    }

    @Test
    void testFechaCreaReg() {
        assertNull(entidad1.getFechaCreaReg());
        
        LocalDateTime now = LocalDateTime.now();
        entidad1.setFechaCreaReg(now);
        
        assertEquals(now, entidad1.getFechaCreaReg());
    }

    @Test
    void testToString() {
        LocalDateTime now = LocalDateTime.now();
        entidad1.setFechaCreaReg(now);
        entidad1.setUsuarioCreaReg("user1");
        
        String result = entidad1.toString();
        
        // Verificamos que el toString contiene la información básica
        assertTrue(result.contains("EntidadInfoImpl"));
        assertTrue(result.contains("id=1"));
    }
    
    @Test
    void testHashCodeAndEquals() {
        entidad1 = new EntidadInfoImpl();
        entidad2 = new EntidadInfoImpl();
        EntidadInfoImpl differentEntity = new EntidadInfoImpl();
        differentEntity.setId(3);
        
        // Test equals reflexivity
        assertTrue(entidad1.equals(entidad1));
        
        // Test equals symmetry and hashCode consistency
        assertEquals(entidad2, entidad1);
        assertEquals(entidad1, entidad2);
        assertEquals(entidad1.hashCode(), entidad2.hashCode());
        
        // Test with null
        assertNotEquals(null, entidad1);
        
        // Test with different class type (covers getClass() != obj.getClass())
        assertNotEquals(differentEntity, entidad1);
        
        // Test with different class
        assertNotEquals(new Object(), entidad1);
        
        // Test with different superclass values (covers !super.equals(obj))
        entidad1.setId(1);
        entidad2.setId(2);
        assertNotEquals(entidad2, entidad1);
        
        // Test with different values
        LocalDateTime now = LocalDateTime.now();
        entidad1.setFechaCreaReg(now);
        entidad1.setFechaModReg(now);
        entidad1.setOrden(1);
        entidad1.setUsuarioCreaReg("user1");
        entidad1.setUsuarioModReg("user1");
        
        assertNotEquals(entidad2, entidad1);
        assertNotEquals(entidad1.hashCode(), entidad2.hashCode());
        
        // Test with same values
        entidad2.setId(1);
        entidad2.setFechaCreaReg(now);
        entidad2.setFechaModReg(now);
        entidad2.setOrden(1);
        entidad2.setUsuarioCreaReg("user1");
        entidad2.setUsuarioModReg("user1");
        
        assertTrue(entidad1.equals(entidad2));
        assertEquals(entidad1.hashCode(), entidad2.hashCode());
        
        // Create a different class that extends EntidadInfo
        class DifferentEntidadInfo extends EntidadInfo<Integer> {
            private static final long serialVersionUID = 1L;
            private Integer id;
            
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
        }
        
        DifferentEntidadInfo differentClassEntity = new DifferentEntidadInfo();
        // This will trigger getClass() != obj.getClass()
        assertNotEquals(entidad1, differentClassEntity);
        
        // Rest of the test remains the same...
        // Test equals reflexivity
        assertEquals(entidad1, entidad1);
    }
    
    private void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Assertion failed");
        }
    }
}