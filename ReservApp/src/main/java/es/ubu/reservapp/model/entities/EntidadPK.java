package es.ubu.reservapp.model.entities;

import java.io.Serializable;

import es.ubu.reservapp.model.EntidadID;

/**
 * Class abstracta EntidadPK Extiende de Serializable e implementa a
 * Serializable, Clonable y EntidadID<E>
 * <p>
 * @author Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
public abstract class EntidadPK<E extends Serializable> implements Serializable, EntidadID<E> {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * hashCode
     *
     * @return int
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (getId() == null ? 0 : getId().hashCode());
        return result;
    }

    /**
     * equals
     *
     * @param obj
     * @return boolean
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        EntidadPK<E> other = (EntidadPK<E>) obj;
        if (getId() == null) {
            if (other.getId() != null) {
                return false;
            }
        } else if (!getId().equals(other.getId())) {
            return false;
        }
        return true;
    }

    /**
     * toString
     *
     * @return String
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getId() + "]";
    }

    /**
     * Crea y devuelve una copia de esta entidad PK.
     * Cada subclase debe implementar este método.
     *
     * @return una nueva instancia copia de esta entidad PK
     */
    public abstract EntidadPK<E> copia();

}
