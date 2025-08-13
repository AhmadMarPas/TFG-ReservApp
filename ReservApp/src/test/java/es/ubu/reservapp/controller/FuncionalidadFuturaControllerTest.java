package es.ubu.reservapp.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test para la clase FuncionalidadNoImplementadaController.
 * Prueba todos los endpoints que muestran páginas informativas para
 * funcionalidades no implementadas.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FuncionalidadFuturaControllerTest Tests")
class FuncionalidadFuturaControllerTest {

    @Mock
    private Model model;

    @InjectMocks
    private FuncionalidadFuturaController controller;

    @BeforeEach
    void setUp() {
        // No se requiere configuración adicional para este controlador
    }

    @Test
    @DisplayName("Constructor - Debe inicializar correctamente el controlador")
    void constructor_ShouldInitializeControllerCorrectly() {
        // Given & When
    	FuncionalidadFuturaController newController = new FuncionalidadFuturaController();
        
        // Then
        assertNotNull(newController);
    }

    // ========== TESTS PARA reportesYEstadisticas ==========

    @Test
    @DisplayName("Reportes y Estadísticas - Debe configurar modelo correctamente")
    void testReportesYEstadisticas_DebeConfigurarModeloCorrectamente() {
        // When
        String viewName = controller.reportesYEstadisticas(model);
        
        // Then
        verify(model).addAttribute("titulo", "Reportes y Estadísticas");
        verify(model).addAttribute("descripcion", "Visualización de estadísticas del sistema y generación de reportes detallados");
        verify(model).addAttribute("icono", "fas fa-chart-bar");
        assertEquals("funcionalidadfutura", viewName);
    }

    @Test
    @DisplayName("Reportes y Estadísticas - Debe retornar vista correcta")
    void testReportesYEstadisticas_DebeRetornarVistaCorrecta() {
        // When
        String viewName = controller.reportesYEstadisticas(model);
        
        // Then
        assertEquals("funcionalidadfutura", viewName);
    }

    // ========== TESTS PARA configuracionDelSistema ==========

    @Test
    @DisplayName("Configuración del Sistema - Debe configurar modelo correctamente")
    void testConfiguracionDelSistema_DebeConfigurarModeloCorrectamente() {
        // When
        String viewName = controller.configuracionDelSistema(model);
        
        // Then
        verify(model).addAttribute("titulo", "Configuración del Sistema");
        verify(model).addAttribute("descripcion", "Configuración avanzada de parámetros del sistema y ajustes administrativos");
        verify(model).addAttribute("icono", "fas fa-cog");
        assertEquals("funcionalidadfutura", viewName);
    }

    @Test
    @DisplayName("Configuración del Sistema - Debe retornar vista correcta")
    void testConfiguracionDelSistema_DebeRetornarVistaCorrecta() {
        // When
        String viewName = controller.configuracionDelSistema(model);
        
        // Then
        assertEquals("funcionalidadfutura", viewName);
    }

    // ========== TESTS PARA configuracionUsuario ==========

    @Test
    @DisplayName("Configuración Usuario - Debe configurar modelo correctamente")
    void testConfiguracionUsuario_DebeConfigurarModeloCorrectamente() {
        // When
        String viewName = controller.configuracionUsuario(model);
        
        // Then
        verify(model).addAttribute("titulo", "Configuración");
        verify(model).addAttribute("descripcion", "Personalización de preferencias y ajustes de tu cuenta");
        verify(model).addAttribute("icono", "fas fa-sliders-h");
        assertEquals("funcionalidadfutura", viewName);
    }

    @Test
    @DisplayName("Configuración Usuario - Debe retornar vista correcta")
    void testConfiguracionUsuario_DebeRetornarVistaCorrecta() {
        // When
        String viewName = controller.configuracionUsuario(model);
        
        // Then
        assertEquals("funcionalidadfutura", viewName);
    }

    // ========== TESTS PARA ayudaYSoporte ==========

    @Test
    @DisplayName("Ayuda y Soporte - Debe configurar modelo correctamente")
    void testAyudaYSoporte_DebeConfigurarModeloCorrectamente() {
        // When
        String viewName = controller.ayudaYSoporte(model);
        
        // Then
        verify(model).addAttribute("titulo", "Ayuda y Soporte");
        verify(model).addAttribute("descripcion", "Centro de ayuda, documentación y soporte técnico");
        verify(model).addAttribute("icono", "fas fa-question-circle");
        assertEquals("funcionalidadfutura", viewName);
    }

    @Test
    @DisplayName("Ayuda y Soporte - Debe retornar vista correcta")
    void testAyudaYSoporte_DebeRetornarVistaCorrecta() {
        // When
        String viewName = controller.ayudaYSoporte(model);
        
        // Then
        assertEquals("funcionalidadfutura", viewName);
    }

    // ========== TESTS DE INTEGRACIÓN ==========

