package es.ubu.reservapp.model.entities;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests para la clase Convocatoria
 */
@DisplayName("Convocatoria Tests")
class ConvocatoriaTest {
    
    private Convocatoria convocatoria;
    private Reserva reserva;
    private List<Convocado> convocados;
    private String enlace;
    private String observaciones;
    private static final Integer ID_RESERVA = 123;
    
    @BeforeEach
    void setUp() {
        convocatoria = new Convocatoria();
        reserva = new Reserva();
        reserva.setId(ID_RESERVA);
    }
    
    @Test
    @DisplayName("Constructor por defecto")
    void testDefaultConstructor() {
        assertNotNull(convocatoria);
        assertNull(convocatoria.getId());
        assertNull(convocatoria.getReserva());
    }
    
    @Test
    @DisplayName("Constructor con Reserva y Usuario")
    void testConstructorWithReservaAndUsuario() {
        Convocatoria conv = new Convocatoria(reserva.getId(), reserva, convocados, enlace, observaciones);
        
        assertNotNull(conv.getId());
        assertEquals(ID_RESERVA, conv.getReserva().getId());
        assertEquals(reserva, conv.getReserva());
    }
    
    @Test
    @DisplayName("Getter y Setter de reserva")
    void testReservaGetterSetter() {
        convocatoria.setReserva(reserva);
        
        assertEquals(reserva, convocatoria.getReserva());
        assertEquals(ID_RESERVA, convocatoria.getReserva().getId());
        
        convocatoria.setReserva(null);
        assertNull(convocatoria.getReserva());
    }
    
    @Test
    @DisplayName("Setter de reserva con id null")
    void testSetReservaWithNullId() {
        convocatoria.setId(null);
        assertDoesNotThrow(() -> convocatoria.setReserva(reserva));
        assertEquals(reserva, convocatoria.getReserva());
    }
    
    @Test
    @DisplayName("Setter de reserva null")
    void testSetReservaNullWithId() {
        convocatoria.setReserva(null);
        assertNull(convocatoria.getReserva());
    }
    
    @Test
    @DisplayName("Getter y Setter de orden")
    void testOrdenGetterSetter() {
        Integer orden = 5;
        convocatoria.setOrden(orden);
        assertEquals(orden, convocatoria.getOrden());
        
        convocatoria.setOrden(null);
        assertNull(convocatoria.getOrden());
    }
    
    @Test
    @DisplayName("Getter y Setter de idUsuarioCreacion")
    void testIdUsuarioCreacionGetterSetter() {
        String idUsuarioCreacion = "ADMIN001";
        convocatoria.setUsuarioCreaReg(idUsuarioCreacion);
        assertEquals(idUsuarioCreacion, convocatoria.getUsuarioCreaReg());
        
        convocatoria.setUsuarioCreaReg(null);
        assertNull(convocatoria.getUsuarioCreaReg());
    }
    
    @Test
    @DisplayName("Getter y Setter de timestampCreacion")
    void testTimestampCreacionGetterSetter() {
        LocalDateTime timestamp = LocalDateTime.now();
        convocatoria.setFechaCreaReg(timestamp);
        assertEquals(timestamp, convocatoria.getFechaCreaReg());
        
        convocatoria.setFechaCreaReg(null);
        assertNull(convocatoria.getFechaCreaReg());
    }
    
    @Test
    @DisplayName("Getter y Setter de idUsuarioModificacion")
    void testIdUsuarioModificacionGetterSetter() {
        String idUsuarioModificacion = "ADMIN002";
        convocatoria.setUsuarioModReg(idUsuarioModificacion);
        assertEquals(idUsuarioModificacion, convocatoria.getUsuarioModReg());
        
        convocatoria.setUsuarioModReg(null);
        assertNull(convocatoria.getUsuarioModReg());
    }
    
    @Test
    @DisplayName("Getter y Setter de timestampModificacion")
    void testTimestampModificacionGetterSetter() {
        LocalDateTime timestamp = LocalDateTime.now();
        convocatoria.setFechaModReg(timestamp);
        assertEquals(timestamp, convocatoria.getFechaModReg());
        
        convocatoria.setFechaModReg(null);
        assertNull(convocatoria.getFechaModReg());
    }
    
    @Nested
    @DisplayName("Tests de equals")
    class EqualsTest {
        
        @Test
        @DisplayName("Equals con el mismo objeto")
        void testEqualsWithSameObject() {
            assertEquals(convocatoria, convocatoria);
        }
        
