package es.ubu.reservapp.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test para la clase Perfil
 * 
 * @author Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
class PerfilTest {

    private Perfil perfil;

    @BeforeEach
    void setUp() {
        perfil = new Perfil();
        perfil.setId(1);
        perfil.setNombre("Perfil Test");
    }

    @Test
    void testGettersAndSetters() {
        // Verificar getters
        assertEquals(1, perfil.getId());
        assertEquals("Perfil Test", perfil.getNombre());
        
        // Probar setters con nuevos valores
        perfil.setId(2);
        perfil.setNombre("Nuevo Perfil");
        
        // Verificar nuevos valores
        assertEquals(2, perfil.getId());
        assertEquals("Nuevo Perfil", perfil.getNombre());
    }

    @Test
    void testRelacionesConOtrasEntidades() {
        List<Menu> menus = new ArrayList<>();
        Menu menuHijo = new Menu();
        menuHijo.setId(2);
        menuHijo.setNombre("Perfil Hijo");
        menus.add(menuHijo);
        
        perfil.setLstMenus(menus);
        
        assertNotNull(perfil.getLstMenus());
        assertEquals(1, perfil.getLstMenus().size());
        assertEquals(2, perfil.getLstMenus().get(0).getId());
        assertEquals("Perfil Hijo", perfil.getLstMenus().get(0).getNombre());
    }

    @Test
    void testConstructorVacio() {
        Perfil perfilVacio = new Perfil();
        
        assertNotNull(perfilVacio);
    }

    @Test
    void testConstructorConParametros() {
        Integer id = 3;
        String nombre = "Test Constructor";
        List<Menu> lstMenu = new ArrayList<>();
        
        Perfil perfilCompleto = new Perfil(id, nombre, lstMenu);
        
        assertEquals(id, perfilCompleto.getId());
        assertEquals(nombre, perfilCompleto.getNombre());
        assertEquals(lstMenu, perfilCompleto.getLstMenus());
    }
    
	@Test
	void testConstructorCopia() {
		// Create test data
		List<Menu> menus = new ArrayList<>();
		Menu menu = new Menu();
		menu.setId(1);
		menu.setNombre("Menu Test");
		menus.add(menu);

		// Create a Perfil object with test data
		Perfil perfilOriginal = new Perfil();
		perfilOriginal.setId(1);
		perfilOriginal.setNombre("Perfil Original");
		perfilOriginal.setLstMenus(menus);
		perfilOriginal.setUsuarioCreaReg("user1");

		// Create a copy using the copy constructor
		Perfil perfilCopia = new Perfil(perfilOriginal);

		// Verify all attributes are equal
		assertEquals(perfilOriginal.getId(), perfilCopia.getId());
		assertEquals(perfilOriginal.getNombre(), perfilCopia.getNombre());

        // Comprobamos que las listas son copias profundas
        assertNotSame(perfilOriginal.getLstMenus(), perfilCopia.getLstMenus());
        assertEquals(perfilOriginal.getLstMenus().size(), perfilCopia.getLstMenus().size());

		// Verify they are different objects
		assertNotSame(perfilOriginal, perfilCopia);

		// Verify modifying the copy doesn't affect the original
		perfilCopia.setId(2);
		perfilCopia.setNombre("Perfil Modificado");
		perfilCopia.setLstMenus(new ArrayList<>());
		assertNotEquals(perfilOriginal.getId(), perfilCopia.getId());
		assertNotEquals(perfilOriginal.getNombre(), perfilCopia.getNombre());
		assertNotEquals(perfilOriginal.getLstMenus(), perfilCopia.getLstMenus());
	}
	
	@Test
	void testMetodoCopia() {
	    // Create test data
	    List<Menu> menus = new ArrayList<>();
	    Menu menu = new Menu();
	    menu.setId(1);
	    menu.setNombre("Menu Test");
	    menus.add(menu);

	    // Create a Perfil object with test data
	    Perfil perfilOriginal = new Perfil();
	    perfilOriginal.setId(1);
	    perfilOriginal.setNombre("Perfil Original");
	    perfilOriginal.setLstMenus(menus);
	    perfilOriginal.setUsuarioCreaReg("user1");

	    // Create a copy using the copia() method
	    Perfil perfilCopia = (Perfil)perfilOriginal.copia();

	    // Verify all attributes are equal
	    assertEquals(perfilOriginal.getId(), perfilCopia.getId());
	    assertEquals(perfilOriginal.getNombre(), perfilCopia.getNombre());

        // Comprobamos que las listas son copias profundas
        assertNotSame(perfilOriginal.getLstMenus(), perfilCopia.getLstMenus());
        assertEquals(perfilOriginal.getLstMenus().size(), perfilCopia.getLstMenus().size());

	    // Verify they are different objects
	    assertNotSame(perfilOriginal, perfilCopia);

	    // Verify modifying the copy doesn't affect the original
	    perfilCopia.setId(2);
	    perfilCopia.setNombre("Perfil Modificado");
	    perfilCopia.setLstMenus(new ArrayList<>());
	    assertNotEquals(perfilOriginal.getId(), perfilCopia.getId());
	    assertNotEquals(perfilOriginal.getNombre(), perfilCopia.getNombre());
	    assertNotEquals(perfilOriginal.getLstMenus(), perfilCopia.getLstMenus());
	}
	
	@Test
    void testConstructorDeCopiaConListasNulas() {
		// Create a Perfil object with null list
		Perfil perfilOriginal = new Perfil();
		perfilOriginal.setId(1);
		perfilOriginal.setNombre("Perfil Original");
		perfilOriginal.setLstMenus(null);

		// Create a copy using the copy constructor
		Perfil perfilCopia = new Perfil(perfilOriginal);

		// Verify attributes are copied correctly
		assertEquals(perfilOriginal.getId(), perfilCopia.getId());
		assertEquals(perfilOriginal.getNombre(), perfilCopia.getNombre());

		// Verify the list is initialized to an empty list
		assertNotNull(perfilCopia.getLstMenus());
		assertEquals(0, perfilCopia.getLstMenus().size());
	}

}