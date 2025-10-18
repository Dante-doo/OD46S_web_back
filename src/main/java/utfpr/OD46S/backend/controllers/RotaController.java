package utfpr.OD46S.backend.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utfpr.OD46S.backend.dtos.RotaDTO;
import utfpr.OD46S.backend.entitys.Rota;
import utfpr.OD46S.backend.entitys.rotas.CreateCollectionPointRequest;
import utfpr.OD46S.backend.entitys.rotas.ReorderPointsRequest;
import utfpr.OD46S.backend.services.RotaPontoService;
import utfpr.OD46S.backend.services.RotaService;

import java.util.List;

@Tag(name = "Rotas", description = "APIs de gerenciamento de rotas")
@RestController
@RequestMapping("/api/v1/routes")
@CrossOrigin(origins = "*")
public class RotaController {


    private final RotaService rotaService;
    private RotaService service;
    private RotaPontoService rotaPontoService;

    public RotaController(RotaService rotaService) {
        this.rotaService = rotaService;
    }

    @GetMapping
    public ResponseEntity<List<RotaDTO>> listRoutes() {
        List<RotaDTO> routes = service.getAllRoutes();
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RotaDTO> getRoute(@PathVariable Long id) {
        RotaDTO route = service.getRotaById(id);
        return ResponseEntity.ok(route);
    }

    @PostMapping("/{id}/points")
    public ResponseEntity<RotaDTO> addCollectionPoint(
            @PathVariable Long id,
            @RequestBody CreateCollectionPointRequest request) {
        RotaDTO rota = rotaService.getRotaById(id);
        RotaDTO route = rotaPontoService.AddPointToRoute(rota, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(route);
    }

    @PutMapping("/{id}/points/reorder")
    public ResponseEntity<RotaDTO> reorderPoints(
            @PathVariable Long id,
            @RequestBody ReorderPointsRequest request) {
        RotaDTO route = rotaPontoService.reorderPoints(id, request);
        return ResponseEntity.ok(route);
    }
}
