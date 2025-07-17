package es.ubu.reservapp.model.entities;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests para la clase Convocado
 */
@DisplayName("Convocado Tests")
class ConvocadoTest {
    
    private Convocado convocado;
    private ConvocadoPK convocadoPK;
    private Convocatoria convocatoria;
    private Usuario usuario;
    private Reserva reserva;
    
    private static final Integer ID_RESERVA = 123;
    private static final String ID_USUARIO = "USER001";
    private static final String ENLACE = "https://example.com/meeting";
    private static final String OBSERVACIONES = "Reunión importante";
    
    @BeforeEach
    void setUp() {
        convocado = new Convocado();
        convocadoPK = new ConvocadoPK(ID_RESERVA, ID_USUARIO);
        
        // Configurar reserva
        reserva = new Reserva();
        reserva.setId(ID_RESERVA);
        
        // Configurar convocatoria
        convocatoria = new Convocatoria();
        convocatoria.setReserva(reserva);
        convocatoria.setEnlace(ENLACE);
        convocatoria.setObservaciones(OBSERVACIONES);
        
        // Configurar usuario
        usuario = new Usuario();
        usuario.setId(ID_USUARIO);
        usuario.setNombre("Test User");
    }
    
    @Test
    @DisplayName("Constructor por defecto")
    void testDefaultConstructor() {
        assertNotNull(convocado);
        assertNull(convocado.getId());
        assertNull(convocado.getConvocatoria());
        assertNull(convocado.getUsuario());
        assertNull(convocado.getOrden());
        assertNull(convocado.getUsuarioCreaReg());
        assertNull(convocado.getFechaCreaReg());
        assertNull(convocado.getUsuarioModReg());
        assertNull(convocado.getFechaModReg());
    }
    
    @Test
    @DisplayName("Constructor con todos los parámetros")
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        
        // @AllArgsConstructor incluye todos los campos: id, convocatoria, usuario, orden, usuarioCreaReg, fechaCreaReg, usuarioModReg, fechaModReg
        Convocado convocadoCompleto = new Convocado(convocadoPK, convocatoria, usuario);
        convocadoCompleto.setOrden(1);
        convocadoCompleto.setUsuarioCreaReg("creator");
        convocadoCompleto.setFechaCreaReg(now);
        convocadoCompleto.setUsuarioModReg("modifier");
        convocadoCompleto.setFechaModReg(now.plusHours(1));
        
