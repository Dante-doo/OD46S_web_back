package utfpr.OD46S.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utfpr.OD46S.backend.dtos.AdministradorDTO;
import utfpr.OD46S.backend.entitys.Administrator;
import utfpr.OD46S.backend.repositorys.AdministratorRepository;

import java.util.List;

@Service
public class AdministratorService {

    private final AdministratorRepository administratorRepository;

    @Autowired
    public AdministratorService(AdministratorRepository administratorRepository) {
        this.administratorRepository = administratorRepository;
    }

    public List<Administrator> findAll() {
        return administratorRepository.findAll();
    }

    public AdministradorDTO saveOrUpdate(AdministradorDTO administradorDTO) {
        Administrator administrator = administradorDTO.toEntity();
        Administrator savedAdmin = administratorRepository.save(administrator);
        return AdministradorDTO.fromEntity(savedAdmin);
    }

}
