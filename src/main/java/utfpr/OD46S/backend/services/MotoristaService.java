package utfpr.OD46S.backend.services;

import org.springframework.stereotype.Service;
import utfpr.OD46S.backend.dtos.MotoristaDTO;
import utfpr.OD46S.backend.entitys.Motorista;
import utfpr.OD46S.backend.repositorys.MotoristaRepository;

@Service
public class MotoristaService {

    private final MotoristaRepository motoristaRepository;


    public MotoristaService(MotoristaRepository motoristaRepository) {
        this.motoristaRepository = motoristaRepository;
    }

    public MotoristaDTO saveOrUpdate(MotoristaDTO motoristaDTO) {
        Motorista motorista = motoristaDTO.toEntity();
        Motorista savedMotorista = motoristaRepository.save(motorista);
        return MotoristaDTO.fromEntity(savedMotorista);
    }
}
