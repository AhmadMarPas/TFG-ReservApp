package es.ubu.reservapp.model.entities;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase que representa la clave primaria compuesta de la entidad Convocado.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ConvocadoPK implements Serializable {

    /**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "id_reserva_pk")
    private Integer idReserva;
    
    @Column(name = "id_usuario_pk", length = 10)
    private String idUsuario;

	@Override
	public int hashCode() {
		return Objects.hash(idReserva, idUsuario);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConvocadoPK other = (ConvocadoPK) obj;
		return Objects.equals(idReserva, other.idReserva) && Objects.equals(idUsuario, other.idUsuario);
	}

	@Override
	public String toString() {
		return "ConvocadoPK [idReserva=" + idReserva + ", idUsuario=" + idUsuario + "]";
	}
    
}
