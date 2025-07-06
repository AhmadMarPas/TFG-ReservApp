package es.ubu.reservapp.model.entities;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

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
    private ConvocatoriaPK convocatoriaId;
    private Reserva reserva;
    private Usuario usuario;
    private static final Integer ID_RESERVA = 123;
    private static final String ID_USUARIO = "USER001";
    
    @BeforeEach
    void setUp() {
        convocatoria = new Convocatoria();
        convocatoriaId = new ConvocatoriaPK(ID_RESERVA, ID_USUARIO);
        reserva = new Reserva();
        reserva.setId(ID_RESERVA);
        usuario = new Usuario();
        usuario.setId(ID_USUARIO);
    }
    
    @Test
    @DisplayName("Constructor por defecto")
    void testDefaultConstructor() {
        assertNotNull(convocatoria);
        assertNull(convocatoria.getId());
        assertNull(convocatoria.getReserva());
        assertNull(convocatoria.getUsuario());
    }
    
    @Test
    @DisplayName("Constructor con Reserva y Usuario")
    void testConstructorWithReservaAndUsuario() {
        Convocatoria conv = new Convocatoria(convocatoriaId, reserva, usuario, null, null);
        
        assertNotNull(conv.getId());
        assertEquals(ID_RESERVA, conv.getId().getIdReserva());
        assertEquals(ID_USUARIO, conv.getId().getIdUsuario());
        assertEquals(reserva, conv.getReserva());
        assertEquals(usuario, conv.getUsuario());
    }
    
    @Test
    @DisplayName("Constructor con ConvocatoriaId")
    void testConstructorWithConvocatoriaId() {
        Convocatoria conv = new Convocatoria();
        conv.setId(convocatoriaId);
        
        assertEquals(convocatoriaId, conv.getId());
    }
    
    @Test
    @DisplayName("Getter y Setter de id")
    void testIdGetterSetter() {
        convocatoria.setId(convocatoriaId);
        assertEquals(convocatoriaId, convocatoria.getId());
        
        convocatoria.setId(null);
        assertNull(convocatoria.getId());
    }
    
    @Test
    @DisplayName("Getter y Setter de reserva")
    void testReservaGetterSetter() {
        convocatoria.setId(new ConvocatoriaPK());
        convocatoria.setReserva(reserva);
        
        assertEquals(reserva, convocatoria.getReserva());
        assertEquals(ID_RESERVA, convocatoria.getId().getIdReserva());
        
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
        convocatoria.setId(convocatoriaId);
        convocatoria.setReserva(null);
        assertNull(convocatoria.getReserva());
    }
    
    @Test
    @DisplayName("Getter y Setter de usuario")
    void testUsuarioGetterSetter() {
        convocatoria.setId(new ConvocatoriaPK());
        convocatoria.setUsuario(usuario);
        
        assertEquals(usuario, convocatoria.getUsuario());
        assertEquals(ID_USUARIO, convocatoria.getId().getIdUsuario());
        
        convocatoria.setUsuario(null);
        assertNull(convocatoria.getUsuario());
    }
    
    @Test
    @DisplayName("Setter de usuario con id null")
    void testSetUsuarioWithNullId() {
        convocatoria.setId(null);
        assertDoesNotThrow(() -> convocatoria.setUsuario(usuario));
        assertEquals(usuario, convocatoria.getUsuario());
    }
    
    @Test
    @DisplayName("Setter de usuario null")
    void testSetUsuarioNullWithId() {
        convocatoria.setId(convocatoriaId);
        convocatoria.setUsuario(null);
        assertNull(convocatoria.getUsuario());
    }
    
    @Test
    @DisplayName("Getter y Setter de enlace")
    void testEnlaceGetterSetter() {
        String enlace = "https://meet.google.com/abc-123";
        convocatoria.setEnlace(enlace);
        assertEquals(enlace, convocatoria.getEnlace());
        
        convocatoria.setEnlace(null);
        assertNull(convocatoria.getEnlace());
    }
    
    @Test
    @DisplayName("Getter y Setter de observaciones")
    void testObservacionesGetterSetter() {
        String observaciones = "Reunión importante del equipo";
        convocatoria.setObservaciones(observaciones);
        assertEquals(observaciones, convocatoria.getObservaciones());
        
        convocatoria.setObservaciones(null);
        assertNull(convocatoria.getObservaciones());
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
            conv1.setId(convocatoriaId);
            Convocatoria conv2 = new Convocatoria();
            conv2.setId(convocatoriaId);
            
            assertEquals(conv1, conv2);
            assertEquals(conv2, conv1);
        }
        
        @Test
        @DisplayName("Equals con objetos diferentes")
        void testEqualsWithDifferentObjects() {
            Convocatoria conv1 = new Convocatoria();
            conv1.setId(convocatoriaId);
            Convocatoria conv2 = new Convocatoria();
            conv2.setId(new ConvocatoriaPK(456, "USER002"));
            Reserva res = new Reserva();
            res.setId(456);
            Usuario usr = new Usuario();
            usr.setId("USER002");
            conv2.setReserva(res);
            conv2.setUsuario(usr);
            
            assertNotEquals(conv1, conv2);
        }
        
        @Test
        @DisplayName("Equals con null")
        void testEqualsWithNull() {
            Convocatoria conv = new Convocatoria();
            conv.setId(convocatoriaId);
            assertNotEquals(null, conv);
        }
        
        @Test
        @DisplayName("Equals con clase diferente")
        void testEqualsWithDifferentClass() {
            Convocatoria conv = new Convocatoria();
            conv.setId(convocatoriaId);
            String differentClass = "different";
            assertNotEquals(conv, differentClass);
        }
        
        @Test
        @DisplayName("Equals con id null")
        void testEqualsWithNullId() {
            Convocatoria conv1 = new Convocatoria();
            Convocatoria conv2 = new Convocatoria();
            
            assertEquals(conv1, conv2);
            
            conv1.setId(convocatoriaId);
            assertNotEquals(conv1, conv2);
        }
    }
    
    @Test
    @DisplayName("hashCode consistente")
    void testHashCode() {
        Convocatoria conv1 = new Convocatoria();
        conv1.setId(convocatoriaId);
        Convocatoria conv2 = new Convocatoria();
        conv2.setId(convocatoriaId);
        
        assertEquals(conv1.hashCode(), conv2.hashCode());
        
        // hashCode con id null
        Convocatoria convNull = new Convocatoria();
        assertDoesNotThrow(convNull::hashCode);
    }
    
    @Test
    @DisplayName("toString")
    void testToString() {
        convocatoria.setId(convocatoriaId);
        convocatoria.setEnlace("https://test.com");
        convocatoria.setObservaciones("Test observaciones");
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
        Convocatoria conv = new Convocatoria(convocatoriaId, reserva, usuario, null, null);
        conv.setEnlace("https://meet.google.com/test");
        conv.setObservaciones("Reunión de seguimiento del proyecto");
        conv.setOrden(10);
        conv.setUsuarioCreaReg("CREATOR001");
        conv.setFechaCreaReg(now);
        conv.setUsuarioModReg("MODIFIER001");
        conv.setFechaModReg(now.plusHours(1));
        
        // Verificar todos los campos
        assertNotNull(conv.getId());
        assertEquals(ID_RESERVA, conv.getId().getIdReserva());
        assertEquals(ID_USUARIO, conv.getId().getIdUsuario());
        assertEquals(reserva, conv.getReserva());
        assertEquals(usuario, conv.getUsuario());
        assertEquals("https://meet.google.com/test", conv.getEnlace());
        assertEquals("Reunión de seguimiento del proyecto", conv.getObservaciones());
        assertEquals(10, conv.getOrden());
        assertEquals("CREATOR001", conv.getUsuarioCreaReg());
        assertEquals(now, conv.getFechaCreaReg());
        assertEquals("MODIFIER001", conv.getUsuarioModReg());
        assertEquals(now.plusHours(1), conv.getFechaModReg());
        
        // Verificar toString, equals y hashCode
        assertNotNull(conv.toString());
        assertTrue(conv.equals(conv));
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
        Reserva reservaTest = new Reserva();
        reservaTest.setId(3);
        ConvocatoriaPK id = new ConvocatoriaPK();
        id.setIdUsuario(usuarioTest.getId());
        id.setIdReserva(reservaTest.getId());
        String enlace = "URL";
        String observaciones = "Reunión";
        
        Convocatoria convocatoriaCompleta = new Convocatoria(id, reservaTest, usuarioTest, enlace, observaciones);
        
        assertEquals(id, convocatoriaCompleta.getId());
        assertEquals(reservaTest, convocatoriaCompleta.getReserva());
        assertEquals(usuarioTest, convocatoriaCompleta.getUsuario());
        assertEquals(enlace, convocatoriaCompleta.getEnlace());
        assertEquals(observaciones, convocatoriaCompleta.getObservaciones());
    }
    
    @Test
    void testConstructorCopia() {
        // Use the existing test data from setUp()
        Usuario usuarioTest = new Usuario();
        usuarioTest.setId("user3");
        Reserva reservaTest = new Reserva();
        reservaTest.setId(3);
        ConvocatoriaPK id = new ConvocatoriaPK();
        id.setIdUsuario(usuarioTest.getId());
        id.setIdReserva(reservaTest.getId());
        String enlace = "URL";
        String observaciones = "Reunión";
        
        Convocatoria convocatoriaOriginal = new Convocatoria(id, reservaTest, usuarioTest, enlace, observaciones);

        // Create a copy using the copy constructor
        Convocatoria convocatoriaCopia = new Convocatoria(convocatoriaOriginal);

        // Verify all attributes are equal
        assertEquals(convocatoriaOriginal.getId(), convocatoriaCopia.getId());
        assertEquals(convocatoriaOriginal.getReserva(), convocatoriaCopia.getReserva());
        assertEquals(convocatoriaOriginal.getUsuario(), convocatoriaCopia.getUsuario());
        assertEquals(convocatoriaOriginal.getEnlace(), convocatoriaCopia.getEnlace());
        assertEquals(convocatoriaOriginal.getObservaciones(), convocatoriaCopia.getObservaciones());

        // Verify they are different objects
        assertNotSame(convocatoriaOriginal, convocatoriaCopia);

        // Verify modifying the copy doesn't affect the original
        convocatoriaCopia.setId(new ConvocatoriaPK(123, "UsuarioCopia"));
        convocatoriaCopia.setEnlace("nueva URL");
        convocatoriaCopia.setObservaciones("Nueva Reunión");
        assertNotEquals(convocatoriaOriginal.getId(), convocatoriaCopia.getId());
        assertNotEquals(convocatoriaOriginal.getEnlace(), convocatoriaCopia.getEnlace());
        assertNotEquals(convocatoriaOriginal.getObservaciones(), convocatoriaCopia.getObservaciones());
    }

    @Test
    void testMetodoCopia() {
        // Use the existing test data from setUp()
    	Convocatoria convocatoriaOriginal = new Convocatoria();
        
        convocatoriaOriginal.setId(new ConvocatoriaPK(reserva.getId(), usuario.getId()));
        convocatoriaOriginal.setReserva(reserva);
        convocatoriaOriginal.setUsuario(usuario);
        convocatoriaOriginal.setEnlace("Enlace");
        convocatoriaOriginal.setObservaciones("Observaciones");

        // Create a copy using the copia() method
        Convocatoria convocatoriaCopia = (Convocatoria) convocatoriaOriginal.copia();

        // Verify all attributes are equal
        assertEquals(convocatoriaOriginal.getId(), convocatoriaCopia.getId());
        assertEquals(convocatoriaOriginal.getUsuario(), convocatoriaCopia.getUsuario());
        assertEquals(convocatoriaOriginal.getReserva(), convocatoriaCopia.getReserva());
        assertEquals(convocatoriaOriginal.getEnlace(), convocatoriaCopia.getEnlace());
        assertEquals(convocatoriaOriginal.getObservaciones(), convocatoriaCopia.getObservaciones());

        // Verify they are different objects
        assertNotSame(convocatoriaOriginal, convocatoriaCopia);

        // Verify modifying the copy doesn't affect the original
        convocatoriaCopia.setId(new ConvocatoriaPK(123, "UsuarioCopia"));
        convocatoriaCopia.setEnlace("nueva URL");
        convocatoriaCopia.setObservaciones("Nueva Reunión");
       assertNotEquals(convocatoriaOriginal.getId(), convocatoriaCopia.getId());
       assertNotEquals(convocatoriaOriginal.getEnlace(), convocatoriaCopia.getEnlace());
       assertNotEquals(convocatoriaOriginal.getObservaciones(), convocatoriaCopia.getObservaciones());
   }

}