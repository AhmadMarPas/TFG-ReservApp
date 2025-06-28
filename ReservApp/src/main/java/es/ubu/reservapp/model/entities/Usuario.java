package es.ubu.reservapp.model.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
import jakarta.validation.constraints.Pattern;
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
	@NotNull(message = "El nombre de usuario no puede ser nulo")
	@NotEmpty(message = "El nombre de usuario no puede estar vacío")
	@Pattern(regexp = "^[a-zA-Z0-9]{3,10}$", message = "El Id de usuario debe tener entre 3 y 10 caracteres alfanuméricos, sin espacios u otros símbolos.")
	@Column(name = "id", length = 10)
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
	@NotEmpty(message = "La contraseña no puede estar vacía")
//	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,40}$", message = "La contraseña debe tener entre 8 y 40 caracteres, incluir al menos una mayúscula, una minúscula, un número y solo caracteres alfanuméricos.")
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

    @Column(name = "confirmation_token")
    private String confirmationToken;

    @Column(name = "email_verified")
    private boolean emailVerified = false;

	@OneToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "usuario_perfil", 
		joinColumns = @JoinColumn(name = "id_usuario_pk", referencedColumnName = "id"), 
		inverseJoinColumns = @JoinColumn(name = "id_perfil_pk", referencedColumnName = "id"))
	private List<Perfil> lstPerfiles = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "usuario_establecimiento", 
		joinColumns = @JoinColumn(name = "id_usuario_pk", referencedColumnName = "id"), 
		inverseJoinColumns = @JoinColumn(name = "id_establecimiento_pk", referencedColumnName = "id"))
	private List<Establecimiento> lstEstablecimientos = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<Reserva> lstReservas = new ArrayList<>();;

	/**
	 * Función que prepara los datos del correo antes de guardarlos.
	 */
	@PrePersist
	@PreUpdate
	private void prepareData() {
		this.correo = correo.toLowerCase();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(id);
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

	@Override
	public EntidadPK<String> copia() {
		return new Usuario(this);
	}

	public Usuario(Usuario usuario) {
		this.setId(usuario.getId());
		this.setPassword(usuario.getPassword());
		this.setNombre(usuario.getNombre());
		this.setApellidos(usuario.getApellidos());
		this.setCorreo(usuario.getCorreo());
		this.setTelefono(usuario.getTelefono());
		this.setAdministrador(usuario.isAdministrador());
		this.setBloqueado(usuario.isBloqueado());
		this.setFechaUltimoAcceso(usuario.getFechaUltimoAcceso());
		this.setConfirmationToken(usuario.getConfirmationToken());
		this.setEmailVerified(usuario.isEmailVerified());
		this.setLstEstablecimientos(usuario.getLstEstablecimientos() == null ? new ArrayList<>() : new ArrayList<>(usuario.getLstEstablecimientos()));
		this.setLstPerfiles(usuario.getLstPerfiles() == null ? new ArrayList<>() : new ArrayList<>(usuario.getLstPerfiles()));
		this.setLstReservas(usuario.getLstReservas() == null ? new ArrayList<>() : new ArrayList<>(usuario.getLstReservas()));
	}
	
}
