package es.ubu.reservapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de formato de fecha y hora para la aplicación web.
 * 
 * Esta clase configura los formateadores de fecha y hora para que se utilicen
 * en los controladores y vistas de la aplicación, asegurando que los formatos
 * sean consistentes con los requerimientos del frontend.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Configuration
@EnableAsync
public class WebConfig implements WebMvcConfigurer {

	private static final String CLASSPATH_STATIC = "classpath:/static/";
	
    @Override
    public void addFormatters(@NonNull FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        // Asegura que LocalTime se formatee como HH:mm para los inputs type="time"
        registrar.setTimeFormatter(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        // Formateadores para LocalDate y LocalDateTime globalmente:
        registrar.setDateFormatter(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        registrar.setDateTimeFormatter(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        registrar.registerFormatters(registry);
    }
    
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Configuración para servir recursos estáticos
        registry.addResourceHandler("/css/**").addResourceLocations(CLASSPATH_STATIC + "css/");
        
        registry.addResourceHandler("/js/**").addResourceLocations(CLASSPATH_STATIC + "js/");
        
        registry.addResourceHandler("/images/**").addResourceLocations(CLASSPATH_STATIC + "images/");
        
        // Configuración específica para favicon y otros iconos
        registry.addResourceHandler("/favicon.ico").addResourceLocations(CLASSPATH_STATIC);
        
        registry.addResourceHandler("/*.png").addResourceLocations(CLASSPATH_STATIC);
        
        registry.addResourceHandler("/*.ico").addResourceLocations(CLASSPATH_STATIC);
    }
}
