package es.ubu.reservapp.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test para la clase Reserva
 * 
 * @author Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
class ReservaTest {

    private Reserva reserva;
    private Usuario usuario;
    private Establecimiento establecimiento;
    private LocalDateTime fechaReserva;
    private LocalTime horaFin;
    private Set<Convocatoria> convocatorias = new HashSet<>();

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId("user1");
        usuario.setNombre("Usuario Test");
        
        establecimiento = new Establecimiento();
        establecimiento.setId(1);
        establecimiento.setNombre("Establecimiento Test");
        
        fechaReserva = LocalDateTime.now().plusDays(1);
        horaFin = LocalTime.of(18, 0);
        
        reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuario);
        reserva.setEstablecimiento(establecimiento);
        reserva.setFechaReserva(fechaReserva);
        reserva.setHoraFin(horaFin);

        Convocatoria conv = new Convocatoria();
        conv.setReserva(new Reserva());
        conv.setUsuario(usuario);
        convocatorias.add(conv);
        
        reserva.setConvocatorias(convocatorias);
    }

    @Test
    void testGettersAndSetters() {
        // Verificar getters
        assertEquals(1, reserva.getId());
        assertEquals(usuario, reserva.getUsuario());
        assertEquals(establecimiento, reserva.getEstablecimiento());
        assertEquals(fechaReserva, reserva.getFechaReserva());
        assertEquals(horaFin, reserva.getHoraFin());
        assertEquals(convocatorias, reserva.getConvocatorias());
        
        // Probar setters con nuevos valores
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setId("user2");
        nuevoUsuario.setNombre("Nuevo Usuario");
        
        Establecimiento nuevoEstablecimiento = new Establecimiento();
        nuevoEstablecimiento.setId(2);
        nuevoEstablecimiento.setNombre("Nuevo Establecimiento");
        
        LocalDateTime nuevaFechaReserva = LocalDateTime.now().plusDays(2);
        LocalTime nuevaHoraFin = LocalTime.of(20, 0);
        Convocatoria conv1 = new Convocatoria();
        conv1.setReserva(new Reserva());
        conv1.setUsuario(usuario);
        convocatorias.add(conv1);
        
        reserva.setId(2);
        reserva.setUsuario(nuevoUsuario);
        reserva.setEstablecimiento(nuevoEstablecimiento);
        reserva.setFechaReserva(nuevaFechaReserva);
        reserva.setHoraFin(nuevaHoraFin);
        reserva.setConvocatorias(convocatorias);
        
        // Verificar nuevos valores
        assertEquals(2, reserva.getId());
        assertEquals(nuevoUsuario, reserva.getUsuario());
        assertEquals(nuevoEstablecimiento, reserva.getEstablecimiento());
        assertEquals(nuevaFechaReserva, reserva.getFechaReserva());
        assertEquals(nuevaHoraFin, reserva.getHoraFin());
    }

    @Test
    void testConstructorVacio() {
        Reserva reservaVacia = new Reserva();
        
        assertNotNull(reservaVacia);
    }

    @Test
    void testConstructorConParametros() {
        Integer id = 3;
        Usuario usuarioTest = new Usuario();
        usuarioTest.setId("user3");
        Establecimiento establecimientoTest = new Establecimiento();
        establecimientoTest.setId(3);
        LocalDateTime fechaTest = LocalDateTime.now().plusDays(3);
        LocalTime horaTest = LocalTime.of(21, 0);
        Convocatoria conv = new Convocatoria();
        conv.setReserva(new Reserva());
        conv.setUsuario(new Usuario());
        convocatorias.add(conv);
        
        Reserva reservaCompleta = new Reserva(id, usuarioTest, establecimientoTest, fechaTest, horaTest, convocatorias);
        
        assertEquals(id, reservaCompleta.getId());
        assertEquals(usuarioTest, reservaCompleta.getUsuario());
        assertEquals(establecimientoTest, reservaCompleta.getEstablecimiento());
        assertEquals(fechaTest, reservaCompleta.getFechaReserva());
        assertEquals(horaTest, reservaCompleta.getHoraFin());
        assertEquals(convocatorias, reservaCompleta.getConvocatorias());
    }
    
    @Test
    void testConstructorCopia() {
        // Use the existing test data from setUp()
        Reserva reservaOriginal = new Reserva();
        Convocatoria conv = new Convocatoria();
        conv.setReserva(reservaOriginal);
        conv.setUsuario(new Usuario());
        convocatorias.add(conv);

        reservaOriginal.setId(1);
        reservaOriginal.setUsuario(usuario);
        reservaOriginal.setEstablecimiento(establecimiento);
        reservaOriginal.setFechaReserva(fechaReserva);
        reservaOriginal.setHoraFin(horaFin);
        reservaOriginal.setConvocatorias(convocatorias);

        // Create a copy using the copy constructor
        Reserva reservaCopia = new Reserva(reservaOriginal);

        // Verify all attributes are equal
        assertEquals(reservaOriginal.getId(), reservaCopia.getId());
        assertEquals(reservaOriginal.getUsuario(), reservaCopia.getUsuario());
        assertEquals(reservaOriginal.getEstablecimiento(), reservaCopia.getEstablecimiento());
        assertEquals(reservaOriginal.getFechaReserva(), reservaCopia.getFechaReserva());
        assertEquals(reservaOriginal.getHoraFin(), reservaCopia.getHoraFin());
        assertEquals(reservaOriginal.getConvocatorias(), reservaCopia.getConvocatorias());

        // Comprobamos que las listas son copias profundas
        assertNotSame(reservaOriginal.getConvocatorias(), reservaCopia.getConvocatorias());
        assertEquals(reservaOriginal.getConvocatorias().size(), reservaCopia.getConvocatorias().size());

        // Verify they are different objects
        assertNotSame(reservaOriginal, reservaCopia);

        // Verify modifying the copy doesn't affect the original
        reservaCopia.setId(2);
        LocalDateTime nuevaFecha = LocalDateTime.now().plusDays(5);
        reservaCopia.setFechaReserva(nuevaFecha);
        assertNotEquals(reservaOriginal.getId(), reservaCopia.getId());
        assertNotEquals(reservaOriginal.getFechaReserva(), reservaCopia.getFechaReserva());
    }

    @Test
    void testMetodoCopia() {
        // Use the existing test data from setUp()
        Reserva reservaOriginal = new Reserva();
        Convocatoria conv = new Convocatoria();
        conv.setReserva(reservaOriginal);
        conv.setUsuario(new Usuario());
        convocatorias.add(conv);
        
        reservaOriginal.setId(1);
        reservaOriginal.setUsuario(usuario);
        reservaOriginal.setEstablecimiento(establecimiento);
        reservaOriginal.setFechaReserva(fechaReserva);
        reservaOriginal.setHoraFin(horaFin);
        reservaOriginal.setConvocatorias(convocatorias);

        // Create a copy using the copia() method
        Reserva reservaCopia = (Reserva) reservaOriginal.copia();

        // Verify all attributes are equal
        assertEquals(reservaOriginal.getId(), reservaCopia.getId());
        assertEquals(reservaOriginal.getUsuario(), reservaCopia.getUsuario());
        assertEquals(reservaOriginal.getEstablecimiento(), reservaCopia.getEstablecimiento());
        assertEquals(reservaOriginal.getFechaReserva(), reservaCopia.getFechaReserva());
        assertEquals(reservaOriginal.getHoraFin(), reservaCopia.getHoraFin());
        assertEquals(reservaOriginal.getFechaReserva(), reservaCopia.getFechaReserva());

        // Comprobamos que las listas son copias profundas
        assertNotSame(reservaOriginal.getConvocatorias(), reservaCopia.getConvocatorias());
        assertEquals(reservaOriginal.getConvocatorias().size(), reservaCopia.getConvocatorias().size());

        // Verify they are different objects
        assertNotSame(reservaOriginal, reservaCopia);

        // Verify modifying the copy doesn't affect the original
        reservaCopia.setId(2);
        LocalDateTime nuevaFecha = LocalDateTime.now().plusDays(5);
        reservaCopia.setFechaReserva(nuevaFecha);
        assertNotEquals(reservaOriginal.getId(), reservaCopia.getId());
        assertNotEquals(reservaOriginal.getFechaReserva(), reservaCopia.getFechaReserva());
    }
    
    @Test
    void testConstructorDeCopiaConListasNulas() {
        Integer id = 3;
        Usuario usuarioTest = new Usuario();
        usuarioTest.setId("user3");
        Establecimiento establecimientoTest = new Establecimiento();
        establecimientoTest.setId(3);
        LocalDateTime fechaTest = LocalDateTime.now().plusDays(3);
        LocalTime horaTest = LocalTime.of(21, 0);
        
        Reserva reservaOriginal = new Reserva(id, usuarioTest, establecimientoTest, fechaTest, horaTest, null);
        // Crear una copia del usuario original
        Reserva reservaCopia = new Reserva(reservaOriginal);
        
        assertEquals(id, reservaCopia.getId());
        assertEquals(usuarioTest, reservaCopia.getUsuario());
        assertEquals(establecimientoTest, reservaCopia.getEstablecimiento());
        assertEquals(fechaTest, reservaCopia.getFechaReserva());
        assertEquals(horaTest, reservaCopia.getHoraFin());
        assertEquals(new HashSet<>(), reservaCopia.getConvocatorias());
        
        // Verificar que las listas se inicializan correctamente
        assertNotNull(reservaCopia.getConvocatorias());
        assertTrue(reservaCopia.getConvocatorias().isEmpty());
    }


}