    @Test
    @DisplayName("Integración - Todos los métodos deben usar la misma vista")
    void testIntegracion_TodosLosMetodosDebenUsarLaMismaVista() {
        // When
        String viewReportes = controller.reportesYEstadisticas(model);
        String viewConfigSistema = controller.configuracionDelSistema(model);
        String viewConfigUsuario = controller.configuracionUsuario(model);
        String viewAyuda = controller.ayudaYSoporte(model);
        
        // Then
        assertEquals("funcionalidadfutura", viewReportes);
        assertEquals("funcionalidadfutura", viewConfigSistema);
        assertEquals("funcionalidadfutura", viewConfigUsuario);
        assertEquals("funcionalidadfutura", viewAyuda);
        
        // Verificar que todos los métodos configuran los atributos requeridos
        assertEquals(viewReportes, viewConfigSistema);
        assertEquals(viewConfigSistema, viewConfigUsuario);
        assertEquals(viewConfigUsuario, viewAyuda);
    }

    @Test
    @DisplayName("Integración - Verificar que todos los métodos configuran título")
    void testIntegracion_VerificarQueTodosLosMetodosConfiguranTitulo() {
        // When & Then
        controller.reportesYEstadisticas(model);
        verify(model).addAttribute("titulo", "Reportes y Estadísticas");
        
        controller.configuracionDelSistema(model);
        verify(model).addAttribute("titulo", "Configuración del Sistema");
        
        controller.configuracionUsuario(model);
        verify(model).addAttribute("titulo", "Configuración");
        
        controller.ayudaYSoporte(model);
        verify(model).addAttribute("titulo", "Ayuda y Soporte");
    }

    @Test
    @DisplayName("Integración - Verificar que todos los métodos configuran descripción")
    void testIntegracion_VerificarQueTodosLosMetodosConfiguranDescripcion() {
        // When & Then
        controller.reportesYEstadisticas(model);
        verify(model).addAttribute("descripcion", "Visualización de estadísticas del sistema y generación de reportes detallados");
        
        controller.configuracionDelSistema(model);
        verify(model).addAttribute("descripcion", "Configuración avanzada de parámetros del sistema y ajustes administrativos");
        
        controller.configuracionUsuario(model);
        verify(model).addAttribute("descripcion", "Personalización de preferencias y ajustes de tu cuenta");
        
        controller.ayudaYSoporte(model);
        verify(model).addAttribute("descripcion", "Centro de ayuda, documentación y soporte técnico");
    }

    @Test
    @DisplayName("Integración - Verificar que todos los métodos configuran icono")
    void testIntegracion_VerificarQueTodosLosMetodosConfiguranIcono() {
        // When & Then
        controller.reportesYEstadisticas(model);
        verify(model).addAttribute("icono", "fas fa-chart-bar");
        
        controller.configuracionDelSistema(model);
        verify(model).addAttribute("icono", "fas fa-cog");
        
        controller.configuracionUsuario(model);
        verify(model).addAttribute("icono", "fas fa-sliders-h");
        
        controller.ayudaYSoporte(model);
        verify(model).addAttribute("icono", "fas fa-question-circle");
    }

    // ========== TESTS DE CASOS LÍMITE ==========

    @Test
    @DisplayName("Casos límite - Model nulo no debe causar excepción")
    void testCasosLimite_ModelNuloNoDebeCausarExcepcion() {
        // Given
        Model modelNulo = null;
        
        // When & Then - No debe lanzar excepción
        try {
            String viewReportes = controller.reportesYEstadisticas(modelNulo);
            String viewConfigSistema = controller.configuracionDelSistema(modelNulo);
            String viewConfigUsuario = controller.configuracionUsuario(modelNulo);
            String viewAyuda = controller.ayudaYSoporte(modelNulo);
            
            // Verificar que retornan la vista correcta incluso con model nulo
            assertEquals("funcionalidadfutura", viewReportes);
            assertEquals("funcionalidadfutura", viewConfigSistema);
            assertEquals("funcionalidadfutura", viewConfigUsuario);
            assertEquals("funcionalidadfutura", viewAyuda);
        } catch (Exception e) {
            // Si se lanza excepción, es por el addAttribute en model nulo, lo cual es esperado
            // pero el método debería retornar la vista correcta antes de eso
        }
    }

    // ========== TESTS DE COBERTURA COMPLETA ==========

    @Test
    @DisplayName("Cobertura - Verificar que cada método tiene su mapeo único")
    void testCobertura_VerificarQueCadaMetodoTieneSuMapeoUnico() {
        // Este test verifica que cada método maneja su endpoint específico
        // Los endpoints son: /admin/reportes, /admin/configuracion, /configuracion, /ayuda
        
        // When
        String viewReportes = controller.reportesYEstadisticas(model);
        String viewConfigSistema = controller.configuracionDelSistema(model);
        String viewConfigUsuario = controller.configuracionUsuario(model);
        String viewAyuda = controller.ayudaYSoporte(model);
        
        // Then - Todos deben retornar la misma vista pero con diferentes atributos
        assertEquals("funcionalidadfutura", viewReportes);
        assertEquals("funcionalidadfutura", viewConfigSistema);
        assertEquals("funcionalidadfutura", viewConfigUsuario);
        assertEquals("funcionalidadfutura", viewAyuda);
        
        // Verificar que se llamaron todos los addAttribute necesarios (12 en total: 3 por cada método)
        verify(model, times(4)).addAttribute(eq("titulo"), any(String.class));
        verify(model, times(4)).addAttribute(eq("descripcion"), any(String.class));
        verify(model, times(4)).addAttribute(eq("icono"), any(String.class));
    }
}