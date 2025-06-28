package es.ubu.reservapp.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test para la clase Menu
 * 
 * @author Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
class MenuTest {

    private Menu menu;

    @BeforeEach
    void setUp() {
        menu = new Menu();
        menu.setId(1);
        menu.setNombre("Menu Test");
        menu.setIdPadre(null);
    }

    @Test
    void testGettersAndSetters() {
        // Verificar getters
        assertEquals(1, menu.getId());
        assertEquals("Menu Test", menu.getNombre());
        assertEquals(null, menu.getIdPadre());
        
        // Probar setters con nuevos valores
        menu.setId(2);
        menu.setNombre("Nuevo Menu");
        menu.setIdPadre(1);
        
        // Verificar nuevos valores
        assertEquals(2, menu.getId());
        assertEquals("Nuevo Menu", menu.getNombre());
        assertEquals(1, menu.getIdPadre());
    }

    @Test
    void testConstructorVacio() {
        Menu menuVacio = new Menu();
        
        assertNotNull(menuVacio);
    }

    @Test
    void testConstructorConParametros() {
        Integer id = 3;
        Integer idPadre = 2;
        String nombre = "Menu Constructor";
        
        Menu menuCompleto = new Menu(id, idPadre, nombre);
        
        assertEquals(id, menuCompleto.getId());
        assertEquals(idPadre, menuCompleto.getIdPadre());
        assertEquals(nombre, menuCompleto.getNombre());
    }

    @Test
    void testHerenciaEntidadInfo() {
        Integer orden = 5;
        String usuarioCreaReg = "user1";
        
        menu.setOrden(orden);
        menu.setUsuarioCreaReg(usuarioCreaReg);
        
        assertEquals(orden, menu.getOrden());
        assertEquals(usuarioCreaReg, menu.getUsuarioCreaReg());
    }
    
    @Test
    void testConstructorCopia() {
        // Create a Menu object with test data
        Menu menuOriginal = new Menu();
        menuOriginal.setId(4);
        menuOriginal.setNombre("Menu Original");
        menuOriginal.setIdPadre(2);
        menuOriginal.setOrden(3);
        menuOriginal.setUsuarioCreaReg("user1");
        
        // Create a copy using the copy constructor
        Menu menuCopia = new Menu(menuOriginal);
        
        // Verify all attributes are equal
        assertEquals(menuOriginal.getId(), menuCopia.getId());
        assertEquals(menuOriginal.getNombre(), menuCopia.getNombre());
        assertEquals(menuOriginal.getIdPadre(), menuCopia.getIdPadre());
        
        // Verify they are different objects
        assertNotSame(menuOriginal, menuCopia);
        
        // Verify modifying the copy doesn't affect the original
        menuCopia.setId(5);
        menuCopia.setNombre("Menu Modificado");
        assertNotEquals(menuOriginal.getId(), menuCopia.getId());
        assertNotEquals(menuOriginal.getNombre(), menuCopia.getNombre());
    }
    
    @Test
	void testMetodoCopia() {
		// Create a Menu object with test data
		Menu menuOriginal = new Menu();
		menuOriginal.setId(4);
		menuOriginal.setNombre("Menu Original");
		menuOriginal.setIdPadre(2);
		menuOriginal.setOrden(3);
		menuOriginal.setUsuarioCreaReg("user1");

		// Create a copy using the copia() method
		Menu menuCopia = (Menu) menuOriginal.copia();

		// Verify all attributes are equal
		assertEquals(menuOriginal.getId(), menuCopia.getId());
		assertEquals(menuOriginal.getNombre(), menuCopia.getNombre());
		assertEquals(menuOriginal.getIdPadre(), menuCopia.getIdPadre());

		// Verify they are different objects
		assertNotSame(menuOriginal, menuCopia);

		// Verify modifying the copy doesn't affect the original
		menuCopia.setId(5);
		menuCopia.setNombre("Menu Modificado");
		assertNotEquals(menuOriginal.getId(), menuCopia.getId());
		assertNotEquals(menuOriginal.getNombre(), menuCopia.getNombre());
	}

}