package es.ubu.reservapp;

import java.time.LocalDateTime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.service.UsuarioService;
import lombok.extern.slf4j.Slf4j;

/**
 * Clase principal de la aplicación.
 * <p>
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
//@Configuration
//@ComponentScan("es.ubu.*")
//@EnableJpaRepositories(basePackages = {"es.ubu.*"})
//@EntityScan("es.ubu.*")
//@EnableAutoConfiguration
@Slf4j
@SpringBootApplication
public class ReservApplication {

	/**
	 * Servicio de la entidad Usuario.
	 */
	private final UsuarioService usrService;
	
	/**
	 * Constructor de la clase.
	 * 
	 * @param usuarioService Servicio de la entidad Usuario.
	 */
	public ReservApplication(UsuarioService usuarioService) {
		this.usrService = usuarioService;
	}
	
	/**
	 * Método principal de la aplicación.
	 * 
	 * @param args Argumentos de la línea de comandos.
	 */
	public static void main(String[] args) {
		SpringApplication.run(ReservApplication.class, args);
		log.info("Inicializando base de datos...");
	}

	public void inicializaBaseDatos() {
		Usuario admin = new Usuario();
		admin.setNombre("admin");
		admin.setApellidos("adminst.");
		admin.setPassword("admin");
		admin.setAdministrador(true);
		admin.setCorreo("admin@admin.es");
		admin.setFechaCreaReg(LocalDateTime.now());
		admin.setUsuarioCreaReg("init");
		usrService.save(admin);
	}

}