        assertEquals(convocadoPK, convocadoCompleto.getId());
        assertEquals(convocatoria, convocadoCompleto.getConvocatoria());
        assertEquals(usuario, convocadoCompleto.getUsuario());
        assertEquals(1, convocadoCompleto.getOrden());
        assertEquals("creator", convocadoCompleto.getUsuarioCreaReg());
        assertEquals(now, convocadoCompleto.getFechaCreaReg());
        assertEquals("modifier", convocadoCompleto.getUsuarioModReg());
        assertEquals(now.plusHours(1), convocadoCompleto.getFechaModReg());
    }
    
    @Test
    @DisplayName("Constructor de copia")
    void testCopyConstructor() {
        // Configurar convocado original
        convocado.setId(convocadoPK);
        convocado.setConvocatoria(convocatoria);
        convocado.setUsuario(usuario);
        convocado.setOrden(1);
        convocado.setUsuarioCreaReg("creator");
        convocado.setFechaCreaReg(LocalDateTime.now());
        
        // Crear copia
        Convocado copia = new Convocado(convocado);
        
        assertEquals(convocado.getId(), copia.getId());
        assertEquals(convocado.getConvocatoria(), copia.getConvocatoria());
        assertEquals(convocado.getUsuario(), copia.getUsuario());
        // Los campos de auditoría no se copian en el constructor de copia
        assertNull(copia.getOrden());
        assertNull(copia.getUsuarioCreaReg());
        assertNull(copia.getFechaCreaReg());
    }
    
    @Test
    @DisplayName("Getter y Setter de id")
    void testIdGetterSetter() {
        convocado.setId(convocadoPK);
        assertEquals(convocadoPK, convocado.getId());
        
        convocado.setId(null);
        assertNull(convocado.getId());
    }
    
    @Test
    @DisplayName("Getter y Setter de convocatoria")
    void testConvocatoriaGetterSetter() {
        convocado.setConvocatoria(convocatoria);
        assertEquals(convocatoria, convocado.getConvocatoria());
        
        convocado.setConvocatoria(null);
        assertNull(convocado.getConvocatoria());
    }
    
    @Test
    @DisplayName("Setter de convocatoria actualiza id")
    void testSetConvocatoriaUpdatesId() {
        convocado.setId(new ConvocadoPK());
        convocado.setConvocatoria(convocatoria);
        
        assertEquals(ID_RESERVA, convocado.getId().getIdReserva());
    }
    
    @Test
    @DisplayName("Setter de convocatoria con id null")
    void testSetConvocatoriaWithNullId() {
        convocado.setId(null);
        assertDoesNotThrow(() -> convocado.setConvocatoria(convocatoria));
        assertEquals(convocatoria, convocado.getConvocatoria());
    }
    
    @Test
    @DisplayName("Setter de convocatoria con convocatoria null")
    void testSetConvocatoriaWithNullConvocatoria() {
        convocado.setId(convocadoPK);
        assertDoesNotThrow(() -> convocado.setConvocatoria(null));
        assertNull(convocado.getConvocatoria());
    }
    
    @Test
    @DisplayName("Getter y Setter de usuario")
    void testUsuarioGetterSetter() {
        convocado.setUsuario(usuario);
        assertEquals(usuario, convocado.getUsuario());
        
        convocado.setUsuario(null);
        assertNull(convocado.getUsuario());
    }
    
    @Test
    @DisplayName("Setter de usuario actualiza id")
    void testSetUsuarioUpdatesId() {
        convocado.setId(new ConvocadoPK());
        convocado.setUsuario(usuario);
        
        assertEquals(ID_USUARIO, convocado.getId().getIdUsuario());
    }
    
    @Test
    @DisplayName("Setter de usuario con id null")
    void testSetUsuarioWithNullId() {
        convocado.setId(null);
        assertDoesNotThrow(() -> convocado.setUsuario(usuario));
        assertEquals(usuario, convocado.getUsuario());
    }
    
    @Test
    @DisplayName("Setter de usuario con usuario null")
    void testSetUsuarioWithNullUsuario() {
        convocado.setId(convocadoPK);
        assertDoesNotThrow(() -> convocado.setUsuario(null));
        assertNull(convocado.getUsuario());
    }
    
    @Test
    @DisplayName("Método copia")
    void testCopia() {
        convocado.setId(convocadoPK);
        convocado.setConvocatoria(convocatoria);
        convocado.setUsuario(usuario);
        
        EntidadPK<ConvocadoPK> copia = convocado.copia();
        
        assertNotNull(copia);
        assertTrue(copia instanceof Convocado);
        
        Convocado convocadoCopia = (Convocado) copia;
        assertEquals(convocado.getId(), convocadoCopia.getId());
        assertEquals(convocado.getConvocatoria(), convocadoCopia.getConvocatoria());
        assertEquals(convocado.getUsuario(), convocadoCopia.getUsuario());
    }
    
    @Nested
    @DisplayName("Tests de campos heredados de EntidadInfo")
    class EntidadInfoFieldsTest {
        
        @Test
        @DisplayName("Getter y Setter de orden")
        void testOrdenGetterSetter() {
            convocado.setOrden(1);
            assertEquals(1, convocado.getOrden());
            
            convocado.setOrden(null);
            assertNull(convocado.getOrden());
        }
        
        @Test
        @DisplayName("Getter y Setter de usuarioCreaReg")
        void testUsuarioCreaRegGetterSetter() {
            convocado.setUsuarioCreaReg("creator");
            assertEquals("creator", convocado.getUsuarioCreaReg());
            
            convocado.setUsuarioCreaReg(null);
            assertNull(convocado.getUsuarioCreaReg());
        }
        
        @Test
        @DisplayName("Getter y Setter de fechaCreaReg")
        void testFechaCreaRegGetterSetter() {
            LocalDateTime now = LocalDateTime.now();
            convocado.setFechaCreaReg(now);
            assertEquals(now, convocado.getFechaCreaReg());
            
            convocado.setFechaCreaReg(null);
            assertNull(convocado.getFechaCreaReg());
        }
        
        @Test
        @DisplayName("Getter y Setter de usuarioModReg")
        void testUsuarioModRegGetterSetter() {
            convocado.setUsuarioModReg("modifier");
            assertEquals("modifier", convocado.getUsuarioModReg());
            
            convocado.setUsuarioModReg(null);
            assertNull(convocado.getUsuarioModReg());
        }
        
        @Test
        @DisplayName("Getter y Setter de fechaModReg")
        void testFechaModRegGetterSetter() {
            LocalDateTime now = LocalDateTime.now();
            convocado.setFechaModReg(now);
            assertEquals(now, convocado.getFechaModReg());
            
            convocado.setFechaModReg(null);
            assertNull(convocado.getFechaModReg());
        }
    }
    
    @Nested
    @DisplayName("Tests de equals y hashCode heredados")
    class EqualsHashCodeTest {
        
        @Test
        @DisplayName("Equals con el mismo objeto")
        void testEqualsWithSameObject() {
            convocado.setId(convocadoPK);
            assertEquals(convocado, convocado);
        }
        
        @Test
        @DisplayName("Equals con objetos iguales")
        void testEqualsWithEqualObjects() {
            convocado.setId(convocadoPK);
            
            Convocado otroConvocado = new Convocado();
            otroConvocado.setId(new ConvocadoPK(ID_RESERVA, ID_USUARIO));
            
            assertEquals(convocado, otroConvocado);
            assertEquals(otroConvocado, convocado);
        }
        
        @Test
        @DisplayName("Equals con objetos diferentes")
        void testEqualsWithDifferentObjects() {
            convocado.setId(convocadoPK);
            
            Convocado otroConvocado = new Convocado();
            otroConvocado.setId(new ConvocadoPK(456, "USER002"));
            
            assertNotEquals(convocado, otroConvocado);
        }
        
        @Test
        @DisplayName("Equals con null")
        void testEqualsWithNull() {
            convocado.setId(convocadoPK);
            assertNotEquals(null, convocado);
        }
        
        @Test
        @DisplayName("Equals con clase diferente")
        void testEqualsWithDifferentClass() {
            convocado.setId(convocadoPK);
            String differentClass = "different";
            assertNotEquals(convocado, differentClass);
        }
        
        @Test
        @DisplayName("Equals con id null")
        void testEqualsWithNullId() {
            Convocado convocado1 = new Convocado();
            Convocado convocado2 = new Convocado();
            
            assertEquals(convocado1, convocado2);
            
            convocado2.setId(convocadoPK);
            assertNotEquals(convocado1, convocado2);
        }
        
        @Test
        @DisplayName("hashCode consistente")
        void testHashCode() {
            convocado.setId(convocadoPK);
            
            Convocado otroConvocado = new Convocado();
            otroConvocado.setId(new ConvocadoPK(ID_RESERVA, ID_USUARIO));
            
            assertEquals(convocado.hashCode(), otroConvocado.hashCode());
            
            // hashCode con id null
            Convocado convocadoNull = new Convocado();
            assertDoesNotThrow(convocadoNull::hashCode);
        }
    }
    
    @Test
    @DisplayName("toString")
    void testToString() {
        convocado.setId(convocadoPK);
        String toString = convocado.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("Convocado"));
        assertTrue(toString.contains("id="));
        
        // toString con id null
        Convocado convocadoNull = new Convocado();
        assertDoesNotThrow(convocadoNull::toString);
    }
    
    @Test
    @DisplayName("Verificar que la clase es Serializable")
    void testSerializable() {
        // Verificar que la clase implementa Serializable
        assertTrue(convocado instanceof java.io.Serializable);
        
        // Verificar que se puede serializar sin errores
        assertDoesNotThrow(() -> {
            convocado.setId(convocadoPK);
            convocado.setConvocatoria(convocatoria);
            convocado.setUsuario(usuario);
            
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
            oos.writeObject(convocado);
            oos.close();
        });
    }
}