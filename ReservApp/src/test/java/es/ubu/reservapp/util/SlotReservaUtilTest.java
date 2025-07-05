package es.ubu.reservapp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.FranjaHoraria;
import es.ubu.reservapp.util.SlotReservaUtil.SlotTiempo;

/**
 * Test para la clase SlotReservaUtil.
 * 
 * @author Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
class SlotReservaUtilTest {

    private Establecimiento establecimiento;
    private FranjaHoraria franjaHoraria;

    @BeforeEach
    void setUp() {
        establecimiento = new Establecimiento();
        franjaHoraria = new FranjaHoraria();
        franjaHoraria.setDiaSemana(DayOfWeek.MONDAY);
        franjaHoraria.setHoraInicio(LocalTime.of(9, 0));
        franjaHoraria.setHoraFin(LocalTime.of(17, 0));
    }

    @Test
    void testConstructorPrivado() {
        // Verificar que el constructor privado lanza excepción
        assertThrows(InvocationTargetException.class, () -> {
            java.lang.reflect.Constructor<SlotReservaUtil> constructor = SlotReservaUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }

    @Test
    void testRequiereSlotsPredefinidos_ConDuracionPositiva() {
        establecimiento.setDuracionReserva(60);
        assertTrue(SlotReservaUtil.requiereSlotsPredefinidos(establecimiento));
    }

    @Test
    void testRequiereSlotsPredefinidos_ConDuracionCero() {
        establecimiento.setDuracionReserva(0);
        assertFalse(SlotReservaUtil.requiereSlotsPredefinidos(establecimiento));
    }

    @Test
    void testRequiereSlotsPredefinidos_ConDuracionNegativa() {
        establecimiento.setDuracionReserva(-30);
        assertFalse(SlotReservaUtil.requiereSlotsPredefinidos(establecimiento));
    }

    @Test
    void testRequiereSlotsPredefinidos_ConDuracionNull() {
        establecimiento.setDuracionReserva(null);
        assertFalse(SlotReservaUtil.requiereSlotsPredefinidos(establecimiento));
    }

    @Test
    void testGenerarSlotsDisponibles_ConDuracionNull() {
        establecimiento.setDuracionReserva(null);
        List<SlotTiempo> slots = SlotReservaUtil.generarSlotsDisponibles(establecimiento, franjaHoraria);
        assertTrue(slots.isEmpty());
    }

    @Test
    void testGenerarSlotsDisponibles_ConDuracionCero() {
        establecimiento.setDuracionReserva(0);
        List<SlotTiempo> slots = SlotReservaUtil.generarSlotsDisponibles(establecimiento, franjaHoraria);
        assertTrue(slots.isEmpty());
    }

    @Test
    void testGenerarSlotsDisponibles_ConDuracionNegativa() {
        establecimiento.setDuracionReserva(-60);
        List<SlotTiempo> slots = SlotReservaUtil.generarSlotsDisponibles(establecimiento, franjaHoraria);
        assertTrue(slots.isEmpty());
    }

    @Test
    void testGenerarSlotsDisponibles_SinDescanso() {
        establecimiento.setDuracionReserva(60); // 1 hora
        establecimiento.setDescansoServicios(null);
        
        List<SlotTiempo> slots = SlotReservaUtil.generarSlotsDisponibles(establecimiento, franjaHoraria);
        
        assertEquals(8, slots.size()); // 9:00-17:00 = 8 horas, slots de 1 hora
        assertEquals("09:00", slots.get(0).getHoraInicio().toString());
        assertEquals("10:00", slots.get(0).getHoraFin().toString());
        assertEquals("16:00", slots.get(7).getHoraInicio().toString());
        assertEquals("17:00", slots.get(7).getHoraFin().toString());
    }

    @Test
    void testGenerarSlotsDisponibles_ConDescanso() {
        establecimiento.setDuracionReserva(60); // 1 hora
        establecimiento.setDescansoServicios(30); // 30 minutos de descanso
        
        List<SlotTiempo> slots = SlotReservaUtil.generarSlotsDisponibles(establecimiento, franjaHoraria);
        
        assertEquals(5, slots.size()); // Con descanso de 30 min entre slots
        assertEquals("09:00", slots.get(0).getHoraInicio().toString());
        assertEquals("10:00", slots.get(0).getHoraFin().toString());
        assertEquals("10:30", slots.get(1).getHoraInicio().toString());
        assertEquals("11:30", slots.get(1).getHoraFin().toString());
    }

    @Test
    void testGenerarSlotsDisponibles_DuracionCorta() {
        establecimiento.setDuracionReserva(30); // 30 minutos
        establecimiento.setDescansoServicios(15); // 15 minutos de descanso
        
        List<SlotTiempo> slots = SlotReservaUtil.generarSlotsDisponibles(establecimiento, franjaHoraria);
        
        assertEquals(11, slots.size());
        assertEquals("09:00", slots.get(0).getHoraInicio().toString());
        assertEquals("09:30", slots.get(0).getHoraFin().toString());
        assertEquals("09:45", slots.get(1).getHoraInicio().toString());
        assertEquals("10:15", slots.get(1).getHoraFin().toString());
    }

    @Test
    void testGenerarSlotsDisponibles_DuracionMuyLarga() {
        establecimiento.setDuracionReserva(480); // 8 horas (toda la jornada)
        establecimiento.setDescansoServicios(0);
        
        List<SlotTiempo> slots = SlotReservaUtil.generarSlotsDisponibles(establecimiento, franjaHoraria);
        
        assertEquals(1, slots.size()); // Solo un slot que ocupa toda la jornada
        assertEquals(LocalTime.of(9, 0), slots.get(0).getHoraInicio());
        assertEquals(LocalTime.of(17, 0), slots.get(0).getHoraFin());
    }

    @Test
    void testGenerarSlotsDisponibles_DuracionMayorQueFranja() {
        establecimiento.setDuracionReserva(600); // 10 horas (mayor que la franja de 8 horas)
        establecimiento.setDescansoServicios(0);
        
        List<SlotTiempo> slots = SlotReservaUtil.generarSlotsDisponibles(establecimiento, franjaHoraria);
        
        assertTrue(slots.isEmpty()); // No se pueden generar slots
    }

    @Test
    void testGenerarSlotsDisponibles_FranjaCorta() {
        // Franja de solo 1 hora
        franjaHoraria.setHoraInicio(LocalTime.of(12, 0));
        franjaHoraria.setHoraFin(LocalTime.of(13, 0));
        
        establecimiento.setDuracionReserva(60); // 1 hora
        establecimiento.setDescansoServicios(0);
        
        List<SlotTiempo> slots = SlotReservaUtil.generarSlotsDisponibles(establecimiento, franjaHoraria);
        
        assertEquals(1, slots.size());
        assertEquals("12:00", slots.get(0).getHoraInicio().toString());
        assertEquals("13:00", slots.get(0).getHoraFin().toString());
    }

    @Test
    void testGenerarSlotsDisponibles_DesbordamientoHoras() {
        // Configurar una franja que termine tarde en el día
        franjaHoraria.setHoraInicio(LocalTime.of(22, 0));
        franjaHoraria.setHoraFin(LocalTime.of(23, 30));
        
        establecimiento.setDuracionReserva(60); // 1 hora
        establecimiento.setDescansoServicios(120); // 2 horas de descanso (causaría desbordamiento)
        
        List<SlotTiempo> slots = SlotReservaUtil.generarSlotsDisponibles(establecimiento, franjaHoraria);
        
        assertEquals(1, slots.size()); // Solo debería generar un slot antes del desbordamiento
        assertEquals(LocalTime.of(22, 0), slots.get(0).getHoraInicio());
        assertEquals(LocalTime.of(23, 0), slots.get(0).getHoraFin());
    }

    @Test
    void testGenerarSlotsDisponibles_SlotExactoAlFinalDeFranja() {
        establecimiento.setDuracionReserva(60); // 1 hora
        establecimiento.setDescansoServicios(0);
        
        // Configurar franja para que el último slot termine exactamente al final
        franjaHoraria.setHoraInicio(LocalTime.of(16, 0));
        franjaHoraria.setHoraFin(LocalTime.of(17, 0));
        
        List<SlotTiempo> slots = SlotReservaUtil.generarSlotsDisponibles(establecimiento, franjaHoraria);
        
        assertEquals(1, slots.size());
        assertEquals(LocalTime.of(16, 0), slots.get(0).getHoraInicio());
        assertEquals(LocalTime.of(17, 0), slots.get(0).getHoraFin());
    }
    
    @Test
    void testSlotTiempo_ConstructorPorDefecto() {
        SlotTiempo slot = new SlotTiempo();
        assertNull(slot.getHoraInicio());
        assertNull(slot.getHoraFin());
        assertNull(slot.getEtiqueta());
    }

    @Test
    void testSlotTiempo_ConstructorConParametros() {
        LocalTime inicio = LocalTime.of(10, 30);
        LocalTime fin = LocalTime.of(11, 30);
        
        SlotTiempo slot = new SlotTiempo(inicio, fin);
        
        assertEquals("10:30", slot.getHoraInicio().toString());
        assertEquals("11:30", slot.getHoraFin().toString());
        assertEquals("10:30 - 11:30", slot.getEtiqueta());
    }

    @Test
    void testSlotTiempo_SettersYGetters() {
        SlotTiempo slot = new SlotTiempo();
        slot.setHoraInicio(LocalTime.of(14, 0));
        slot.setHoraFin(LocalTime.of(15, 0));
        slot.setEtiqueta("Tarde");
        
        assertEquals(LocalTime.of(14, 0), slot.getHoraInicio());
        assertEquals(LocalTime.of(15, 0), slot.getHoraFin());
        assertEquals("Tarde", slot.getEtiqueta());
    }

    @Test
    void testSlotTiempo_ToString() {
        LocalTime inicio = LocalTime.of(9, 0);
        LocalTime fin = LocalTime.of(10, 0);
        
        SlotTiempo slot = new SlotTiempo(inicio, fin);
        
        assertEquals("09:00 - 10:00", slot.getEtiqueta());
    }

    @Test
    void testGenerarSlotsDisponibles_DescansoMayorQueDuracion() {
        establecimiento.setDuracionReserva(30); // 30 minutos
        establecimiento.setDescansoServicios(60); // 1 hora de descanso
        
        List<SlotTiempo> slots = SlotReservaUtil.generarSlotsDisponibles(establecimiento, franjaHoraria);
        
        assertEquals(6, slots.size());
        assertEquals(LocalTime.of(9, 0), slots.get(0).getHoraInicio());
        assertEquals(LocalTime.of(9, 30), slots.get(0).getHoraFin());
        assertEquals(LocalTime.of(10, 30), slots.get(1).getHoraInicio());
        assertEquals(LocalTime.of(11, 0), slots.get(1).getHoraFin());
    }

    @Test
    void testGenerarSlotsDisponibles_HorarioNocturno() {
        // Franja nocturna
        franjaHoraria.setHoraInicio(LocalTime.of(20, 0));
        franjaHoraria.setHoraFin(LocalTime.of(23, 0));
        
        establecimiento.setDuracionReserva(90); // 1.5 horas
        establecimiento.setDescansoServicios(30); // 30 minutos
        
        List<SlotTiempo> slots = SlotReservaUtil.generarSlotsDisponibles(establecimiento, franjaHoraria);
        
        assertEquals(1, slots.size());
        assertEquals(LocalTime.of(20, 0), slots.get(0).getHoraInicio());
        assertEquals(LocalTime.of(21, 30), slots.get(0).getHoraFin());
    }
}