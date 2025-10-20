package utfpr.OD46S.backend.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CategoriaCNHTest {

    @Test
    void testEnumValues() {
        assertEquals(5, CategoriaCNH.values().length);
        assertTrue(CategoriaCNH.valueOf("A") instanceof CategoriaCNH);
        assertTrue(CategoriaCNH.valueOf("B") instanceof CategoriaCNH);
        assertTrue(CategoriaCNH.valueOf("C") instanceof CategoriaCNH);
        assertTrue(CategoriaCNH.valueOf("D") instanceof CategoriaCNH);
        assertTrue(CategoriaCNH.valueOf("E") instanceof CategoriaCNH);
    }

    @Test
    void testToString() {
        assertEquals("A", CategoriaCNH.A.toString());
        assertEquals("B", CategoriaCNH.B.toString());
        assertEquals("C", CategoriaCNH.C.toString());
        assertEquals("D", CategoriaCNH.D.toString());
        assertEquals("E", CategoriaCNH.E.toString());
    }

    @Test
    void testValueOf() {
        assertEquals(CategoriaCNH.A, CategoriaCNH.valueOf("A"));
        assertEquals(CategoriaCNH.B, CategoriaCNH.valueOf("B"));
        assertEquals(CategoriaCNH.C, CategoriaCNH.valueOf("C"));
        assertEquals(CategoriaCNH.D, CategoriaCNH.valueOf("D"));
        assertEquals(CategoriaCNH.E, CategoriaCNH.valueOf("E"));
    }

    @Test
    void testValueOf_InvalidValue() {
        assertThrows(NullPointerException.class, () -> CategoriaCNH.valueOf(null));
    }

    @Test
    void testOrdinal() {
        assertEquals(0, CategoriaCNH.A.ordinal());
        assertEquals(1, CategoriaCNH.B.ordinal());
        assertEquals(2, CategoriaCNH.C.ordinal());
        assertEquals(3, CategoriaCNH.D.ordinal());
        assertEquals(4, CategoriaCNH.E.ordinal());
    }

    @Test
    void testName() {
        assertEquals("A", CategoriaCNH.A.name());
        assertEquals("B", CategoriaCNH.B.name());
        assertEquals("C", CategoriaCNH.C.name());
        assertEquals("D", CategoriaCNH.D.name());
        assertEquals("E", CategoriaCNH.E.name());
    }

    @Test
    void testEnumEquality() {
        assertEquals(CategoriaCNH.A, CategoriaCNH.A);
        assertEquals(CategoriaCNH.B, CategoriaCNH.B);
        assertEquals(CategoriaCNH.C, CategoriaCNH.C);
        assertEquals(CategoriaCNH.D, CategoriaCNH.D);
        assertEquals(CategoriaCNH.E, CategoriaCNH.E);
    }

    @Test
    void testEnumInequality() {
        assertNotEquals(CategoriaCNH.A, CategoriaCNH.B);
        assertNotEquals(CategoriaCNH.B, CategoriaCNH.C);
        assertNotEquals(CategoriaCNH.C, CategoriaCNH.D);
        assertNotEquals(CategoriaCNH.D, CategoriaCNH.E);
        assertNotEquals(CategoriaCNH.E, CategoriaCNH.A);
    }

    @Test
    void testEnumHashCode() {
        assertEquals(CategoriaCNH.A.hashCode(), CategoriaCNH.A.hashCode());
        assertEquals(CategoriaCNH.B.hashCode(), CategoriaCNH.B.hashCode());
        assertEquals(CategoriaCNH.C.hashCode(), CategoriaCNH.C.hashCode());
        assertEquals(CategoriaCNH.D.hashCode(), CategoriaCNH.D.hashCode());
        assertEquals(CategoriaCNH.E.hashCode(), CategoriaCNH.E.hashCode());
    }

    @Test
    void testEnumComparable() {
        assertTrue(CategoriaCNH.A.compareTo(CategoriaCNH.B) < 0);
        assertTrue(CategoriaCNH.B.compareTo(CategoriaCNH.C) < 0);
        assertTrue(CategoriaCNH.C.compareTo(CategoriaCNH.D) < 0);
        assertTrue(CategoriaCNH.D.compareTo(CategoriaCNH.E) < 0);
        assertTrue(CategoriaCNH.E.compareTo(CategoriaCNH.A) > 0);
    }

    @Test
    void testEnumSwitch() {
        String result = switch (CategoriaCNH.A) {
            case A -> "Moto";
            case B -> "Carro";
            case C -> "Caminhão";
            case D -> "Ônibus";
            case E -> "Carreta";
        };
        assertEquals("Moto", result);
    }

    @Test
    void testEnumArray() {
        CategoriaCNH[] categories = CategoriaCNH.values();
        assertEquals(5, categories.length);
        assertEquals(CategoriaCNH.A, categories[0]);
        assertEquals(CategoriaCNH.B, categories[1]);
        assertEquals(CategoriaCNH.C, categories[2]);
        assertEquals(CategoriaCNH.D, categories[3]);
        assertEquals(CategoriaCNH.E, categories[4]);
    }

    @Test
    void testEnumClass() {
        assertEquals(CategoriaCNH.class, CategoriaCNH.A.getClass());
        assertEquals(CategoriaCNH.class, CategoriaCNH.B.getClass());
        assertEquals(CategoriaCNH.class, CategoriaCNH.C.getClass());
        assertEquals(CategoriaCNH.class, CategoriaCNH.D.getClass());
        assertEquals(CategoriaCNH.class, CategoriaCNH.E.getClass());
    }
}