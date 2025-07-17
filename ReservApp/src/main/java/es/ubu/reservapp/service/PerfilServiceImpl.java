package es.ubu.reservapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ubu.reservapp.model.entities.Perfil;
import es.ubu.reservapp.model.repositories.PerfilRepo;

/**
 * Clase que implementa el servicio de la entidad Perfil.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Service
public class PerfilServiceImpl implements PerfilService {

	/**
	 * Repositorio para la entidad Perfil.	
	 */
    private final PerfilRepo perfilRepo;

    /**
	 * Constructor del servicio que inyecta el repositorio de perfiles.
	 * 
	 * @param perfilRepo el repositorio para gestionar perfiles.
     */
    public PerfilServiceImpl(PerfilRepo perfilRepo) {
        this.perfilRepo = perfilRepo;
    }

	/**
	 * Método para obtener el nombre del servicio.
	 * 
	 * @return el nombre del servicio.
	 */
    @Override
    @Transactional(readOnly = true)
    public List<Perfil> findAll() {
        return perfilRepo.findAll();
    }

	/**
	 * Método para buscar un perfil por su ID.
	 * 
	 * @param id el ID del perfil a buscar.
	 * @return un Optional que contiene el perfil si se encuentra, o vacío si no.
	 */
    @Override
    @Transactional(readOnly = true)
    public Optional<Perfil> findById(Integer id) {
        return perfilRepo.findById(id);
    }

	/**
	 * Método para guardar un perfil.
	 * 
	 * @param perfil el perfil a guardar.
	 * @return el perfil guardado.
	 */
    @Override
    @Transactional
    public Perfil save(Perfil perfil) {
         if (perfil.getId() == null && perfilRepo.findByNombre(perfil.getNombre()).isPresent()) {
             throw new IllegalArgumentException("Ya existe un perfil con el nombre: " + perfil.getNombre());
         }
        return perfilRepo.save(perfil);
    }

	/**
	 * Método para eliminar un perfil por su ID.
	 * 
	 * @param id el ID del perfil a eliminar.
	 */
    @Override
    @Transactional
    public void deleteById(Integer id) {
        // TODO: Verificar si el perfil existe antes de eliminarlo
        perfilRepo.deleteById(id);
    }
    
	/**
	 * * Método para buscar un perfil por su nombre.
	 * 
	 * @param nombre el nombre del perfil a buscar.
	 * @return un Optional que contiene el perfil si se encuentra, o vacío si no.
	 */
    @Override
    @Transactional(readOnly = true)
    public Optional<Perfil> findByNombre(String nombre) {
        return perfilRepo.findByNombre(nombre); 
    }
}