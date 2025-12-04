package utfpr.OD46S.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import utfpr.OD46S.backend.dtos.GeoJsonFeatureCollection;
import utfpr.OD46S.backend.services.RouteAreaService;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Route Maps", description = "Gestão de áreas de rotas em mapas (GeoJSON)")
@RestController
@RequestMapping("/api/v1/routes/map")
@CrossOrigin(origins = "*")
public class RouteMapController {

    @Autowired
    private RouteAreaService routeAreaService;

    /**
     * Import GeoJSON FeatureCollection
     * POST /api/v1/routes/map/import-geojson
     */
    @Operation(summary = "Importar áreas de rotas via GeoJSON", 
               description = "Importa um FeatureCollection GeoJSON e cria/atualiza rotas e áreas de rotas")
    @PostMapping("/import-geojson")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> importGeoJson(@RequestBody GeoJsonFeatureCollection featureCollection) {
        try {
            Long adminId = getCurrentAdminId();
            Map<String, Object> result = routeAreaService.importGeoJson(featureCollection, adminId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            response.put("message", "Map imported successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", Map.of(
                "code", "IMPORT_ERROR",
                "message", e.getMessage()
            ));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Get all route areas as GeoJSON
     * GET /api/v1/routes/map/geo
     */
    @Operation(summary = "Obter todas as áreas de rotas em formato GeoJSON",
               description = "Retorna um FeatureCollection com todas as áreas de rotas, opcionalmente filtradas")
    @GetMapping("/geo")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER')")
    public ResponseEntity<Map<String, Object>> getGeoJson(
            @RequestParam(required = false) String wasteType,
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) Boolean active) {
        try {
            GeoJsonFeatureCollection geojson = routeAreaService.getGeoJsonFeatureCollection(wasteType, routeId, active);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("geojson", geojson));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", Map.of(
                "code", "GEOJSON_ERROR",
                "message", e.getMessage()
            ));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private Long getCurrentAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            // TODO: Extract actual admin ID from JWT
            return 1L; // Default for now
        }
        return 1L;
    }
}

