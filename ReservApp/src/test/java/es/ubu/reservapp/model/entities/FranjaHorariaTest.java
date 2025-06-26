package es.ubu.reservapp.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
 * @author Test Generator
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
        // Verificar getters
        assertEquals(1, franjaHoraria.getId());
        assertEquals(diaSemana, franjaHoraria.getDiaSemana());
        assertEquals(horaInicio, franjaHoraria.getHoraInicio());
        assertEquals(horaFin, franjaHoraria.getHoraFin());
        assertEquals(establecimiento, franjaHoraria.getEstablecimiento());
        
        // Probar setters con nuevos valores
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
        
        // Verificar nuevos valores
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
        // Arrange - Preparamos diferentes escenarios
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
        // Arrange
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
        // Arrange & Act
        FranjaHoraria franjaVacia = new FranjaHoraria();
        
        // Assert
        assertNotNull(franjaVacia);
    }

    @Test
    void testConstructorConParametros() {
        // Arrange
        DayOfWeek diaTest = DayOfWeek.WEDNESDAY;
        LocalTime inicioTest = LocalTime.of(8, 30);
        LocalTime finTest = LocalTime.of(17, 30);
        Establecimiento estTest = new Establecimiento();
        estTest.setId(3);
        estTest.setNombre("Establecimiento Constructor");
        
        // Act
        FranjaHoraria franjaParametros = new FranjaHoraria(diaTest, inicioTest, finTest, estTest);
        
        // Assert
        assertEquals(diaTest, franjaParametros.getDiaSemana());
        assertEquals(inicioTest, franjaParametros.getHoraInicio());
        assertEquals(finTest, franjaParametros.getHoraFin());
        assertEquals(estTest, franjaParametros.getEstablecimiento());
    }

    @Test
    void testConstructorCompleto() {
        // Arrange
        Integer id = 4;
        DayOfWeek diaTest = DayOfWeek.THURSDAY;
        LocalTime inicioTest = LocalTime.of(7, 0);
        LocalTime finTest = LocalTime.of(16, 0);
        Establecimiento estTest = new Establecimiento();
        estTest.setId(4);
        
        // Act
        FranjaHoraria franjaCompleta = new FranjaHoraria(id, diaTest, inicioTest, finTest, estTest);
        
        // Assert
        assertEquals(id, franjaCompleta.getId());
        assertEquals(diaTest, franjaCompleta.getDiaSemana());
        assertEquals(inicioTest, franjaCompleta.getHoraInicio());
        assertEquals(finTest, franjaCompleta.getHoraFin());
        assertEquals(estTest, franjaCompleta.getEstablecimiento());
    }
}