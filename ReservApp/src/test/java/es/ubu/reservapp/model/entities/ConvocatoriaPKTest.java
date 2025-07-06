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
 * Tests para la clase ConvocatoriaId
 */
@DisplayName("ConvocatoriaPK Tests")
class ConvocatoriaPKTest {
    
    private ConvocatoriaPK convocatoriaId;
    private static final Integer ID_RESERVA = 123;
    private static final String ID_USUARIO = "USER001";
    
    @BeforeEach
    void setUp() {
        convocatoriaId = new ConvocatoriaPK();
    }
    
    @Test
    @DisplayName("Constructor por defecto")
    void testDefaultConstructor() {
        assertNotNull(convocatoriaId);
        assertNull(convocatoriaId.getIdReserva());
        assertNull(convocatoriaId.getIdUsuario());
    }
    
    @Test
    @DisplayName("Constructor con parámetros")
    void testParameterizedConstructor() {
    	ConvocatoriaPK id = new ConvocatoriaPK(ID_RESERVA, ID_USUARIO);
        
        assertEquals(ID_RESERVA, id.getIdReserva());
        assertEquals(ID_USUARIO, id.getIdUsuario());
    }
    
    @Test
    @DisplayName("Getter y Setter de idReserva")
    void testIdReservaGetterSetter() {
        convocatoriaId.setIdReserva(ID_RESERVA);
        assertEquals(ID_RESERVA, convocatoriaId.getIdReserva());
        
        convocatoriaId.setIdReserva(null);
        assertNull(convocatoriaId.getIdReserva());
    }
    
    @Test
    @DisplayName("Getter y Setter de idUsuario")
    void testIdUsuarioGetterSetter() {
        convocatoriaId.setIdUsuario(ID_USUARIO);
        assertEquals(ID_USUARIO, convocatoriaId.getIdUsuario());
        
        convocatoriaId.setIdUsuario(null);
        assertNull(convocatoriaId.getIdUsuario());
    }
    
    @Nested
    @DisplayName("Tests de equals")
    class EqualsTest {
        
        @Test
        @DisplayName("Equals con el mismo objeto")
        void testEqualsWithSameObject() {
        	ConvocatoriaPK id = new ConvocatoriaPK(ID_RESERVA, ID_USUARIO);
            assertEquals(id, id);
        }
        
        @Test
        @DisplayName("Equals con objetos iguales")
        void testEqualsWithEqualObjects() {
        	ConvocatoriaPK id1 = new ConvocatoriaPK(ID_RESERVA, ID_USUARIO);
        	ConvocatoriaPK id2 = new ConvocatoriaPK(ID_RESERVA, ID_USUARIO);
            
            assertEquals(id1, id2);
            assertEquals(id2, id1);
        }
        
        @Test
        @DisplayName("Equals con objetos diferentes")
        void testEqualsWithDifferentObjects() {
        	ConvocatoriaPK id1 = new ConvocatoriaPK(ID_RESERVA, ID_USUARIO);
            ConvocatoriaPK id2 = new ConvocatoriaPK(456, "USER002");
            
            assertNotEquals(id1, id2);
        }
        
        @Test
        @DisplayName("Equals con null")
        void testEqualsWithNull() {
        	ConvocatoriaPK id = new ConvocatoriaPK(ID_RESERVA, ID_USUARIO);
            assertNotEquals(null, id);
        }
        
        @Test
        @DisplayName("Equals con clase diferente")
        void testEqualsWithDifferentClass() {
        	ConvocatoriaPK id = new ConvocatoriaPK(ID_RESERVA, ID_USUARIO);
            String differentClass = "different";
            assertNotEquals(id, differentClass);
        }
        
        @Test
        @DisplayName("Equals con campos null")
        void testEqualsWithNullFields() {
        	ConvocatoriaPK id1 = new ConvocatoriaPK(null, null);
            ConvocatoriaPK id2 = new ConvocatoriaPK(null, null);
            ConvocatoriaPK id3 = new ConvocatoriaPK(ID_RESERVA, null);
            ConvocatoriaPK id4 = new ConvocatoriaPK(null, ID_USUARIO);
            
            assertEquals(id1, id2);
            assertNotEquals(id1, id3);
            assertNotEquals(id1, id4);
            assertNotEquals(id3, id4);
        }
    }
    
    @Test
    @DisplayName("hashCode consistente")
    void testHashCode() {
    	ConvocatoriaPK id1 = new ConvocatoriaPK(ID_RESERVA, ID_USUARIO);
    	ConvocatoriaPK id2 = new ConvocatoriaPK(ID_RESERVA, ID_USUARIO);
        
        assertEquals(id1.hashCode(), id2.hashCode());
        
        // hashCode con campos null
        ConvocatoriaPK idNull = new ConvocatoriaPK(null, null);
        assertDoesNotThrow(idNull::hashCode);
    }
    
    @Test
    @DisplayName("toString")
    void testToString() {
    	ConvocatoriaPK id = new ConvocatoriaPK(ID_RESERVA, ID_USUARIO);
        String toString = id.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("ConvocatoriaPK"));
        assertTrue(toString.contains(ID_RESERVA.toString()));
        assertTrue(toString.contains(ID_USUARIO));
        
        // toString con campos null
        ConvocatoriaPK idNull = new ConvocatoriaPK(null, null);
        assertDoesNotThrow(idNull::toString);
    }
}