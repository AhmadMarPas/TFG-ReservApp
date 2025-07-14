package es.ubu.reservapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.ubu.reservapp.model.entities.Convocatoria;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.repositories.ConvocatoriaRepo;

/**
 * Test para la clase ConvocatoriaServiceImpl.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class ConvocatoriaServiceImplTest {

    @Mock
    private ConvocatoriaRepo convocatoriaRepo;

    @InjectMocks
    private ConvocatoriaServiceImpl convocatoriaService;
    private Convocatoria convocatoria;

    @BeforeEach
    void setUp() {
        convocatoriaService = new ConvocatoriaServiceImpl(convocatoriaRepo);
        convocatoria = new Convocatoria();
        Reserva id = new Reserva();
        id.setId(1);
        convocatoria.setId(id.getId());
        Reserva reserva = new Reserva();
        reserva.setId(1);
        convocatoria.setReserva(reserva);
    }

    @Test
    void findAll_ShouldReturnAllConvocatorias() {
        // Given
        List<Convocatoria> expectedList = Arrays.asList(convocatoria);
        when(convocatoriaRepo.findAll()).thenReturn(expectedList);

        // When
        List<Convocatoria> result = convocatoriaService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(convocatoriaRepo).findAll();
    }

    @Test
    void findById_WhenConvocatoriaExists_ShouldReturnConvocatoria() {
        // Given
    	Reserva id = new Reserva();
    	id.setId(1);
        when(convocatoriaRepo.findById(id.getId())).thenReturn(Optional.of(convocatoria));

        // When
        Optional<Convocatoria> result = convocatoriaService.findById(id.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(convocatoria, result.get());
        verify(convocatoriaRepo).findById(id.getId());
    }

    @Test
    void findById_WhenConvocatoriaDoesNotExist_ShouldReturnEmptyOptional() {
        // Given
    	Reserva id = new Reserva();
    	id.setId(1);
        when(convocatoriaRepo.findById(id.getId())).thenReturn(Optional.empty());

        // When
        Optional<Convocatoria> result = convocatoriaService.findById(id.getId());

        // Then
        assertFalse(result.isPresent());
        verify(convocatoriaRepo).findById(id.getId());
    }

    @Test
    void save_WhenUpdatingExistingConvocatoria_ShouldSaveConvocatoria() {
        // Given
        when(convocatoriaRepo.save(convocatoria)).thenReturn(convocatoria);

        // When
        Convocatoria result = convocatoriaService.save(convocatoria);

        // Then
        assertNotNull(result);
        assertEquals(convocatoria, result);
        verify(convocatoriaRepo).save(convocatoria);
    }

    @Test
    void testFindConvocatoriaByUsuario() {
        // Preparar datos
        Usuario usuario = new Usuario();
        usuario.setId("usuario1");

        Convocatoria convocatoria1 = new Convocatoria();
    	Reserva id = new Reserva();
        convocatoria1.setId(id.getId());

        Convocatoria convocatoria2 = new Convocatoria();
        convocatoria2.setId(id.getId());

        List<Convocatoria> convocatorias = new ArrayList<>();
        convocatorias.add(convocatoria1);
        convocatorias.add(convocatoria2);

        // Configurar mock
        when(convocatoriaRepo.findConvocatoriaByReserva(id)).thenReturn(convocatorias);

        // Ejecutar método
        List<Convocatoria> resultado = convocatoriaService.findConvocatoriaByReserva(id);

        // Verificar resultado
        assertEquals(convocatorias, resultado);
    }

    @Test
    void testFindConvocatoriaByReserva() {
        // Preparar datos
        Reserva reserva = new Reserva();
        reserva.setId(1);

        Convocatoria convocatoria1 = new Convocatoria();
        convocatoria1.setId(reserva.getId());
        convocatoria1.setReserva(reserva);

        Convocatoria convocatoria2 = new Convocatoria();
        convocatoria2.setId(reserva.getId());
        convocatoria2.setReserva(reserva);

        List<Convocatoria> convocatorias = new ArrayList<>();
        convocatorias.add(convocatoria1);
        convocatorias.add(convocatoria2);

        // Configurar mock
        when(convocatoriaRepo.findConvocatoriaByReserva(reserva)).thenReturn(convocatorias);

        // Ejecutar método
        List<Convocatoria> resultado = convocatoriaService.findConvocatoriaByReserva(reserva);

        // Verificar resultado
        assertEquals(convocatorias, resultado);
    }
}
