package utfpr.OD46S.backend.services;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import utfpr.OD46S.backend.dtos.RotaDTO;
import utfpr.OD46S.backend.entitys.Rota;
import utfpr.OD46S.backend.repositorys.RotaRepository;

import java.util.List;

@Service
public class RotaService {

    private RotaRepository repository;

    public RotaService(RotaRepository repository) {
        this.repository = repository;
    }

    public ResponseEntity<Rota> save(RotaDTO rota) {
        Rota rotaEntity = rota.toEntity();
        rotaEntity.setId(null);
        return ResponseEntity.status(200).body(rotaEntity);
    }

    public ResponseEntity<List<Rota>> findAll() {
        List<Rota> rotas = repository.findAll();
        return ResponseEntity.status(200).body(rotas);
    }
}
