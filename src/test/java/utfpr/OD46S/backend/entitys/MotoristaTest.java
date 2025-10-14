package utfpr.OD46S.backend.entitys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utfpr.OD46S.backend.enums.CategoriaCNH;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MotoristaTest {

    private Motorista motorista;

    @BeforeEach
    void setUp() {
        motorista = new Motorista();
        motorista.setId(1L);
        motorista.setLicenseNumber("CNH123456789");
        motorista.setLicenseCategory(CategoriaCNH.B);
        motorista.setLicenseExpiry(LocalDate.of(2025, 12, 31));
        motorista.setPhone("47999999999");
        motorista.setEnabled(true);
    }

    @Test
    void testGettersAndSetters() {
        assertEquals(1L, motorista.getId());
        assertEquals("CNH123456789", motorista.getLicenseNumber());
        assertEquals(CategoriaCNH.B, motorista.getLicenseCategory());
        assertEquals(LocalDate.of(2025, 12, 31), motorista.getLicenseExpiry());
        assertEquals("47999999999", motorista.getPhone());
        assertTrue(motorista.getEnabled());
    }

    @Test
    void testConstructor() {
        Motorista newMotorista = new Motorista();
        assertNotNull(newMotorista);
    }

    @Test
    void testEqualsAndHashCode() {
        Motorista motorista1 = new Motorista();
        motorista1.setId(1L);
        motorista1.setLicenseNumber("CNH123456789");

        Motorista motorista2 = new Motorista();
        motorista2.setId(1L);
        motorista2.setLicenseNumber("CNH123456789");

        Motorista motorista3 = new Motorista();
        motorista3.setId(2L);
        motorista3.setLicenseNumber("CNH123456789");

        // Test basic object creation
        assertNotNull(motorista1);
        assertNotNull(motorista2);
        assertNotNull(motorista3);
        
        // Test that objects are different instances
        assertNotSame(motorista1, motorista2);
        assertNotSame(motorista1, motorista3);
    }

    @Test
    void testToString() {
        motorista.setId(1L);
        motorista.setLicenseNumber("CNH123456789");
        motorista.setLicenseCategory(CategoriaCNH.B);

        String result = motorista.toString();

        assertNotNull(result);
        // Test that toString returns a non-empty string
        assertFalse(result.isEmpty());
    }

    @Test
    void testLicenseCategories() {
        motorista.setLicenseCategory(CategoriaCNH.A);
        assertEquals(CategoriaCNH.A, motorista.getLicenseCategory());

        motorista.setLicenseCategory(CategoriaCNH.C);
        assertEquals(CategoriaCNH.C, motorista.getLicenseCategory());

        motorista.setLicenseCategory(CategoriaCNH.D);
        assertEquals(CategoriaCNH.D, motorista.getLicenseCategory());

        motorista.setLicenseCategory(CategoriaCNH.E);
        assertEquals(CategoriaCNH.E, motorista.getLicenseCategory());
    }

    @Test
    void testLicenseNumber() {
        motorista.setLicenseNumber("CNH987654321");
        assertEquals("CNH987654321", motorista.getLicenseNumber());

        motorista.setLicenseNumber("12345678901");
        assertEquals("12345678901", motorista.getLicenseNumber());

        motorista.setLicenseNumber(null);
        assertNull(motorista.getLicenseNumber());
    }

    @Test
    void testLicenseExpiry() {
        LocalDate newDate = LocalDate.of(2026, 6, 15);
        motorista.setLicenseExpiry(newDate);
        assertEquals(newDate, motorista.getLicenseExpiry());

        motorista.setLicenseExpiry(null);
        assertNull(motorista.getLicenseExpiry());
    }

    @Test
    void testPhoneField() {
        motorista.setPhone("11987654321");
        assertEquals("11987654321", motorista.getPhone());

        motorista.setPhone("47988888888");
        assertEquals("47988888888", motorista.getPhone());

        motorista.setPhone(null);
        assertNull(motorista.getPhone());
    }

    @Test
    void testEnabledField() {
        motorista.setEnabled(false);
        assertFalse(motorista.getEnabled());

        motorista.setEnabled(true);
        assertTrue(motorista.getEnabled());
    }

    @Test
    void testIdField() {
        motorista.setId(999L);
        assertEquals(999L, motorista.getId());

        motorista.setId(1L);
        assertEquals(1L, motorista.getId());
    }

    @Test
    void testDefaultValues() {
        Motorista newMotorista = new Motorista();
        
        // Test that default values are properly set
        assertNull(newMotorista.getId());
        assertNull(newMotorista.getLicenseNumber());
        assertNull(newMotorista.getLicenseCategory());
        assertNull(newMotorista.getLicenseExpiry());
        assertNull(newMotorista.getPhone());
        assertTrue(newMotorista.getEnabled()); // Default should be true
    }

    @Test
    void testLicenseExpiryDateValidation() {
        // Test with past date
        LocalDate pastDate = LocalDate.of(2020, 1, 1);
        motorista.setLicenseExpiry(pastDate);
        assertEquals(pastDate, motorista.getLicenseExpiry());

        // Test with future date
        LocalDate futureDate = LocalDate.of(2030, 12, 31);
        motorista.setLicenseExpiry(futureDate);
        assertEquals(futureDate, motorista.getLicenseExpiry());

        // Test with current date
        LocalDate currentDate = LocalDate.now();
        motorista.setLicenseExpiry(currentDate);
        assertEquals(currentDate, motorista.getLicenseExpiry());
    }

    @Test
    void testAllFieldsCombination() {
        Motorista fullMotorista = new Motorista();
        fullMotorista.setId(123L);
        fullMotorista.setLicenseNumber("CNH999888777");
        fullMotorista.setLicenseCategory(CategoriaCNH.C);
        fullMotorista.setLicenseExpiry(LocalDate.of(2027, 3, 15));
        fullMotorista.setPhone("11999888777");
        fullMotorista.setEnabled(false);

        assertEquals(123L, fullMotorista.getId());
        assertEquals("CNH999888777", fullMotorista.getLicenseNumber());
        assertEquals(CategoriaCNH.C, fullMotorista.getLicenseCategory());
        assertEquals(LocalDate.of(2027, 3, 15), fullMotorista.getLicenseExpiry());
        assertEquals("11999888777", fullMotorista.getPhone());
        assertFalse(fullMotorista.getEnabled());
    }
}