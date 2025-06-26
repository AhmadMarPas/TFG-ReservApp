package es.ubu.reservapp.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        List<Perfil> perfiles = new ArrayList<>();
        Perfil perfilHijo = new Perfil();
        perfilHijo.setId(2);
        perfilHijo.setNombre("Perfil Hijo");
        perfiles.add(perfilHijo);
        
        perfil.setLstPerfil(perfiles);
        
        assertNotNull(perfil.getLstPerfil());
        assertEquals(1, perfil.getLstPerfil().size());
        assertEquals(2, perfil.getLstPerfil().get(0).getId());
        assertEquals("Perfil Hijo", perfil.getLstPerfil().get(0).getNombre());
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
        List<Perfil> lstPerfil = new ArrayList<>();
        
        Perfil perfilCompleto = new Perfil(id, nombre, lstPerfil);
        
        assertEquals(id, perfilCompleto.getId());
        assertEquals(nombre, perfilCompleto.getNombre());
        assertEquals(lstPerfil, perfilCompleto.getLstPerfil());
    }
}