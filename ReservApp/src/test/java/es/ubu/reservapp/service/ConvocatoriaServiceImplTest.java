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
import es.ubu.reservapp.model.entities.ConvocatoriaPK;
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
        convocatoria.setId(new ConvocatoriaPK(1, "USER0001"));
        Reserva reserva = new Reserva();
        reserva.setId(1);
        convocatoria.setReserva(reserva);
        Usuario usuario = new Usuario();
        usuario.setId("USER0001");
        convocatoria.setUsuario(usuario);
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
    	ConvocatoriaPK id = new ConvocatoriaPK(1, "USER0001");
        when(convocatoriaRepo.findById(id)).thenReturn(Optional.of(convocatoria));

        // When
        Optional<Convocatoria> result = convocatoriaService.findById(id);

        // Then
        assertTrue(result.isPresent());
        assertEquals(convocatoria, result.get());
        verify(convocatoriaRepo).findById(id);
    }

    @Test
    void findById_WhenConvocatoriaDoesNotExist_ShouldReturnEmptyOptional() {
        // Given
    	ConvocatoriaPK id = new ConvocatoriaPK(1, "USER0001");
        when(convocatoriaRepo.findById(id)).thenReturn(Optional.empty());

        // When
        Optional<Convocatoria> result = convocatoriaService.findById(id);

        // Then
        assertFalse(result.isPresent());
        verify(convocatoriaRepo).findById(id);
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
        convocatoria1.setId(new ConvocatoriaPK());
        convocatoria1.setUsuario(usuario);

        Convocatoria convocatoria2 = new Convocatoria();
        convocatoria2.setId(new ConvocatoriaPK());
        convocatoria2.setUsuario(usuario);

        List<Convocatoria> convocatorias = new ArrayList<>();
        convocatorias.add(convocatoria1);
        convocatorias.add(convocatoria2);

        // Configurar mock
        when(convocatoriaRepo.findConvocatoriaByUsuario(usuario)).thenReturn(convocatorias);

        // Ejecutar método
        List<Convocatoria> resultado = convocatoriaService.findConvocatoriaByUsuario(usuario);

        // Verificar resultado
        assertEquals(convocatorias, resultado);
    }

    @Test
    void testFindConvocatoriaByReserva() {
        // Preparar datos
        Reserva reserva = new Reserva();
        reserva.setId(1);

        Convocatoria convocatoria1 = new Convocatoria();
        convocatoria1.setId(new ConvocatoriaPK());
        convocatoria1.setReserva(reserva);

        Convocatoria convocatoria2 = new Convocatoria();
        convocatoria2.setId(new ConvocatoriaPK());
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
