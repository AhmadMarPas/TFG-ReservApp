package es.ubu.reservapp.model.entities;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public EntidadPK<Integer> copia() {
		return new FranjaHoraria(this);
	}
    
	/**
	 * Constructor de copia para crear una nueva franja horaria a partir de otra.
	 * 
	 * @param franjaHoraria Franja horaria a copiar.
	 */
	public FranjaHoraria(FranjaHoraria franjaHoraria) {
		this.setId(franjaHoraria.getId());
		this.setDiaSemana(franjaHoraria.getDiaSemana());
		this.setHoraInicio(franjaHoraria.getHoraInicio());
		this.setHoraFin(franjaHoraria.getHoraFin());
		this.setEstablecimiento(franjaHoraria.getEstablecimiento());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(diaSemana, establecimiento, horaFin, horaInicio);
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FranjaHoraria other = (FranjaHoraria) obj;
		return diaSemana == other.diaSemana && Objects.equals(establecimiento, other.establecimiento)
				&& Objects.equals(horaFin, other.horaFin) && Objects.equals(horaInicio, other.horaInicio);
	}

}
