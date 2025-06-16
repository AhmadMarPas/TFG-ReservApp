package es.ubu.reservapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Manejador global de excepciones para la aplicación. Captura y maneja
 * diferentes tipos de excepciones lanzadas por los controladores.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleUserNotFoundException(UserNotFoundException ex, Model model) {
        log.error("Usuario no encontrado: {}", ex.getMessage());
        model.addAttribute("error", "Usuario no encontrado: " + ex.getMessage());
        return "error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException ex, 
                                                HttpServletRequest request,
                                                Model model) {
    	log.error("Argumento inválido en {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("error", "Datos inválidos: " + ex.getMessage());
        return "error";
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleRuntimeException(RuntimeException ex, 
                                             HttpServletRequest request,
                                             HttpServletResponse response) {
    	log.error("Error de runtime en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        
        ModelAndView mav = new ModelAndView();
        
        // Asegurar que la respuesta se complete correctamente
        try {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType("text/html;charset=UTF-8");
            
            mav.addObject("error", "Ha ocurrido un error interno. Por favor, inténtelo de nuevo.");
            mav.addObject("timestamp", System.currentTimeMillis());
            mav.addObject("path", request.getRequestURI());
            mav.setViewName("error");
            
            return mav;
        } catch (Exception e) {
        	log.error("Error adicional al manejar excepción: {}", e.getMessage(), e);
            mav.setViewName("error");
            return mav;
        }
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, 
                                       HttpServletRequest request,
                                       Model model,
                                       HttpServletResponse response) {
    	log.error("Error genérico en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        
        try {
            // Asegurar respuesta completa
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType("text/html;charset=UTF-8");
            
            model.addAttribute("error", "Ha ocurrido un error inesperado. Por favor, contacte al administrador.");
            model.addAttribute("timestamp", System.currentTimeMillis());
            model.addAttribute("path", request.getRequestURI());
            
            return "error";
        } catch (Exception e) {
        	log.error("Error crítico al manejar excepción: {}", e.getMessage(), e);
            return "error";
        }
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public String handleMethodNotSupported(org.springframework.web.HttpRequestMethodNotSupportedException ex,
                                         Model model) {
    	log.error("Método HTTP no soportado: {}", ex.getMessage());
        model.addAttribute("error", "Método HTTP no permitido: " + ex.getMethod());
        return "error";
    }

    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleDataAccessException(org.springframework.dao.DataAccessException ex,
                                          Model model,
                                          HttpServletResponse response) {
    	log.error("Error de acceso a datos: {}", ex.getMessage(), ex);
        
        try {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType("text/html;charset=UTF-8");
            
            model.addAttribute("error", "Error de base de datos. Por favor, inténtelo más tarde.");
            return "error";
        } catch (Exception e) {
        	log.error("Error al manejar excepción de base de datos: {}", e.getMessage(), e);
            return "error";
        }
    }
}