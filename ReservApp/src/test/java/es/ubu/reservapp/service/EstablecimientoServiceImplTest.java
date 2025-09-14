package es.ubu.reservapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.FranjaHoraria;
import es.ubu.reservapp.model.repositories.EstablecimientoRepo;

/**
 * Test para la clase EstablecimientoServiceImpl.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class EstablecimientoServiceImplTest {

    @Mock
    private EstablecimientoRepo establecimientoRepo;
    
    private EstablecimientoServiceImpl establecimientoService;
    private Establecimiento establecimiento;

    @BeforeEach
    void setUp() {
        establecimientoService = new EstablecimientoServiceImpl(establecimientoRepo);
        establecimiento = new Establecimiento();
        establecimiento.setId(1);
        establecimiento.setNombre("Test Establecimiento");
        establecimiento.setFranjasHorarias(Arrays.asList(new FranjaHoraria()));
    }

    @Test
    void findAll_ShouldReturnAllEstablecimientos() {
        // Given
        List<Establecimiento> expectedList = Arrays.asList(establecimiento);
        when(establecimientoRepo.findAll()).thenReturn(expectedList);

        // When
        List<Establecimiento> result = establecimientoService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(establecimientoRepo).findAll();
    }

    @Test
    void findById_WhenEstablecimientoExists_ShouldReturnEstablecimiento() {
        // Given
        when(establecimientoRepo.findById(1)).thenReturn(Optional.of(establecimiento));

        // When
        Optional<Establecimiento> result = establecimientoService.findById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(establecimiento, result.get());
        verify(establecimientoRepo).findById(1);
    }

    @Test
    void findById_WhenEstablecimientoDoesNotExist_ShouldReturnEmptyOptional() {
        // Given
        when(establecimientoRepo.findById(99)).thenReturn(Optional.empty());

        // When
        Optional<Establecimiento> result = establecimientoService.findById(99);

        // Then
        assertFalse(result.isPresent());
        verify(establecimientoRepo).findById(99);
    }

    @Test
    void save_ShouldReturnSavedEstablecimiento() {
        // Given
        when(establecimientoRepo.save(establecimiento)).thenReturn(establecimiento);

        // When
        Establecimiento result = establecimientoService.save(establecimiento);
        
        // Then
        assertNotNull(result);
        assertEquals(establecimiento, result);
        verify(establecimientoRepo).save(establecimiento);
    }
    
    @Test
    void estaAbiertoEnFecha_WhenEstablecimientoIsNull_ShouldReturnFalse() {
        // Given
        Establecimiento establecimientoNull = null;
        java.time.LocalDate fecha = java.time.LocalDate.now();
        
        // When
        boolean result = establecimientoService.estaAbiertoEnFecha(establecimientoNull, fecha);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void estaAbiertoEnFecha_WhenFechaIsNull_ShouldReturnFalse() {
        // Given
        java.time.LocalDate fechaNull = null;
        
        // When
        boolean result = establecimientoService.estaAbiertoEnFecha(establecimiento, fechaNull);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void estaAbiertoEnFecha_WhenFranjasHorariasIsNull_ShouldReturnFalse() {
        // Given
        Establecimiento establecimientoSinFranjas = new Establecimiento();
        establecimientoSinFranjas.setFranjasHorarias(null);
        java.time.LocalDate fecha = java.time.LocalDate.now();
        
        // When
        boolean result = establecimientoService.estaAbiertoEnFecha(establecimientoSinFranjas, fecha);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void estaAbiertoEnFecha_WhenNoFranjasForDayOfWeek_ShouldReturnFalse() {
        // Given
        Establecimiento establecimientoConFranjas = new Establecimiento();
        List<FranjaHoraria> franjas = new ArrayList<>();
        
        // Crear franja para un día diferente al de la fecha de prueba
        FranjaHoraria franja = new FranjaHoraria();
        franja.setDiaSemana(DayOfWeek.MONDAY);
        franjas.add(franja);
        
        establecimientoConFranjas.setFranjasHorarias(franjas);
        
        // Usar un día que no sea lunes para la prueba
        java.time.LocalDate fechaMartes = java.time.LocalDate.now();
        while (fechaMartes.getDayOfWeek() == DayOfWeek.MONDAY) {
            fechaMartes = fechaMartes.plusDays(1);
        }
        
        // When
        boolean result = establecimientoService.estaAbiertoEnFecha(establecimientoConFranjas, fechaMartes);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void estaAbiertoEnFecha_WhenFranjaExistsForDayOfWeek_ShouldReturnTrue() {
        // Given
        Establecimiento establecimientoConFranjas = new Establecimiento();
        List<FranjaHoraria> franjas = new ArrayList<>();
        
        // Obtener el día de la semana actual
        java.time.LocalDate fechaHoy = java.time.LocalDate.now();
        DayOfWeek diaHoy = fechaHoy.getDayOfWeek();
        
        // Crear franja para el día actual
        FranjaHoraria franja = new FranjaHoraria();
        franja.setDiaSemana(diaHoy);
        franjas.add(franja);
        
        establecimientoConFranjas.setFranjasHorarias(franjas);
        
        // When
        boolean result = establecimientoService.estaAbiertoEnFecha(establecimientoConFranjas, fechaHoy);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void estaAbiertoEnFecha_WhenMultipleFranjasExistForDifferentDays_ShouldReturnTrueForMatchingDay() {
        // Given
        Establecimiento establecimientoConFranjas = new Establecimiento();
        List<FranjaHoraria> franjas = new ArrayList<>();
        
        // Crear franjas para varios días
        for (DayOfWeek dia : DayOfWeek.values()) {
            FranjaHoraria franja = new FranjaHoraria();
            franja.setDiaSemana(dia);
            franjas.add(franja);
        }
        
        establecimientoConFranjas.setFranjasHorarias(franjas);
        
        // Probar con un día específico
        java.time.LocalDate fechaMiercoles = java.time.LocalDate.now();
        while (fechaMiercoles.getDayOfWeek() != DayOfWeek.WEDNESDAY) {
            fechaMiercoles = fechaMiercoles.plusDays(1);
        }
        
        // When
        boolean result = establecimientoService.estaAbiertoEnFecha(establecimientoConFranjas, fechaMiercoles);
        
        // Then
        assertTrue(result);
    }
    
    @Test
     void estaAbiertoEnFecha_WhenEmptyFranjasHorarias_ShouldReturnFalse() {
         // Given
         Establecimiento establecimientoConFranjasVacias = new Establecimiento();
         establecimientoConFranjasVacias.setFranjasHorarias(new ArrayList<>());
         java.time.LocalDate fecha = java.time.LocalDate.now();
         
         // When
         boolean result = establecimientoService.estaAbiertoEnFecha(establecimientoConFranjasVacias, fecha);
         
         // Then
         assertFalse(result);
    }

    @Test
    void deleteById_ShouldDeleteEstablecimiento() {
        // Given
        Integer id = 1;
        doNothing().when(establecimientoRepo).deleteById(id);

        // When
        establecimientoService.deleteById(id);

        // Then
        verify(establecimientoRepo).deleteById(id);
    }
    
    @Test
    void findAllById_ShouldReturnEstablecimientosByIds() {
        // Given
    	List<Integer> ids = Arrays.asList(1, 2);
        List<Establecimiento> expectedList = Arrays.asList(establecimiento);
        when(establecimientoRepo.findAllById(ids)).thenReturn(expectedList);

        // When
        List<Establecimiento> result = establecimientoService.findAllById(ids);

        // Then
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(establecimientoRepo).findAllById(ids);
    }
    
    @Test
    void testFindAllByIdListaVacia() {
        // Crear una lista de IDs vacía
        List<Integer> ids = new ArrayList<>();

        // Llamar al método findAllById
        List<Establecimiento> establecimientos = establecimientoService.findAllById(ids);

        // Verificar que la lista sea vacía
        assertTrue(establecimientos.isEmpty());
    }

    @Test
    void testFindAllByIdListaNull() {
        // Llamar al método findAllById con una lista nula
        List<Establecimiento> establecimientos = establecimientoService.findAllById(null);

        // Verificar que la lista sea vacía
        assertTrue(establecimientos.isEmpty());
    }

    @Test
    void findAllAndFranjaHoraria_ShouldReturnEstablecimientosWithSortedFranjas() {
        // Given
        FranjaHoraria franja1 = new FranjaHoraria();
        franja1.setDiaSemana(DayOfWeek.WEDNESDAY); // Miércoles
        FranjaHoraria franja2 = new FranjaHoraria();
        franja2.setDiaSemana(DayOfWeek.MONDAY); // Lunes
        FranjaHoraria franja3 = new FranjaHoraria();
        franja3.setDiaSemana(DayOfWeek.FRIDAY); // Viernes
        
        List<FranjaHoraria> franjasDesordenadas = Arrays.asList(franja1, franja2, franja3);
        establecimiento.setFranjasHorarias(franjasDesordenadas);
        
        List<Establecimiento> establecimientos = Arrays.asList(establecimiento);
        when(establecimientoRepo.findAll()).thenReturn(establecimientos);

        // When
        List<Establecimiento> result = establecimientoService.findAllAndFranjaHoraria();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        Establecimiento resultEstablecimiento = result.get(0);
        assertNotNull(resultEstablecimiento.getFranjasHorarias());
        assertEquals(3, resultEstablecimiento.getFranjasHorarias().size());
        
        // Verificar que las franjas están ordenadas por día de la semana
        assertEquals(DayOfWeek.MONDAY, resultEstablecimiento.getFranjasHorarias().get(0).getDiaSemana());
        assertEquals(DayOfWeek.WEDNESDAY, resultEstablecimiento.getFranjasHorarias().get(1).getDiaSemana());
        assertEquals(DayOfWeek.FRIDAY, resultEstablecimiento.getFranjasHorarias().get(2).getDiaSemana());
        
        verify(establecimientoRepo).findAll();
    }

    @Test
    void findAllAndFranjaHoraria_WhenEstablecimientoHasNullFranjas_ShouldReturnEstablecimiento() {
        // Given
        establecimiento.setFranjasHorarias(null);
        List<Establecimiento> establecimientos = Arrays.asList(establecimiento);
        when(establecimientoRepo.findAll()).thenReturn(establecimientos);

        // When
        List<Establecimiento> result = establecimientoService.findAllAndFranjaHoraria();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(establecimiento, result.get(0));
        verify(establecimientoRepo).findAll();
    }

    @Test
    void findAllAndFranjaHoraria_WhenEstablecimientoHasEmptyFranjas_ShouldReturnEstablecimiento() {
        // Given
        establecimiento.setFranjasHorarias(new ArrayList<>());
        List<Establecimiento> establecimientos = Arrays.asList(establecimiento);
        when(establecimientoRepo.findAll()).thenReturn(establecimientos);

        // When
        List<Establecimiento> result = establecimientoService.findAllAndFranjaHoraria();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        Establecimiento resultEstablecimiento = result.get(0);
        assertNotNull(resultEstablecimiento.getFranjasHorarias());
        assertTrue(resultEstablecimiento.getFranjasHorarias().isEmpty());
        verify(establecimientoRepo).findAll();
    }

    @Test
    void findById_WhenEstablecimientoExistsWithNullFranjas_ShouldReturnEstablecimiento() {
        // Given
        establecimiento.setFranjasHorarias(null);
        when(establecimientoRepo.findById(1)).thenReturn(Optional.of(establecimiento));

        // When
        Optional<Establecimiento> result = establecimientoService.findById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(establecimiento, result.get());
        verify(establecimientoRepo).findById(1);
    }

    @Test
    void constructor_ShouldInitializeServiceCorrectly() {
        // Given & When
        EstablecimientoServiceImpl newService = new EstablecimientoServiceImpl(establecimientoRepo);
        
        // Then
        assertNotNull(newService);
    }

}
