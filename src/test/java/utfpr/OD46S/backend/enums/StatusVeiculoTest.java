package utfpr.OD46S.backend.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StatusVeiculoTest {

    @Test
    void testEnumValues() {
        assertEquals(4, StatusVeiculo.values().length);
        assertTrue(StatusVeiculo.valueOf("AVAILABLE") instanceof StatusVeiculo);
        assertTrue(StatusVeiculo.valueOf("INACTIVE") instanceof StatusVeiculo);
        assertTrue(StatusVeiculo.valueOf("IN_USE") instanceof StatusVeiculo);
        assertTrue(StatusVeiculo.valueOf("MAINTENANCE") instanceof StatusVeiculo);
    }

    @Test
    void testToString() {
        assertEquals("AVAILABLE", StatusVeiculo.AVAILABLE.toString());
        assertEquals("INACTIVE", StatusVeiculo.INACTIVE.toString());
        assertEquals("IN_USE", StatusVeiculo.IN_USE.toString());
        assertEquals("MAINTENANCE", StatusVeiculo.MAINTENANCE.toString());
    }

    @Test
    void testValueOf() {
        assertEquals(StatusVeiculo.AVAILABLE, StatusVeiculo.valueOf("AVAILABLE"));
        assertEquals(StatusVeiculo.INACTIVE, StatusVeiculo.valueOf("INACTIVE"));
        assertEquals(StatusVeiculo.IN_USE, StatusVeiculo.valueOf("IN_USE"));
        assertEquals(StatusVeiculo.MAINTENANCE, StatusVeiculo.valueOf("MAINTENANCE"));
    }

    @Test
    void testValueOf_InvalidValue() {
        assertThrows(NullPointerException.class, () -> StatusVeiculo.valueOf(null));
    }

    @Test
    void testOrdinal() {
        assertEquals(0, StatusVeiculo.AVAILABLE.ordinal());
        assertEquals(3, StatusVeiculo.INACTIVE.ordinal());
        assertEquals(1, StatusVeiculo.IN_USE.ordinal());
        assertEquals(2, StatusVeiculo.MAINTENANCE.ordinal());
    }

    @Test
    void testName() {
        assertEquals("AVAILABLE", StatusVeiculo.AVAILABLE.name());
        assertEquals("INACTIVE", StatusVeiculo.INACTIVE.name());
        assertEquals("IN_USE", StatusVeiculo.IN_USE.name());
        assertEquals("MAINTENANCE", StatusVeiculo.MAINTENANCE.name());
    }

    @Test
    void testEnumEquality() {
        assertEquals(StatusVeiculo.AVAILABLE, StatusVeiculo.AVAILABLE);
        assertEquals(StatusVeiculo.INACTIVE, StatusVeiculo.INACTIVE);
        assertEquals(StatusVeiculo.IN_USE, StatusVeiculo.IN_USE);
        assertEquals(StatusVeiculo.MAINTENANCE, StatusVeiculo.MAINTENANCE);
    }

    @Test
    void testEnumInequality() {
        assertNotEquals(StatusVeiculo.AVAILABLE, StatusVeiculo.INACTIVE);
        assertNotEquals(StatusVeiculo.INACTIVE, StatusVeiculo.IN_USE);
        assertNotEquals(StatusVeiculo.IN_USE, StatusVeiculo.MAINTENANCE);
        assertNotEquals(StatusVeiculo.MAINTENANCE, StatusVeiculo.AVAILABLE);
    }

    @Test
    void testEnumHashCode() {
        assertEquals(StatusVeiculo.AVAILABLE.hashCode(), StatusVeiculo.AVAILABLE.hashCode());
        assertEquals(StatusVeiculo.INACTIVE.hashCode(), StatusVeiculo.INACTIVE.hashCode());
        assertEquals(StatusVeiculo.IN_USE.hashCode(), StatusVeiculo.IN_USE.hashCode());
        assertEquals(StatusVeiculo.MAINTENANCE.hashCode(), StatusVeiculo.MAINTENANCE.hashCode());
    }

    @Test
    void testEnumComparable() {
        assertTrue(StatusVeiculo.AVAILABLE.compareTo(StatusVeiculo.IN_USE) < 0);
        assertTrue(StatusVeiculo.IN_USE.compareTo(StatusVeiculo.MAINTENANCE) < 0);
        assertTrue(StatusVeiculo.MAINTENANCE.compareTo(StatusVeiculo.INACTIVE) < 0);
        assertTrue(StatusVeiculo.INACTIVE.compareTo(StatusVeiculo.AVAILABLE) > 0);
    }

    @Test
    void testEnumSwitch() {
        String result = switch (StatusVeiculo.AVAILABLE) {
            case AVAILABLE -> "Disponível";
            case INACTIVE -> "Inativo";
            case IN_USE -> "Em uso";
            case MAINTENANCE -> "Manutenção";
        };
        assertEquals("Disponível", result);
    }

    @Test
    void testEnumArray() {
        StatusVeiculo[] statuses = StatusVeiculo.values();
        assertEquals(4, statuses.length);
        assertEquals(StatusVeiculo.AVAILABLE, statuses[0]);
        assertEquals(StatusVeiculo.IN_USE, statuses[1]);
        assertEquals(StatusVeiculo.MAINTENANCE, statuses[2]);
        assertEquals(StatusVeiculo.INACTIVE, statuses[3]);
    }

    @Test
    void testEnumClass() {
        assertEquals(StatusVeiculo.class, StatusVeiculo.AVAILABLE.getClass());
        assertEquals(StatusVeiculo.class, StatusVeiculo.INACTIVE.getClass());
        assertEquals(StatusVeiculo.class, StatusVeiculo.IN_USE.getClass());
        assertEquals(StatusVeiculo.class, StatusVeiculo.MAINTENANCE.getClass());
    }

    @Test
    void testBusinessLogic_Available() {
        StatusVeiculo status = StatusVeiculo.AVAILABLE;
        assertTrue(status == StatusVeiculo.AVAILABLE);
        assertFalse(status == StatusVeiculo.IN_USE);
        assertFalse(status == StatusVeiculo.MAINTENANCE);
        assertFalse(status == StatusVeiculo.INACTIVE);
    }

    @Test
    void testBusinessLogic_InUse() {
        StatusVeiculo status = StatusVeiculo.IN_USE;
        assertTrue(status == StatusVeiculo.IN_USE);
        assertFalse(status == StatusVeiculo.AVAILABLE);
        assertFalse(status == StatusVeiculo.MAINTENANCE);
        assertFalse(status == StatusVeiculo.INACTIVE);
    }

    @Test
    void testBusinessLogic_Maintenance() {
        StatusVeiculo status = StatusVeiculo.MAINTENANCE;
        assertTrue(status == StatusVeiculo.MAINTENANCE);
        assertFalse(status == StatusVeiculo.AVAILABLE);
        assertFalse(status == StatusVeiculo.IN_USE);
        assertFalse(status == StatusVeiculo.INACTIVE);
    }

    @Test
    void testBusinessLogic_Inactive() {
        StatusVeiculo status = StatusVeiculo.INACTIVE;
        assertTrue(status == StatusVeiculo.INACTIVE);
        assertFalse(status == StatusVeiculo.AVAILABLE);
        assertFalse(status == StatusVeiculo.IN_USE);
        assertFalse(status == StatusVeiculo.MAINTENANCE);
    }
}