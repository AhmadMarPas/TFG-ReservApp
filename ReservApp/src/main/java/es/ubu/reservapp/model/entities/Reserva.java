package es.ubu.reservapp.model.entities;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase que representa una reserva de la aplicación.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "reserva")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Reserva extends EntidadInfo<Integer> {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_usuario_fk")
	private Usuario usuario;
	
	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_establecimiento_fk")
	private Establecimiento establecimiento;
	
	@NotNull
	@Column(name = "fecha_reserva")
	private LocalDateTime fechaReserva;
	
	@Column(name = "hora_fin")
	private LocalTime horaFin;

    // Relación bidireccional con Convocatoria
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Convocatoria> convocatorias = new HashSet<>();
    
	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public EntidadPK<Integer> copia() {
		return new Reserva(this);
	}

	/**
	 * Constructor de copia.
	 * 
	 * @param reserva Objeto Reserva a copiar.
	 */
	public Reserva(Reserva reserva) {
		this.setId(reserva.getId());
		this.setUsuario(reserva.getUsuario());
		this.setEstablecimiento(reserva.getEstablecimiento());
		this.setFechaReserva(reserva.getFechaReserva());
		this.setHoraFin(reserva.getHoraFin());
		this.setConvocatorias(reserva.getConvocatorias() == null ? new HashSet<>() : new HashSet<>(reserva.getConvocatorias()));
	}

}
