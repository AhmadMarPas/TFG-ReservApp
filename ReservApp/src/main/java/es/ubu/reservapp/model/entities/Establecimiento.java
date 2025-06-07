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
	
	/**
	 * Lista de reservas que tiene el establecimiento.
	 */
	@OneToMany(mappedBy = "establecimiento", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Reserva> lstReservas;

    /**
     * Lista de franjas horarias de apertura del establecimiento.
     */
    @OneToMany(mappedBy = "establecimiento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FranjaHoraria> franjasHorarias = new ArrayList<>();


	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

}
