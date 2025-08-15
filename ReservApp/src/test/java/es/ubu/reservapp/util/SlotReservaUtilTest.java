package es.ubu.reservapp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.FranjaHoraria;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.util.SlotReservaUtil.DisponibilidadInfo;
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

    // ================================
    // TESTS ADICIONALES PARA SLOTTIEMPO
    // ================================

    @Test
    void testSlotTiempo_ConstructorCompleto() {
        LocalTime inicio = LocalTime.of(10, 0);
        LocalTime fin = LocalTime.of(11, 0);
        
        SlotTiempo slot = new SlotTiempo(inicio, fin, false, 3, 5);
        
        assertEquals(inicio, slot.getHoraInicio());
        assertEquals(fin, slot.getHoraFin());
        assertEquals("10:00 - 11:00", slot.getEtiqueta());
        assertFalse(slot.isDisponible());
        assertEquals(3, slot.getReservasExistentes());
        assertEquals(5, slot.getAforoMaximo());
    }

    @Test
    void testSlotTiempo_SettersCompletos() {
        SlotTiempo slot = new SlotTiempo();
        
        slot.setDisponible(false);
        slot.setReservasExistentes(2);
        slot.setAforoMaximo(10);
        
        assertFalse(slot.isDisponible());
        assertEquals(2, slot.getReservasExistentes());
        assertEquals(10, slot.getAforoMaximo());
    }

    @Test
    void testSlotTiempo_GetInfoDisponibilidad_SinLimite() {
        SlotTiempo slot = new SlotTiempo();
        slot.setAforoMaximo(0);
        
        assertEquals("Sin límite", slot.getInfoDisponibilidad());
    }

    @Test
    void testSlotTiempo_GetInfoDisponibilidad_ConLimite() {
        SlotTiempo slot = new SlotTiempo();
        slot.setAforoMaximo(5);
        slot.setReservasExistentes(2);
        
        assertEquals("3/5 disponibles", slot.getInfoDisponibilidad());
    }

    @Test
    void testSlotTiempo_GetInfoDisponibilidad_AforoNegativo() {
        SlotTiempo slot = new SlotTiempo();
        slot.setAforoMaximo(-1);
        
        assertEquals("Sin límite", slot.getInfoDisponibilidad());
    }

    // ================================
    // TESTS PARA seSolapanHorarios
    // ================================

    @Test
    void testSeSolapanHorarios_SinSolapamiento() {
        LocalTime inicio1 = LocalTime.of(9, 0);
        LocalTime fin1 = LocalTime.of(10, 0);
        LocalTime inicio2 = LocalTime.of(11, 0);
        LocalTime fin2 = LocalTime.of(12, 0);
        
        assertFalse(SlotReservaUtil.seSolapanHorarios(inicio1, fin1, inicio2, fin2));
    }

    @Test
    void testSeSolapanHorarios_ConSolapamiento() {
        LocalTime inicio1 = LocalTime.of(9, 0);
        LocalTime fin1 = LocalTime.of(11, 0);
        LocalTime inicio2 = LocalTime.of(10, 0);
        LocalTime fin2 = LocalTime.of(12, 0);
        
        assertTrue(SlotReservaUtil.seSolapanHorarios(inicio1, fin1, inicio2, fin2));
    }

    @Test
    void testSeSolapanHorarios_HorariosConsecutivos() {
        LocalTime inicio1 = LocalTime.of(9, 0);
        LocalTime fin1 = LocalTime.of(10, 0);
        LocalTime inicio2 = LocalTime.of(10, 0);
        LocalTime fin2 = LocalTime.of(11, 0);
        
        assertFalse(SlotReservaUtil.seSolapanHorarios(inicio1, fin1, inicio2, fin2));
    }

    @Test
    void testSeSolapanHorarios_HorarioContenido() {
        LocalTime inicio1 = LocalTime.of(9, 0);
        LocalTime fin1 = LocalTime.of(12, 0);
        LocalTime inicio2 = LocalTime.of(10, 0);
        LocalTime fin2 = LocalTime.of(11, 0);
        
        assertTrue(SlotReservaUtil.seSolapanHorarios(inicio1, fin1, inicio2, fin2));
    }

    @Test
    void testSeSolapanHorarios_HorarioIgual() {
        LocalTime inicio = LocalTime.of(10, 0);
        LocalTime fin = LocalTime.of(11, 0);
        
        assertTrue(SlotReservaUtil.seSolapanHorarios(inicio, fin, inicio, fin));
    }

    // ================================
    // TESTS PARA generarSlotsConDisponibilidad
    // ================================

    @Test
    void testGenerarSlotsConDisponibilidad_SinAforo() {
        establecimiento.setDuracionReserva(60);
        establecimiento.setDescansoServicios(0);
        establecimiento.setAforo(null);
        
        List<Reserva> reservas = new ArrayList<>();
        
        List<SlotTiempo> slots = SlotReservaUtil.generarSlotsConDisponibilidad(establecimiento, franjaHoraria, reservas);
        
        assertEquals(8, slots.size());
        assertTrue(slots.stream().allMatch(SlotTiempo::isDisponible));
    }

    @Test
    void testGenerarSlotsConDisponibilidad_ConAforo() {
        establecimiento.setDuracionReserva(60);
        establecimiento.setDescansoServicios(0);
        establecimiento.setAforo(2);
        
        // Crear reservas que ocupen el primer slot (9:00-10:00)
        List<Reserva> reservas = new ArrayList<>();
        Reserva reserva1 = new Reserva();
        reserva1.setFechaReserva(LocalDateTime.of(2024, 1, 1, 9, 0)); // Hora de inicio 9:00
        reserva1.setHoraFin(LocalTime.of(10, 0)); // Hora de fin 10:00
        reservas.add(reserva1);
        
        Reserva reserva2 = new Reserva();
        reserva2.setFechaReserva(LocalDateTime.of(2024, 1, 1, 9, 30)); // Hora de inicio 9:30
        reserva2.setHoraFin(LocalTime.of(10, 30)); // Hora de fin 10:30
        reservas.add(reserva2);
        
        List<SlotTiempo> slots = SlotReservaUtil.generarSlotsConDisponibilidad(establecimiento, franjaHoraria, reservas);
        
        assertEquals(8, slots.size());
        assertFalse(slots.get(0).isDisponible()); // Primer slot ocupado
        assertEquals(2, slots.get(0).getReservasExistentes());
        assertEquals(2, slots.get(0).getAforoMaximo());
        assertTrue(slots.get(1).isDisponible()); // Segundo slot disponible
    }

    @Test
    void testGenerarSlotsConDisponibilidad_AforoCero() {
        establecimiento.setDuracionReserva(60);
        establecimiento.setAforo(0);
        
        List<Reserva> reservas = new ArrayList<>();
        
        List<SlotTiempo> slots = SlotReservaUtil.generarSlotsConDisponibilidad(establecimiento, franjaHoraria, reservas);
        
        assertEquals(8, slots.size());
        assertTrue(slots.stream().allMatch(SlotTiempo::isDisponible));
    }

    // ================================
    // TESTS PARA filtrarSlotsPorAforo
    // ================================

    @Test
    void testFiltrarSlotsPorAforo_SinAforo() {
        List<SlotTiempo> slotsBase = List.of(
            new SlotTiempo(LocalTime.of(9, 0), LocalTime.of(10, 0)),
            new SlotTiempo(LocalTime.of(10, 0), LocalTime.of(11, 0))
        );
        
        establecimiento.setAforo(null);
        List<Reserva> reservas = new ArrayList<>();
        
        List<SlotTiempo> resultado = SlotReservaUtil.filtrarSlotsPorAforo(slotsBase, establecimiento, reservas);
        
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(SlotTiempo::isDisponible));
    }

    @Test
    void testFiltrarSlotsPorAforo_ConAforo() {
        List<SlotTiempo> slotsBase = List.of(
            new SlotTiempo(LocalTime.of(9, 0), LocalTime.of(10, 0)),
            new SlotTiempo(LocalTime.of(10, 0), LocalTime.of(11, 0))
        );
        
        establecimiento.setAforo(1);
        
        // Crear una reserva que ocupe el primer slot (9:00-10:00)
        List<Reserva> reservas = new ArrayList<>();
        Reserva reserva = new Reserva();
        reserva.setFechaReserva(LocalDateTime.of(2024, 1, 1, 9, 0)); // Hora de inicio 9:00
        reserva.setHoraFin(LocalTime.of(10, 0)); // Hora de fin 10:00
        reservas.add(reserva);
        
        List<SlotTiempo> resultado = SlotReservaUtil.filtrarSlotsPorAforo(slotsBase, establecimiento, reservas);
        
        assertEquals(2, resultado.size());
        assertFalse(resultado.get(0).isDisponible()); // Primer slot ocupado
        assertTrue(resultado.get(1).isDisponible()); // Segundo slot disponible
    }

    @Test
    void testFiltrarSlotsPorAforo_AforoCero() {
        List<SlotTiempo> slotsBase = List.of(
            new SlotTiempo(LocalTime.of(9, 0), LocalTime.of(10, 0))
        );
        
        establecimiento.setAforo(0);
        List<Reserva> reservas = new ArrayList<>();
        
        List<SlotTiempo> resultado = SlotReservaUtil.filtrarSlotsPorAforo(slotsBase, establecimiento, reservas);
        
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).isDisponible());
    }

    // ================================
    // TESTS PARA obtenerInfoDisponibilidad
    // ================================

    @Test
    void testObtenerInfoDisponibilidad_SinAforo() {
        establecimiento.setAforo(null);
        List<Reserva> reservas = new ArrayList<>();
        
        DisponibilidadInfo info = SlotReservaUtil.obtenerInfoDisponibilidad(
            establecimiento, LocalTime.of(10, 0), LocalTime.of(11, 0), reservas);
        
        assertTrue(info.isDisponible());
        assertEquals(0, info.getReservasExistentes());
        assertEquals(0, info.getAforoMaximo());
        assertEquals("Sin límite de aforo", info.getMensaje());
    }

    @Test
    void testObtenerInfoDisponibilidad_ConAforoDisponible() {
        establecimiento.setAforo(3);
        
        // Crear una reserva que se solape
        List<Reserva> reservas = new ArrayList<>();
        Reserva reserva = new Reserva();
        reserva.setFechaReserva(LocalDateTime.of(2024, 1, 1, 10, 30));
        reserva.setHoraFin(LocalTime.of(11, 30));
        reservas.add(reserva);
        
        DisponibilidadInfo info = SlotReservaUtil.obtenerInfoDisponibilidad(
            establecimiento, LocalTime.of(10, 0), LocalTime.of(11, 0), reservas);
        
        assertTrue(info.isDisponible());
        assertEquals(1, info.getReservasExistentes());
        assertEquals(3, info.getAforoMaximo());
        assertEquals("Disponible (1/3 ocupado)", info.getMensaje());
    }

    @Test
    void testObtenerInfoDisponibilidad_SinDisponibilidad() {
        establecimiento.setAforo(2);
        
        // Crear reservas que llenen el aforo
        List<Reserva> reservas = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Reserva reserva = new Reserva();
            reserva.setFechaReserva(LocalDateTime.of(2024, 1, 1, 10, 30));
            reserva.setHoraFin(LocalTime.of(11, 30));
            reservas.add(reserva);
        }
        
        DisponibilidadInfo info = SlotReservaUtil.obtenerInfoDisponibilidad(
            establecimiento, LocalTime.of(10, 0), LocalTime.of(11, 0), reservas);
        
        assertFalse(info.isDisponible());
        assertEquals(2, info.getReservasExistentes());
        assertEquals(2, info.getAforoMaximo());
        assertEquals("No disponible (2/2 ocupado)", info.getMensaje());
    }

    @Test
    void testObtenerInfoDisponibilidad_AforoCero() {
        establecimiento.setAforo(0);
        List<Reserva> reservas = new ArrayList<>();
        
        DisponibilidadInfo info = SlotReservaUtil.obtenerInfoDisponibilidad(
            establecimiento, LocalTime.of(10, 0), LocalTime.of(11, 0), reservas);
        
        assertTrue(info.isDisponible());
        assertEquals("Sin límite de aforo", info.getMensaje());
    }

    // ================================
    // TESTS PARA DisponibilidadInfo
    // ================================

    @Test
    void testDisponibilidadInfo_Constructor() {
        DisponibilidadInfo info = new DisponibilidadInfo(true, 2, 5, "Test mensaje");
        
        assertTrue(info.isDisponible());
        assertEquals(2, info.getReservasExistentes());
        assertEquals(5, info.getAforoMaximo());
        assertEquals("Test mensaje", info.getMensaje());
    }

    @Test
    void testDisponibilidadInfo_ConstructorNoDisponible() {
        DisponibilidadInfo info = new DisponibilidadInfo(false, 5, 5, "Completo");
        
        assertFalse(info.isDisponible());
        assertEquals(5, info.getReservasExistentes());
        assertEquals(5, info.getAforoMaximo());
        assertEquals("Completo", info.getMensaje());
    }

    @Test
    void testContarReservasEnSlot_ReservaSinHoraFin() {
        // Test para cubrir la rama donde reserva.getHoraFin() es null
        
        establecimiento.setDuracionReserva(60);
        establecimiento.setDescansoServicios(0);
        establecimiento.setAforo(1);
        
        List<Reserva> reservas = new ArrayList<>();
        
        // Crear reserva SIN hora de fin (null)
        Reserva reserva = new Reserva();
        reserva.setFechaReserva(LocalDateTime.of(2024, 1, 1, 10, 30));
        reserva.setHoraFin(null); // Explícitamente null
        reservas.add(reserva);
        
        List<SlotTiempo> slots = SlotReservaUtil.generarSlotsConDisponibilidad(establecimiento, franjaHoraria, reservas);
        
        assertEquals(8, slots.size());
        
        // El segundo slot (10:00-11:00) debería tener 1 reserva existente 
        // (usando hora por defecto + 1 hora desde 10:30)
        assertEquals(1, slots.get(1).getReservasExistentes());
        assertFalse(slots.get(1).isDisponible());
        
        // Los otros slots no deberían tener reservas
        assertEquals(0, slots.get(0).getReservasExistentes());
        assertTrue(slots.get(0).isDisponible());
    }
}