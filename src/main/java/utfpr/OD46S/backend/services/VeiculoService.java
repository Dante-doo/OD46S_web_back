package utfpr.OD46S.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utfpr.OD46S.backend.dtos.VeiculoDTO;
import utfpr.OD46S.backend.entitys.Veiculo;
import utfpr.OD46S.backend.enums.StatusVeiculo;
import utfpr.OD46S.backend.repositorys.VeiculoRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class VeiculoService {

    @Autowired
    private VeiculoRepository veiculoRepository;

    public List<VeiculoDTO> listarTodos() {
        return veiculoRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listarTodosPaginado(Integer page, Integer limit, String search, 
                                                     String status, Boolean active, String sort, String order) {
        // Valores padrão
        if (page == null || page < 1) page = 1;
        if (limit == null || limit < 1 || limit > 100) limit = 20;
        if (sort == null || sort.isEmpty()) sort = "licensePlate";
        // Mapear nomes de campos para nomes de propriedades da entidade
        if ("license_plate".equals(sort)) sort = "licensePlate";
        if (order == null || order.isEmpty()) order = "asc";

        // Criar objeto de paginação
        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortObj = Sort.by(direction, sort);
        Pageable pageable = PageRequest.of(page - 1, limit, sortObj);

        // Converter status string para enum se fornecido
        StatusVeiculo statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = StatusVeiculo.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Status inválido, ignorar
            }
        }

        // Buscar veículos
        Page<Veiculo> veiculosPage;
        if (search != null && !search.isEmpty()) {
            // Se houver busca, filtrar por placa ou modelo
            if (statusEnum != null && active != null) {
                veiculosPage = veiculoRepository.findByLicensePlateContainingIgnoreCaseOrModelContainingIgnoreCaseAndStatusAndActive(
                    search, search, statusEnum, active, pageable);
            } else if (statusEnum != null) {
                veiculosPage = veiculoRepository.findByLicensePlateContainingIgnoreCaseOrModelContainingIgnoreCaseAndStatus(
                    search, search, statusEnum, pageable);
            } else if (active != null) {
                veiculosPage = veiculoRepository.findByLicensePlateContainingIgnoreCaseOrModelContainingIgnoreCaseAndActive(
                    search, search, active, pageable);
            } else {
                veiculosPage = veiculoRepository.findByLicensePlateContainingIgnoreCaseOrModelContainingIgnoreCase(
                    search, search, pageable);
            }
        } else {
            // Sem busca
            if (statusEnum != null && active != null) {
                veiculosPage = veiculoRepository.findByStatusAndActive(statusEnum, active, pageable);
            } else if (statusEnum != null) {
                veiculosPage = veiculoRepository.findByStatus(statusEnum, pageable);
            } else if (active != null) {
                veiculosPage = veiculoRepository.findByActive(active, pageable);
            } else {
                veiculosPage = veiculoRepository.findAll(pageable);
            }
        }

        // Converter para DTOs
        List<VeiculoDTO> veiculoDTOs = veiculosPage.getContent().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());

        // Informações de paginação
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("current_page", page);
        pagination.put("per_page", limit);
        pagination.put("total", veiculosPage.getTotalElements());
        pagination.put("total_pages", veiculosPage.getTotalPages());
        pagination.put("has_next", veiculosPage.hasNext());
        pagination.put("has_prev", veiculosPage.hasPrevious());

        // Resposta
        Map<String, Object> data = new HashMap<>();
        data.put("vehicles", veiculoDTOs);
        data.put("pagination", pagination);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return response;
    }

    public VeiculoDTO cadastrar(VeiculoDTO dto) {
        if (veiculoRepository.existsByLicensePlate(dto.getLicensePlate())) {
            throw new RuntimeException("Placa já cadastrada");
        }
        Veiculo entity = toEntity(dto);
        entity.setId(null);
        if (entity.getStatus() == null) entity.setStatus(StatusVeiculo.AVAILABLE);
        if (entity.getActive() == null) entity.setActive(true);
        // Ensure timestamps are set to satisfy NOT NULL constraints
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return toDTO(veiculoRepository.save(entity));
    }

    public VeiculoDTO atualizar(Long id, VeiculoDTO dto) {
        Veiculo existente = veiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        if (!existente.getLicensePlate().equals(dto.getLicensePlate()) &&
                veiculoRepository.existsByLicensePlate(dto.getLicensePlate())) {
            throw new RuntimeException("Placa já cadastrada");
        }

        existente.setLicensePlate(dto.getLicensePlate());
        existente.setModel(dto.getModel());
        existente.setBrand(dto.getBrand());
        existente.setYear(dto.getYear());
        existente.setCapacityKg(dto.getCapacityKg());
        existente.setFuelType(dto.getFuelType());
        existente.setAverageConsumption(dto.getAverageConsumption());
        existente.setCurrentKm(dto.getCurrentKm());
        existente.setAcquisitionDate(dto.getAcquisitionDate());
        existente.setNotes(dto.getNotes());
        if (dto.getStatus() != null) existente.setStatus(dto.getStatus());
        if (dto.getActive() != null) existente.setActive(dto.getActive());

        return toDTO(veiculoRepository.save(existente));
    }

    public VeiculoDTO alterarStatus(Long id, StatusVeiculo status) {
        Veiculo existente = veiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));
        existente.setStatus(status);
        return toDTO(veiculoRepository.save(existente));
    }

    private VeiculoDTO toDTO(Veiculo v) {
        VeiculoDTO dto = new VeiculoDTO();
        dto.setId(v.getId());
        dto.setLicensePlate(v.getLicensePlate());
        dto.setModel(v.getModel());
        dto.setBrand(v.getBrand());
        dto.setYear(v.getYear());
        dto.setCapacityKg(v.getCapacityKg());
        dto.setFuelType(v.getFuelType());
        dto.setAverageConsumption(v.getAverageConsumption());
        dto.setStatus(v.getStatus());
        dto.setCurrentKm(v.getCurrentKm());
        dto.setAcquisitionDate(v.getAcquisitionDate());
        dto.setNotes(v.getNotes());
        dto.setActive(v.getActive());
        return dto;
    }

    private Veiculo toEntity(VeiculoDTO dto) {
        Veiculo v = new Veiculo();
        v.setId(dto.getId());
        v.setLicensePlate(dto.getLicensePlate());
        v.setModel(dto.getModel());
        v.setBrand(dto.getBrand());
        v.setYear(dto.getYear());
        v.setCapacityKg(dto.getCapacityKg());
        v.setFuelType(dto.getFuelType());
        v.setAverageConsumption(dto.getAverageConsumption());
        v.setStatus(dto.getStatus());
        v.setCurrentKm(dto.getCurrentKm());
        v.setAcquisitionDate(dto.getAcquisitionDate());
        v.setNotes(dto.getNotes());
        v.setActive(dto.getActive());
        return v;
    }
}


