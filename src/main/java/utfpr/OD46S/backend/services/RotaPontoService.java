package utfpr.OD46S.backend.services;

import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import utfpr.OD46S.backend.dtos.RotaDTO;
import utfpr.OD46S.backend.entitys.Rota;
import utfpr.OD46S.backend.entitys.RotaPonto;
import utfpr.OD46S.backend.entitys.rotas.CreateCollectionPointRequest;
import utfpr.OD46S.backend.entitys.rotas.ReorderPointsRequest;
import utfpr.OD46S.backend.repositorys.RotaPontoRepository;
import utfpr.OD46S.backend.repositorys.RotaRepository;

import java.util.List;

@Service
public class RotaPontoService {

    private final RotaPontoRepository repository;
    private final RotaRepository rotaRepository;
    private final RotaService rotaService;

    public RotaPontoService(RotaPontoRepository rotaPontoRepository, RotaRepository rotaRepository, RotaService rotaService) {
        this.repository = rotaPontoRepository;
        this.rotaRepository = rotaRepository;
        this.rotaService = rotaService;
    }

    public ResponseEntity<List<RotaPonto>> findAll() {
        List<RotaPonto> rotas = repository.findAll();
        return ResponseEntity.status(200).body(rotas);
    }

    public RotaDTO AddPointToRoute(RotaDTO rotaDTO, CreateCollectionPointRequest rotaPonto) {
        Rota rota = rotaService.getRotaById(rotaDTO.getId()).toEntity();
        Integer nextSequence = rota.getCollectionPoints().size() + 1;

        RotaPonto point = rotaPonto.toEntity(nextSequence);

        point.setRoute(rota);

        repository.save(point);

        rota.getCollectionPoints().add(point);

        return RotaDTO.fromEntity(rota);
    }

    @Transactional
    public RotaDTO reorderPoints(Long routeId, ReorderPointsRequest request) {
        Rota route = rotaRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Rota não encontrada"));

        for (ReorderPointsRequest.ReorderItem item : request.getPoints()) {
            RotaPonto point = repository.findById(item.getPointId())
                    .orElseThrow(() -> new RuntimeException("Ponto não encontrado: " + item.getPointId()));

            if (!point.getRoute().getId().equals(routeId)) {
                throw new RuntimeException("Ponto não pertence a esta rota");
            }

            point.setSequence(item.getNewSequence());
            repository.save(point);
        }

        Rota updatedRoute = rotaRepository.findById(routeId).get();
        return RotaDTO.fromEntity(updatedRoute);
    }
}
