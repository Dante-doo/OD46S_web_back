package utfpr.OD46S.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utfpr.OD46S.backend.dtos.RouteAssignmentDTO;
import utfpr.OD46S.backend.entitys.*;
import utfpr.OD46S.backend.enums.AssignmentStatus;
import utfpr.OD46S.backend.enums.StatusMotorista;
import utfpr.OD46S.backend.enums.StatusVeiculo;
import utfpr.OD46S.backend.repositorys.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssignmentService {

    @Autowired
    private RouteAssignmentRepository assignmentRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private MotoristaRepository motoristaRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> listarAssignments(Long routeId, Long driverId, Long vehicleId,
                                                  AssignmentStatus status, Integer page, Integer limit,
                                                  String sort, String order) {
        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(direction, sort != null ? sort : "createdAt"));

        Page<RouteAssignment> assignmentsPage = assignmentRepository.findAllWithFilters(
                routeId, driverId, vehicleId, status, pageable);

        List<RouteAssignmentDTO> dtos = assignmentsPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("current_page", page);
        pagination.put("total_pages", assignmentsPage.getTotalPages());
        pagination.put("total_items", assignmentsPage.getTotalElements());
        pagination.put("items_per_page", limit);

        Map<String, Object> data = new HashMap<>();
        data.put("assignments", dtos);
        data.put("pagination", pagination);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obterAssignmentPorId(Long id) {
        RouteAssignment assignment = assignmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        RouteAssignmentDTO dto = toDTO(assignment);

        Map<String, Object> data = new HashMap<>();
        data.put("assignment", dto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return response;
    }

    @Transactional
    public Map<String, Object> criarAssignment(Map<String, Object> request, String authenticatedUserEmail) {
        // Get authenticated admin
        Usuario user = usuarioRepository.findByEmail(authenticatedUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Administrator admin = administratorRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Administrator not found"));

        Long routeId = getLongFromMap(request, "route_id");
        Long driverId = getLongFromMap(request, "driver_id");
        Long vehicleId = getLongFromMap(request, "vehicle_id");
        String startDateStr = (String) request.get("start_date");
        String endDateStr = (String) request.get("end_date");
        String notes = (String) request.get("notes");

        // Validate required fields
        if (routeId == null || driverId == null || vehicleId == null || startDateStr == null) {
            throw new RuntimeException("Missing required fields: route_id, driver_id, vehicle_id, start_date");
        }

        // Fetch entities
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        Motorista driver = motoristaRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        Veiculo vehicle = veiculoRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        // Validations
        if (!route.getActive()) {
            throw new RuntimeException("Route is not active");
        }

        if (!driver.getEnabled()) {
            throw new RuntimeException("Driver is not enabled");
        }

        if (vehicle.getStatus() != StatusVeiculo.AVAILABLE || !vehicle.getActive()) {
            throw new RuntimeException("Vehicle is not available or active");
        }

        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = endDateStr != null ? LocalDate.parse(endDateStr) : null;

        // Check for conflicts
        LocalDate checkEndDate = endDate != null ? endDate : startDate.plusYears(100); // Far future for open-ended

        if (assignmentRepository.existsActiveAssignmentForDriverInPeriod(driverId, startDate, checkEndDate)) {
            throw new RuntimeException("Driver already has an active assignment for this period");
        }

        if (assignmentRepository.existsActiveAssignmentForVehicleInPeriod(vehicleId, startDate, checkEndDate)) {
            throw new RuntimeException("Vehicle already has an active assignment for this period");
        }

        // Create assignment
        RouteAssignment assignment = new RouteAssignment(route, driver, vehicle, startDate, admin);
        assignment.setEndDate(endDate);
        assignment.setNotes(notes);

        assignmentRepository.save(assignment);

        RouteAssignmentDTO dto = toDTO(assignment);

        Map<String, Object> data = new HashMap<>();
        data.put("assignment", dto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "Assignment created successfully");

        return response;
    }

    @Transactional
    public Map<String, Object> atualizarAssignment(Long id, Map<String, Object> request) {
        RouteAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // Update route if provided
        if (request.containsKey("route_id")) {
            Long routeId = getLongFromMap(request, "route_id");
            Route route = routeRepository.findById(routeId)
                    .orElseThrow(() -> new RuntimeException("Route not found"));
            if (!route.getActive()) {
                throw new RuntimeException("Route is not active");
            }
            assignment.setRoute(route);
        }

        // Update driver if provided
        if (request.containsKey("driver_id")) {
            Long driverId = getLongFromMap(request, "driver_id");
            Motorista driver = motoristaRepository.findById(driverId)
                    .orElseThrow(() -> new RuntimeException("Driver not found"));
            if (!driver.getEnabled()) {
                throw new RuntimeException("Driver is not enabled");
            }
            assignment.setDriver(driver);
        }

        // Update vehicle if provided
        if (request.containsKey("vehicle_id")) {
            Long vehicleId = getLongFromMap(request, "vehicle_id");
            Veiculo vehicle = veiculoRepository.findById(vehicleId)
                    .orElseThrow(() -> new RuntimeException("Vehicle not found"));
            if (!vehicle.getActive()) {
                throw new RuntimeException("Vehicle is not active");
            }
            assignment.setVehicle(vehicle);
        }

        // Update dates if provided
        if (request.containsKey("start_date")) {
            assignment.setStartDate(LocalDate.parse((String) request.get("start_date")));
        }

        if (request.containsKey("end_date")) {
            String endDateStr = (String) request.get("end_date");
            assignment.setEndDate(endDateStr != null ? LocalDate.parse(endDateStr) : null);
        }

        // Update notes if provided
        if (request.containsKey("notes")) {
            assignment.setNotes((String) request.get("notes"));
        }

        assignmentRepository.save(assignment);

        RouteAssignmentDTO dto = toDTO(assignment);

        Map<String, Object> data = new HashMap<>();
        data.put("assignment", dto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "Assignment updated successfully");

        return response;
    }

    @Transactional
    public Map<String, Object> desativarAssignment(Long id, Map<String, Object> request) {
        RouteAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        assignment.setStatus(AssignmentStatus.INACTIVE);

        if (request.containsKey("end_date")) {
            String endDateStr = (String) request.get("end_date");
            assignment.setEndDate(LocalDate.parse(endDateStr));
        } else {
            assignment.setEndDate(LocalDate.now());
        }

        if (request.containsKey("reason")) {
            String reason = (String) request.get("reason");
            String currentNotes = assignment.getNotes() != null ? assignment.getNotes() : "";
            assignment.setNotes(currentNotes + "\nDeactivated: " + reason);
        }

        assignmentRepository.save(assignment);

        RouteAssignmentDTO dto = toDTO(assignment);

        Map<String, Object> data = new HashMap<>();
        data.put("assignment", dto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "Assignment deactivated successfully");

        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obterAssignmentAtualDoMotorista(String driverEmail) {
        Usuario user = usuarioRepository.findByEmail(driverEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Motorista driver = motoristaRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        List<RouteAssignment> activeAssignments = assignmentRepository.findActiveAssignmentsByDriverId(
                driver.getId(), LocalDate.now());

        if (activeAssignments.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", Map.of(
                    "code", "NO_ACTIVE_ASSIGNMENT",
                    "message", "No active assignment found for this driver"
            ));
            return response;
        }

        // Return the first active assignment (assuming one active assignment per driver)
        RouteAssignment assignment = activeAssignments.get(0);
        RouteAssignmentDTO dto = toDTO(assignment);

        Map<String, Object> data = new HashMap<>();
        data.put("assignment", dto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return response;
    }

    // Helper methods
    private RouteAssignmentDTO toDTO(RouteAssignment assignment) {
        RouteAssignmentDTO dto = new RouteAssignmentDTO();
        dto.setId(assignment.getId());
        dto.setStatus(assignment.getStatus());
        dto.setStartDate(assignment.getStartDate());
        dto.setEndDate(assignment.getEndDate());
        dto.setNotes(assignment.getNotes());
        dto.setCreatedBy(assignment.getCreatedBy().getId());
        dto.setCreatedAt(assignment.getCreatedAt());
        dto.setUpdatedAt(assignment.getUpdatedAt());

        // Route info
        Route route = assignment.getRoute();
        dto.setRoute(new RouteAssignmentDTO.RouteBasicDTO(
                route.getId(),
                route.getName(),
                route.getPeriodicity(),
                route.getCollectionType().toString()
        ));

        // Driver info
        Motorista driver = assignment.getDriver();
        Usuario driverUser = usuarioRepository.findById(driver.getId())
                .orElse(null);
        dto.setDriver(new RouteAssignmentDTO.DriverBasicDTO(
                driver.getId(),
                driverUser != null ? driverUser.getName() : "",
                driverUser != null ? driverUser.getEmail() : "",
                driver.getLicenseNumber()
        ));

        // Vehicle info
        Veiculo vehicle = assignment.getVehicle();
        dto.setVehicle(new RouteAssignmentDTO.VehicleBasicDTO(
                vehicle.getId(),
                vehicle.getLicensePlate(),
                vehicle.getModel(),
                vehicle.getYear()
        ));

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
}

