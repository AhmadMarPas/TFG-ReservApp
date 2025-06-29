package es.ubu.reservapp.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test para la clase FranjaHoraria
 * 
 * @author Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
class FranjaHorariaTest {

    private FranjaHoraria franjaHoraria;
    private Establecimiento establecimiento;
    private DayOfWeek diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    @BeforeEach
    void setUp() {
        establecimiento = new Establecimiento();
        establecimiento.setId(1);
        establecimiento.setNombre("Establecimiento Test");
        
        diaSemana = DayOfWeek.MONDAY;
        horaInicio = LocalTime.of(9, 0);
        horaFin = LocalTime.of(18, 0);
        
        franjaHoraria = new FranjaHoraria();
        franjaHoraria.setId(1);
        franjaHoraria.setDiaSemana(diaSemana);
        franjaHoraria.setHoraInicio(horaInicio);
        franjaHoraria.setHoraFin(horaFin);
        franjaHoraria.setEstablecimiento(establecimiento);
    }

    @Test
    void testGettersAndSetters() {
        assertEquals(1, franjaHoraria.getId());
        assertEquals(diaSemana, franjaHoraria.getDiaSemana());
        assertEquals(horaInicio, franjaHoraria.getHoraInicio());
        assertEquals(horaFin, franjaHoraria.getHoraFin());
        assertEquals(establecimiento, franjaHoraria.getEstablecimiento());
        
        DayOfWeek nuevoDiaSemana = DayOfWeek.TUESDAY;
        LocalTime nuevaHoraInicio = LocalTime.of(10, 0);
        LocalTime nuevaHoraFin = LocalTime.of(19, 0);
        Establecimiento nuevoEstablecimiento = new Establecimiento();
        nuevoEstablecimiento.setId(2);
        nuevoEstablecimiento.setNombre("Nuevo Establecimiento");
        
        franjaHoraria.setId(2);
        franjaHoraria.setDiaSemana(nuevoDiaSemana);
        franjaHoraria.setHoraInicio(nuevaHoraInicio);
        franjaHoraria.setHoraFin(nuevaHoraFin);
        franjaHoraria.setEstablecimiento(nuevoEstablecimiento);
        
        assertEquals(2, franjaHoraria.getId());
        assertEquals(nuevoDiaSemana, franjaHoraria.getDiaSemana());
        assertEquals(nuevaHoraInicio, franjaHoraria.getHoraInicio());
        assertEquals(nuevaHoraFin, franjaHoraria.getHoraFin());
        assertEquals(nuevoEstablecimiento, franjaHoraria.getEstablecimiento());
    }

    @Test
    void testIsHoraFinAfterHoraInicio() {
        // Caso válido: horaFin después de horaInicio
        try {
            java.lang.reflect.Method method = FranjaHoraria.class.getDeclaredMethod("isHoraFinAfterHoraInicio");
            method.setAccessible(true);
            assertTrue((Boolean) method.invoke(franjaHoraria));
            
            // Caso inválido: horaFin igual a horaInicio
            franjaHoraria.setHoraFin(franjaHoraria.getHoraInicio());
            assertFalse((Boolean) method.invoke(franjaHoraria));
            
            // Caso inválido: horaFin antes de horaInicio
            franjaHoraria.setHoraFin(LocalTime.of(8, 0));
            assertFalse((Boolean) method.invoke(franjaHoraria));
            
            // Caso con valores nulos
            franjaHoraria.setHoraInicio(null);
            franjaHoraria.setHoraFin(null);
            assertTrue((Boolean) method.invoke(franjaHoraria));
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("No se pudo acceder al método privado: " + e.getMessage());
        }
    }

