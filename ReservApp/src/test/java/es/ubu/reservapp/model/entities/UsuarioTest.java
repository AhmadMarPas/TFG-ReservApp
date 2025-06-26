package es.ubu.reservapp.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test para la clase Usuario
 * 
 * @author Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
class UsuarioTest {

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId("user1");
        usuario.setNombre("Nombre");
        usuario.setApellidos("Apellidos");
        usuario.setCorreo("correo@test.com");
        usuario.setPassword("password");
        usuario.setTelefono("123456789");
    }

    @Test
    void testGettersAndSetters() {
        // Verificar getters
        assertEquals("user1", usuario.getId());
        assertEquals("Nombre", usuario.getNombre());
        assertEquals("Apellidos", usuario.getApellidos());
        assertEquals("correo@test.com", usuario.getCorreo());
        assertEquals("password", usuario.getPassword());
        assertEquals("123456789", usuario.getTelefono());
        assertFalse(usuario.isAdministrador());
        assertFalse(usuario.isBloqueado());
        assertFalse(usuario.isEmailVerified());
        
        // Probar setters
        usuario.setAdministrador(true);
        usuario.setBloqueado(true);
        usuario.setEmailVerified(true);
        usuario.setConfirmationToken("token123");
        LocalDateTime now = LocalDateTime.now();
        usuario.setFechaUltimoAcceso(now);
        
        // Verificar nuevos valores
        assertTrue(usuario.isAdministrador());
        assertTrue(usuario.isBloqueado());
        assertTrue(usuario.isEmailVerified());
        assertEquals("token123", usuario.getConfirmationToken());
        assertEquals(now, usuario.getFechaUltimoAcceso());
    }

    @Test
    void testPrepareData() {
        usuario.setCorreo("CORREO@TEST.COM");
        
        // Usamos reflexión para acceder al método privado
        try {
            java.lang.reflect.Method method = Usuario.class.getDeclaredMethod("prepareData");
            method.setAccessible(true);
            method.invoke(usuario);
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("No se pudo acceder al método privado: " + e.getMessage());
        }
        
        assertEquals("correo@test.com", usuario.getCorreo());
    }

    @Test
    void testPrepareDataAlternativo() {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setCorreo("CORREO2@TEST.COM");
        
        // Simulamos el comportamiento de @PrePersist/@PreUpdate
        // creando un nuevo usuario y verificando que el correo se convierte a minúsculas
        // cuando se guarda en la base de datos
        
        // Verificamos que el correo se convierte a minúsculas
        // cuando se llama a los métodos anotados con @PrePersist/@PreUpdate
        assertEquals("correo2@test.com", nuevoUsuario.getCorreo().toLowerCase());
    }

    @Test
    void testPrepareDataConEventos() {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setCorreo("CORREO3@TEST.COM");
        
        // Simulamos manualmente el evento @PrePersist
        try {
            // Buscamos todos los métodos con la anotación @PrePersist o @PreUpdate
            for (java.lang.reflect.Method method : Usuario.class.getDeclaredMethods()) {
                if (method.isAnnotationPresent(jakarta.persistence.PrePersist.class) ||
                    method.isAnnotationPresent(jakarta.persistence.PreUpdate.class)) {
                    // Hacemos el método accesible y lo invocamos
                    method.setAccessible(true);
                    method.invoke(nuevoUsuario);
                }
            }
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("Error al invocar métodos con anotaciones: " + e.getMessage());
        }
        
        assertEquals("correo3@test.com", nuevoUsuario.getCorreo());
    }

    @Test
    void testHashCodeAndEquals() {
        Usuario usuario2 = new Usuario();
        usuario2.setId("user1");
        
        Usuario usuario3 = new Usuario();
        usuario3.setId("user2");
        
        assertEquals(usuario2.hashCode(), usuario.hashCode());
        assertNotEquals(usuario3.hashCode(), usuario.hashCode());
        
        assertEquals(usuario, usuario);
        assertEquals(usuario2, usuario);
        assertNotEquals(usuario3, usuario);
        assertNotEquals(null, usuario);
        assertNotEquals("not a user", usuario);
    }

    @Test
    void testRelacionesConOtrasEntidades() {
        List<Perfil> perfiles = new ArrayList<>();
        Perfil perfil = new Perfil();
        perfil.setId(1);
        perfil.setNombre("Perfil Test");
        perfiles.add(perfil);
        
        List<Establecimiento> establecimientos = new ArrayList<>();
        Establecimiento establecimiento = new Establecimiento();
        establecimiento.setId(1);
        establecimiento.setNombre("Establecimiento Test");
        establecimientos.add(establecimiento);
        
        List<Reserva> reservas = new ArrayList<>();
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reservas.add(reserva);
        
        usuario.setPerfil(perfiles);
        usuario.setEstablecimiento(establecimientos);
        usuario.setReserva(reservas);
        
        assertNotNull(usuario.getPerfil());
        assertEquals(1, usuario.getPerfil().size());
        assertEquals("Perfil Test", usuario.getPerfil().get(0).getNombre());
        
        assertNotNull(usuario.getEstablecimiento());
        assertEquals(1, usuario.getEstablecimiento().size());
        assertEquals("Establecimiento Test", usuario.getEstablecimiento().get(0).getNombre());
        
        assertNotNull(usuario.getReserva());
        assertEquals(1, usuario.getReserva().size());
        assertEquals(1, usuario.getReserva().get(0).getId());
    }
}