        @Test
        @DisplayName("Equals con objetos iguales")
        void testEqualsWithEqualObjects() {
            Convocatoria conv1 = new Convocatoria();
            conv1.setId(reserva.getId());
            Convocatoria conv2 = new Convocatoria();
            conv2.setId(reserva.getId());
            
            assertEquals(conv1, conv2);
            assertEquals(conv2, conv1);
        }
        
        @Test
        @DisplayName("Equals con objetos diferentes")
        void testEqualsWithDifferentObjects() {
            Convocatoria conv1 = new Convocatoria();
            conv1.setId(reserva.getId());
            Reserva reserva2 = new Reserva();
            reserva2.setId(456);
            Convocatoria conv2 = new Convocatoria();
            conv2.setId(reserva2.getId());
            Reserva res = new Reserva();
            res.setId(456);
            Usuario usr = new Usuario();
            usr.setId("USER002");
            conv2.setReserva(res);
            
            assertNotEquals(conv1, conv2);
        }
        
        @Test
        @DisplayName("Equals con null")
        void testEqualsWithNull() {
            Convocatoria conv = new Convocatoria();
            conv.setId(reserva.getId());
            assertNotEquals(null, conv);
        }
        
        @Test
        @DisplayName("Equals con clase diferente")
        void testEqualsWithDifferentClass() {
            Convocatoria conv = new Convocatoria();
            conv.setId(reserva.getId());
            String differentClass = "different";
            assertNotEquals(conv, differentClass);
        }
        
        @Test
        @DisplayName("Equals con id null")
        void testEqualsWithNullId() {
            Convocatoria conv1 = new Convocatoria();
            Convocatoria conv2 = new Convocatoria();
            
            assertEquals(conv1, conv2);
            
            conv1.setId(reserva.getId());
            assertNotEquals(conv1, conv2);
        }
    }
    
    @Test
    @DisplayName("hashCode consistente")
    void testHashCode() {
        Convocatoria conv1 = new Convocatoria();
        conv1.setId(reserva.getId());
        Convocatoria conv2 = new Convocatoria();
        conv2.setId(reserva.getId());
        
        assertEquals(conv1.hashCode(), conv2.hashCode());
        
        // hashCode con id null
        Convocatoria convNull = new Convocatoria();
        assertDoesNotThrow(convNull::hashCode);
    }
    
    @Test
    @DisplayName("toString")
    void testToString() {
        convocatoria.setId(reserva.getId());
        convocatoria.setOrden(1);
        convocatoria.setUsuarioCreaReg("ADMIN001");
        convocatoria.setFechaCreaReg(LocalDateTime.now());
        convocatoria.setUsuarioModReg("ADMIN002");
        convocatoria.setFechaModReg(LocalDateTime.now());
        
        String toString = convocatoria.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("Convocatoria"));
        
