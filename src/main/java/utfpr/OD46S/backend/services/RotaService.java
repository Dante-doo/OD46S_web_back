package utfpr.OD46S.backend.services;

import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import utfpr.OD46S.backend.dtos.RotaDTO;
import utfpr.OD46S.backend.entitys.Rota;
import utfpr.OD46S.backend.entitys.rotas.CreateRouteRequest;
import utfpr.OD46S.backend.repositorys.RotaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RotaService {

    private RotaRepository repository;

    private RotaPontoService service;

    public RotaService(RotaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public List<RotaDTO> getAllRoutes() {
        return repository.findAll()
                .stream()
                .map(RotaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public RotaDTO getRotaById(Long id) {
        Rota rota = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rota não encontrada com id: " + id));

        return RotaDTO.fromEntity(rota);
    }

    @Transactional
    public RotaDTO createRoute(CreateRouteRequest request) {
        Rota route = request.toEntity();
        Rota savedRoute = repository.save(route);
        return RotaDTO.fromEntity(savedRoute);
    }

    public RotaDTO changeRouteStatus(Long id, String status) {
        Rota rota = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rota não encontrada com id: " + id));

        rota.setStatus(Enum.valueOf(utfpr.OD46S.backend.enums.RotaStatus.class, status));
        Rota updatedRota = repository.save(rota);

        return RotaDTO.fromEntity(updatedRota);
    }

    public ResponseEntity<List<Rota>> findAll() {
        List<Rota> rotas = repository.findAll();
        return ResponseEntity.status(200).body(rotas);
    }
}
