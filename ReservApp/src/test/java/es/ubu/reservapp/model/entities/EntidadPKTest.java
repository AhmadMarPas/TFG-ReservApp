package es.ubu.reservapp.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test para la clase EntidadPK
 * 
 * @author Test Generator
 * @version 1.0
 * @since 1.0
 */
class EntidadPKTest {

    /**
     * Implementación concreta de EntidadPK para pruebas
     */
    private static class EntidadPKImpl extends EntidadPK<Integer> {
        private Integer id;

        public EntidadPKImpl(Integer id) {
            this.id = id;
        }

        @Override
        public Integer getId() {
            return id;
        }

        @Override
        public void setId(Integer id) {
            this.id = id;
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
        entidadNull = new EntidadPKImpl(null);
    }

    @Test
    void testHashCode() {
        // Arrange & Act & Assert
        assertEquals(entidad1.hashCode(), entidad2.hashCode());
        assertNotEquals(entidad1.hashCode(), entidad3.hashCode());
        assertNotEquals(entidad1.hashCode(), entidadNull.hashCode());
    }

    @Test
    void testEquals() {
        // Mismo objeto
        assertTrue(entidad1.equals(entidad1));
        
        // Null
        assertFalse(entidad1.equals(null));
        
        // Diferente clase
        assertFalse(entidad1.equals("not an entity"));
        
        // Mismo ID
        assertTrue(entidad1.equals(entidad2));
        
        // Diferente ID
        assertFalse(entidad1.equals(entidad3));
        
        // ID null en uno
        assertFalse(entidad1.equals(entidadNull));
        assertFalse(entidadNull.equals(entidad1));
        
        // ID null en ambos
        EntidadPKImpl otherNullId = new EntidadPKImpl(null);
        assertTrue(entidadNull.equals(otherNullId));
    }

    @Test
    void testToString() {
        // Arrange & Act
        String result = entidad1.toString();
        
        // Assert
        assertEquals("EntidadPKImpl[1]", result);
    }

    @Test
    void testClone() throws CloneNotSupportedException {
        // Arrange & Act
        EntidadPK<Integer> cloned = entidad1.clone();
        
        // Assert
        assertNotNull(cloned);
        assertEquals(entidad1.getId(), cloned.getId());
        assertEquals(entidad1.getClass(), cloned.getClass());
    }
}