package es.ubu.reservapp.model.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

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
     * valido
     */
//	@NotNull(message = "El campo 'valido' no puede ser nulo")
//    @Column(name = "VALIDO")
//    private Boolean valido = true;

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

    public String toString() {
        return getClass().getSimpleName() + "[id=" + getId() + "]";
    }

}
