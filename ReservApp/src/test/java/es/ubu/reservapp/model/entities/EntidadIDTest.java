package es.ubu.reservapp.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.Serializable;

import org.junit.jupiter.api.Test;

import es.ubu.reservapp.model.EntidadID;

/**
 * Test para la interfaz EntidadID
 * 
 * @author Test Generator
 * @version 1.0
 * @since 1.0
 */
class EntidadIDTest {

    /**
     * Implementación concreta de EntidadID para pruebas
     */
    private static class EntidadIDImpl implements EntidadID<Integer> {
        private Integer id;

        @Override
        public Integer getId() {
            return id;
        }

        @Override
        public void setId(Integer id) {
            this.id = id;
        }
    }

    @Test
    void testGetAndSetId() {
        // Arrange
        EntidadIDImpl entidad = new EntidadIDImpl();
        assertNull(entidad.getId());
        
        // Act
        entidad.setId(1);
        
        // Assert
        assertEquals(1, entidad.getId());
        
        // Act again
        entidad.setId(null);
        
        // Assert again
        assertNull(entidad.getId());
    }
}