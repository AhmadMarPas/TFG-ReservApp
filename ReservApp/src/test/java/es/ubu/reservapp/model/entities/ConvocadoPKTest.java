package es.ubu.reservapp.model.entities;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests para la clase ConvocadoPK
 */
@DisplayName("ConvocadoPK Tests")
class ConvocadoPKTest {
    
    private ConvocadoPK convocadoId;
    private static final Integer ID_RESERVA = 123;
    private static final String ID_USUARIO = "USER001";
    
    @BeforeEach
    void setUp() {
        convocadoId = new ConvocadoPK();
    }
    
    @Test
    @DisplayName("Constructor por defecto")
    void testDefaultConstructor() {
        assertNotNull(convocadoId);
        assertNull(convocadoId.getIdReserva());
        assertNull(convocadoId.getIdUsuario());
    }
    
    @Test
    @DisplayName("Constructor con parámetros")
    void testParameterizedConstructor() {
    	ConvocadoPK id = new ConvocadoPK(ID_RESERVA, ID_USUARIO);
        
        assertEquals(ID_RESERVA, id.getIdReserva());
        assertEquals(ID_USUARIO, id.getIdUsuario());
    }
    
    @Test
    @DisplayName("Getter y Setter de idReserva")
    void testIdReservaGetterSetter() {
        convocadoId.setIdReserva(ID_RESERVA);
        assertEquals(ID_RESERVA, convocadoId.getIdReserva());
        
        convocadoId.setIdReserva(null);
        assertNull(convocadoId.getIdReserva());
    }
    
    @Test
    @DisplayName("Getter y Setter de idUsuario")
    void testIdUsuarioGetterSetter() {
        convocadoId.setIdUsuario(ID_USUARIO);
        assertEquals(ID_USUARIO, convocadoId.getIdUsuario());
        
        convocadoId.setIdUsuario(null);
        assertNull(convocadoId.getIdUsuario());
    }
    
    @Nested
    @DisplayName("Tests de equals")
    class EqualsTest {
        
        @Test
        @DisplayName("Equals con el mismo objeto")
        void testEqualsWithSameObject() {
        	ConvocadoPK id = new ConvocadoPK(ID_RESERVA, ID_USUARIO);
            assertEquals(id, id);
        }
        
        @Test
        @DisplayName("Equals con objetos iguales")
        void testEqualsWithEqualObjects() {
        	ConvocadoPK id1 = new ConvocadoPK(ID_RESERVA, ID_USUARIO);
        	ConvocadoPK id2 = new ConvocadoPK(ID_RESERVA, ID_USUARIO);
            
            assertEquals(id1, id2);
            assertEquals(id2, id1);
        }
        
        @Test
        @DisplayName("Equals con objetos diferentes")
        void testEqualsWithDifferentObjects() {
        	ConvocadoPK id1 = new ConvocadoPK(ID_RESERVA, ID_USUARIO);
            ConvocadoPK id2 = new ConvocadoPK(456, "USER002");
            
            assertNotEquals(id1, id2);
        }
        
        @Test
        @DisplayName("Equals con null")
        void testEqualsWithNull() {
        	ConvocadoPK id = new ConvocadoPK(ID_RESERVA, ID_USUARIO);
            assertNotEquals(null, id);
            id.equals(null);
            assertNotEquals(true, id.equals(null));
        }
        
        @Test
        @DisplayName("Equals con clase diferente")
        void testEqualsWithDifferentClass() {
        	ConvocadoPK id = new ConvocadoPK(ID_RESERVA, ID_USUARIO);
            String differentClass = "different";
            assertNotEquals(id, differentClass);
        }
        
        @Test
        @DisplayName("Equals con campos null")
        void testEqualsWithNullFields() {
        	ConvocadoPK id1 = new ConvocadoPK(null, null);
            ConvocadoPK id2 = new ConvocadoPK(null, null);
            ConvocadoPK id3 = new ConvocadoPK(ID_RESERVA, null);
            ConvocadoPK id4 = new ConvocadoPK(null, ID_USUARIO);
            
            assertEquals(id1, id2);
            assertNotEquals(id1, id3);
            assertNotEquals(id1, id4);
            assertNotEquals(id3, id4);
        }
    }
    
    @Test
    @DisplayName("hashCode consistente")
    void testHashCode() {
    	ConvocadoPK id1 = new ConvocadoPK(ID_RESERVA, ID_USUARIO);
    	ConvocadoPK id2 = new ConvocadoPK(ID_RESERVA, ID_USUARIO);
        
        assertEquals(id1.hashCode(), id2.hashCode());
        
        // hashCode con campos null
        ConvocadoPK idNull = new ConvocadoPK(null, null);
        assertDoesNotThrow(idNull::hashCode);
    }
    
    @Test
    @DisplayName("toString")
    void testToString() {
    	ConvocadoPK id = new ConvocadoPK(ID_RESERVA, ID_USUARIO);
        String toString = id.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("ConvocadoPK"));
        assertTrue(toString.contains(ID_RESERVA.toString()));
        assertTrue(toString.contains(ID_USUARIO));
        
        // toString con campos null
        ConvocadoPK idNull = new ConvocadoPK(null, null);
        assertDoesNotThrow(idNull::toString);
    }
    

}