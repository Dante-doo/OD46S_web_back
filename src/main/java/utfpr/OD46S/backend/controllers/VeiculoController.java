package utfpr.OD46S.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import utfpr.OD46S.backend.dtos.VeiculoDTO;
import utfpr.OD46S.backend.enums.StatusVeiculo;
import utfpr.OD46S.backend.services.VeiculoService;

import java.util.List;

@Tag(name = "Veículos", description = "Gestão de veículos")
@RestController
@RequestMapping("/api/v1/vehicles")
@CrossOrigin(origins = "*")
public class VeiculoController {

    @Autowired
    private VeiculoService veiculoService;

    @Operation(summary = "Listar veículos")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER')")
    public ResponseEntity<List<VeiculoDTO>> listar() {
        return ResponseEntity.ok(veiculoService.listarTodos());
    }

    @Operation(summary = "Cadastrar veículo")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VeiculoDTO> cadastrar(@RequestBody VeiculoDTO dto) {
        return ResponseEntity.ok(veiculoService.cadastrar(dto));
    }

    @Operation(summary = "Atualizar veículo")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VeiculoDTO> atualizar(@PathVariable Long id, @RequestBody VeiculoDTO dto) {
        return ResponseEntity.ok(veiculoService.atualizar(id, dto));
    }

    @Operation(summary = "Alterar status do veículo")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER')")
    public ResponseEntity<VeiculoDTO> alterarStatus(@PathVariable Long id, @RequestParam StatusVeiculo status) {
        return ResponseEntity.ok(veiculoService.alterarStatus(id, status));
    }
}


