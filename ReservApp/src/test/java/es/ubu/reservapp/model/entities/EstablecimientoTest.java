package es.ubu.reservapp.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test para la clase Establecimiento
 * 
 * @author Test Generator
 * @version 1.0
 * @since 1.0
 */
class EstablecimientoTest {

    private Establecimiento establecimiento;

    @BeforeEach
    void setUp() {
        establecimiento = new Establecimiento();
        establecimiento.setId(1);
        establecimiento.setNombre("Establecimiento Test");
        establecimiento.setDescripcion("Descripción de prueba");
        establecimiento.setAforo(100);
        establecimiento.setCapacidad(50);
        establecimiento.setDuracionReserva(60);
        establecimiento.setDescansoServicios(15);
        establecimiento.setTipo("Restaurante");
        establecimiento.setDireccion("Calle Test, 123");
        establecimiento.setTelefono("123456789");
        establecimiento.setEmail("establecimiento@test.com");
        establecimiento.setActivo(true);
    }

    @Test
    void testGettersAndSetters() {
        // Verificar getters
        assertEquals(1, establecimiento.getId());
        assertEquals("Establecimiento Test", establecimiento.getNombre());
        assertEquals("Descripción de prueba", establecimiento.getDescripcion());
        assertEquals(100, establecimiento.getAforo());
        assertEquals(50, establecimiento.getCapacidad());
        assertEquals(60, establecimiento.getDuracionReserva());
        assertEquals(15, establecimiento.getDescansoServicios());
        assertEquals("Restaurante", establecimiento.getTipo());
        assertEquals("Calle Test, 123", establecimiento.getDireccion());
        assertEquals("123456789", establecimiento.getTelefono());
        assertEquals("establecimiento@test.com", establecimiento.getEmail());
        assertTrue(establecimiento.isActivo());
        
        // Probar setters con nuevos valores
        establecimiento.setNombre("Nuevo Nombre");
        establecimiento.setDescripcion("Nueva descripción");
        establecimiento.setAforo(200);
        establecimiento.setCapacidad(100);
        establecimiento.setDuracionReserva(90);
        establecimiento.setDescansoServicios(30);
        establecimiento.setTipo("Bar");
        establecimiento.setDireccion("Nueva Calle, 456");
        establecimiento.setTelefono("987654321");
        establecimiento.setEmail("nuevo@test.com");
        establecimiento.setActivo(false);
        
        // Verificar nuevos valores
        assertEquals("Nuevo Nombre", establecimiento.getNombre());
        assertEquals("Nueva descripción", establecimiento.getDescripcion());
        assertEquals(200, establecimiento.getAforo());
        assertEquals(100, establecimiento.getCapacidad());
        assertEquals(90, establecimiento.getDuracionReserva());
        assertEquals(30, establecimiento.getDescansoServicios());
        assertEquals("Bar", establecimiento.getTipo());
        assertEquals("Nueva Calle, 456", establecimiento.getDireccion());
        assertEquals("987654321", establecimiento.getTelefono());
        assertEquals("nuevo@test.com", establecimiento.getEmail());
        assertFalse(establecimiento.isActivo());
    }

    @Test
    void testRelacionesConOtrasEntidades() {
        // Arrange
        List<Reserva> reservas = new ArrayList<>();
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reservas.add(reserva);
        
        List<FranjaHoraria> franjasHorarias = new ArrayList<>();
        FranjaHoraria franjaHoraria = new FranjaHoraria();
        franjaHoraria.setId(1);
        franjaHoraria.setDiaSemana(DayOfWeek.MONDAY);
        franjaHoraria.setHoraInicio(LocalTime.of(9, 0));
        franjaHoraria.setHoraFin(LocalTime.of(18, 0));
        franjaHoraria.setEstablecimiento(establecimiento);
        franjasHorarias.add(franjaHoraria);
        
        // Act
        establecimiento.setLstReservas(reservas);
        establecimiento.setFranjasHorarias(franjasHorarias);
        
        // Assert
        assertNotNull(establecimiento.getLstReservas());
        assertEquals(1, establecimiento.getLstReservas().size());
        assertEquals(1, establecimiento.getLstReservas().get(0).getId());
        
        assertNotNull(establecimiento.getFranjasHorarias());
        assertEquals(1, establecimiento.getFranjasHorarias().size());
        assertEquals(DayOfWeek.MONDAY, establecimiento.getFranjasHorarias().get(0).getDiaSemana());
        assertEquals(LocalTime.of(9, 0), establecimiento.getFranjasHorarias().get(0).getHoraInicio());
        assertEquals(LocalTime.of(18, 0), establecimiento.getFranjasHorarias().get(0).getHoraFin());
    }

    @Test
    void testConstructorVacio() {
        // Arrange & Act
        Establecimiento establecimientoVacio = new Establecimiento();
        
        // Assert
        assertNotNull(establecimientoVacio);
        assertNotNull(establecimientoVacio.getFranjasHorarias());
        assertEquals(0, establecimientoVacio.getFranjasHorarias().size());
    }

    @Test
    void testConstructorConParametros() {
        // Arrange
        Integer id = 2;
        String nombre = "Test Constructor";
        String descripcion = "Descripción constructor";
        Integer aforo = 150;
        Integer duracionReserva = 45;
        Integer descansoServicios = 10;
        Integer capacidad = 75;
        String tipo = "Cafetería";
        String direccion = "Dirección constructor";
        String telefono = "555555555";
        String email = "constructor@test.com";
        boolean activo = true;
        List<Reserva> lstReservas = new ArrayList<>();
        List<FranjaHoraria> franjasHorarias = new ArrayList<>();
        
        // Act
        Establecimiento establecimientoCompleto = new Establecimiento(
                id, nombre, descripcion, aforo, duracionReserva, descansoServicios,
                capacidad, tipo, direccion, telefono, email, activo, lstReservas, franjasHorarias);
        
        // Assert
        assertEquals(id, establecimientoCompleto.getId());
        assertEquals(nombre, establecimientoCompleto.getNombre());
        assertEquals(descripcion, establecimientoCompleto.getDescripcion());
        assertEquals(aforo, establecimientoCompleto.getAforo());
        assertEquals(duracionReserva, establecimientoCompleto.getDuracionReserva());
        assertEquals(descansoServicios, establecimientoCompleto.getDescansoServicios());
        assertEquals(capacidad, establecimientoCompleto.getCapacidad());
        assertEquals(tipo, establecimientoCompleto.getTipo());
        assertEquals(direccion, establecimientoCompleto.getDireccion());
        assertEquals(telefono, establecimientoCompleto.getTelefono());
        assertEquals(email, establecimientoCompleto.getEmail());
        assertEquals(activo, establecimientoCompleto.isActivo());
        assertEquals(lstReservas, establecimientoCompleto.getLstReservas());
        assertEquals(franjasHorarias, establecimientoCompleto.getFranjasHorarias());
    }
}