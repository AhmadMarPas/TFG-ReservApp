package es.ubu.reservapp.model.entities;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase que representa una franja horaria de un establecimiento.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "franja_horaria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FranjaHoraria extends EntidadInfo<Integer> {

    /**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "El día de la semana no puede ser nulo")
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false)
    private DayOfWeek diaSemana;

    @NotNull(message = "La hora de inicio no puede ser nula")
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin no puede ser nula")
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @NotNull(message = "El establecimiento no puede ser nulo")
    @ManyToOne()
    @JoinColumn(name = "id_establecimiento_fk", nullable = false)
    private Establecimiento establecimiento;

    @AssertTrue(message = "La hora de fin debe ser posterior a la hora de inicio")
    private boolean isHoraFinAfterHoraInicio() {
        if (horaInicio == null || horaFin == null) {
            return true;
        }
        return horaFin.isAfter(horaInicio);
    }
    
    /**
     * Constructor para crear una franja horaria.
     * 
     * @param diaSemana
     * @param horaInicio
     * @param horaFin
     * @param establecimiento
     */
    public FranjaHoraria(DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFin, Establecimiento establecimiento) {
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.establecimiento = establecimiento;
    }

	@Override
	public void setId(Integer id) {
		this.id = id;
		
	}

}
