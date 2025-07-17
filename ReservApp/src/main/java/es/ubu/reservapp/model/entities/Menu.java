package es.ubu.reservapp.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase que representa la entidad Menu.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "menu")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Menu extends EntidadInfo<Integer> {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "id_menu_fk")
	private Integer idPadre;

	@NotNull
	@NotEmpty
	@Size(max = 40)
	@Column(name = "nombre")
	private String nombre;

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Constructor de copia.
	 * 
	 * @param menu Objeto Menu a copiar.
	 */
	public Menu(Menu menu) {
		this.setId(menu.getId());
		this.setIdPadre(menu.getIdPadre());
		this.setNombre(menu.getNombre());
	}

	@Override
	public EntidadPK<Integer> copia() {
		return new Menu(this);
	}

}
