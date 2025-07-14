package es.ubu.reservapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.ubu.reservapp.model.entities.Convocado;
import es.ubu.reservapp.model.entities.ConvocadoPK;
import es.ubu.reservapp.model.entities.Convocatoria;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.repositories.ConvocadoRepo;

/**
 * Test para la clase ConvocadoServiceImpl.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConvocadoServiceImpl Tests")
class ConvocadoServiceImplTest {

    @Mock
    private ConvocadoRepo convocadoRepo;

    @InjectMocks
    private ConvocadoServiceImpl convocadoService;
    
    private Convocatoria convocatoria;
    private Convocado convocado1;
    private Convocado convocado2;
    private Usuario usuario1;
    private Usuario usuario2;
    private Reserva reserva;
    private ConvocadoPK convocadoPK1;
    private ConvocadoPK convocadoPK2;
    
    private static final Integer ID_RESERVA = 123;
    private static final String ID_USUARIO_1 = "USER001";
    private static final String ID_USUARIO_2 = "USER002";
    private static final String ENLACE = "https://example.com/meeting";
    private static final String OBSERVACIONES = "Reunión importante";

    @BeforeEach
    void setUp() {
        // Crear reserva
        reserva = new Reserva();
        reserva.setId(ID_RESERVA);
        
        // Crear convocatoria
        convocatoria = new Convocatoria();
        convocatoria.setId(ID_RESERVA);
        convocatoria.setReserva(reserva);
        convocatoria.setEnlace(ENLACE);
        convocatoria.setObservaciones(OBSERVACIONES);
        
        // Crear usuarios
        usuario1 = new Usuario();
        usuario1.setId(ID_USUARIO_1);
        usuario1.setNombre("Usuario");
        usuario1.setApellidos("Uno");
        usuario1.setCorreo("usuario1@test.com");
        
        usuario2 = new Usuario();
        usuario2.setId(ID_USUARIO_2);
        usuario2.setNombre("Usuario");
        usuario2.setApellidos("Dos");
        usuario2.setCorreo("usuario2@test.com");
        
        // Crear claves primarias compuestas
        convocadoPK1 = new ConvocadoPK(ID_RESERVA, ID_USUARIO_1);
        convocadoPK2 = new ConvocadoPK(ID_RESERVA, ID_USUARIO_2);
        
        // Crear convocados
        convocado1 = new Convocado();
        convocado1.setId(convocadoPK1);
        convocado1.setConvocatoria(convocatoria);
        convocado1.setUsuario(usuario1);
        
        convocado2 = new Convocado();
        convocado2.setId(convocadoPK2);
        convocado2.setConvocatoria(convocatoria);
        convocado2.setUsuario(usuario2);
    }

    @Test
    @DisplayName("Constructor - Debe inicializar correctamente el servicio")
    void constructor_ShouldInitializeServiceCorrectly() {
        // Given & When
        ConvocadoServiceImpl service = new ConvocadoServiceImpl(convocadoRepo);
        
        // Then
        assertNotNull(service);
    }

    @Test
    @DisplayName("findByConvocatoria - Debe retornar lista de convocados cuando existen")
    void findByConvocatoria_WhenConvocadosExist_ShouldReturnListOfConvocados() {
        // Given
        List<Convocado> expectedConvocados = Arrays.asList(convocado1, convocado2);
        when(convocadoRepo.findByConvocatoria(convocatoria)).thenReturn(expectedConvocados);

        // When
        List<Convocado> result = convocadoService.findByConvocatoria(convocatoria);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedConvocados, result);
        assertTrue(result.contains(convocado1));
        assertTrue(result.contains(convocado2));
        verify(convocadoRepo, times(1)).findByConvocatoria(convocatoria);
    }

    @Test
    @DisplayName("findByConvocatoria - Debe retornar lista vacía cuando no existen convocados")
    void findByConvocatoria_WhenNoConvocadosExist_ShouldReturnEmptyList() {
        // Given
        List<Convocado> emptyList = new ArrayList<>();
        when(convocadoRepo.findByConvocatoria(convocatoria)).thenReturn(emptyList);

        // When
        List<Convocado> result = convocadoService.findByConvocatoria(convocatoria);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
        verify(convocadoRepo, times(1)).findByConvocatoria(convocatoria);
    }

    @Test
    @DisplayName("findByConvocatoria - Debe retornar lista con un solo convocado")
    void findByConvocatoria_WhenSingleConvocadoExists_ShouldReturnSingleElementList() {
        // Given
        List<Convocado> singleConvocadoList = Arrays.asList(convocado1);
        when(convocadoRepo.findByConvocatoria(convocatoria)).thenReturn(singleConvocadoList);

        // When
        List<Convocado> result = convocadoService.findByConvocatoria(convocatoria);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(convocado1, result.get(0));
        assertEquals(ID_USUARIO_1, result.get(0).getUsuario().getId());
        assertEquals(convocatoria, result.get(0).getConvocatoria());
        verify(convocadoRepo, times(1)).findByConvocatoria(convocatoria);
    }

    @Test
    @DisplayName("findByConvocatoria - Debe manejar convocatoria con datos mínimos")
    void findByConvocatoria_WithMinimalConvocatoriaData_ShouldWork() {
        // Given
        Convocatoria minimalConvocatoria = new Convocatoria();
        minimalConvocatoria.setId(999);
        
        List<Convocado> expectedConvocados = Arrays.asList(convocado1);
        when(convocadoRepo.findByConvocatoria(minimalConvocatoria)).thenReturn(expectedConvocados);

        // When
        List<Convocado> result = convocadoService.findByConvocatoria(minimalConvocatoria);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedConvocados, result);
        verify(convocadoRepo, times(1)).findByConvocatoria(minimalConvocatoria);
    }

    @Test
    @DisplayName("findByConvocatoria - Debe manejar convocatoria null")
    void findByConvocatoria_WithNullConvocatoria_ShouldCallRepository() {
        // Given
        List<Convocado> emptyList = new ArrayList<>();
        when(convocadoRepo.findByConvocatoria(null)).thenReturn(emptyList);

        // When
        List<Convocado> result = convocadoService.findByConvocatoria(null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(convocadoRepo, times(1)).findByConvocatoria(null);
    }

    @Test
    @DisplayName("findByConvocatoria - Debe verificar que se llama exactamente una vez al repositorio")
    void findByConvocatoria_ShouldCallRepositoryExactlyOnce() {
        // Given
        List<Convocado> expectedConvocados = Arrays.asList(convocado1, convocado2);
        when(convocadoRepo.findByConvocatoria(convocatoria)).thenReturn(expectedConvocados);

        // When
        convocadoService.findByConvocatoria(convocatoria);

        // Then
        verify(convocadoRepo, times(1)).findByConvocatoria(convocatoria);
        verify(convocadoRepo, never()).findAll();
        verify(convocadoRepo, never()).save(convocado1);
        verify(convocadoRepo, never()).delete(convocado1);
    }

    @Test
    @DisplayName("findByConvocatoria - Debe retornar la misma referencia que el repositorio")
    void findByConvocatoria_ShouldReturnSameReferenceAsRepository() {
        // Given
        List<Convocado> repositoryResult = Arrays.asList(convocado1, convocado2);
        when(convocadoRepo.findByConvocatoria(convocatoria)).thenReturn(repositoryResult);

        // When
        List<Convocado> serviceResult = convocadoService.findByConvocatoria(convocatoria);

        // Then
        assertEquals(repositoryResult, serviceResult);
        // Verificar que es la misma referencia (no una copia)
        assertTrue(repositoryResult == serviceResult);
    }

    @Test
    @DisplayName("findByConvocatoria - Debe manejar lista grande de convocados")
    void findByConvocatoria_WithLargeListOfConvocados_ShouldWork() {
        // Given
        List<Convocado> largeList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Convocado convocado = new Convocado();
            ConvocadoPK pk = new ConvocadoPK(ID_RESERVA, "USER" + String.format("%03d", i));
            convocado.setId(pk);
            convocado.setConvocatoria(convocatoria);
            
            Usuario usuario = new Usuario();
            usuario.setId("USER" + String.format("%03d", i));
            convocado.setUsuario(usuario);
            
            largeList.add(convocado);
        }
        
        when(convocadoRepo.findByConvocatoria(convocatoria)).thenReturn(largeList);

        // When
        List<Convocado> result = convocadoService.findByConvocatoria(convocatoria);

        // Then
        assertNotNull(result);
        assertEquals(100, result.size());
        assertEquals(largeList, result);
        verify(convocadoRepo, times(1)).findByConvocatoria(convocatoria);
    }
}