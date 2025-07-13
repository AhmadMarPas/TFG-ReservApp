package es.ubu.reservapp.model.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "convocatoria", indexes = {
	    @Index(name = "idx_convocatoria_reserva", columnList = "id_reserva_pk")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Convocatoria extends EntidadInfo<Integer> {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@JoinColumn(name = "id_reserva_pk", insertable = false, updatable = false)
    private Integer id;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_reserva_pk", insertable = false, updatable = false)
//	@JoinColumn(name = "id_reserva_pk")
	@NotNull
	private Reserva reserva;
    
    // Relación Many-to-One con Usuario
    @OneToMany(mappedBy = "convocatoria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Convocado> convocados;
	
    @Column(name = "enlace", length = 120)
    private String enlace;
    
	@Size(max = 250)
	@Column(name = "observaciones", columnDefinition = "TEXT")
	private String observaciones;

	@Override
    public Integer getId() {
    	return id;
    }
    @Override
    public void setId(Integer id) {
    	this.id = id;
    }
    
    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
        if (reserva != null) {
            this.setId(reserva.getId());
        }
    }
    
    @Override
    public EntidadPK<Integer> copia() {
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
		this.setReserva(convocatoria.getReserva());
		this.setConvocados(convocatoria.getConvocados() == null ? new ArrayList<>() : new ArrayList<>(convocatoria.getConvocados()));
		this.setEnlace(convocatoria.getEnlace());
		this.setObservaciones(convocatoria.getObservaciones());
	}

}
