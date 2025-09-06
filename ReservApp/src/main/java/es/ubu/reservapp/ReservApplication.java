package es.ubu.reservapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

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
@ComponentScan(basePackages = {"es.ubu.reservapp", "es.ubu.config"})
public class ReservApplication {

	/**
	 * Método principal de la aplicación.
	 * 
	 * @param args Argumentos de la línea de comandos.
	 */
	public static void main(String[] args) {
		SpringApplication.run(ReservApplication.class, args);
		log.info("Inicializando base de datos...");
		log.info("Aplicación iniciada.");
	}

}
