package es.ubu.reservapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**	
 * Test para la clase ServletInitializer
 */
class ServletInitializerTest {

    @Test
    void testConfigure() {
        ServletInitializer servletInitializer = new ServletInitializer();
        SpringApplicationBuilder builder = new SpringApplicationBuilder();
        SpringApplicationBuilder result = servletInitializer.configure(builder);

        assertNotNull(result, "El resultado no debería ser nulo");
    }
}
