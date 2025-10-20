package utfpr.OD46S.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import utfpr.OD46S.backend.dtos.RouteCollectionPointDTO;
import utfpr.OD46S.backend.dtos.RouteDTO;
import utfpr.OD46S.backend.entitys.Route;
import utfpr.OD46S.backend.entitys.RouteCollectionPoint;
import utfpr.OD46S.backend.enums.CollectionType;
import utfpr.OD46S.backend.enums.Priority;
import utfpr.OD46S.backend.repositorys.RouteCollectionPointRepository;
import utfpr.OD46S.backend.repositorys.RouteRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class RouteService {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private RouteCollectionPointRepository pointRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public Map<String, Object> listarTodos(
        Integer page,
        Integer limit,
        String search,
        String collectionType,
        String priority,
        Boolean active,
        String sort,
        String order
    ) {
        // Default values
        if (page == null || page < 1) page = 1;
        if (limit == null || limit < 1) limit = 20;
        if (limit > 100) limit = 100;
        if (sort == null) sort = "name";
        if (order == null) order = "asc";

        // Create sorting
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortObj = Sort.by(direction, sort);
        Pageable pageable = PageRequest.of(page - 1, limit, sortObj);

        // Convert string enums to enum types
        CollectionType collectionTypeEnum = null;
        if (collectionType != null && !collectionType.isEmpty()) {
            try {
                collectionTypeEnum = CollectionType.valueOf(collectionType.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid enum value, ignore
            }
        }

        Priority priorityEnum = null;
        if (priority != null && !priority.isEmpty()) {
            try {
                priorityEnum = Priority.valueOf(priority.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid enum value, ignore
            }
        }

        // Find routes
        Page<Route> routesPage = routeRepository.findByFilters(
            search,
            collectionTypeEnum,
            priorityEnum,
            active,
            pageable
        );

        // Convert to DTOs
        List<RouteDTO> routeDTOs = routesPage.getContent().stream()
            .map(this::toDTOWithoutPoints)
            .collect(Collectors.toList());

        // Pagination info
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("current_page", page);
        pagination.put("per_page", limit);
        pagination.put("total", routesPage.getTotalElements());
        pagination.put("total_pages", routesPage.getTotalPages());
        pagination.put("has_next", routesPage.hasNext());
        pagination.put("has_prev", routesPage.hasPrevious());

        // Response
        Map<String, Object> data = new HashMap<>();
        data.put("routes", routeDTOs);
        data.put("pagination", pagination);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return response;
    }

    public Map<String, Object> buscarPorId(Long id) {
        Route route = routeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Route not found"));

        RouteDTO dto = toDTOWithPoints(route);

        Map<String, Object> data = new HashMap<>();
        data.put("route", dto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return response;
    }

    public Map<String, Object> criar(RouteDTO dto, Long createdBy) {
        Route route = new Route();
        route.setName(dto.getName());
        route.setDescription(dto.getDescription());
        route.setCollectionType(dto.getCollectionType());
        route.setPeriodicity(dto.getPeriodicity());
        route.setPriority(dto.getPriority() != null ? dto.getPriority() : Priority.MEDIUM);
        route.setEstimatedTimeMinutes(dto.getEstimatedTimeMinutes());
        route.setDistanceKm(dto.getDistanceKm());
        route.setActive(dto.getActive() != null ? dto.getActive() : true);
        route.setNotes(dto.getNotes());
        route.setCreatedBy(createdBy);

        // Save route
        Route savedRoute = routeRepository.save(route);

        // Save collection points if provided
        if (dto.getCollectionPoints() != null && !dto.getCollectionPoints().isEmpty()) {
            for (RouteCollectionPointDTO pointDTO : dto.getCollectionPoints()) {
                RouteCollectionPoint point = new RouteCollectionPoint();
                point.setRoute(savedRoute);
                point.setSequenceOrder(pointDTO.getSequenceOrder());
                point.setAddress(pointDTO.getAddress());
                point.setLatitude(pointDTO.getLatitude());
                point.setLongitude(pointDTO.getLongitude());
                point.setWasteType(pointDTO.getWasteType());
                point.setEstimatedCapacityKg(pointDTO.getEstimatedCapacityKg());
                point.setCollectionFrequency(pointDTO.getCollectionFrequency());
                point.setNotes(pointDTO.getNotes());
                point.setActive(pointDTO.getActive() != null ? pointDTO.getActive() : true);
                
                savedRoute.addCollectionPoint(point);
            }
            savedRoute = routeRepository.save(savedRoute);
        }

        RouteDTO resultDTO = toDTOWithPoints(savedRoute);

        Map<String, Object> data = new HashMap<>();
        data.put("route", resultDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "Route created successfully");

        return response;
    }

    public Map<String, Object> adicionarPonto(Long routeId, RouteCollectionPointDTO pointDTO) {
        Route route = routeRepository.findById(routeId)
            .orElseThrow(() -> new RuntimeException("Route not found"));

        // Get next sequence order if not provided
        Integer sequenceOrder = pointDTO.getSequenceOrder();
        if (sequenceOrder == null) {
            Integer maxOrder = pointRepository.findMaxSequenceOrderByRouteId(routeId).orElse(0);
            sequenceOrder = maxOrder + 1;
        }

        // Check if sequence order already exists
        if (pointRepository.existsByRouteIdAndSequenceOrder(routeId, sequenceOrder)) {
            throw new RuntimeException("Sequence order already exists for this route");
        }

        RouteCollectionPoint point = new RouteCollectionPoint();
        point.setRoute(route);
        point.setSequenceOrder(sequenceOrder);
        point.setAddress(pointDTO.getAddress());
        point.setLatitude(pointDTO.getLatitude());
        point.setLongitude(pointDTO.getLongitude());
        point.setWasteType(pointDTO.getWasteType());
        point.setEstimatedCapacityKg(pointDTO.getEstimatedCapacityKg());
        point.setCollectionFrequency(pointDTO.getCollectionFrequency());
        point.setNotes(pointDTO.getNotes());
        point.setActive(pointDTO.getActive() != null ? pointDTO.getActive() : true);

        RouteCollectionPoint savedPoint = pointRepository.save(point);
        RouteCollectionPointDTO resultDTO = toPointDTO(savedPoint);

        Map<String, Object> data = new HashMap<>();
        data.put("point", resultDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "Collection point added successfully");

        return response;
    }

    public Map<String, Object> reordenarPontos(Long routeId, List<Map<String, Integer>> reorderList) {
        Route route = routeRepository.findById(routeId)
            .orElseThrow(() -> new RuntimeException("Route not found"));

        List<RouteCollectionPoint> points = pointRepository.findByRouteIdOrderBySequenceOrderAsc(routeId);

        // Create a map of point ID to new sequence order
        Map<Long, Integer> newOrderMap = new HashMap<>();
        for (Map<String, Integer> item : reorderList) {
            Long pointId = item.get("id").longValue();
            Integer newOrder = item.get("sequence_order");
            newOrderMap.put(pointId, newOrder);
        }

        // First pass: set temporary negative values to avoid unique constraint violation
        for (RouteCollectionPoint point : points) {
            Integer newOrder = newOrderMap.get(point.getId());
            if (newOrder != null) {
                point.setSequenceOrder(-1 * point.getId().intValue()); // Temporary negative value
            }
        }
        pointRepository.saveAll(points);
        entityManager.flush();

        // Second pass: set final values
        for (RouteCollectionPoint point : points) {
            Integer newOrder = newOrderMap.get(point.getId());
            if (newOrder != null) {
                point.setSequenceOrder(newOrder);
            }
        }
        pointRepository.saveAll(points);
        entityManager.flush();
        entityManager.clear();

        // Reload route to get updated points
        route = routeRepository.findById(routeId)
            .orElseThrow(() -> new RuntimeException("Route not found"));

        // Return updated route
        RouteDTO resultDTO = toDTOWithPoints(route);

        Map<String, Object> data = new HashMap<>();
        data.put("route", resultDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "Collection points reordered successfully");

        return response;
    }

    private RouteDTO toDTOWithoutPoints(Route route) {
        RouteDTO dto = new RouteDTO();
        dto.setId(route.getId());
        dto.setName(route.getName());
        dto.setDescription(route.getDescription());
        dto.setCollectionType(route.getCollectionType());
        dto.setPeriodicity(route.getPeriodicity());
        dto.setPriority(route.getPriority());
        dto.setEstimatedTimeMinutes(route.getEstimatedTimeMinutes());
        dto.setDistanceKm(route.getDistanceKm());
        dto.setActive(route.getActive());
        dto.setNotes(route.getNotes());
        dto.setCreatedBy(route.getCreatedBy());
        dto.setCreatedAt(route.getCreatedAt());
        dto.setUpdatedAt(route.getUpdatedAt());
        dto.setCollectionPointsCount(route.getCollectionPoints() != null ? route.getCollectionPoints().size() : 0);
        return dto;
    }

    private RouteDTO toDTOWithPoints(Route route) {
        RouteDTO dto = toDTOWithoutPoints(route);
        
        if (route.getCollectionPoints() != null) {
            List<RouteCollectionPointDTO> pointDTOs = route.getCollectionPoints().stream()
                .map(this::toPointDTO)
                .collect(Collectors.toList());
            dto.setCollectionPoints(pointDTOs);
        } else {
            dto.setCollectionPoints(new ArrayList<>());
        }
        
        return dto;
    }

    private RouteCollectionPointDTO toPointDTO(RouteCollectionPoint point) {
        RouteCollectionPointDTO dto = new RouteCollectionPointDTO();
        dto.setId(point.getId());
        dto.setRouteId(point.getRoute() != null ? point.getRoute().getId() : null);
        dto.setSequenceOrder(point.getSequenceOrder());
        dto.setAddress(point.getAddress());
        dto.setLatitude(point.getLatitude());
        dto.setLongitude(point.getLongitude());
        dto.setWasteType(point.getWasteType());
        dto.setEstimatedCapacityKg(point.getEstimatedCapacityKg());
        dto.setCollectionFrequency(point.getCollectionFrequency());
        dto.setNotes(point.getNotes());
        dto.setActive(point.getActive());
        return dto;
    }
}

