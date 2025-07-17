package es.ubu.reservapp.model.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
@EntityListeners({AuditingEntityListener.class, EntidadInfoInterceptor.class})
@SoftDelete(strategy = SoftDeleteType.ACTIVE, columnName = "valido")
public abstract class EntidadInfo<E extends Serializable> extends EntidadPK<E> {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * orden
     */
    @Column(name = "orden")
    private Integer orden;
    
    /**
     * Usuario que creó el registro
     */
    @CreatedBy
    @Column(name = "id_usuario_creacion_fk")
    private String usuarioCreaReg;
    
    /**
     * Fecha de creación
     */
    @CreatedDate
    @Column(name = "tst_creacion")
    private LocalDateTime fechaCreaReg;

    /**
     * Usuario que modificó el registro
     */
    @LastModifiedBy
    @Column(name = "id_usuario_modificacion_fk")
    private String usuarioModReg;

    /**
     * Fecha de última modificación
     */
    @LastModifiedDate
    @Column(name = "tst_modificacion")
    private LocalDateTime fechaModReg;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + getId() + "]";
    }

}
