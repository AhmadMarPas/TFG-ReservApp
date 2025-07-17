package es.ubu.reservapp.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.service.EstablecimientoService;
import es.ubu.reservapp.service.ReservaService;
import es.ubu.reservapp.util.FechaUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para la gestión de reservas desde la perspectiva del
 * administrador. Permite visualizar un calendario mensual por establecimiento y
 * las reservas diarias.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Controller
@Slf4j
@RequestMapping("/admin/reservas")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminReservaController {

    private final EstablecimientoService establecimientoService;
    private final ReservaService reservaService;

    /**
     * Constructor del controlador que inyecta los servicios necesarios.
     *
     * @param establecimientoService Servicio para gestionar establecimientos.
     * @param reservaRepo Repositorio de reservas.
     */
    public AdminReservaController(EstablecimientoService establecimientoService, ReservaService reservaService) {
        this.establecimientoService = establecimientoService;
        this.reservaService = reservaService;
    }

    /**
     * Muestra la lista de establecimientos para seleccionar y ver su calendario de reservas.
     *
     * @param model Modelo para pasar datos a la vista.
     * @return Vista con la lista de establecimientos.
     */
    @GetMapping
    public String listarEstablecimientos(Model model) {
        List<Establecimiento> establecimientos = establecimientoService.findAll();
        model.addAttribute("establecimientos", establecimientos);
        return "admin/reservas/lista_establecimientos";
    }

    /**
     * Muestra el calendario mensual de reservas para un establecimiento específico.
     *
     * @param establecimientoId ID del establecimiento.
     * @param mes Mes seleccionado (opcional, por defecto el mes actual).
     * @param anio Año seleccionado (opcional, por defecto el año actual).
     * @param model Modelo para pasar datos a la vista.
     * @param redirectAttributes Atributos para redirección.
     * @return Vista del calendario de reservas o redirección en caso de error.
     */
    @GetMapping("/establecimiento/{id}")
    public String mostrarCalendarioReservas(
            @PathVariable("id") Integer establecimientoId,
            @RequestParam(name = "mes", required = false) Integer mes,
            @RequestParam(name = "anio", required = false) Integer anio,
            Model model, 
            RedirectAttributes redirectAttributes) {
        
        Optional<Establecimiento> establecimientoOpt = establecimientoService.findById(establecimientoId);
        if (establecimientoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Establecimiento no encontrado.");
            return "redirect:/admin/reservas";
        }
        
        Establecimiento establecimiento = establecimientoOpt.get();
        model.addAttribute("establecimiento", establecimiento);
        
        LocalDate hoy = LocalDate.now();
        int mesActual = (mes != null) ? mes : hoy.getMonthValue();
        int anioActual = (anio != null) ? anio : hoy.getYear();
        
        YearMonth yearMonth = YearMonth.of(anioActual, mesActual);
        LocalDate primerDiaMes = yearMonth.atDay(1);
        LocalDate ultimoDiaMes = yearMonth.atEndOfMonth();
        
        // Obtener todas las reservas del establecimiento para el mes seleccionado
		List<Reserva> reservasMes = reservaService.findByEstablecimientoAndFechaReservaBetween(establecimiento, primerDiaMes.atStartOfDay(), ultimoDiaMes.plusDays(1).atStartOfDay());

        // Organizar las reservas por día
        Map<Integer, List<Reserva>> reservasPorDia = new HashMap<>();
        for (int dia = 1; dia <= ultimoDiaMes.getDayOfMonth(); dia++) {
            final int diaFinal = dia;
            List<Reserva> reservasDia = reservasMes.stream()
                    .filter(r -> r.getFechaReserva().getDayOfMonth() == diaFinal)
                    .toList();
            reservasPorDia.put(dia, reservasDia);
        }
        
        model.addAttribute("yearMonth", yearMonth);
        model.addAttribute("mesActual", mesActual);
        model.addAttribute("anioActual", anioActual);
        model.addAttribute("diasMes", ultimoDiaMes.getDayOfMonth());
        model.addAttribute("primerDiaSemana", primerDiaMes.getDayOfWeek().getValue());
        model.addAttribute("reservasPorDia", reservasPorDia);
        model.addAttribute("nombreMes", FechaUtil.formatearMes(mesActual));
        
        YearMonth mesPrevio = yearMonth.minusMonths(1);
        YearMonth mesSiguiente = yearMonth.plusMonths(1);
        model.addAttribute("mesPrevio", mesPrevio.getMonthValue());
        model.addAttribute("anioPrevio", mesPrevio.getYear());
        model.addAttribute("mesSiguiente", mesSiguiente.getMonthValue());
        model.addAttribute("anioSiguiente", mesSiguiente.getYear());
        
        return "admin/reservas/calendario_mensual";
    }
    
    /**
     * Muestra el detalle de las reservas para un día específico de un establecimiento.
     *
     * @param establecimientoId ID del establecimiento.
     * @param dia Día seleccionado.
     * @param mes Mes seleccionado.
     * @param anio Año seleccionado.
     * @param model Modelo para pasar datos a la vista.
     * @param redirectAttributes Atributos para redirección.
     * @return Vista con el detalle de reservas del día o redirección en caso de error.
     */
    @GetMapping("/establecimiento/{id}/dia")
    public String mostrarReservasDia(
            @PathVariable("id") Integer establecimientoId,
            @RequestParam("dia") Integer dia,
            @RequestParam("mes") Integer mes,
            @RequestParam("anio") Integer anio,
            Model model, 
            RedirectAttributes redirectAttributes) {
        
        Optional<Establecimiento> establecimientoOpt = establecimientoService.findById(establecimientoId);
        if (establecimientoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Establecimiento no encontrado.");
            return "redirect:/admin/reservas";
        }
        
        Establecimiento establecimiento = establecimientoOpt.get();
        model.addAttribute("establecimiento", establecimiento);
        
        LocalDate fechaSeleccionada = LocalDate.of(anio, mes, dia);
        model.addAttribute("fechaSeleccionada", fechaSeleccionada);
        
        // Obtener las reservas para el día seleccionado
        LocalDate diaSiguiente = fechaSeleccionada.plusDays(1);
		List<Reserva> reservasDia = reservaService.findByEstablecimientoAndFechaReservaBetween(establecimiento, fechaSeleccionada.atStartOfDay(), diaSiguiente.atStartOfDay());

        model.addAttribute("reservas", reservasDia);
        model.addAttribute("nombreDia", FechaUtil.formatearDiaSemana(fechaSeleccionada.getDayOfWeek()));
        model.addAttribute("nombreMes", FechaUtil.formatearMes(mes));
        
        return "admin/reservas/detalle_dia";
    }
}