package es.ubu.reservapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void deleteById_ShouldDeleteEstablecimiento() {
        // Given
        Integer id = 1;
        doNothing().when(establecimientoRepo).deleteById(id);

        // When
        establecimientoService.deleteById(id);

        // Then
        verify(establecimientoRepo).deleteById(id);
    }
}
