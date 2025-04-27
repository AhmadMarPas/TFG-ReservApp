package es.ubu.reservapp.model.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase que representa un usuario de la aplicación.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Usuario extends EntidadInfo<String> implements Serializable {
	
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Size(max = 10, message = "El id no puede tener más de 10 caracteres")
	@Column(name = "id")
	private String id;
	
	@NotNull
	@NotEmpty
	@Size(max = 50, message = "El nombre no puede tener más de 50 caracteres")
	@Column(name = "nombre")
	private String nombre;

	@NotNull
	@NotEmpty
	@Size(max = 50, message = "El apellido no puede tener más de 50 caracteres")
	@Column(name = "apellidos")
	private String apellidos;
	
	@NotNull
	@Email
	@Size(max = 100, message = "El correo no puede tener más de 100 caracteres")
	@Column(name = "correo", unique = true)
	private String correo;

    @NotNull(message = "La contraseña no puede ser nula")
	@NotEmpty
	@Size(min = 5, max = 40, message = "La contraseña debe tener entre 5 y 40 caracteres")
	@Column(name = "password")
	private String password;

	@NotNull(message = "El teléfono no puede ser nulo")
	@NotEmpty
	@Size(min = 5, max = 12, message = "El teléfono debe tener entre 5  y 12 caracteres")
	@Column(name = "telefono")
	private String telefono = "";

	@NotNull
	@Column(name = "administrador")
	private boolean administrador = false;
	
	@Column(name = "fechaUltimoAcceso")
	private LocalDateTime fechaUltimoAcceso;

	@NotNull
	@Column(name = "bloqueado")
	private boolean bloqueado = false;

	@OneToMany
	@JoinTable(name = "usuario_perfil", 
		joinColumns = @JoinColumn(name = "id_usuario_pk", referencedColumnName = "id"), 
		inverseJoinColumns = @JoinColumn(name = "id_perfil_pk", referencedColumnName = "id"))
	private List<Perfil> perfil;

	@ManyToMany
	@JoinTable(name = "usuario_establecimiento", 
		joinColumns = @JoinColumn(name = "id_usuario_pk", referencedColumnName = "id"), 
		inverseJoinColumns = @JoinColumn(name = "id_establecimiento_pk", referencedColumnName = "id"))
	private List<Establecimiento> establecimiento;

    @OneToMany(mappedBy = "usuario")
    private List<Reserva> reserva;

	/**
	 * Función que prepara los datos del correo antes de guardarlos.
	 */
	@PrePersist
	@PreUpdate
	private void prepareData() {
		this.correo = correo == null ? null : correo.toLowerCase();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Usuario other = (Usuario) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	
}
