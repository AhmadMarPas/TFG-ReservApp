package es.ubu.reservapp.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test para la clase EntidadPK
 * 
 * @author Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
class EntidadPKTest {

    /**
     * Implementación concreta de EntidadPK para pruebas
     */
    private static class EntidadPKImpl extends EntidadPK<Integer> {
        private static final long serialVersionUID = 1L;
		private Integer id;

        public EntidadPKImpl(Integer id) {
            this.id = id;
        }

        public EntidadPKImpl(EntidadPKImpl entidadPKImpl) {
			this.setId(entidadPKImpl.getId());
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
			return new EntidadPKImpl(this);
		}
    }

    private EntidadPKImpl entidad1;
    private EntidadPKImpl entidad2;
    private EntidadPKImpl entidad3;
    private EntidadPKImpl entidadNull;

    @BeforeEach
    void setUp() {
        entidad1 = new EntidadPKImpl(1);
        entidad2 = new EntidadPKImpl(1);
        entidad3 = new EntidadPKImpl(2);
        entidadNull = new EntidadPKImpl((Integer)null);
    }

    @Test
    void testHashCode() {
        assertEquals(entidad1.hashCode(), entidad2.hashCode());
        assertNotEquals(entidad1.hashCode(), entidad3.hashCode());
        assertNotEquals(entidad1.hashCode(), entidadNull.hashCode());
    }

    @Test
    void testEquals() {
        assertEquals(entidad1, entidad1);
        
        assertNotEquals(null, entidad1);
        
        assertNotEquals("not an entity", entidad1);
        
        assertEquals(entidad2, entidad1);
        
        assertNotEquals(entidad3, entidad1);
        
        assertNotEquals(entidadNull, entidad1);
        assertNotEquals(entidad1, entidadNull);
        
        EntidadPKImpl otherNullId = new EntidadPKImpl((Integer)null);
        assertEquals(otherNullId, entidadNull);
    }

    @Test
    void testToString() {
        String result = entidad1.toString();
        
        assertEquals("EntidadPKImpl[1]", result);
    }

    @Test
    void testClone() {
        EntidadPK<Integer> cloned = entidad1.copia();
        
        assertNotNull(cloned);
        assertEquals(entidad1.getId(), cloned.getId());
        assertEquals(entidad1.getClass(), cloned.getClass());
    }
    
    @Test
    void testEqualsWithNull() {
        // Test equals with null
        assertNotEquals(null, entidad1);
        entidad2 = null;
        assertNotEquals(entidad1, entidad2);
    }
}