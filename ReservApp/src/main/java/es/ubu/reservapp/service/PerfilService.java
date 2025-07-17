package es.ubu.reservapp.service;

import java.util.List;
import java.util.Optional;

import es.ubu.reservapp.model.entities.Perfil;

/**
 * Interfaz que representa el servicio de la entidad Perfil.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
public interface PerfilService {

    /**
     * Recupera todos los perfiles.
     * 
     * @return una lista de todos los perfiles.
     */
    List<Perfil> findAll();

    /**
     * Busca un perfil por su ID.
     * 
     * @param id el ID del perfil a buscar.
     * @return un Optional conteniendo el perfil si se encuentra, o un Optional vacío si no.
     */
    Optional<Perfil> findById(Integer id);

    /**
     * Guarda o actualiza un perfil.
     * 
     * @param perfil el perfil a guardar.
     * @return el perfil guardado.
     */
    Perfil save(Perfil perfil);

    /**
     * Elimina un perfil por su ID.
     * 
     * @param id el ID del perfil a eliminar.
     */
    void deleteById(Integer id);
    
    /**
     * Busca un perfil por su nombre.
     * 
     * @param nombre el nombre del perfil a buscar.
     * @return un Optional conteniendo el perfil si se encuentra, o un Optional vacío si no.
     */
    Optional<Perfil> findByNombre(String nombre);
}