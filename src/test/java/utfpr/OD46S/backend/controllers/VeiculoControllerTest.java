package utfpr.OD46S.backend.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import utfpr.OD46S.backend.dtos.VeiculoDTO;
import utfpr.OD46S.backend.enums.StatusVeiculo;
import utfpr.OD46S.backend.services.VeiculoService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoControllerTest {

    @Mock
    private VeiculoService veiculoService;

    @InjectMocks
    private VeiculoController veiculoController;

    private VeiculoDTO veiculoDTO;

    @BeforeEach
    void setUp() {
        veiculoDTO = new VeiculoDTO();
        veiculoDTO.setId(1L);
        veiculoDTO.setLicensePlate("ABC1234");
        veiculoDTO.setModel("Compactor 15m³");
        veiculoDTO.setBrand("Volvo");
        veiculoDTO.setYear(2022);
        veiculoDTO.setCapacityKg(java.math.BigDecimal.valueOf(15000.0));
        veiculoDTO.setFuelType("DIESEL");
        veiculoDTO.setAverageConsumption(java.math.BigDecimal.valueOf(3.5));
        veiculoDTO.setStatus(StatusVeiculo.AVAILABLE);
        veiculoDTO.setCurrentKm(12500);
        veiculoDTO.setAcquisitionDate(LocalDate.of(2022, 1, 15));
        veiculoDTO.setNotes("New vehicle");
        veiculoDTO.setActive(true);
    }

    @Test
    void testListar_Success() {
        // Given
        List<VeiculoDTO> veiculos = Arrays.asList(veiculoDTO);
        when(veiculoService.listarTodos()).thenReturn(veiculos);

        // When
        ResponseEntity<List<VeiculoDTO>> response = veiculoController.listar();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("ABC1234", response.getBody().get(0).getLicensePlate());
        
        verify(veiculoService, times(1)).listarTodos();
    }

    @Test
    void testListar_Empty() {
        // Given
        when(veiculoService.listarTodos()).thenReturn(Arrays.asList());

        // When
        ResponseEntity<List<VeiculoDTO>> response = veiculoController.listar();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        
        verify(veiculoService, times(1)).listarTodos();
    }

    @Test
    void testCadastrar_Success() {
        // Given
        when(veiculoService.cadastrar(any(VeiculoDTO.class))).thenReturn(veiculoDTO);

        // When
        ResponseEntity<VeiculoDTO> response = veiculoController.cadastrar(veiculoDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ABC1234", response.getBody().getLicensePlate());
        assertEquals("Compactor 15m³", response.getBody().getModel());
        assertEquals(StatusVeiculo.AVAILABLE, response.getBody().getStatus());
        
        verify(veiculoService, times(1)).cadastrar(veiculoDTO);
    }

    @Test
    void testCadastrar_LicensePlateExists() {
        // Given
        when(veiculoService.cadastrar(any(VeiculoDTO.class)))
                .thenThrow(new RuntimeException("Placa já cadastrada"));

        // When & Then
        assertThrows(RuntimeException.class, () -> veiculoController.cadastrar(veiculoDTO));
        
        verify(veiculoService, times(1)).cadastrar(veiculoDTO);
    }

    @Test
    void testAtualizar_Success() {
        // Given
        VeiculoDTO updatedDTO = new VeiculoDTO();
        updatedDTO.setLicensePlate("ABC1234");
        updatedDTO.setModel("Updated Model");
        updatedDTO.setBrand("Updated Brand");
        updatedDTO.setYear(2023);
        
        when(veiculoService.atualizar(eq(1L), any(VeiculoDTO.class))).thenReturn(updatedDTO);

        // When
        ResponseEntity<VeiculoDTO> response = veiculoController.atualizar(1L, updatedDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Model", response.getBody().getModel());
        assertEquals("Updated Brand", response.getBody().getBrand());
        assertEquals(2023, response.getBody().getYear());
        
        verify(veiculoService, times(1)).atualizar(1L, updatedDTO);
    }

    @Test
    void testAtualizar_NotFound() {
        // Given
        when(veiculoService.atualizar(eq(999L), any(VeiculoDTO.class)))
                .thenThrow(new RuntimeException("Veículo não encontrado"));

        // When & Then
        assertThrows(RuntimeException.class, () -> veiculoController.atualizar(999L, veiculoDTO));
        
        verify(veiculoService, times(1)).atualizar(999L, veiculoDTO);
    }

    @Test
    void testAlterarStatus_Success() {
        // Given
        VeiculoDTO updatedVeiculo = new VeiculoDTO();
        updatedVeiculo.setId(1L);
        updatedVeiculo.setLicensePlate("ABC1234");
        updatedVeiculo.setModel("Compactor 15m³");
        updatedVeiculo.setBrand("Volvo");
        updatedVeiculo.setStatus(StatusVeiculo.MAINTENANCE);
        
        when(veiculoService.alterarStatus(1L, StatusVeiculo.MAINTENANCE)).thenReturn(updatedVeiculo);

        // When
        ResponseEntity<VeiculoDTO> response = veiculoController.alterarStatus(1L, StatusVeiculo.MAINTENANCE);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(StatusVeiculo.MAINTENANCE, response.getBody().getStatus());
        assertEquals("ABC1234", response.getBody().getLicensePlate());
        
        verify(veiculoService, times(1)).alterarStatus(1L, StatusVeiculo.MAINTENANCE);
    }

    @Test
    void testAlterarStatus_NotFound() {
        // Given
        when(veiculoService.alterarStatus(999L, StatusVeiculo.MAINTENANCE))
                .thenThrow(new RuntimeException("Veículo não encontrado"));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                veiculoController.alterarStatus(999L, StatusVeiculo.MAINTENANCE));
        
        verify(veiculoService, times(1)).alterarStatus(999L, StatusVeiculo.MAINTENANCE);
    }

    @Test
    void testAlterarStatus_AllStatuses() {
        // Given
        VeiculoDTO updatedVeiculo = new VeiculoDTO();
        updatedVeiculo.setId(1L);
        updatedVeiculo.setLicensePlate("ABC1234");
        updatedVeiculo.setStatus(StatusVeiculo.IN_USE);
        
        when(veiculoService.alterarStatus(1L, StatusVeiculo.IN_USE)).thenReturn(updatedVeiculo);

        // When
        ResponseEntity<VeiculoDTO> response = veiculoController.alterarStatus(1L, StatusVeiculo.IN_USE);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(StatusVeiculo.IN_USE, response.getBody().getStatus());
        
        verify(veiculoService, times(1)).alterarStatus(1L, StatusVeiculo.IN_USE);
    }
}
