package es.ubu.reservapp.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.DayOfWeek;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Test para la clase FechaUtil
 */
@ExtendWith(MockitoExtension.class)
class FechaUtilTest {

    @Test
    void testConstructorPrivado() {
        // Verificar que el constructor es privado y no se puede instanciar
        assertThrows(InvocationTargetException.class, () -> {
            Constructor<FechaUtil> constructor = FechaUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }

    @Test
    void testFormatearDiaSemana_DiaNull() {
        String resultado = FechaUtil.formatearDiaSemana(null);
        assertEquals("", resultado);
    }

    @Test
    void testFormatearDiaSemana_Lunes() {
        String resultado = FechaUtil.formatearDiaSemana(DayOfWeek.MONDAY);
        assertEquals("Lunes", resultado);
    }

    @Test
    void testFormatearDiaSemana_Martes() {
        String resultado = FechaUtil.formatearDiaSemana(DayOfWeek.TUESDAY);
        assertEquals("Martes", resultado);
    }

    @Test
    void testFormatearDiaSemana_Miercoles() {
        String resultado = FechaUtil.formatearDiaSemana(DayOfWeek.WEDNESDAY);
        assertEquals("Miércoles", resultado);
    }

    @Test
    void testFormatearDiaSemana_Jueves() {
        String resultado = FechaUtil.formatearDiaSemana(DayOfWeek.THURSDAY);
        assertEquals("Jueves", resultado);
    }

    @Test
    void testFormatearDiaSemana_Viernes() {
        String resultado = FechaUtil.formatearDiaSemana(DayOfWeek.FRIDAY);
        assertEquals("Viernes", resultado);
    }

    @Test
    void testFormatearDiaSemana_Sabado() {
        String resultado = FechaUtil.formatearDiaSemana(DayOfWeek.SATURDAY);
        assertEquals("Sábado", resultado);
    }

    @Test
    void testFormatearDiaSemana_Domingo() {
        String resultado = FechaUtil.formatearDiaSemana(DayOfWeek.SUNDAY);
        assertEquals("Domingo", resultado);
    }

    @Test
    void testFormatearDiaSemana_StringVacio() {
        try (MockedStatic<DayOfWeek> mockedDayOfWeek = Mockito.mockStatic(DayOfWeek.class, Mockito.CALLS_REAL_METHODS)) {
            DayOfWeek mockDay = Mockito.mock(DayOfWeek.class);
            Mockito.when(mockDay.getDisplayName(any(TextStyle.class), any(Locale.class))).thenReturn("");
            
            String resultado = FechaUtil.formatearDiaSemana(mockDay);
            assertEquals("", resultado);
        }
    }

    @Test
    void testFormatearDiaSemana_StringNull() {
        try (MockedStatic<DayOfWeek> mockedDayOfWeek = Mockito.mockStatic(DayOfWeek.class, Mockito.CALLS_REAL_METHODS)) {
            DayOfWeek mockDay = Mockito.mock(DayOfWeek.class);
            Mockito.when(mockDay.getDisplayName(any(TextStyle.class), any(Locale.class))).thenReturn(null);
            
            String resultado = FechaUtil.formatearDiaSemana(mockDay);
            assertEquals("", resultado);
        }
    }

    @Test
    void testFormatearMes_Enero() {
        String resultado = FechaUtil.formatearMes(1);
        assertEquals("Enero", resultado);
    }

    @Test
    void testFormatearMes_Febrero() {
        String resultado = FechaUtil.formatearMes(2);
        assertEquals("Febrero", resultado);
    }

    @Test
    void testFormatearMes_Marzo() {
        String resultado = FechaUtil.formatearMes(3);
        assertEquals("Marzo", resultado);
    }

    @Test
    void testFormatearMes_Abril() {
        String resultado = FechaUtil.formatearMes(4);
        assertEquals("Abril", resultado);
    }

    @Test
    void testFormatearMes_Mayo() {
        String resultado = FechaUtil.formatearMes(5);
        assertEquals("Mayo", resultado);
    }

    @Test
    void testFormatearMes_Junio() {
        String resultado = FechaUtil.formatearMes(6);
        assertEquals("Junio", resultado);
    }

    @Test
    void testFormatearMes_Julio() {
        String resultado = FechaUtil.formatearMes(7);
        assertEquals("Julio", resultado);
    }

    @Test
    void testFormatearMes_Agosto() {
        String resultado = FechaUtil.formatearMes(8);
        assertEquals("Agosto", resultado);
    }

    @Test
    void testFormatearMes_Septiembre() {
        String resultado = FechaUtil.formatearMes(9);
        assertEquals("Septiembre", resultado);
    }

    @Test
    void testFormatearMes_Octubre() {
        String resultado = FechaUtil.formatearMes(10);
        assertEquals("Octubre", resultado);
    }

    @Test
    void testFormatearMes_Noviembre() {
        String resultado = FechaUtil.formatearMes(11);
        assertEquals("Noviembre", resultado);
    }

    @Test
    void testFormatearMes_Diciembre() {
        String resultado = FechaUtil.formatearMes(12);
        assertEquals("Diciembre", resultado);
    }

    @Test
    void testFormatearMes_MenorQueUno() {
        String resultado = FechaUtil.formatearMes(0);
        assertEquals("", resultado);
    }

    @Test
    void testFormatearMes_MayorQueDoce() {
        String resultado = FechaUtil.formatearMes(13);
        assertEquals("", resultado);
    }

    @Test
    void testFormatearMes_NumeroNegativo() {
        String resultado = FechaUtil.formatearMes(-1);
        assertEquals("", resultado);
    }

    @Test
    void testFormatearMes_StringVacio() {
        try (MockedStatic<Month> mockedMonth = Mockito.mockStatic(Month.class, Mockito.CALLS_REAL_METHODS)) {
            Month mockMonth = Mockito.mock(Month.class);
            mockedMonth.when(() -> Month.of(eq(1))).thenReturn(mockMonth);
            Mockito.when(mockMonth.getDisplayName(any(TextStyle.class), any(Locale.class))).thenReturn("");
            
            String resultado = FechaUtil.formatearMes(1);
            assertEquals("", resultado);
        }
    }

    @Test
    void testFormatearMes_StringNull() {
        try (MockedStatic<Month> mockedMonth = Mockito.mockStatic(Month.class, Mockito.CALLS_REAL_METHODS)) {
            Month mockMonth = Mockito.mock(Month.class);
            mockedMonth.when(() -> Month.of(eq(1))).thenReturn(mockMonth);
            Mockito.when(mockMonth.getDisplayName(any(TextStyle.class), any(Locale.class))).thenReturn(null);
            
            String resultado = FechaUtil.formatearMes(1);
            assertEquals("", resultado);
        }
    }
}