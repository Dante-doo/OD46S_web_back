package utfpr.OD46S.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import utfpr.OD46S.backend.dtos.VeiculoDTO;
import utfpr.OD46S.backend.entitys.Veiculo;
import utfpr.OD46S.backend.enums.StatusVeiculo;
import utfpr.OD46S.backend.repositorys.VeiculoRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @InjectMocks
    private VeiculoService veiculoService;

    private Veiculo veiculo;
    private VeiculoDTO veiculoDTO;

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
        veiculo.setCreatedAt(LocalDateTime.now());
        veiculo.setUpdatedAt(LocalDateTime.now());

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
    void testListarTodos_Success() {
        List<Veiculo> veiculos = Arrays.asList(veiculo);

        when(veiculoRepository.findAll()).thenReturn(veiculos);

        List<VeiculoDTO> result = veiculoService.listarTodos();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(veiculoRepository, times(1)).findAll();
    }

    @Test
    void testCadastrar_Success() {
        when(veiculoRepository.existsByLicensePlate("ABC1234")).thenReturn(false);
        when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculo);

        VeiculoDTO result = veiculoService.cadastrar(veiculoDTO);

        assertNotNull(result);
        assertEquals("ABC1234", result.getLicensePlate());
        assertEquals("Compactor 15m³", result.getModel());
        verify(veiculoRepository, times(1)).existsByLicensePlate("ABC1234");
        verify(veiculoRepository, times(1)).save(any(Veiculo.class));
    }

    @Test
    void testCadastrar_LicensePlateExists() {
        when(veiculoRepository.existsByLicensePlate("ABC1234")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> veiculoService.cadastrar(veiculoDTO));
        verify(veiculoRepository, times(1)).existsByLicensePlate("ABC1234");
    }

    @Test
    void testAtualizar_Success() {
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculo));
        when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculo);

        veiculoDTO.setModel("Updated Model");
        VeiculoDTO result = veiculoService.atualizar(1L, veiculoDTO);

        assertNotNull(result);
        assertEquals("Updated Model", result.getModel());
        verify(veiculoRepository, times(1)).findById(1L);
        verify(veiculoRepository, times(1)).save(any(Veiculo.class));
    }

    @Test
    void testAtualizar_NotFound() {
        when(veiculoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> veiculoService.atualizar(1L, veiculoDTO));
        verify(veiculoRepository, times(1)).findById(1L);
    }

    @Test
    void testAlterarStatus_Success() {
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculo));
        when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculo);

        VeiculoDTO result = veiculoService.alterarStatus(1L, StatusVeiculo.IN_USE);

        assertNotNull(result);
        assertEquals(StatusVeiculo.IN_USE, result.getStatus());
        verify(veiculoRepository, times(1)).findById(1L);
        verify(veiculoRepository, times(1)).save(any(Veiculo.class));
    }

    @Test
    void testAlterarStatus_NotFound() {
        when(veiculoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> veiculoService.alterarStatus(1L, StatusVeiculo.IN_USE));
        verify(veiculoRepository, times(1)).findById(1L);
    }
}