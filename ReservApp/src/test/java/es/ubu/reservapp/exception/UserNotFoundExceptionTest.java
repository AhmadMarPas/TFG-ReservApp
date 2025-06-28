package es.ubu.reservapp.exception;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test para la clase UserNotFoundException
 */
class UserNotFoundExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Usuario no encontrado";

        UserNotFoundException exception = new UserNotFoundException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof Exception);
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Usuario no encontrado";
        Throwable cause = new RuntimeException("Causa raíz");

        UserNotFoundException exception = new UserNotFoundException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertTrue(exception instanceof Exception);
    }

    @Test
    void testConstructorWithNullMessage() {
        UserNotFoundException exception = new UserNotFoundException(null);

        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithNullMessageAndCause() {
        UserNotFoundException exception = new UserNotFoundException(null, null);

        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithEmptyMessage() {
        String message = "";

        UserNotFoundException exception = new UserNotFoundException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithEmptyMessageAndCause() {
        String message = "";
        Throwable cause = new IllegalArgumentException("Argumento inválido");

        UserNotFoundException exception = new UserNotFoundException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testSerialVersionUID() {
        UserNotFoundException exception = new UserNotFoundException("Test");

        // Verificar que la excepción es serializable
        assertDoesNotThrow(() -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(exception);
            oos.close();
        });
    }

    @Test
    void testSerialization() throws IOException, ClassNotFoundException {
        String originalMessage = "Usuario con ID 123 no encontrado";
        Throwable originalCause = new RuntimeException("Error de conexión");
        UserNotFoundException originalException = new UserNotFoundException(originalMessage, originalCause);

        // Serializar
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalException);
        oos.close();

        // Deserializar
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        UserNotFoundException deserializedException = (UserNotFoundException) ois.readObject();
        ois.close();

        // Assert
        assertEquals(originalMessage, deserializedException.getMessage());
        assertEquals(originalCause.getMessage(), deserializedException.getCause().getMessage());
        assertEquals(originalCause.getClass(), deserializedException.getCause().getClass());
    }

    @Test
    void testInheritanceFromException() {
        UserNotFoundException exception = new UserNotFoundException("Test");

        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
        assertFalse(RuntimeException.class.isInstance(exception));
    }

    @Test
    void testStackTrace() {
        UserNotFoundException exception = new UserNotFoundException("Test exception");

        StackTraceElement[] stackTrace = exception.getStackTrace();

        assertNotNull(stackTrace);
        assertTrue(stackTrace.length > 0);
        assertEquals("testStackTrace", stackTrace[0].getMethodName());
    }

    @Test
    void testToString() {
        String message = "Usuario no encontrado";
        UserNotFoundException exception = new UserNotFoundException(message);

        String result = exception.toString();

        assertTrue(result.contains("UserNotFoundException"));
        assertTrue(result.contains(message));
    }

    @Test
    void testToStringWithNullMessage() {
        UserNotFoundException exception = new UserNotFoundException(null);

        String result = exception.toString();

        assertTrue(result.contains("UserNotFoundException"));
    }

    @Test
    void testCauseChaining() {
        RuntimeException rootCause = new RuntimeException("Causa raíz");
        IllegalArgumentException intermediateCause = new IllegalArgumentException("Causa intermedia", rootCause);
        UserNotFoundException exception = new UserNotFoundException("Usuario no encontrado", intermediateCause);

        assertEquals(intermediateCause, exception.getCause());
        assertEquals(rootCause, exception.getCause().getCause());
        assertNull(exception.getCause().getCause().getCause());
    }

    @Test
    void testExceptionWithLongMessage() {
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("Usuario no encontrado con ID muy largo ");
        }
        String message = longMessage.toString();

        UserNotFoundException exception = new UserNotFoundException(message);

        assertEquals(message, exception.getMessage());
        assertTrue(exception.getMessage().length() > 1000);
    }

    @Test
    void testExceptionWithSpecialCharacters() {
        String message = "Usuario no encontrado: áéíóú ñÑ @#$%^&*()[]{}|\\:;\"'<>,.?/~`";

        UserNotFoundException exception = new UserNotFoundException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void testMultipleExceptionsWithSameMessage() {
        String message = "Usuario duplicado";

        UserNotFoundException exception1 = new UserNotFoundException(message);
        UserNotFoundException exception2 = new UserNotFoundException(message);

        assertEquals(exception1.getMessage(), exception2.getMessage());
        assertNotSame(exception1, exception2);
    }
}