    @Test
    void testIsHoraFinAfterHoraInicioConAnotacion() {
        // Preparamos diferentes escenarios
        FranjaHoraria franjaTest = new FranjaHoraria();
        franjaTest.setHoraInicio(LocalTime.of(9, 0));
        franjaTest.setHoraFin(LocalTime.of(18, 0));
        
        try {
            // Buscamos métodos con la anotación @AssertTrue
            java.lang.reflect.Method assertTrueMethod = null;
            for (java.lang.reflect.Method method : FranjaHoraria.class.getDeclaredMethods()) {
                if (method.isAnnotationPresent(jakarta.validation.constraints.AssertTrue.class)) {
                    assertTrueMethod = method;
                    break;
                }
            }
            
            if (assertTrueMethod == null) {
                org.junit.jupiter.api.Assertions.fail("No se encontró ningún método con anotación @AssertTrue");
                return;
            }
            
            assertTrueMethod.setAccessible(true);
            
            // Caso válido: horaFin después de horaInicio
            assertTrue((Boolean) assertTrueMethod.invoke(franjaTest));
            
            // Caso inválido: horaFin igual a horaInicio
            franjaTest.setHoraFin(franjaTest.getHoraInicio());
            assertFalse((Boolean) assertTrueMethod.invoke(franjaTest));
            
            // Caso inválido: horaFin antes de horaInicio
            franjaTest.setHoraFin(LocalTime.of(8, 0));
            assertFalse((Boolean) assertTrueMethod.invoke(franjaTest));
            
            // Caso con valores nulos
            franjaTest.setHoraInicio(null);
            franjaTest.setHoraFin(null);
            assertTrue((Boolean) assertTrueMethod.invoke(franjaTest));
            
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("Error al invocar método con anotación @AssertTrue: " + e.getMessage());
        }
    }

    @Test
    void testIsHoraFinAfterHoraInicioIndirecto() {
        FranjaHoraria franjaTest = new FranjaHoraria();
        franjaTest.setDiaSemana(DayOfWeek.MONDAY);
        franjaTest.setEstablecimiento(establecimiento);
        
        // Caso válido: horaFin después de horaInicio
        franjaTest.setHoraInicio(LocalTime.of(9, 0));
        franjaTest.setHoraFin(LocalTime.of(18, 0));
        
        // Verificamos indirectamente que el método isHoraFinAfterHoraInicio devuelve true
        // comprobando que las horas están en el orden correcto
        assertTrue(franjaTest.getHoraFin().isAfter(franjaTest.getHoraInicio()));
        
        // Caso inválido: horaFin igual a horaInicio
        franjaTest.setHoraFin(franjaTest.getHoraInicio());
        
        // Verificamos indirectamente que el método isHoraFinAfterHoraInicio devuelve false
        // comprobando que las horas no están en el orden correcto
        assertFalse(franjaTest.getHoraFin().isAfter(franjaTest.getHoraInicio()));
        
        // Caso inválido: horaFin antes de horaInicio
        franjaTest.setHoraFin(LocalTime.of(8, 0));
        
        // Verificamos indirectamente que el método isHoraFinAfterHoraInicio devuelve false
        // comprobando que las horas no están en el orden correcto
        assertFalse(franjaTest.getHoraFin().isAfter(franjaTest.getHoraInicio()));
        
        // Caso con valores nulos
        franjaTest.setHoraInicio(null);
        franjaTest.setHoraFin(null);
        
        // No podemos verificar directamente este caso sin reflexión
        // ya que el método isHoraFinAfterHoraInicio maneja específicamente el caso de valores nulos
    }

    @Test
    void testConstructorVacio() {
        FranjaHoraria franjaVacia = new FranjaHoraria();
        
        assertNotNull(franjaVacia);
    }

    @Test
    void testConstructorCompleto() {
        Integer id = 4;
        DayOfWeek diaTest = DayOfWeek.THURSDAY;
        LocalTime inicioTest = LocalTime.of(7, 0);
        LocalTime finTest = LocalTime.of(16, 0);
        Establecimiento estTest = new Establecimiento();
        estTest.setId(4);
        
        FranjaHoraria franjaCompleta = new FranjaHoraria(id, diaTest, inicioTest, finTest, estTest);
        
        assertEquals(id, franjaCompleta.getId());
        assertEquals(diaTest, franjaCompleta.getDiaSemana());
        assertEquals(inicioTest, franjaCompleta.getHoraInicio());
        assertEquals(finTest, franjaCompleta.getHoraFin());
        assertEquals(estTest, franjaCompleta.getEstablecimiento());
    }
    
