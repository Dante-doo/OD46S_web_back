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
import utfpr.OD46S.backend.enums.StatusVeiculo;
import utfpr.OD46S.backend.repositorys.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
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

        // Permite múltiplas atribuições ativas para o mesmo motorista
        // O sistema permite que um motorista tenha várias rotas atribuídas,
        // mas apenas uma execução ativa por vez (validado no ExecutionService)
        
        // Verifica conflitos de datas E periodicidade para o veículo
        // Um veículo não pode estar em dois lugares ao mesmo tempo
        // Mas pode ter rotas em dias diferentes da semana (ex: terça e quinta)
        LocalDate checkEndDate = endDate != null ? endDate : startDate.plusYears(100); // Far future for open-ended
        
        // Extrai os dias da semana da nova rota
        Set<DayOfWeek> newRouteDaysOfWeek = extractDaysOfWeekFromPeriodicity(route.getPeriodicity(), startDate);
        
        List<RouteAssignment> existingVehicleAssignments = assignmentRepository.findByVehicleIdAndStatus(vehicleId, AssignmentStatus.ACTIVE);
        for (RouteAssignment existing : existingVehicleAssignments) {
            // Verifica se há sobreposição de períodos de datas
            boolean hasDateOverlap = false;
            
            if (existing.getEndDate() == null) {
                // Atribuição existente sem data de fim - conflita se começar antes do fim da nova
                hasDateOverlap = existing.getStartDate().isBefore(checkEndDate) || existing.getStartDate().isEqual(checkEndDate);
            } else {
                // Verifica sobreposição de intervalos
                hasDateOverlap = (existing.getStartDate().isBefore(checkEndDate) || existing.getStartDate().isEqual(checkEndDate))
                    && (existing.getEndDate().isAfter(startDate) || existing.getEndDate().isEqual(startDate));
            }
            
            if (hasDateOverlap) {
                // Se há sobreposição de datas, verifica se os dias da semana conflitam
                Set<DayOfWeek> existingRouteDaysOfWeek = extractDaysOfWeekFromPeriodicity(
                    existing.getRoute().getPeriodicity(), existing.getStartDate());
                
                // Verifica se há interseção entre os dias da semana
                Set<DayOfWeek> intersection = new HashSet<>(newRouteDaysOfWeek);
                intersection.retainAll(existingRouteDaysOfWeek);
                
                if (!intersection.isEmpty()) {
                    // Há conflito: mesmo veículo, mesmo período de datas, mesmo(s) dia(s) da semana
                    throw new RuntimeException("Vehicle already has an active assignment that conflicts with the requested schedule. " +
                        "Period: " + existing.getStartDate() + " to " + 
                        (existing.getEndDate() != null ? existing.getEndDate() : "open-ended") + 
                        ", Days: " + formatDaysOfWeek(existingRouteDaysOfWeek) +
                        ". New assignment would conflict on days: " + formatDaysOfWeek(intersection));
                }
                // Se não há interseção de dias da semana, permite (ex: terça vs quinta)
            }
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
    
    /**
     * Extrai os dias da semana da periodicidade da rota.
     * Suporta:
     * - Formato cron: "0 8 * * 1,3,5" (minuto hora dia mês dia-da-semana)
     *   No cron, 0=domingo, 1=segunda, 2=terça, 3=quarta, 4=quinta, 5=sexta, 6=sábado
     * - Formato texto: "TERÇA", "QUINTA", "SEGUNDA,QUARTA", "MONDAY,WEDNESDAY", etc.
     * Se não conseguir extrair, assume que a rota ocorre no dia da semana da data de início.
     */
    private Set<DayOfWeek> extractDaysOfWeekFromPeriodicity(String periodicity, LocalDate startDate) {
        Set<DayOfWeek> days = new HashSet<>();
        
        if (periodicity == null || periodicity.trim().isEmpty()) {
            // Se não há periodicidade, assume o dia da semana da data de início
            days.add(startDate.getDayOfWeek());
            return days;
        }
        
        String periodicityTrimmed = periodicity.trim();
        
        // Verifica se é formato cron (contém espaços e números no padrão cron)
        // Formato cron típico: "0 8 * * 1,3,5" ou "0 8 * * 2"
        // Padrão: número espaço número espaço * espaço * espaço (número ou vírgula ou *)
        if (periodicityTrimmed.matches("\\d+\\s+\\d+\\s+\\*\\s+\\*\\s+[0-6,*\\s]+")) {
            // Formato cron: "0 8 * * 1,3,5"
            // Extrai o último campo (dia da semana)
            String[] cronParts = periodicityTrimmed.split("\\s+");
            if (cronParts.length >= 5) {
                String dayOfWeekField = cronParts[4]; // Último campo é o dia da semana
                
                // Mapeamento cron para DayOfWeek (cron: 0=domingo, 1=segunda, ..., 6=sábado)
                Map<Integer, DayOfWeek> cronDayMap = new HashMap<>();
                cronDayMap.put(0, DayOfWeek.SUNDAY);    // Domingo
                cronDayMap.put(1, DayOfWeek.MONDAY);    // Segunda
                cronDayMap.put(2, DayOfWeek.TUESDAY);   // Terça
                cronDayMap.put(3, DayOfWeek.WEDNESDAY); // Quarta
                cronDayMap.put(4, DayOfWeek.THURSDAY);  // Quinta
                cronDayMap.put(5, DayOfWeek.FRIDAY);   // Sexta
                cronDayMap.put(6, DayOfWeek.SATURDAY); // Sábado
                
                // Pode ter múltiplos dias separados por vírgula: "1,3,5"
                String[] dayNumbers = dayOfWeekField.split(",");
                for (String dayNumStr : dayNumbers) {
                    dayNumStr = dayNumStr.trim();
                    if (dayNumStr.equals("*")) {
                        // * significa todos os dias
                        days.addAll(cronDayMap.values());
                        break;
                    }
                    try {
                        int dayNum = Integer.parseInt(dayNumStr);
                        if (cronDayMap.containsKey(dayNum)) {
                            days.add(cronDayMap.get(dayNum));
                        }
                    } catch (NumberFormatException e) {
                        // Ignora valores não numéricos
                    }
                }
            }
        } else {
            // Formato texto: "TERÇA", "QUINTA", "SEGUNDA,QUARTA", etc.
            String periodicityUpper = periodicityTrimmed.toUpperCase();
            
            // Mapeamento de nomes de dias em português e inglês
            Map<String, DayOfWeek> dayMap = new HashMap<>();
            dayMap.put("SEGUNDA", DayOfWeek.MONDAY);
            dayMap.put("MONDAY", DayOfWeek.MONDAY);
            dayMap.put("TERÇA", DayOfWeek.TUESDAY);
            dayMap.put("TERCA", DayOfWeek.TUESDAY);
            dayMap.put("TUESDAY", DayOfWeek.TUESDAY);
            dayMap.put("QUARTA", DayOfWeek.WEDNESDAY);
            dayMap.put("WEDNESDAY", DayOfWeek.WEDNESDAY);
            dayMap.put("QUINTA", DayOfWeek.THURSDAY);
            dayMap.put("THURSDAY", DayOfWeek.THURSDAY);
            dayMap.put("SEXTA", DayOfWeek.FRIDAY);
            dayMap.put("FRIDAY", DayOfWeek.FRIDAY);
            dayMap.put("SÁBADO", DayOfWeek.SATURDAY);
            dayMap.put("SABADO", DayOfWeek.SATURDAY);
            dayMap.put("SATURDAY", DayOfWeek.SATURDAY);
            dayMap.put("DOMINGO", DayOfWeek.SUNDAY);
            dayMap.put("SUNDAY", DayOfWeek.SUNDAY);
            
            // Tenta extrair dias separados por vírgula, ponto e vírgula, ou espaço
            String[] parts = periodicityUpper.split("[,;\\s]+");
            
            for (String part : parts) {
                part = part.trim();
                if (dayMap.containsKey(part)) {
                    days.add(dayMap.get(part));
                }
            }
        }
        
        // Se não encontrou nenhum dia, assume o dia da semana da data de início
        if (days.isEmpty()) {
            days.add(startDate.getDayOfWeek());
        }
        
        return days;
    }
    
    /**
     * Formata os dias da semana para exibição em mensagens de erro
     */
    private String formatDaysOfWeek(Set<DayOfWeek> days) {
        Map<DayOfWeek, String> dayNames = new HashMap<>();
        dayNames.put(DayOfWeek.MONDAY, "Segunda");
        dayNames.put(DayOfWeek.TUESDAY, "Terça");
        dayNames.put(DayOfWeek.WEDNESDAY, "Quarta");
        dayNames.put(DayOfWeek.THURSDAY, "Quinta");
        dayNames.put(DayOfWeek.FRIDAY, "Sexta");
        dayNames.put(DayOfWeek.SATURDAY, "Sábado");
        dayNames.put(DayOfWeek.SUNDAY, "Domingo");
        
        return days.stream()
                .sorted()
                .map(dayNames::get)
                .collect(Collectors.joining(", "));
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

