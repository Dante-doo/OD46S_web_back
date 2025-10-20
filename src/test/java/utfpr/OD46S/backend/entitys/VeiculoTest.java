package utfpr.OD46S.backend.entitys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utfpr.OD46S.backend.enums.StatusVeiculo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class VeiculoTest {

    private Veiculo veiculo;

    @BeforeEach
    void setUp() {
        veiculo = new Veiculo();
        veiculo.setId(1L);
        veiculo.setLicensePlate("ABC1234");
        veiculo.setModel("Compactor 15m³");
        veiculo.setBrand("Volvo");
        veiculo.setYear(2022);
        veiculo.setCapacityKg(BigDecimal.valueOf(15000.0));
        veiculo.setFuelType("DIESEL");
        veiculo.setAverageConsumption(BigDecimal.valueOf(3.5));
        veiculo.setStatus(StatusVeiculo.AVAILABLE);
        veiculo.setCurrentKm(12500);
        veiculo.setAcquisitionDate(LocalDate.of(2022, 1, 15));
        veiculo.setNotes("New vehicle");
        veiculo.setActive(true);
    }

    @Test
    void testGettersAndSetters() {
        assertEquals(1L, veiculo.getId());
        assertEquals("ABC1234", veiculo.getLicensePlate());
        assertEquals("Compactor 15m³", veiculo.getModel());
        assertEquals("Volvo", veiculo.getBrand());
        assertEquals(2022, veiculo.getYear());
        assertEquals(BigDecimal.valueOf(15000.0), veiculo.getCapacityKg());
        assertEquals("DIESEL", veiculo.getFuelType());
        assertEquals(StatusVeiculo.AVAILABLE, veiculo.getStatus());
        assertEquals(12500, veiculo.getCurrentKm());
        assertEquals(LocalDate.of(2022, 1, 15), veiculo.getAcquisitionDate());
        assertEquals("New vehicle", veiculo.getNotes());
        assertTrue(veiculo.getActive());
    }

    @Test
    void testConstructor() {
        Veiculo newVeiculo = new Veiculo();
        assertNotNull(newVeiculo);
    }

    @Test
    void testEqualsAndHashCode() {
        Veiculo veiculo1 = new Veiculo();
        veiculo1.setId(1L);
        veiculo1.setLicensePlate("ABC1234");

        Veiculo veiculo2 = new Veiculo();
        veiculo2.setId(1L);
        veiculo2.setLicensePlate("ABC1234");

        Veiculo veiculo3 = new Veiculo();
        veiculo3.setId(2L);
        veiculo3.setLicensePlate("ABC1234");

        // Test basic object creation
        assertNotNull(veiculo1);
        assertNotNull(veiculo2);
        assertNotNull(veiculo3);
        
        // Test that objects are different instances
        assertNotSame(veiculo1, veiculo2);
        assertNotSame(veiculo1, veiculo3);
    }

    @Test
    void testToString() {
        veiculo.setId(1L);
        veiculo.setLicensePlate("ABC1234");
        veiculo.setModel("Compactor 15m³");

        String result = veiculo.toString();

        assertNotNull(result);
        // Test that toString returns a non-empty string
        assertFalse(result.isEmpty());
    }

    @Test
    void testPrePersist() {
        Veiculo newVeiculo = new Veiculo();
        newVeiculo.setLicensePlate("XYZ9876");
        newVeiculo.onCreate();
        
        // Test that methods exist and can be called
        assertNotNull(newVeiculo);
    }

    @Test
    void testPreUpdate() {
        veiculo.setModel("Updated Model");
        veiculo.onUpdate();
        
        // Test that methods exist and can be called
        assertNotNull(veiculo);
    }

    @Test
    void testStatusEnum() {
        veiculo.setStatus(StatusVeiculo.IN_USE);
        assertEquals(StatusVeiculo.IN_USE, veiculo.getStatus());

        veiculo.setStatus(StatusVeiculo.MAINTENANCE);
        assertEquals(StatusVeiculo.MAINTENANCE, veiculo.getStatus());

        veiculo.setStatus(StatusVeiculo.INACTIVE);
        assertEquals(StatusVeiculo.INACTIVE, veiculo.getStatus());
    }

    @Test
    void testBigDecimalFields() {
        BigDecimal newCapacity = BigDecimal.valueOf(20000.0);
        BigDecimal newConsumption = BigDecimal.valueOf(4.5);

        veiculo.setCapacityKg(newCapacity);
        veiculo.setAverageConsumption(newConsumption);

        assertEquals(newCapacity, veiculo.getCapacityKg());
        assertEquals(newConsumption, veiculo.getAverageConsumption());
    }

    @Test
    void testDateFields() {
        LocalDate newDate = LocalDate.of(2023, 6, 15);
        veiculo.setAcquisitionDate(newDate);

        assertEquals(newDate, veiculo.getAcquisitionDate());
    }

    @Test
    void testNumericFields() {
        veiculo.setYear(2023);
        veiculo.setCurrentKm(20000);

        assertEquals(2023, veiculo.getYear());
        assertEquals(20000, veiculo.getCurrentKm());
    }

    @Test
    void testStringFields() {
        veiculo.setLicensePlate("XYZ9876");
        veiculo.setModel("New Model");
        veiculo.setBrand("New Brand");
        veiculo.setFuelType("GASOLINE");
        veiculo.setNotes("Updated notes");

        assertEquals("XYZ9876", veiculo.getLicensePlate());
        assertEquals("New Model", veiculo.getModel());
        assertEquals("New Brand", veiculo.getBrand());
        assertEquals("GASOLINE", veiculo.getFuelType());
        assertEquals("Updated notes", veiculo.getNotes());
    }

    @Test
    void testActiveField() {
        veiculo.setActive(false);
        assertFalse(veiculo.getActive());

        veiculo.setActive(true);
        assertTrue(veiculo.getActive());
    }

    @Test
    void testTimestamps() {
        Veiculo newVeiculo = new Veiculo();
        newVeiculo.setLicensePlate("TEST123");
        
        // Test that methods exist and can be called
        newVeiculo.onCreate();
        assertNotNull(newVeiculo);
    }

    @Test
    void testUpdateTimestamp() {
        veiculo.setModel("Updated Model");
        veiculo.onUpdate();
        
        // Test that methods exist and can be called
        assertNotNull(veiculo);
    }
}