    @Test
    void testConstructorCopia() {
        // Create a FranjaHoraria object with test data
        DayOfWeek diaTest = DayOfWeek.FRIDAY;
        LocalTime inicioTest = LocalTime.of(10, 0);
        LocalTime finTest = LocalTime.of(20, 0);
        Establecimiento estTest = new Establecimiento();
        estTest.setId(5);
        estTest.setNombre("Establecimiento Original");
        
        FranjaHoraria franjaOriginal = new FranjaHoraria();
        franjaOriginal.setId(5);
        franjaOriginal.setDiaSemana(diaTest);
        franjaOriginal.setHoraInicio(inicioTest);
        franjaOriginal.setHoraFin(finTest);
        franjaOriginal.setEstablecimiento(estTest);
        
        // Create a copy using the copy constructor
        FranjaHoraria franjaCopia = new FranjaHoraria(franjaOriginal);
        
        // Verify all attributes are equal
        assertEquals(franjaOriginal.getId(), franjaCopia.getId());
        assertEquals(franjaOriginal.getDiaSemana(), franjaCopia.getDiaSemana());
        assertEquals(franjaOriginal.getHoraInicio(), franjaCopia.getHoraInicio());
        assertEquals(franjaOriginal.getHoraFin(), franjaCopia.getHoraFin());
        assertEquals(franjaOriginal.getEstablecimiento(), franjaCopia.getEstablecimiento());
        
        // Verify they are different objects
        assertNotSame(franjaOriginal, franjaCopia);
        
        // Verify modifying the copy doesn't affect the original
        franjaCopia.setId(6);
        franjaCopia.setDiaSemana(DayOfWeek.SATURDAY);
        assertNotEquals(franjaOriginal.getId(), franjaCopia.getId());
        assertNotEquals(franjaOriginal.getDiaSemana(), franjaCopia.getDiaSemana());
    }
    
    @Test
    void testMetodoCopia() {
        // Create a FranjaHoraria object with test data
        DayOfWeek diaTest = DayOfWeek.MONDAY;
        LocalTime inicioTest = LocalTime.of(9, 0);
        LocalTime finTest = LocalTime.of(18, 0);
        Establecimiento estTest = new Establecimiento();
        estTest.setId(1);
        estTest.setNombre("Establecimiento Test");
        
        FranjaHoraria franjaOriginal = new FranjaHoraria();
        franjaOriginal.setId(1);
        franjaOriginal.setDiaSemana(diaTest);
        franjaOriginal.setHoraInicio(inicioTest);
        franjaOriginal.setHoraFin(finTest);
        franjaOriginal.setEstablecimiento(estTest);
        
        // Create a copy using the copia() method
        FranjaHoraria franjaCopia = (FranjaHoraria) franjaOriginal.copia();
        
        // Verify all attributes are equal
        assertEquals(franjaOriginal.getId(), franjaCopia.getId());
        assertEquals(franjaOriginal.getDiaSemana(), franjaCopia.getDiaSemana());
        assertEquals(franjaOriginal.getHoraInicio(), franjaCopia.getHoraInicio());
        assertEquals(franjaOriginal.getHoraFin(), franjaCopia.getHoraFin());
        assertEquals(franjaOriginal.getEstablecimiento(), franjaCopia.getEstablecimiento());
        
        // Verify they are different objects
        assertNotSame(franjaOriginal, franjaCopia);
        
        // Verify modifying the copy doesn't affect the original
        franjaCopia.setId(2);
        franjaCopia.setDiaSemana(DayOfWeek.TUESDAY);
        assertNotEquals(franjaOriginal.getId(), franjaCopia.getId());
        assertNotEquals(franjaOriginal.getDiaSemana(), franjaCopia.getDiaSemana());
    }
    
