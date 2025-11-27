package utfpr.OD46S.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utfpr.OD46S.backend.dtos.RouteExecutionDTO;
import utfpr.OD46S.backend.entitys.*;
import utfpr.OD46S.backend.enums.ExecutionStatus;
import utfpr.OD46S.backend.repositorys.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExecutionService {

    @Autowired
    private RouteExecutionRepository executionRepository;

    @Autowired
    private RouteAssignmentRepository assignmentRepository;

    @Autowired
    private MotoristaRepository motoristaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> listarExecutions(Long assignmentId, Long driverId, ExecutionStatus status,
                                                 LocalDate startDate, LocalDate endDate,
                                                 Integer page, Integer limit, String sort, String order) {
        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(direction, sort != null ? sort : "executionDate"));

        Page<RouteExecution> executionsPage = executionRepository.findByFilters(
                assignmentId, driverId, status, startDate, endDate, pageable);

        List<RouteExecutionDTO> dtos = executionsPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("current_page", page);
        pagination.put("total_pages", executionsPage.getTotalPages());
        pagination.put("total_items", executionsPage.getTotalElements());
        pagination.put("items_per_page", limit);

        Map<String, Object> data = new HashMap<>();
        data.put("executions", dtos);
        data.put("pagination", pagination);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obterExecutionPorId(Long id) {
        RouteExecution execution = executionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Execution not found"));

        RouteExecutionDTO dto = toDTO(execution);

        Map<String, Object> data = new HashMap<>();
        data.put("execution", dto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return response;
    }

    @Transactional
    public Map<String, Object> iniciarExecution(Map<String, Object> request, String driverEmail) {
        // Get authenticated driver
        Usuario user = usuarioRepository.findByEmail(driverEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Motorista driver = motoristaRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        // Check if driver has an active execution already
        executionRepository.findCurrentExecutionByDriverId(driver.getId())
                .ifPresent(e -> {
                    throw new RuntimeException("Driver already has an active execution");
                });

        Long assignmentId = getLongFromMap(request, "assignment_id");
        if (assignmentId == null) {
            throw new RuntimeException("assignment_id is required");
        }

        // Verify assignment exists and belongs to driver
        RouteAssignment assignment = assignmentRepository.findByIdWithDetails(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (!assignment.getDriver().getId().equals(driver.getId())) {
            throw new RuntimeException("Assignment does not belong to this driver");
        }

        LocalDate executionDate = LocalDate.now();

        // Check if execution already exists for today
        if (executionRepository.existsByAssignmentIdAndDate(assignmentId, executionDate)) {
            throw new RuntimeException("Execution already exists for this assignment today");
        }

        Integer initialKm = getIntegerFromMap(request, "initial_km");
        String initialNotes = (String) request.get("initial_notes");

        // Create execution
        RouteExecution execution = new RouteExecution(assignment, executionDate);
        execution.setStartTime(LocalDateTime.now());
        execution.setInitialKm(initialKm);
        execution.setInitialNotes(initialNotes);
        execution.setStatus(ExecutionStatus.IN_PROGRESS);

        executionRepository.save(execution);

        RouteExecutionDTO dto = toDTO(execution);

        Map<String, Object> data = new HashMap<>();
        data.put("execution", dto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "Execution started successfully");

        return response;
    }

    @Transactional
    public Map<String, Object> finalizarExecution(Long id, Map<String, Object> request) {
        RouteExecution execution = executionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Execution not found"));

        if (execution.getStatus() != ExecutionStatus.IN_PROGRESS) {
            throw new RuntimeException("Execution is not in progress");
        }

        execution.setStatus(ExecutionStatus.COMPLETED);
        execution.setEndTime(LocalDateTime.now());

        if (request.containsKey("final_km")) {
            execution.setFinalKm(getIntegerFromMap(request, "final_km"));
        }

        if (request.containsKey("total_collected_weight_kg")) {
            Object weightObj = request.get("total_collected_weight_kg");
            if (weightObj != null) {
                if (weightObj instanceof Number) {
                    execution.setTotalCollectedWeightKg(new BigDecimal(weightObj.toString()));
                } else if (weightObj instanceof String) {
                    execution.setTotalCollectedWeightKg(new BigDecimal((String) weightObj));
                }
            }
        }

        if (request.containsKey("points_visited")) {
            execution.setPointsVisited(getIntegerFromMap(request, "points_visited"));
        }

        if (request.containsKey("points_collected")) {
            execution.setPointsCollected(getIntegerFromMap(request, "points_collected"));
        }

        if (request.containsKey("final_notes")) {
            execution.setFinalNotes((String) request.get("final_notes"));
        }

        if (request.containsKey("problems_found")) {
            execution.setProblemsFound((String) request.get("problems_found"));
        }

        if (request.containsKey("driver_rating")) {
            Integer rating = getIntegerFromMap(request, "driver_rating");
            if (rating != null && (rating < 1 || rating > 5)) {
                throw new RuntimeException("Driver rating must be between 1 and 5");
            }
            execution.setDriverRating(rating);
        }

        executionRepository.save(execution);

        RouteExecutionDTO dto = toDTO(execution);

        Map<String, Object> data = new HashMap<>();
        data.put("execution", dto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "Execution completed successfully");

        return response;
    }

    @Transactional
    public Map<String, Object> cancelarExecution(Long id, Map<String, Object> request) {
        RouteExecution execution = executionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Execution not found"));

        if (execution.getStatus() != ExecutionStatus.IN_PROGRESS) {
            throw new RuntimeException("Execution is not in progress");
        }

        execution.setStatus(ExecutionStatus.CANCELLED);
        execution.setEndTime(LocalDateTime.now());

        String reason = (String) request.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("Cancellation reason is required");
        }
        execution.setCancellationReason(reason);

        executionRepository.save(execution);

        RouteExecutionDTO dto = toDTO(execution);

        Map<String, Object> data = new HashMap<>();
        data.put("execution", dto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "Execution cancelled successfully");

        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obterExecutionAtualDoMotorista(String driverEmail) {
        Usuario user = usuarioRepository.findByEmail(driverEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Motorista driver = motoristaRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        RouteExecution execution = executionRepository.findCurrentExecutionByDriverId(driver.getId())
                .orElse(null);

        if (execution == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", Map.of(
                    "code", "NO_ACTIVE_EXECUTION",
                    "message", "No active execution found for this driver"
            ));
            return response;
        }

        RouteExecutionDTO dto = toDTO(execution);

        Map<String, Object> data = new HashMap<>();
        data.put("execution", dto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return response;
    }

    // Helper methods
    private RouteExecutionDTO toDTO(RouteExecution execution) {
        RouteExecutionDTO dto = new RouteExecutionDTO();
        dto.setId(execution.getId());
        dto.setAssignmentId(execution.getAssignment().getId());
        dto.setExecutionDate(execution.getExecutionDate());
        dto.setStartTime(execution.getStartTime());
        dto.setEndTime(execution.getEndTime());
        dto.setStatus(execution.getStatus());
        dto.setInitialKm(execution.getInitialKm());
        dto.setFinalKm(execution.getFinalKm());
        dto.setTotalCollectedWeightKg(execution.getTotalCollectedWeightKg());
        dto.setPointsVisited(execution.getPointsVisited());
        dto.setPointsCollected(execution.getPointsCollected());
        dto.setInitialNotes(execution.getInitialNotes());
        dto.setFinalNotes(execution.getFinalNotes());
        dto.setProblemsFound(execution.getProblemsFound());
        dto.setCancellationReason(execution.getCancellationReason());
        dto.setDriverRating(execution.getDriverRating());
        dto.setCreatedAt(execution.getCreatedAt());

        // Assignment details
        RouteAssignment assignment = execution.getAssignment();
        RouteExecutionDTO.AssignmentDetailsDTO assignmentDTO = new RouteExecutionDTO.AssignmentDetailsDTO();
        assignmentDTO.setId(assignment.getId());

        // Route info
        Route route = assignment.getRoute();
        RouteExecutionDTO.RouteBasicDTO routeDTO = new RouteExecutionDTO.RouteBasicDTO();
        routeDTO.setId(route.getId());
        routeDTO.setName(route.getName());
        routeDTO.setCollectionType(route.getCollectionType().toString());
        assignmentDTO.setRoute(routeDTO);

        // Driver info
        Motorista driver = assignment.getDriver();
        Usuario driverUser = usuarioRepository.findById(driver.getId()).orElse(null);
        RouteExecutionDTO.DriverBasicDTO driverDTO = new RouteExecutionDTO.DriverBasicDTO();
        driverDTO.setId(driver.getId());
        driverDTO.setName(driverUser != null ? driverUser.getName() : "");
        driverDTO.setEmail(driverUser != null ? driverUser.getEmail() : "");
        assignmentDTO.setDriver(driverDTO);

        // Vehicle info
        Veiculo vehicle = assignment.getVehicle();
        RouteExecutionDTO.VehicleBasicDTO vehicleDTO = new RouteExecutionDTO.VehicleBasicDTO();
        vehicleDTO.setId(vehicle.getId());
        vehicleDTO.setLicensePlate(vehicle.getLicensePlate());
        vehicleDTO.setModel(vehicle.getModel());
        vehicleDTO.setBrand(vehicle.getBrand());
        assignmentDTO.setVehicle(vehicleDTO);

        dto.setAssignment(assignmentDTO);

        return dto;
    }

    private Long getLongFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            return Long.parseLong((String) value);
        }
        return null;
    }

    private Integer getIntegerFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        return null;
    }
}

