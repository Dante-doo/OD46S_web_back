package utfpr.OD46S.backend.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utfpr.OD46S.backend.dtos.GeoJsonFeature;
import utfpr.OD46S.backend.dtos.GeoJsonFeatureCollection;
import utfpr.OD46S.backend.dtos.RouteDTO;
import utfpr.OD46S.backend.entitys.Route;
import utfpr.OD46S.backend.entitys.RouteArea;
import utfpr.OD46S.backend.enums.CollectionType;
import utfpr.OD46S.backend.repositorys.RouteAreaRepository;
import utfpr.OD46S.backend.repositorys.RouteRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RouteAreaService {

    @Autowired
    private RouteAreaRepository routeAreaRepository;

    @Autowired
    private RouteRepository routeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Import GeoJSON FeatureCollection and create/update route areas
     */
    public Map<String, Object> importGeoJson(GeoJsonFeatureCollection featureCollection, Long adminId) {
        int totalFeatures = featureCollection.getFeatures().size();
        int routesCreated = 0;
        int routesLinked = 0;
        int areasCreated = 0;
        int areasUpdated = 0;
        List<Map<String, Object>> errors = new ArrayList<>();

        for (int i = 0; i < featureCollection.getFeatures().size(); i++) {
            GeoJsonFeature feature = featureCollection.getFeatures().get(i);
            try {
                Map<String, Object> properties = feature.getProperties();
                if (properties == null) {
                    errors.add(createError(i, "Feature has no properties"));
                    continue;
                }

                String externalName = (String) properties.get("name");
                if (externalName == null || externalName.trim().isEmpty()) {
                    errors.add(createError(i, "Feature missing 'name' in properties"));
                    continue;
                }

                String wasteType = (String) properties.get("waste_type");
                if (wasteType == null || wasteType.trim().isEmpty()) {
                    errors.add(createError(i, "Feature missing 'waste_type' in properties"));
                    continue;
                }

                // Determine route_id
                Long routeId = null;
                if (properties.containsKey("route_id") && properties.get("route_id") != null) {
                    routeId = Long.valueOf(properties.get("route_id").toString());
                    if (!routeRepository.existsById(routeId)) {
                        errors.add(createError(i, "Route ID " + routeId + " does not exist"));
                        continue;
                    }
                    routesLinked++;
                } else {
                    // Try to find route by name
                    Optional<Route> existingRoute = routeRepository.findAll().stream()
                            .filter(r -> r.getName().equalsIgnoreCase(externalName))
                            .findFirst();

                    if (existingRoute.isPresent()) {
                        routeId = existingRoute.get().getId();
                        routesLinked++;
                    } else {
                        // Create new route
                        Route newRoute = new Route();
                        newRoute.setName(externalName);
                        newRoute.setDescription("Route created from map import");
                        newRoute.setCollectionType(mapWasteTypeToCollectionType(wasteType));
                        newRoute.setPeriodicity((String) properties.getOrDefault("periodicity", "0 8 * * *"));
                        newRoute.setCreatedBy(adminId);
                        newRoute.setActive(true);
                        newRoute = routeRepository.save(newRoute);
                        routeId = newRoute.getId();
                        routesCreated++;
                    }
                }

                // Create or update route area
                Optional<RouteArea> existingArea = routeAreaRepository
                        .findByRouteIdAndExternalName(routeId, externalName);

                RouteArea area;
                if (existingArea.isPresent()) {
                    area = existingArea.get();
                    areasUpdated++;
                } else {
                    area = new RouteArea();
                    areasCreated++;
                }

                area.setRoute(routeRepository.findById(routeId).orElseThrow());
                area.setExternalName(externalName);
                area.setWasteType(wasteType.toUpperCase());
                area.setGeometryGeojson(objectMapper.writeValueAsString(feature.getGeometry()));
                area.setStrokeColor((String) properties.getOrDefault("stroke_color", "#000000"));
                area.setFillColor((String) properties.getOrDefault("fill_color", "#000000"));
                
                Object fillOpacityObj = properties.get("fill_opacity");
                if (fillOpacityObj != null) {
                    area.setFillOpacity(new BigDecimal(fillOpacityObj.toString()));
                } else {
                    area.setFillOpacity(new BigDecimal("0.40"));
                }
                
                area.setActive(true);
                routeAreaRepository.save(area);

            } catch (Exception e) {
                errors.add(createError(i, "Error processing feature: " + e.getMessage()));
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("total_features", totalFeatures);
        summary.put("routes_created", routesCreated);
        summary.put("routes_linked", routesLinked);
        summary.put("areas_created", areasCreated);
        summary.put("areas_updated", areasUpdated);

        Map<String, Object> response = new HashMap<>();
        response.put("summary", summary);
        response.put("errors", errors);

        return response;
    }

    /**
     * Get all route areas as GeoJSON FeatureCollection
     */
    public GeoJsonFeatureCollection getGeoJsonFeatureCollection(String wasteType, Long routeId, Boolean active) {
        if (active == null) active = true;

        List<RouteArea> areas = routeAreaRepository.findWithFilters(active, wasteType, routeId);

        List<GeoJsonFeature> features = areas.stream().map(area -> {
            GeoJsonFeature feature = new GeoJsonFeature();
            Map<String, Object> properties = new HashMap<>();
            
            Route route = area.getRoute();
            properties.put("routeId", route.getId());
            properties.put("routeName", route.getName());
            properties.put("externalName", area.getExternalName());
            properties.put("wasteType", area.getWasteType());
            properties.put("collection_type", route.getCollectionType().name());
            properties.put("periodicity", route.getPeriodicity());
            properties.put("strokeColor", area.getStrokeColor());
            properties.put("fillColor", area.getFillColor());
            properties.put("fillOpacity", area.getFillOpacity());
            properties.put("active", area.getActive());
            
            feature.setProperties(properties);
            
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> geometry = objectMapper.readValue(area.getGeometryGeojson(), Map.class);
                feature.setGeometry(geometry);
            } catch (JsonProcessingException e) {
                // Log error but continue
            }
            
            return feature;
        }).collect(Collectors.toList());

        GeoJsonFeatureCollection collection = new GeoJsonFeatureCollection();
        collection.setFeatures(features);
        return collection;
    }

    /**
     * Get route areas for a specific route
     */
    public Map<String, Object> getRouteMap(Long routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        List<RouteArea> areas = routeAreaRepository.findByRouteIdAndActive(routeId, true);

        RouteDTO routeDTO = new RouteDTO();
        routeDTO.setId(route.getId());
        routeDTO.setName(route.getName());
        routeDTO.setCollectionType(route.getCollectionType());
        routeDTO.setPeriodicity(route.getPeriodicity());

        List<GeoJsonFeature> features = areas.stream().map(area -> {
            GeoJsonFeature feature = new GeoJsonFeature();
            Map<String, Object> properties = new HashMap<>();
            properties.put("areaId", area.getId());
            properties.put("externalName", area.getExternalName());
            properties.put("wasteType", area.getWasteType());
            properties.put("strokeColor", area.getStrokeColor());
            properties.put("fillColor", area.getFillColor());
            properties.put("fillOpacity", area.getFillOpacity());
            properties.put("active", area.getActive());
            
            feature.setProperties(properties);
            
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> geometry = objectMapper.readValue(area.getGeometryGeojson(), Map.class);
                feature.setGeometry(geometry);
            } catch (JsonProcessingException e) {
                // Log error but continue
            }
            
            return feature;
        }).collect(Collectors.toList());

        GeoJsonFeatureCollection geojson = new GeoJsonFeatureCollection();
        geojson.setFeatures(features);

        Map<String, Object> response = new HashMap<>();
        response.put("route", routeDTO);
        response.put("geojson", geojson);

        return response;
    }

    private Map<String, Object> createError(int index, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("feature_index", index);
        error.put("message", message);
        return error;
    }

    private CollectionType mapWasteTypeToCollectionType(String wasteType) {
        String upper = wasteType.toUpperCase();
        if (upper.equals("WET") || upper.equals("ORGANIC")) {
            return CollectionType.RESIDENTIAL;
        } else if (upper.equals("RECYCLE") || upper.equals("RECYCLABLE")) {
            return CollectionType.RECYCLABLE;
        } else if (upper.equals("SELECTIVE")) {
            return CollectionType.SELECTIVE;
        } else if (upper.equals("COMMERCIAL")) {
            return CollectionType.COMMERCIAL;
        } else if (upper.equals("HOSPITAL")) {
            return CollectionType.HOSPITAL;
        }
        return CollectionType.RESIDENTIAL; // default
    }

}

