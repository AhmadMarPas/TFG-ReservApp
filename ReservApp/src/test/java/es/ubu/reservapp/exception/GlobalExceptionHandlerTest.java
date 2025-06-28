package es.ubu.reservapp.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test para la clase GlobalExceptionHandler
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private Model model;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleUserNotFoundException() {
        // Arrange
        UserNotFoundException exception = new UserNotFoundException("Usuario no encontrado");

        // Act
        String result = globalExceptionHandler.handleUserNotFoundException(exception, model);

        // Assert
        assertEquals("error", result);
        verify(model).addAttribute("error", "Usuario no encontrado: Usuario no encontrado");
    }

    @Test
    void testHandleValidationExceptions() {
        // Arrange
        FieldError fieldError1 = new FieldError("objectName", "field1", "Error message 1");
        FieldError fieldError2 = new FieldError("objectName", "field2", "Error message 2");
        List<FieldError> fieldErrors = List.of(fieldError1, fieldError2);

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // Act
        Map<String, String> result = globalExceptionHandler.handleValidationExceptions(methodArgumentNotValidException);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Error message 1", result.get("field1"));
        assertEquals("Error message 2", result.get("field2"));
    }

    @Test
    void testHandleValidationExceptions_EmptyErrors() {
        // Arrange
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        // Act
        Map<String, String> result = globalExceptionHandler.handleValidationExceptions(methodArgumentNotValidException);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testHandleIllegalArgumentException() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Argumento inválido");
        when(request.getRequestURI()).thenReturn("/test-uri");

        // Act
        String result = globalExceptionHandler.handleIllegalArgumentException(exception, request, model);

        // Assert
        assertEquals("error", result);
        verify(model).addAttribute("error", "Datos inválidos: Argumento inválido");
    }

    @Test
    void testHandleRuntimeException_Success() throws Exception {
        // Arrange
        RuntimeException exception = new RuntimeException("Error de runtime");
        when(request.getRequestURI()).thenReturn("/test-uri");

        // Act
        ModelAndView result = globalExceptionHandler.handleRuntimeException(exception, request, response);

        // Assert
        assertNotNull(result);
        assertEquals("error", result.getViewName());
        assertEquals("Ha ocurrido un error interno. Por favor, inténtelo de nuevo.", result.getModel().get("error"));
        assertEquals("/test-uri", result.getModel().get("path"));
        assertNotNull(result.getModel().get("timestamp"));
        
        verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(response).setContentType("text/html;charset=UTF-8");
    }

    @Test
    void testHandleRuntimeException_WithResponseException() throws Exception {
        // Arrange
        RuntimeException exception = new RuntimeException("Error de runtime");
        when(request.getRequestURI()).thenReturn("/test-uri");
        doThrow(new RuntimeException("Response error")).when(response).setStatus(anyInt());

        // Act
        ModelAndView result = globalExceptionHandler.handleRuntimeException(exception, request, response);

        // Assert
        assertNotNull(result);
        assertEquals("error", result.getViewName());
    }

    @Test
    void testHandleMethodNotSupported() {
        // Arrange
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("POST");

        // Act
        String result = globalExceptionHandler.handleMethodNotSupported(exception, model);

        // Assert
        assertEquals("error", result);
        verify(model).addAttribute("error", "Método HTTP no permitido: POST");
    }

    @Test
    void testHandleDataAccessException_Success() throws Exception {
        // Arrange
        DataAccessException exception = new DataAccessException("Error de base de datos") {
            private static final long serialVersionUID = 1L;
        };

        // Act
        String result = globalExceptionHandler.handleDataAccessException(exception, model, response);

        // Assert
        assertEquals("error", result);
        verify(model).addAttribute("error", "Error de base de datos. Por favor, inténtelo más tarde.");
        verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(response).setContentType("text/html;charset=UTF-8");
    }

    @Test
    void testHandleDataAccessException_WithResponseException() throws Exception {
        // Arrange
        DataAccessException exception = new DataAccessException("Error de base de datos") {
            private static final long serialVersionUID = 1L;
        };
        doThrow(new RuntimeException("Response error")).when(response).setStatus(anyInt());

        // Act
        String result = globalExceptionHandler.handleDataAccessException(exception, model, response);

        // Assert
        assertEquals("error", result);
    }

    @Test
    void testUserNotFoundException_WithMessage() {
        // Arrange
        String message = "Usuario con ID 123 no encontrado";
        UserNotFoundException exception = new UserNotFoundException(message);

        // Act & Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testUserNotFoundException_WithMessageAndCause() {
        // Arrange
        String message = "Usuario no encontrado";
        Throwable cause = new RuntimeException("Causa raíz");
        UserNotFoundException exception = new UserNotFoundException(message, cause);

        // Act & Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testUserNotFoundException_SerialVersionUID() {
        // Arrange
        UserNotFoundException exception = new UserNotFoundException("Test");

        // Act & Assert
        assertNotNull(exception);
        assertTrue(exception instanceof Exception);
    }

    @Test
    void testGlobalExceptionHandler_ErrorConstant() {
        // Test que verifica que la constante ERROR se usa correctamente
        UserNotFoundException exception = new UserNotFoundException("Test");
        
        String result = globalExceptionHandler.handleUserNotFoundException(exception, model);
        
        assertEquals("error", result);
        verify(model).addAttribute(eq("error"), anyString());
    }

    @Test
    void testHandleRuntimeException_TimestampIsRecent() {
        // Arrange
        RuntimeException exception = new RuntimeException("Error de runtime");
        when(request.getRequestURI()).thenReturn("/test-uri");
        long beforeCall = System.currentTimeMillis();

        // Act
        ModelAndView result = globalExceptionHandler.handleRuntimeException(exception, request, response);
        long afterCall = System.currentTimeMillis();

        // Assert
        Long timestamp = (Long) result.getModel().get("timestamp");
        assertNotNull(timestamp);
        assertTrue(timestamp >= beforeCall && timestamp <= afterCall);
    }
}