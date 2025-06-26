package es.ubu.reservapp.model.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase que representa un establecimiento de la aplicación.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "establecimiento")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Establecimiento extends EntidadInfo<Integer> {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@NotNull
	@NotEmpty
	@Size(max = 40)
	@Column(name = "nombre")
	private String nombre;

	@NotNull
	@NotEmpty
	@Size(max = 250)
	@Column(name = "descripcion")
	private String descripcion;

	@NotNull
	@Column(name = "aforo")
	private Integer aforo;

	@Column(name = "duracion_reserva")
	private Integer duracionReserva;

	@Column(name = "descanso_servicios")
	private Integer descansoServicios;

	@NotNull
	@Column(name = "capacidad")
	private Integer capacidad;

	@Size(max = 80)
	@Column(name = "tipo")
	private String tipo;

	@Size(max = 250)
	@Column(name = "direccion")
	private String direccion;

	@Size(max = 20)
	@Column(name = "telefono")
	private String telefono;

	@Size(max = 100)
	@Column(name = "email")
	private String email;

	@NotNull(message = "El campo 'activo' no puede ser nulo")
	@Column(name = "activo")
	private boolean activo = true;

	/**
	 * Lista de reservas que tiene el establecimiento.
	 */
	@OneToMany(mappedBy = "establecimiento", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Reserva> lstReservas;

    /**
     * Lista de franjas horarias de apertura del establecimiento.
     */
    @OneToMany(mappedBy = "establecimiento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<FranjaHoraria> franjasHorarias = new ArrayList<>();

	/**
	 * Constructor de copia para crear una nueva instancia de Establecimiento a
	 * partir de otro.
	 * 
	 * @param establecimiento	Establecimiento a copiar.
	 */
	public Establecimiento(Establecimiento establecimiento) {
		this.setId(establecimiento.getId());
		this.setNombre(establecimiento.getNombre());
		this.setDescripcion(establecimiento.getDescripcion());
		this.setAforo(establecimiento.getAforo());
		this.setDuracionReserva(establecimiento.getDuracionReserva());
		this.setDescansoServicios(establecimiento.getDescansoServicios());
		this.setCapacidad(establecimiento.getCapacidad());
		this.setTipo(establecimiento.getTipo());
		this.setDireccion(establecimiento.getDireccion());
		this.setTelefono(establecimiento.getTelefono());
		this.setEmail(establecimiento.getEmail());
		this.setActivo(establecimiento.isActivo());
		this.setLstReservas(new ArrayList<>(establecimiento.getLstReservas()));
		this.setFranjasHorarias(new ArrayList<>(establecimiento.getFranjasHorarias()));
	}

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
		return new Establecimiento(this);
	}

}
