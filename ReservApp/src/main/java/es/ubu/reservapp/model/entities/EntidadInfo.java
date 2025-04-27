package es.ubu.reservapp.model.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
@ToString
@RequiredArgsConstructor
@MappedSuperclass
@EntityListeners(value = EntidadInfoInterceptor.class)
public abstract class EntidadInfo<E extends Serializable> extends EntidadPK<E> {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * valido
     */
    @Column(name = "VALIDO")
    private Boolean valido = true;

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

    /**
     * orden
     */
    @Column(name = "ORDEN")
    private Integer orden;

}
