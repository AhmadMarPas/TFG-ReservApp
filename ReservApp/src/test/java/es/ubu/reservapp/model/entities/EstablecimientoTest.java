package es.ubu.reservapp.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test para la clase Establecimiento
 * 
 * @author Ahmad Mareie Pascual
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
        
        establecimiento.setLstReservas(reservas);
        establecimiento.setFranjasHorarias(franjasHorarias);
        
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
        Establecimiento establecimientoVacio = new Establecimiento();
        
        assertNotNull(establecimientoVacio);
        assertNotNull(establecimientoVacio.getFranjasHorarias());
        assertEquals(0, establecimientoVacio.getFranjasHorarias().size());
    }

    @Test
    void testConstructorConParametros() {
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
        
        Establecimiento establecimientoCompleto = new Establecimiento(
                id, nombre, descripcion, aforo, duracionReserva, descansoServicios,
                capacidad, tipo, direccion, telefono, email, activo, lstReservas, franjasHorarias);
        
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
    
    @Test
    void testConstructorDeCopia() {
        // Crear establecimiento original con datos completos
        Establecimiento original = new Establecimiento();
        original.setId(10);
        original.setNombre("Establecimiento Original");
        original.setDescripcion("Descripción original");
        original.setAforo(200);
        original.setDuracionReserva(90);
        original.setDescansoServicios(20);
        original.setCapacidad(100);
        original.setTipo("Hotel");
        original.setDireccion("Calle Original, 789");
        original.setTelefono("111222333");
        original.setEmail("original@test.com");
        original.setActivo(false);
        
        // Crear listas para reservas y franjas horarias
        List<Reserva> reservasOriginales = new ArrayList<>();
        Reserva reserva1 = new Reserva();
        reserva1.setId(1);
        reservasOriginales.add(reserva1);
        
        List<FranjaHoraria> franjasOriginales = new ArrayList<>();
        FranjaHoraria franja1 = new FranjaHoraria();
        franja1.setId(1);
        franja1.setDiaSemana(DayOfWeek.TUESDAY);
        franja1.setHoraInicio(LocalTime.of(10, 0));
        franja1.setHoraFin(LocalTime.of(20, 0));
        franjasOriginales.add(franja1);
        
        original.setLstReservas(reservasOriginales);
        original.setFranjasHorarias(franjasOriginales);
        
        // Crear copia usando constructor de copia
        Establecimiento copia = new Establecimiento(original);
        
        // Verificar que todos los campos se copiaron correctamente
        assertEquals(original.getId(), copia.getId());
        assertEquals(original.getNombre(), copia.getNombre());
        assertEquals(original.getDescripcion(), copia.getDescripcion());
        assertEquals(original.getAforo(), copia.getAforo());
        assertEquals(original.getDuracionReserva(), copia.getDuracionReserva());
        assertEquals(original.getDescansoServicios(), copia.getDescansoServicios());
        assertEquals(original.getCapacidad(), copia.getCapacidad());
        assertEquals(original.getTipo(), copia.getTipo());
        assertEquals(original.getDireccion(), copia.getDireccion());
        assertEquals(original.getTelefono(), copia.getTelefono());
        assertEquals(original.getEmail(), copia.getEmail());
        assertEquals(original.isActivo(), copia.isActivo());
        
        // Verificar que las listas se copiaron pero son instancias diferentes
        assertNotSame(original.getLstReservas(), copia.getLstReservas());
        assertNotSame(original.getFranjasHorarias(), copia.getFranjasHorarias());
        assertEquals(original.getLstReservas().size(), copia.getLstReservas().size());
        assertEquals(original.getFranjasHorarias().size(), copia.getFranjasHorarias().size());
    }

    @Test
    void testConstructorDeCopiaConListasNulas() {
        // Crear establecimiento original con listas nulas
        Establecimiento original = new Establecimiento();
        original.setId(5);
        original.setNombre("Test Nulo");
        original.setDescripcion("Descripción test");
        original.setAforo(50);
        original.setCapacidad(25);
        original.setActivo(true);
        original.setLstReservas(null);
        original.setFranjasHorarias(null);
        
        // Crear copia
        Establecimiento copia = new Establecimiento(original);
        
        // Verificar campos básicos
        assertEquals(original.getId(), copia.getId());
        assertEquals(original.getNombre(), copia.getNombre());
        assertEquals(original.getDescripcion(), copia.getDescripcion());
        assertEquals(original.getAforo(), copia.getAforo());
        assertEquals(original.getCapacidad(), copia.getCapacidad());
        assertEquals(original.isActivo(), copia.isActivo());
        
        // Verificar que las listas nulas se manejan correctamente
        assertNotNull(copia.getLstReservas());
        assertNotNull(copia.getFranjasHorarias());
        assertEquals(0, copia.getLstReservas().size());
        assertEquals(0, copia.getFranjasHorarias().size());
    }

    @Test
    void testMetodoCopia() {
        // Configurar establecimiento con todos los datos
        establecimiento.setLstReservas(new ArrayList<>());
        establecimiento.setFranjasHorarias(new ArrayList<>());
        
        // Llamar al método copia()
        Establecimiento copiaMetodo = (Establecimiento) establecimiento.copia();
        
        // Verificar que es una instancia diferente
        assertNotSame(establecimiento, copiaMetodo);
        
        // Verificar que todos los campos se copiaron
        assertEquals(establecimiento.getId(), copiaMetodo.getId());
        assertEquals(establecimiento.getNombre(), copiaMetodo.getNombre());
        assertEquals(establecimiento.getDescripcion(), copiaMetodo.getDescripcion());
        assertEquals(establecimiento.getAforo(), copiaMetodo.getAforo());
        assertEquals(establecimiento.getDuracionReserva(), copiaMetodo.getDuracionReserva());
        assertEquals(establecimiento.getDescansoServicios(), copiaMetodo.getDescansoServicios());
        assertEquals(establecimiento.getCapacidad(), copiaMetodo.getCapacidad());
        assertEquals(establecimiento.getTipo(), copiaMetodo.getTipo());
        assertEquals(establecimiento.getDireccion(), copiaMetodo.getDireccion());
        assertEquals(establecimiento.getTelefono(), copiaMetodo.getTelefono());
        assertEquals(establecimiento.getEmail(), copiaMetodo.getEmail());
        assertEquals(establecimiento.isActivo(), copiaMetodo.isActivo());
    }

    @Test
    void testCasosEdgeConValoresNulos() {
        Establecimiento establecimientoNulos = new Establecimiento();
        
        // Probar setters con valores nulos (campos opcionales)
        establecimientoNulos.setDuracionReserva(null);
        establecimientoNulos.setDescansoServicios(null);
        establecimientoNulos.setTipo(null);
        establecimientoNulos.setDireccion(null);
        establecimientoNulos.setTelefono(null);
        establecimientoNulos.setEmail(null);
        
        // Verificar que los valores nulos se asignan correctamente
        assertNull(establecimientoNulos.getDuracionReserva());
        assertNull(establecimientoNulos.getDescansoServicios());
        assertNull(establecimientoNulos.getTipo());
        assertNull(establecimientoNulos.getDireccion());
        assertNull(establecimientoNulos.getTelefono());
        assertNull(establecimientoNulos.getEmail());
    }

    @Test
    void testCasosEdgeConValoresLimite() {
        Establecimiento establecimientoLimites = new Establecimiento();
        
        // Probar con valores en los límites de las validaciones
        establecimientoLimites.setNombre("A"); // Mínimo 1 carácter
        establecimientoLimites.setDescripcion("B"); // Mínimo 1 carácter
        establecimientoLimites.setAforo(0); // Valor mínimo
        establecimientoLimites.setCapacidad(0); // Valor mínimo
        establecimientoLimites.setDuracionReserva(1); // Valor mínimo
        establecimientoLimites.setDescansoServicios(0); // Valor mínimo
        
        // Verificar asignaciones
        assertEquals("A", establecimientoLimites.getNombre());
        assertEquals("B", establecimientoLimites.getDescripcion());
        assertEquals(0, establecimientoLimites.getAforo());
        assertEquals(0, establecimientoLimites.getCapacidad());
        assertEquals(1, establecimientoLimites.getDuracionReserva());
        assertEquals(0, establecimientoLimites.getDescansoServicios());
    }

    @Test
    void testCasosEdgeConValoresMaximos() {
        Establecimiento establecimientoMaximos = new Establecimiento();
        
        // Probar con valores máximos según las validaciones @Size
        String nombreMaximo = "A".repeat(40); // Máximo 40 caracteres
        String descripcionMaxima = "B".repeat(250); // Máximo 250 caracteres
        String tipoMaximo = "C".repeat(80); // Máximo 80 caracteres
        String direccionMaxima = "D".repeat(250); // Máximo 250 caracteres
        String telefonoMaximo = "1".repeat(20); // Máximo 20 caracteres
        String emailMaximo = "e".repeat(95) + "@t.co"; // Máximo 100 caracteres
        
        establecimientoMaximos.setNombre(nombreMaximo);
        establecimientoMaximos.setDescripcion(descripcionMaxima);
        establecimientoMaximos.setTipo(tipoMaximo);
        establecimientoMaximos.setDireccion(direccionMaxima);
        establecimientoMaximos.setTelefono(telefonoMaximo);
        establecimientoMaximos.setEmail(emailMaximo);
        
        // Verificar asignaciones
        assertEquals(nombreMaximo, establecimientoMaximos.getNombre());
        assertEquals(descripcionMaxima, establecimientoMaximos.getDescripcion());
        assertEquals(tipoMaximo, establecimientoMaximos.getTipo());
        assertEquals(direccionMaxima, establecimientoMaximos.getDireccion());
        assertEquals(telefonoMaximo, establecimientoMaximos.getTelefono());
        assertEquals(emailMaximo, establecimientoMaximos.getEmail());
    }

    @Test
    void testValorPorDefectoActivo() {
        Establecimiento establecimientoDefault = new Establecimiento();
        
        // Verificar que el valor por defecto de 'activo' es true
        assertTrue(establecimientoDefault.isActivo());
    }

    @Test
    void testInicializacionListaFranjasHorarias() {
        Establecimiento establecimientoNuevo = new Establecimiento();
        
        // Verificar que la lista de franjas horarias se inicializa automáticamente
        assertNotNull(establecimientoNuevo.getFranjasHorarias());
        assertEquals(0, establecimientoNuevo.getFranjasHorarias().size());
        assertTrue(establecimientoNuevo.getFranjasHorarias() instanceof ArrayList);
    }

    @Test
    void testModificacionListasIndependientes() {
        // Crear establecimiento original
        List<Reserva> reservasOriginales = new ArrayList<>();
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reservasOriginales.add(reserva);
        
        List<FranjaHoraria> franjasOriginales = new ArrayList<>();
        FranjaHoraria franja = new FranjaHoraria();
        franja.setId(1);
        franjasOriginales.add(franja);
        
        Establecimiento original = new Establecimiento();
        original.setLstReservas(reservasOriginales);
        original.setFranjasHorarias(franjasOriginales);
        
        // Crear copia
        Establecimiento copia = new Establecimiento(original);
        
        // Modificar listas en la copia
        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setId(2);
        copia.getLstReservas().add(nuevaReserva);
        
        FranjaHoraria nuevaFranja = new FranjaHoraria();
        nuevaFranja.setId(2);
        copia.getFranjasHorarias().add(nuevaFranja);
        
        // Verificar que las listas originales no se modificaron
        assertEquals(1, original.getLstReservas().size());
        assertEquals(1, original.getFranjasHorarias().size());
        assertEquals(2, copia.getLstReservas().size());
        assertEquals(2, copia.getFranjasHorarias().size());
    }

    @Test
    void testHerenciaEntidadInfo() {
        // Verificar que Establecimiento extiende EntidadInfo
        assertTrue(establecimiento instanceof EntidadInfo);
        
        // Verificar métodos heredados de EntidadInfo
        establecimiento.setId(999);
        assertEquals(999, establecimiento.getId());
        
        // Verificar que el método copia() retorna una instancia de EntidadPK
        assertTrue(establecimiento.copia() instanceof EntidadPK);
    }
}