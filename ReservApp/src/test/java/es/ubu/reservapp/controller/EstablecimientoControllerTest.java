package es.ubu.reservapp.controller;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.FranjaHoraria;
import es.ubu.reservapp.service.EstablecimientoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test para el controlador EstablecimientoController
 */
@ExtendWith(MockitoExtension.class)
class EstablecimientoControllerTest {

    @Mock
    private EstablecimientoService establecimientoService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private EstablecimientoController establecimientoController;

    private Establecimiento establecimiento;
    private List<Establecimiento> establecimientos;

    @BeforeEach
    void setUp() {
        establecimiento = new Establecimiento();
        establecimiento.setId(1);
        establecimiento.setNombre("Establecimiento Test");
        establecimiento.setDireccion("Dirección Test");
        establecimiento.setCapacidad(10);
        establecimiento.setActivo(true);
        establecimiento.setFranjasHorarias(new ArrayList<>());

        Establecimiento establecimiento2 = new Establecimiento();
        establecimiento2.setId(2);
        establecimiento2.setNombre("Establecimiento Test 2");
        establecimiento2.setDireccion("Dirección Test 2");
        establecimiento2.setCapacidad(20);
        establecimiento2.setActivo(false);
        establecimiento2.setFranjasHorarias(new ArrayList<>());

        establecimientos = new ArrayList<>();
        establecimientos.add(establecimiento);
        establecimientos.add(establecimiento2);
    }

    @Test
    void testListarEstablecimientos() {
        when(establecimientoService.findAll()).thenReturn(establecimientos);
        
        String viewName = establecimientoController.listarEstablecimientos(model);
        
        verify(model).addAttribute("establecimientos", establecimientos);
        verify(model).addAttribute("estActivoCount", 1L);
        verify(model).addAttribute("estCapacidad", 30L);
        
        assertEquals("establecimientos/listado", viewName);
    }

    @Test
    void testMostrarFormularioNuevo() {
        String viewName = establecimientoController.mostrarFormularioNuevo(model);
        
        verify(model).addAttribute(eq("establecimiento"), any(Establecimiento.class));
        verify(model).addAttribute("isEdit", false);
        
        assertEquals("establecimientos/formulario", viewName);
    }

    @Test
    void testMostrarFormularioEditarEstablecimientoNoEncontrado() {
        when(establecimientoService.findById(999)).thenReturn(Optional.empty());
        
        String viewName = establecimientoController.mostrarFormularioEditar(999, model, redirectAttributes);
        
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        
        assertEquals("redirect:/admin/establecimientos/listado", viewName);
    }

    @Test
    void testMostrarFormularioEditarSuccess() {
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        
        String viewName = establecimientoController.mostrarFormularioEditar(1, model, redirectAttributes);
        
        verify(model).addAttribute("establecimiento", establecimiento);
        
        assertEquals("establecimientos/formulario", viewName);
    }

    @Test
    void testActivarEstablecimientoNoEncontrado() {
        when(establecimientoService.findById(999)).thenReturn(Optional.empty());
        
        String viewName = establecimientoController.activarEstablecimiento(999, model, redirectAttributes);
        
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        
        assertEquals("redirect:/admin/establecimientos/listado", viewName);
    }

    @Test
    void testActivarEstablecimientoSuccess() {
        when(establecimientoService.findById(1)).thenReturn(Optional.of(establecimiento));
        
        String viewName = establecimientoController.activarEstablecimiento(1, model, redirectAttributes);
        
        assertEquals(false, establecimiento.isActivo()); // Cambia de true a false
        
        verify(establecimientoService).save(establecimiento);
        
        verify(redirectAttributes).addFlashAttribute(eq("exito"), anyString());
        
        assertEquals("redirect:/admin/establecimientos/listado", viewName);
    }

    @Test
    void testGuardarEstablecimientoWithValidationErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);
        
        String viewName = establecimientoController.guardarEstablecimiento(establecimiento, bindingResult, model, redirectAttributes);
        
        verify(model).addAttribute("isEdit", true);
        verify(model).addAttribute(eq("error"), anyString());
        
        assertEquals("establecimientos/formulario", viewName);
    }

    @Test
    void testGuardarEstablecimientoNuevoSuccess() {
        Establecimiento newEstablecimiento = new Establecimiento();
        newEstablecimiento.setId(null);
        newEstablecimiento.setNombre("Nuevo Establecimiento");
        newEstablecimiento.setDireccion("Nueva Dirección");
        newEstablecimiento.setCapacidad(15);
        newEstablecimiento.setActivo(true);
        
        when(bindingResult.hasErrors()).thenReturn(false);
        
        String viewName = establecimientoController.guardarEstablecimiento(newEstablecimiento, bindingResult, model, redirectAttributes);
        
        assertEquals(new ArrayList<>(), newEstablecimiento.getFranjasHorarias());
        
        verify(establecimientoService).save(newEstablecimiento);
        
        verify(redirectAttributes).addFlashAttribute(eq("exito"), anyString());
        
        assertEquals("redirect:/admin/establecimientos/listado", viewName);
    }

    @Test
    void testGuardarEstablecimientoExistenteSuccess() {
        FranjaHoraria franjaHoraria = new FranjaHoraria();
        franjaHoraria.setId(1);
        franjaHoraria.setDiaSemana(DayOfWeek.MONDAY);
        franjaHoraria.setHoraInicio(LocalTime.parse("09:00"));
        franjaHoraria.setHoraFin(LocalTime.parse("14:00"));
        establecimiento.getFranjasHorarias().add(franjaHoraria);
        
        when(bindingResult.hasErrors()).thenReturn(false);
        
        String viewName = establecimientoController.guardarEstablecimiento(establecimiento, bindingResult, model, redirectAttributes);
        
        for (FranjaHoraria franja : establecimiento.getFranjasHorarias()) {
            assertEquals(establecimiento, franja.getEstablecimiento());
        }
        
        verify(establecimientoService).save(establecimiento);
        
        verify(redirectAttributes).addFlashAttribute(eq("exito"), anyString());
        
        assertEquals("redirect:/admin/establecimientos/listado", viewName);
    }

    @Test
    void testGuardarEstablecimientoWithException() {
        when(bindingResult.hasErrors()).thenReturn(false);
        
        doThrow(new RuntimeException("Error de prueba")).when(establecimientoService).save(any(Establecimiento.class));
        
        String viewName = establecimientoController.guardarEstablecimiento(establecimiento, bindingResult, model, redirectAttributes);
        
        verify(model).addAttribute("isEdit", true);
        verify(model).addAttribute(eq("establecimiento"), eq(establecimiento));
        verify(model).addAttribute(eq("error"), anyString());
        
        assertEquals("establecimientos/formulario", viewName);
    }
}