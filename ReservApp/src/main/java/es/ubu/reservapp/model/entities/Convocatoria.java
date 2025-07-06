package es.ubu.reservapp.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase que representa la convocatoria para una reserva.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "convocatoria")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Convocatoria extends EntidadInfo<ConvocatoriaPK> {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	@EmbeddedId
    private ConvocatoriaPK id;
	
    // Relación Many-to-One con Reserva
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva_pk", insertable = false, updatable = false)
    private Reserva reserva;
    
    // Relación Many-to-One con Usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_pk", insertable = false, updatable = false)
    private Usuario usuario;
	
    @Column(name = "enlace", length = 120)
    private String enlace;
    
	@Size(max = 250)
	@Column(name = "observaciones", columnDefinition = "TEXT")
	private String observaciones;

    @Override
    public ConvocatoriaPK getId() {
    	return id;
    }
    
    @Override
    public void setId(ConvocatoriaPK id) {
    	this.id = id;
		if (id != null) {
			this.reserva = new Reserva();
			this.reserva.setId(id.getIdReserva() == null ? null : id.getIdReserva());
			this.usuario = new Usuario();
			this.usuario.setId(id.getIdUsuario() == null ? null : id.getIdUsuario());
		}
    }
    
    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
        if (reserva != null && id != null) {
            id.setIdReserva(reserva.getId());
        }
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null && id != null) {
            id.setIdUsuario(usuario.getId());
        }
    }
    
    @Override
    public EntidadPK<ConvocatoriaPK> copia() {
    	return new Convocatoria(this);
    }

	/**
	 * Constructor de copia para crear una nueva instancia de la convocatoria
	 * a partir de otra.
	 * 
	 * @param convocatoria	Convocatoria a copiar.
	 */
	public Convocatoria(Convocatoria convocatoria) {
		this.setId(convocatoria.getId());
		this.setEnlace(convocatoria.getEnlace());
		this.setObservaciones(convocatoria.getObservaciones());
		this.setReserva(convocatoria.getReserva());
		this.setUsuario(convocatoria.getUsuario());
	}

}
