package es.ubu.reservapp.model.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
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
 * Clase que representa la entidad Perfil.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "perfil")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Perfil extends EntidadInfo<Integer> {

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

	@OneToMany
	@JoinTable(name = "perfil_menu", 
		joinColumns = @JoinColumn(name = "id_perfil_pk", referencedColumnName = "id"), 
		inverseJoinColumns = @JoinColumn(name = "id_menu_pk", referencedColumnName = "id"))
	private List<Menu> lstMenus = new ArrayList<>();

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
		return new Perfil(this);
	}

	/**
	 * Constructor de copia.
	 * 
	 * @param perfil Perfil a copiar.
	 */
	public Perfil(Perfil perfil) {
		this.setId(perfil.getId());
		this.setNombre(perfil.getNombre());
		this.setLstMenus(perfil.getLstMenus() == null ? new ArrayList<>() : new ArrayList<>(perfil.getLstMenus()));
	}

}
