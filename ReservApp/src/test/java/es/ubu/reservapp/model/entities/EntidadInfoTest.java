package es.ubu.reservapp.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test para la clase EntidadInfo
 * 
 * @author Test Generator
 * @version 1.0
 * @since 1.0
 */
class EntidadInfoTest {

    /**
     * Implementación concreta de EntidadInfo para pruebas
     */
    private static class EntidadInfoImpl extends EntidadInfo<Integer> {
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

    private EntidadInfoImpl entidad;

    @BeforeEach
    void setUp() {
        entidad = new EntidadInfoImpl();
        entidad.setId(1);
    }

    @Test
    void testOrden() {
        // Arrange & Act & Assert
        assertNull(entidad.getOrden());
        
        // Act
        entidad.setOrden(5);
        
        // Assert
        assertEquals(5, entidad.getOrden());
    }

    @Test
    void testUsuarioModReg() {
        // Arrange & Act & Assert
        assertNull(entidad.getUsuarioModReg());
        
        // Act
        entidad.setUsuarioModReg("user1");
        
        // Assert
        assertEquals("user1", entidad.getUsuarioModReg());
    }

    @Test
    void testUsuarioCreaReg() {
        // Arrange & Act & Assert
        assertNull(entidad.getUsuarioCreaReg());
        
        // Act
        entidad.setUsuarioCreaReg("user2");
        
        // Assert
        assertEquals("user2", entidad.getUsuarioCreaReg());
    }

    @Test
    void testFechaModReg() {
        // Arrange & Act & Assert
        assertNull(entidad.getFechaModReg());
        
        // Act
        LocalDateTime now = LocalDateTime.now();
        entidad.setFechaModReg(now);
        
        // Assert
        assertEquals(now, entidad.getFechaModReg());
    }

    @Test
    void testFechaCreaReg() {
        // Arrange & Act & Assert
        assertNull(entidad.getFechaCreaReg());
        
        // Act
        LocalDateTime now = LocalDateTime.now();
        entidad.setFechaCreaReg(now);
        
        // Assert
        assertEquals(now, entidad.getFechaCreaReg());
    }

    @Test
    void testToString() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        entidad.setFechaCreaReg(now);
        entidad.setUsuarioCreaReg("user1");
        
        // Act
        String result = entidad.toString();
        
        // Assert
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