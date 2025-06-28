package es.ubu.reservapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
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

import es.ubu.reservapp.model.entities.Perfil;
import es.ubu.reservapp.model.repositories.PerfilRepo;

/**
 * Test para la clase PerfilServiceImpl.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class PerfilServiceImplTest {

    @Mock
    private PerfilRepo perfilRepo;

    private PerfilServiceImpl perfilService;
    private Perfil perfil;

    @BeforeEach
    void setUp() {
        perfilService = new PerfilServiceImpl(perfilRepo);
        perfil = new Perfil();
        perfil.setId(1);
        perfil.setNombre("Test Perfil");
    }

    @Test
    void findAll_ShouldReturnAllPerfiles() {
        // Given
        List<Perfil> expectedList = Arrays.asList(perfil);
        when(perfilRepo.findAll()).thenReturn(expectedList);

        // When
        List<Perfil> result = perfilService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(perfilRepo).findAll();
    }

    @Test
    void findById_WhenPerfilExists_ShouldReturnPerfil() {
        // Given
        when(perfilRepo.findById(1)).thenReturn(Optional.of(perfil));

        // When
        Optional<Perfil> result = perfilService.findById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(perfil, result.get());
        verify(perfilRepo).findById(1);
    }

    @Test
    void findById_WhenPerfilDoesNotExist_ShouldReturnEmptyOptional() {
        // Given
        when(perfilRepo.findById(99)).thenReturn(Optional.empty());

        // When
        Optional<Perfil> result = perfilService.findById(99);

        // Then
        assertFalse(result.isPresent());
        verify(perfilRepo).findById(99);
    }

    @Test
    void save_WhenNewPerfilWithUniqueNombre_ShouldSavePerfil() {
        // Given
        Perfil newPerfil = new Perfil();
        newPerfil.setNombre("Nuevo Perfil");
        when(perfilRepo.findByNombre("Nuevo Perfil")).thenReturn(Optional.empty());
        when(perfilRepo.save(newPerfil)).thenReturn(newPerfil);

        // When
        Perfil result = perfilService.save(newPerfil);

        // Then
        assertNotNull(result);
        assertEquals(newPerfil, result);
        verify(perfilRepo).findByNombre("Nuevo Perfil");
        verify(perfilRepo).save(newPerfil);
    }

    @Test
    void save_WhenNewPerfilWithDuplicateNombre_ShouldThrowException() {
        // Given
        Perfil newPerfil = new Perfil();
        newPerfil.setNombre("Existente");
        when(perfilRepo.findByNombre("Existente")).thenReturn(Optional.of(new Perfil()));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> perfilService.save(newPerfil));
        verify(perfilRepo).findByNombre("Existente");
        verify(perfilRepo, never()).save(any());
    }

    @Test
    void save_WhenUpdatingExistingPerfil_ShouldSavePerfil() {
        // Given
        when(perfilRepo.save(perfil)).thenReturn(perfil);

        // When
        Perfil result = perfilService.save(perfil);

        // Then
        assertNotNull(result);
        assertEquals(perfil, result);
        verify(perfilRepo).save(perfil);
    }

    @Test
    void deleteById_ShouldDeletePerfil() {
        // Given
        Integer id = 1;
        doNothing().when(perfilRepo).deleteById(id);

        // When
        perfilService.deleteById(id);

        // Then
        verify(perfilRepo).deleteById(id);
    }

    @Test
    void findByNombre_WhenPerfilExists_ShouldReturnPerfil() {
        // Given
        when(perfilRepo.findByNombre("Test Perfil")).thenReturn(Optional.of(perfil));

        // When
        Optional<Perfil> result = perfilService.findByNombre("Test Perfil");

        // Then
        assertTrue(result.isPresent());
        assertEquals(perfil, result.get());
        verify(perfilRepo).findByNombre("Test Perfil");
    }

    @Test
    void findByNombre_WhenPerfilDoesNotExist_ShouldReturnEmptyOptional() {
        // Given
        when(perfilRepo.findByNombre("No Existe")).thenReturn(Optional.empty());

        // When
        Optional<Perfil> result = perfilService.findByNombre("No Existe");

        // Then
        assertFalse(result.isPresent());
        verify(perfilRepo).findByNombre("No Existe");
    }
}
