package es.ubu.reservapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class ReservApplicationTests {

	@Test
	void contextLoads() {
   // Creado por Spring Boot para verificar que el contexto de la aplicación se carga correctamente.
 }
	
    @Test
    void testMainMethod() {
        assertDoesNotThrow(() -> {
            ReservApplication.main(new String[]{});
        }, "El método main no debería lanzar excepciones");
    }

}
