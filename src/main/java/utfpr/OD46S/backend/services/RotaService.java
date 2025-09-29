package utfpr.OD46S.backend.services;

import org.springframework.stereotype.Service;
import utfpr.OD46S.backend.entitys.Rota;
import utfpr.OD46S.backend.repositorys.RotaRepository;

@Service
public class RotaService {

    private RotaRepository repository;

    public RotaService(RotaRepository repository) {
        this.repository = repository;
    }

    public Rota saveOrUpdate(Rota rota) {
        return repository.save(rota);
    }

    public Iterable<Rota> findAll() {
        return repository.findAll();
    }


}
