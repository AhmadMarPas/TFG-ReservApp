package es.ubu.reservapp.model.entities;

import java.time.LocalDateTime;

import es.ubu.reservapp.model.shared.SessionData;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

/**
 * Clase EntidadInfoInterceptor con los métodos guardar y actualizar.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
public class EntidadInfoInterceptor {

	/**
     * sessionData
     */
	private SessionData sessionData;

	public EntidadInfoInterceptor(SessionData sessionData) {
		this.sessionData = sessionData;
	}

	/**
     * guardar
     * <p>
     * Pone la fecha de creación y el id de usuario quien creó el registro.
     *
     * @param entidadInfo
     */
    @PrePersist
	public void guardar(EntidadInfo<?> entidadInfo) {
		entidadInfo.setFechaCreaReg(LocalDateTime.now());
		entidadInfo.setUsuarioCreaReg(getSessionData().getUsuario().getId());
	}

	/**
	 * actualizar
	 * <p>
	 * Pone la fecha de modificación y el id de usuario quien modificó el registro
	 *
	 * @param entidadInfo
	 */
	@PreUpdate
	public void actualizar(EntidadInfo<?> entidadInfo) {
		entidadInfo.setFechaModReg(LocalDateTime.now());
		if (entidadInfo.getFechaCreaReg() == null) {
			entidadInfo.setFechaCreaReg(LocalDateTime.now());
		}
		entidadInfo.setUsuarioModReg(getSessionData().getUsuario().getId());
		if (entidadInfo.getUsuarioCreaReg() == null) {
			entidadInfo.setUsuarioCreaReg(getSessionData().getUsuario().getId());
		}
	}
	
	/**
	 * getSessionData
	 * 
	 * @return the sessionData
	 */
	private SessionData getSessionData() {
		if (sessionData == null) {
			sessionData = CDI.current().select(SessionData.class).get();
		}
		return sessionData;
	}

}
