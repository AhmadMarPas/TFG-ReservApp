package es.ubu.reservapp.model.entities;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "convocado")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Convocado extends EntidadInfo<ConvocadoPK> {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	@EmbeddedId
    private ConvocadoPK id;
	
    // Relación con Convocatoria (N:1)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_reserva_pk", insertable=false, updatable=false)
    private Convocatoria convocatoria;
    
    // Relación con Usuario (N:1)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario_pk", insertable=false, updatable=false)
    private Usuario usuario;
    
	@Override
    public ConvocadoPK getId() {
    	return id;
    }
    
    @Override
    public void setId(ConvocadoPK id) {
    	this.id = id;
    }
    
    public void setConvocatoria(Convocatoria convocatoria) {
        this.convocatoria = convocatoria;
        if (convocatoria != null && id != null) {
            id.setIdReserva(convocatoria.getReserva().getId());
        }
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null && id != null) {
            id.setIdUsuario(usuario.getId());
        }
    }
    
    @Override
    public EntidadPK<ConvocadoPK> copia() {
    	return new Convocado(this);
    }

	/**
	 * Constructor de copia para crear una nueva instancia de la convocatoria
	 * a partir de otra.
	 * 
	 * @param convocado	Convocatoria a copiar.
	 */
	public Convocado(Convocado convocado) {
		this.setId(convocado.getId());
		this.setConvocatoria(convocado.getConvocatoria());
		this.setUsuario(convocado.getUsuario());
	}

}
