package es.ubu.reservapp.model.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Clase EntidadInfo con los atributos comunes de la entidad.
 * <p>
 * Extiende de EntidadPK<E>
 *
 * @param <E>
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@RequiredArgsConstructor
@MappedSuperclass
@EntityListeners(value = EntidadInfoInterceptor.class)
@SQLDelete(sql = "UPDATE #{#entityName} SET valido = false WHERE id = ?")
@SoftDelete(strategy = SoftDeleteType.ACTIVE, columnName = "valido")
public abstract class EntidadInfo<E extends Serializable> extends EntidadPK<E> {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * orden
     */
    @Column(name = "ORDEN")
    private Integer orden;

    /**
     * usuarioModReg
     */
    @Column(name = "ID_USUARIO_MODIFICACION_FK")
    private String usuarioModReg;

    /**
     * usuarioCreaReg
     */
    @Column(name = "ID_USUARIO_CREACION_FK")
    private String usuarioCreaReg;

    /**
     * fechaModReg
     */
    @Column(name = "TST_MODIFICACION")
    private LocalDateTime fechaModReg;

    /**
     * fechaCreaReg
     */
    @Column(name = "TST_CREACION")
    private LocalDateTime fechaCreaReg;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + getId() + "]";
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(fechaCreaReg, fechaModReg, orden, usuarioCreaReg, usuarioModReg);
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
		EntidadInfo other = (EntidadInfo) obj;
		return Objects.equals(fechaCreaReg, other.fechaCreaReg) && Objects.equals(fechaModReg, other.fechaModReg)
				&& Objects.equals(orden, other.orden) && Objects.equals(usuarioCreaReg, other.usuarioCreaReg)
				&& Objects.equals(usuarioModReg, other.usuarioModReg);
	}

}