        // toString con campos null
        Convocatoria convNull = new Convocatoria();
        assertDoesNotThrow(convNull::toString);
    }
    
    @Test
    @DisplayName("Test de cobertura completa con todos los campos")
    void testFullCoverageWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        
        // Crear convocatoria completa
        Convocatoria conv = new Convocatoria(reserva.getId(), reserva, convocados, enlace, observaciones);
        conv.setOrden(10);
        conv.setUsuarioCreaReg("CREATOR001");
        conv.setFechaCreaReg(now);
        conv.setUsuarioModReg("MODIFIER001");
        conv.setFechaModReg(now.plusHours(1));
        
        // Verificar todos los campos
        assertNotNull(conv.getId());
        assertEquals(ID_RESERVA, conv.getReserva().getId());
        assertEquals(reserva, conv.getReserva());
        assertEquals(10, conv.getOrden());
        assertEquals("CREATOR001", conv.getUsuarioCreaReg());
        assertEquals(now, conv.getFechaCreaReg());
        assertEquals("MODIFIER001", conv.getUsuarioModReg());
        assertEquals(now.plusHours(1), conv.getFechaModReg());
        
        // Verificar toString, equals y hashCode
        assertNotNull(conv.toString());
        Convocatoria conv2 = conv;
        assertEquals(conv2, conv);
        assertNotEquals(0, conv.hashCode());
    }
    
    @Test
    void testConstructorVacio() {
        Convocatoria convocatoriaVacia = new Convocatoria();
        
        assertNotNull(convocatoriaVacia);
    }

    @Test
    void testConstructorConParametros() {
    	Usuario usuarioTest = new Usuario();
    	usuarioTest.setId("user3");
    	Convocado convocadoTest = new Convocado();
        convocadoTest.setUsuario(usuarioTest);
        List<Convocado> convocadosTest = new ArrayList<>();
        convocadosTest.add(convocadoTest);
        Reserva reservaTest = new Reserva();
        reservaTest.setId(3);
        String enlaceTest = "enlaceTest";
        String obsrvacionesTest = "observacionesTest";
        
        Convocatoria convocatoriaCompleta = new Convocatoria(reservaTest.getId(), reservaTest, convocadosTest, enlaceTest, obsrvacionesTest);
        
        assertEquals(reservaTest, convocatoriaCompleta.getReserva());
        assertEquals(convocadosTest, convocatoriaCompleta.getConvocados());
        assertEquals(enlaceTest, convocatoriaCompleta.getEnlace());
        assertEquals(obsrvacionesTest, convocatoriaCompleta.getObservaciones());
    }
    
    @Test
    void testConstructorCopia() {
        // Use the existing test data from setUp()
        Reserva reservaTest = new Reserva();
        reservaTest.setId(3);
        Usuario usuarioTest = new Usuario();
        usuarioTest.setId("user3");
    	Convocado convocadoTest = new Convocado();
        convocadoTest.setUsuario(usuarioTest);
        List<Convocado> convocadosTest = new ArrayList<>();
        convocadosTest.add(convocadoTest);
        String enlaceTest = "enlaceTest";
        String obsrvacionesTest = "observacionesTest";
        ConvocadoPK id = new ConvocadoPK();
        id.setIdUsuario(usuarioTest.getId());
        id.setIdReserva(reservaTest.getId());
        
        Convocatoria convocatoriaOriginal = new Convocatoria(reservaTest.getId(), reservaTest, convocadosTest, enlaceTest, obsrvacionesTest);

        // Create a copy using the copy constructor
        Convocatoria convocatoriaCopia = new Convocatoria(convocatoriaOriginal);

        // Verify all attributes are equal
        assertEquals(convocatoriaOriginal.getId(), convocatoriaCopia.getId());
        assertEquals(convocatoriaOriginal.getReserva(), convocatoriaCopia.getReserva());
        assertEquals(convocatoriaOriginal.getConvocados(), convocatoriaCopia.getConvocados());
        assertEquals(convocatoriaOriginal.getEnlace(), convocatoriaCopia.getEnlace());
        assertEquals(convocatoriaOriginal.getObservaciones(), convocatoriaCopia.getObservaciones());

        // Verify they are different objects
        assertNotSame(convocatoriaOriginal, convocatoriaCopia);

        // Verify modifying the copy doesn't affect the original
        Reserva reservaCopia = new Reserva();
        reservaCopia.setId(123);
        convocatoriaCopia.setId(reservaCopia.getId());
        assertNotEquals(convocatoriaOriginal.getId(), convocatoriaCopia.getId());
    }

    @Test
    void testMetodoCopia() {
        // Use the existing test data from setUp()
    	Convocatoria convocatoriaOriginal = new Convocatoria();
        
        convocatoriaOriginal.setReserva(reserva);
        convocatoriaOriginal.setConvocados(convocados);
        convocatoriaOriginal.setEnlace(enlace);
        convocatoriaOriginal.setObservaciones(observaciones);

        // Create a copy using the copia() method
        Convocatoria convocatoriaCopia = (Convocatoria) convocatoriaOriginal.copia();

        // Verify all attributes are equal
        assertEquals(convocatoriaOriginal.getId(), convocatoriaCopia.getId());
        assertEquals(convocatoriaOriginal.getReserva(), convocatoriaCopia.getReserva());
        assertEquals(convocatoriaOriginal.getEnlace(), convocatoriaCopia.getEnlace());
        assertEquals(convocatoriaOriginal.getObservaciones(), convocatoriaCopia.getObservaciones());

        // Verify they are different objects
        assertNotSame(convocatoriaOriginal, convocatoriaCopia);

        // Verify modifying the copy doesn't affect the original
        Reserva reservaCopia = new Reserva();
        reservaCopia.setId(124);
        convocatoriaCopia.setId(reservaCopia.getId());
        assertNotEquals(convocatoriaOriginal.getId(), convocatoriaCopia.getId());
   }

}