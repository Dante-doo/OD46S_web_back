package utfpr.OD46S.backend.dtos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utfpr.OD46S.backend.enums.StatusVeiculo;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class VeiculoDTOTest {

    private VeiculoDTO veiculoDTO;

    @BeforeEach
    void setUp() {
        veiculoDTO = new VeiculoDTO();
        veiculoDTO.setId(1L);
        veiculoDTO.setLicensePlate("ABC1234");
        veiculoDTO.setModel("Compactor 15m³");
        veiculoDTO.setBrand("Volvo");
        veiculoDTO.setYear(2022);
        veiculoDTO.setCapacityKg(BigDecimal.valueOf(15000.0));
        veiculoDTO.setFuelType("DIESEL");
        veiculoDTO.setAverageConsumption(BigDecimal.valueOf(3.5));
        veiculoDTO.setStatus(StatusVeiculo.AVAILABLE);
        veiculoDTO.setCurrentKm(12500);
        veiculoDTO.setAcquisitionDate(LocalDate.of(2022, 1, 15));
        veiculoDTO.setNotes("New vehicle");
        veiculoDTO.setActive(true);
    }

    @Test
    void testGettersAndSetters() {
        assertEquals(1L, veiculoDTO.getId());
        assertEquals("ABC1234", veiculoDTO.getLicensePlate());
        assertEquals("Compactor 15m³", veiculoDTO.getModel());
        assertEquals("Volvo", veiculoDTO.getBrand());
        assertEquals(2022, veiculoDTO.getYear());
        assertEquals(BigDecimal.valueOf(15000.0), veiculoDTO.getCapacityKg());
        assertEquals("DIESEL", veiculoDTO.getFuelType());
        assertEquals(BigDecimal.valueOf(3.5), veiculoDTO.getAverageConsumption());
        assertEquals(StatusVeiculo.AVAILABLE, veiculoDTO.getStatus());
        assertEquals(12500, veiculoDTO.getCurrentKm());
        assertEquals(LocalDate.of(2022, 1, 15), veiculoDTO.getAcquisitionDate());
        assertEquals("New vehicle", veiculoDTO.getNotes());
        assertTrue(veiculoDTO.getActive());
    }

    @Test
    void testConstructor() {
        VeiculoDTO newVeiculoDTO = new VeiculoDTO();
        assertNotNull(newVeiculoDTO);
    }

    @Test
    void testAllArgsConstructor() {
        VeiculoDTO allArgsDTO = new VeiculoDTO(
                2L, "XYZ9876", "Truck", "Scania", 2020, BigDecimal.valueOf(20000.0),
                "GASOLINE", BigDecimal.valueOf(2.0), StatusVeiculo.IN_USE, 50000,
                LocalDate.of(2020, 5, 1), "Old truck", false
        );

        assertEquals(2L, allArgsDTO.getId());
        assertEquals("XYZ9876", allArgsDTO.getLicensePlate());
        assertEquals("Truck", allArgsDTO.getModel());
        assertEquals("Scania", allArgsDTO.getBrand());
        assertEquals(2020, allArgsDTO.getYear());
        assertEquals(BigDecimal.valueOf(20000.0), allArgsDTO.getCapacityKg());
        assertEquals("GASOLINE", allArgsDTO.getFuelType());
        assertEquals(BigDecimal.valueOf(2.0), allArgsDTO.getAverageConsumption());
        assertEquals(StatusVeiculo.IN_USE, allArgsDTO.getStatus());
        assertEquals(50000, allArgsDTO.getCurrentKm());
        assertEquals(LocalDate.of(2020, 5, 1), allArgsDTO.getAcquisitionDate());
        assertEquals("Old truck", allArgsDTO.getNotes());
        assertFalse(allArgsDTO.getActive());
    }

    @Test
    void testStatusEnum() {
        veiculoDTO.setStatus(StatusVeiculo.IN_USE);
        assertEquals(StatusVeiculo.IN_USE, veiculoDTO.getStatus());

        veiculoDTO.setStatus(StatusVeiculo.MAINTENANCE);
        assertEquals(StatusVeiculo.MAINTENANCE, veiculoDTO.getStatus());

        veiculoDTO.setStatus(StatusVeiculo.INACTIVE);
        assertEquals(StatusVeiculo.INACTIVE, veiculoDTO.getStatus());

        veiculoDTO.setStatus(StatusVeiculo.AVAILABLE);
        assertEquals(StatusVeiculo.AVAILABLE, veiculoDTO.getStatus());
    }

    @Test
    void testBigDecimalFields() {
        BigDecimal newCapacity = BigDecimal.valueOf(25000.0);
        BigDecimal newConsumption = BigDecimal.valueOf(4.5);

        veiculoDTO.setCapacityKg(newCapacity);
        veiculoDTO.setAverageConsumption(newConsumption);

        assertEquals(newCapacity, veiculoDTO.getCapacityKg());
        assertEquals(newConsumption, veiculoDTO.getAverageConsumption());
    }

    @Test
    void testDateFields() {
        LocalDate newDate = LocalDate.of(2023, 6, 15);
        veiculoDTO.setAcquisitionDate(newDate);

        assertEquals(newDate, veiculoDTO.getAcquisitionDate());
    }

    @Test
    void testNumericFields() {
        veiculoDTO.setYear(2023);
        veiculoDTO.setCurrentKm(20000);

        assertEquals(2023, veiculoDTO.getYear());
        assertEquals(20000, veiculoDTO.getCurrentKm());
    }

    @Test
    void testStringFields() {
        veiculoDTO.setLicensePlate("XYZ9876");
        veiculoDTO.setModel("New Model");
        veiculoDTO.setBrand("New Brand");
        veiculoDTO.setFuelType("GASOLINE");
        veiculoDTO.setNotes("Updated notes");

        assertEquals("XYZ9876", veiculoDTO.getLicensePlate());
        assertEquals("New Model", veiculoDTO.getModel());
        assertEquals("New Brand", veiculoDTO.getBrand());
        assertEquals("GASOLINE", veiculoDTO.getFuelType());
        assertEquals("Updated notes", veiculoDTO.getNotes());
    }

    @Test
    void testActiveField() {
        veiculoDTO.setActive(false);
        assertFalse(veiculoDTO.getActive());

        veiculoDTO.setActive(true);
        assertTrue(veiculoDTO.getActive());
    }

    @Test
    void testNullFields() {
        VeiculoDTO nullDTO = new VeiculoDTO();
        
        assertNull(nullDTO.getId());
        assertNull(nullDTO.getLicensePlate());
        assertNull(nullDTO.getModel());
        assertNull(nullDTO.getBrand());
        assertNull(nullDTO.getYear());
        assertNull(nullDTO.getCapacityKg());
        assertNull(nullDTO.getFuelType());
        assertNull(nullDTO.getAverageConsumption());
        assertNull(nullDTO.getStatus());
        assertNull(nullDTO.getCurrentKm());
        assertNull(nullDTO.getAcquisitionDate());
        assertNull(nullDTO.getNotes());
        assertNull(nullDTO.getActive());
    }

    @Test
    void testFuelTypes() {
        veiculoDTO.setFuelType("DIESEL");
        assertEquals("DIESEL", veiculoDTO.getFuelType());

        veiculoDTO.setFuelType("GASOLINE");
        assertEquals("GASOLINE", veiculoDTO.getFuelType());

        veiculoDTO.setFuelType("ELECTRIC");
        assertEquals("ELECTRIC", veiculoDTO.getFuelType());

        veiculoDTO.setFuelType("HYBRID");
        assertEquals("HYBRID", veiculoDTO.getFuelType());
    }

    @Test
    void testYearRange() {
        veiculoDTO.setYear(1990);
        assertEquals(1990, veiculoDTO.getYear());

        veiculoDTO.setYear(2024);
        assertEquals(2024, veiculoDTO.getYear());

        veiculoDTO.setYear(2000);
        assertEquals(2000, veiculoDTO.getYear());
    }

    @Test
    void testCurrentKmRange() {
        veiculoDTO.setCurrentKm(0);
        assertEquals(0, veiculoDTO.getCurrentKm());

        veiculoDTO.setCurrentKm(100000);
        assertEquals(100000, veiculoDTO.getCurrentKm());

        veiculoDTO.setCurrentKm(50000);
        assertEquals(50000, veiculoDTO.getCurrentKm());
    }

    @Test
    void testCapacityRange() {
        BigDecimal smallCapacity = BigDecimal.valueOf(1000.0);
        BigDecimal largeCapacity = BigDecimal.valueOf(50000.0);

        veiculoDTO.setCapacityKg(smallCapacity);
        assertEquals(smallCapacity, veiculoDTO.getCapacityKg());

        veiculoDTO.setCapacityKg(largeCapacity);
        assertEquals(largeCapacity, veiculoDTO.getCapacityKg());
    }

    @Test
    void testConsumptionRange() {
        BigDecimal lowConsumption = BigDecimal.valueOf(1.0);
        BigDecimal highConsumption = BigDecimal.valueOf(10.0);

        veiculoDTO.setAverageConsumption(lowConsumption);
        assertEquals(lowConsumption, veiculoDTO.getAverageConsumption());

        veiculoDTO.setAverageConsumption(highConsumption);
        assertEquals(highConsumption, veiculoDTO.getAverageConsumption());
    }

    @Test
    void testLicensePlateFormats() {
        veiculoDTO.setLicensePlate("ABC1234");
        assertEquals("ABC1234", veiculoDTO.getLicensePlate());

        veiculoDTO.setLicensePlate("XYZ9876");
        assertEquals("XYZ9876", veiculoDTO.getLicensePlate());

        veiculoDTO.setLicensePlate("DEF4567");
        assertEquals("DEF4567", veiculoDTO.getLicensePlate());
    }
}