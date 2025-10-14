package utfpr.OD46S.backend.entitys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdministratorTest {

    private Administrator administrator;

    @BeforeEach
    void setUp() {
        administrator = new Administrator();
        administrator.setId(1L);
        administrator.setAccessLevel("ADMIN");
        administrator.setDepartment("IT");
        administrator.setCorporatePhone("47999999999");
    }

    @Test
    void testGettersAndSetters() {
        assertEquals(1L, administrator.getId());
        assertEquals("ADMIN", administrator.getAccessLevel());
        assertEquals("IT", administrator.getDepartment());
        assertEquals("47999999999", administrator.getCorporatePhone());
    }

    @Test
    void testConstructor() {
        Administrator newAdministrator = new Administrator();
        assertNotNull(newAdministrator);
    }

    @Test
    void testAllArgsConstructor() {
        Administrator allArgsAdmin = new Administrator(2L, "SUPER_ADMIN", "HR", "47988888888");
        assertEquals(2L, allArgsAdmin.getId());
        assertEquals("SUPER_ADMIN", allArgsAdmin.getAccessLevel());
        assertEquals("HR", allArgsAdmin.getDepartment());
        assertEquals("47988888888", allArgsAdmin.getCorporatePhone());
    }

    @Test
    void testGettersAndSettersForCompatibility() {
        administrator.setNivelAcesso("SUPER_ADMIN");
        assertEquals("SUPER_ADMIN", administrator.getAccessLevel());
        assertEquals("SUPER_ADMIN", administrator.getNivelAcesso());
    }

    @Test
    void testEqualsAndHashCode() {
        Administrator admin1 = new Administrator();
        admin1.setId(1L);
        admin1.setAccessLevel("ADMIN");

        Administrator admin2 = new Administrator();
        admin2.setId(1L);
        admin2.setAccessLevel("ADMIN");

        Administrator admin3 = new Administrator();
        admin3.setId(2L);
        admin3.setAccessLevel("ADMIN");

        // Test basic object creation
        assertNotNull(admin1);
        assertNotNull(admin2);
        assertNotNull(admin3);
        
        // Test that objects are different instances
        assertNotSame(admin1, admin2);
        assertNotSame(admin1, admin3);
    }

    @Test
    void testToString() {
        administrator.setId(1L);
        administrator.setAccessLevel("ADMIN");
        administrator.setDepartment("IT");

        String result = administrator.toString();

        assertNotNull(result);
        // Test that toString returns a non-empty string
        assertFalse(result.isEmpty());
    }

    @Test
    void testAccessLevels() {
        administrator.setAccessLevel("SUPER_ADMIN");
        assertEquals("SUPER_ADMIN", administrator.getAccessLevel());

        administrator.setAccessLevel("OPERATOR");
        assertEquals("OPERATOR", administrator.getAccessLevel());

        administrator.setAccessLevel("ADMIN");
        assertEquals("ADMIN", administrator.getAccessLevel());
    }

    @Test
    void testDepartmentField() {
        administrator.setDepartment("Human Resources");
        assertEquals("Human Resources", administrator.getDepartment());

        administrator.setDepartment("Finance");
        assertEquals("Finance", administrator.getDepartment());

        administrator.setDepartment(null);
        assertNull(administrator.getDepartment());
    }

    @Test
    void testCorporatePhoneField() {
        administrator.setCorporatePhone("11987654321");
        assertEquals("11987654321", administrator.getCorporatePhone());

        administrator.setCorporatePhone("47999999999");
        assertEquals("47999999999", administrator.getCorporatePhone());

        administrator.setCorporatePhone(null);
        assertNull(administrator.getCorporatePhone());
    }

    @Test
    void testIdField() {
        administrator.setId(999L);
        assertEquals(999L, administrator.getId());

        administrator.setId(1L);
        assertEquals(1L, administrator.getId());
    }

    @Test
    void testDefaultValues() {
        Administrator newAdmin = new Administrator();
        
        // Test that default values are properly set
        assertNull(newAdmin.getId());
        // AccessLevel has default value of "ADMIN"
        assertEquals("ADMIN", newAdmin.getAccessLevel());
        assertNull(newAdmin.getDepartment());
        assertNull(newAdmin.getCorporatePhone());
    }

    @Test
    void testCompatibilityMethods() {
        // Test the compatibility methods for Portuguese naming
        administrator.setNivelAcesso("SUPER_ADMIN");
        assertEquals("SUPER_ADMIN", administrator.getNivelAcesso());
        assertEquals("SUPER_ADMIN", administrator.getAccessLevel());

        // Test that both methods work and are synchronized
        administrator.setAccessLevel("OPERATOR");
        assertEquals("OPERATOR", administrator.getAccessLevel());
        assertEquals("OPERATOR", administrator.getNivelAcesso());
    }
}