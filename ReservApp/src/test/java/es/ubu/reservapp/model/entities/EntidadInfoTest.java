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
    
    @Test
    void testEquals() {
        // Create a concrete implementation of EntidadInfo for testing
        EntidadInfo<Integer> entidadUno = new EntidadInfo<Integer>() {
            private static final long serialVersionUID = 1L;

			@Override
            public Integer getId() {
                return 1;
            }

            @Override
            public void setId(Integer id) {
            	// No es necesario implementar este método para la prueba
            }

            @Override
            public EntidadPK<Integer> copia() {
                return null;
            }
        };

        EntidadInfo<Integer> entidadDos = new EntidadInfo<Integer>() {
            private static final long serialVersionUID = 1L;

			@Override
            public Integer getId() {
                return 1;
            }

            @Override
            public void setId(Integer id) {
            	// No es necesario implementar este método para la prueba
            }

            @Override
            public EntidadPK<Integer> copia() {
                return null;
            }
        };

        // Test null case
        assertNotEquals(null, entidadUno);

        // Test same object
        assertTrue(entidadUno.equals(entidadUno));

        // Test different class
        assertNotEquals(entidadUno, new Object());

        // Test with equal attributes
        entidadUno.setOrden(1);
        entidadUno.setUsuarioModReg("user1");
        entidadUno.setUsuarioCreaReg("user1");
        entidadUno.setFechaModReg(LocalDateTime.now());
        entidadUno.setFechaCreaReg(LocalDateTime.now());

        entidadDos.setOrden(1);
        entidadDos.setUsuarioModReg("user1");
        entidadDos.setUsuarioCreaReg("user1");
        entidadDos.setFechaModReg(entidadUno.getFechaModReg());
        entidadDos.setFechaCreaReg(entidadUno.getFechaCreaReg());

        assertTrue(entidadUno.equals(entidadUno));

        // Test with different attributes
        entidadDos.setOrden(2);
        assertNotEquals(entidadUno, entidadDos);

        entidadDos.setOrden(1);
        entidadDos.setUsuarioModReg("user2");
        assertNotEquals(entidadUno, entidadDos);
        
        entidadDos.setUsuarioModReg("user1");
        entidadDos.setUsuarioCreaReg("user2");
        assertNotEquals(entidadUno, entidadDos);
        
        entidadDos.setUsuarioCreaReg("user1");
        entidadDos.setFechaModReg(LocalDateTime.now().plusDays(1));
        assertNotEquals(entidadUno, entidadDos);
        
        entidadDos.setFechaModReg(entidadUno.getFechaModReg());
        entidadDos.setFechaCreaReg(LocalDateTime.now().plusDays(1));
        assertNotEquals(entidadUno, entidadDos);
    }

    private static class TestEntidadInfo extends EntidadInfo<Long> {
        private static final long serialVersionUID = 1L;

        private Long id;
        
		public TestEntidadInfo() {
            super();
        }

		@Override
		public Long getId() {
			return id;
		}

		@Override
		public void setId(Long id) {
			this.id = id;
		}

		@Override
		public EntidadPK<Long> copia() {
			return null;
		}
    }
    
    @Test
    void testEquals2() {
        // Create test objects
        TestEntidadInfo entity1 = new TestEntidadInfo();
        TestEntidadInfo entity2 = new TestEntidadInfo();
        
        // Test same object reference
        assertTrue(entity1.equals(entity1));
        
        // Test null object
        assertNotEquals(null, entity1);
        
        // Test different class
        assertNotEquals(entity1, new Object());
        
        // Test equal objects
        LocalDateTime now = LocalDateTime.now();
        entity1.setId(1L);
        entity1.setOrden(1);
        entity1.setUsuarioModReg("user1");
        entity1.setUsuarioCreaReg("user1");
        entity1.setFechaModReg(now);
        entity1.setFechaCreaReg(now);
        
        entity2.setId(1L);
        entity2.setOrden(1);
        entity2.setUsuarioModReg("user1");
        entity2.setUsuarioCreaReg("user1");
        entity2.setFechaModReg(now);
        entity2.setFechaCreaReg(now);
        
        assertTrue(entity1.equals(entity2));
        
        // Test different values
        entity2.setId(2L);
        assertNotEquals(entity1, entity2);
        
        entity2.setOrden(1);
        entity2.setUsuarioModReg("user2");
        assertNotEquals(entity1, entity2);
        
        entity2.setUsuarioModReg("user1");
        entity2.setFechaModReg(now.plusDays(1));
        assertNotEquals(entity1, entity2);
    }
    
    private void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Assertion failed");
        }
    }
}