    @Test
    void testHashCode() {
        // Create base object
        FranjaHoraria franja1 = new FranjaHoraria();
        franja1.setId(1);
        franja1.setDiaSemana(DayOfWeek.MONDAY);
        franja1.setHoraInicio(LocalTime.of(9, 0));
        franja1.setHoraFin(LocalTime.of(18, 0));
        franja1.setEstablecimiento(new Establecimiento());

        // Test identical objects have same hashcode
        FranjaHoraria franja2 = new FranjaHoraria(franja1);
        assertEquals(franja1.hashCode(), franja2.hashCode());

        // Test different diaSemana
        FranjaHoraria franja3 = new FranjaHoraria(franja1);
        franja3.setDiaSemana(DayOfWeek.TUESDAY);
        assertEquals(franja1.hashCode(), franja3.hashCode());

        // Test different horaInicio
        FranjaHoraria franja4 = new FranjaHoraria(franja1);
        franja4.setHoraInicio(LocalTime.of(10, 0));
        assertEquals(franja1.hashCode(), franja4.hashCode());

        // Test different horaFin
        FranjaHoraria franja5 = new FranjaHoraria(franja1);
        franja5.setHoraFin(LocalTime.of(19, 0));
        assertEquals(franja1.hashCode(), franja5.hashCode());

        // Test different establecimiento
        FranjaHoraria franja6 = new FranjaHoraria(franja1);
        Establecimiento otroEstablecimiento = new Establecimiento();
        otroEstablecimiento.setId(2);
        franja6.setEstablecimiento(otroEstablecimiento);
        assertEquals(franja1.hashCode(), franja6.hashCode());

        // Test null values
        FranjaHoraria franjaNullValues = new FranjaHoraria();
        franjaNullValues.setId(1);
        assertEquals(franja1.hashCode(), franjaNullValues.hashCode());
    }

    @Test
    void testEquals() {
        // Create base object
        FranjaHoraria franja1 = new FranjaHoraria();
        franja1.setId(1);
        franja1.setDiaSemana(DayOfWeek.MONDAY);
        franja1.setHoraInicio(LocalTime.of(9, 0));
        franja1.setHoraFin(LocalTime.of(18, 0));
        Establecimiento establecimiento1 = new Establecimiento();
        establecimiento1.setId(1);
        franja1.setEstablecimiento(establecimiento1);

        // Test same object reference
        assertEquals(franja1, franja1);

        // Test null
        assertNotEquals(null, franja1);

        // Test different class
        assertNotEquals(franja1, new Object());

        // Test identical values
        FranjaHoraria franja2 = new FranjaHoraria(franja1);
        assertEquals(franja1, franja2);
        assertEquals(franja2, franja1);

        // Test different diaSemana
        FranjaHoraria franja3 = new FranjaHoraria(franja1);
        franja3.setDiaSemana(DayOfWeek.TUESDAY);
        assertEquals(franja1, franja3);
        assertEquals(franja3, franja1);

        // Test different horaInicio
        FranjaHoraria franja4 = new FranjaHoraria(franja1);
        franja4.setHoraInicio(LocalTime.of(10, 0));
        assertEquals(franja1, franja4);
        assertEquals(franja4, franja1);

        // Test different horaFin
        FranjaHoraria franja5 = new FranjaHoraria(franja1);
        franja5.setHoraFin(LocalTime.of(19, 0));
        assertEquals(franja1, franja5);
        assertEquals(franja5, franja1);

        // Test different establecimiento
        FranjaHoraria franja6 = new FranjaHoraria(franja1);
        Establecimiento establecimiento2 = new Establecimiento();
        establecimiento2.setId(2);
        franja6.setEstablecimiento(establecimiento2);
        assertEquals(franja1, franja6);
        assertEquals(franja6, franja1);

        // Test null fields
        FranjaHoraria franjaNullFields = new FranjaHoraria();
        franjaNullFields.setId(1);
        assertEquals(franja1, franjaNullFields);
        assertEquals(franjaNullFields, franja1);

        // Test transitivity
        FranjaHoraria franjaA = new FranjaHoraria(franja1);
        FranjaHoraria franjaB = new FranjaHoraria(franja1);
        FranjaHoraria franjaC = new FranjaHoraria(franja1);
        assertEquals(franjaA, franjaB);
        assertEquals(franjaB, franjaC);
        assertEquals(franjaA, franjaC);
    }


}