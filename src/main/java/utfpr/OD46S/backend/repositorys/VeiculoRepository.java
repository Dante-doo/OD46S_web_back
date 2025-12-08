package utfpr.OD46S.backend.repositorys;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import utfpr.OD46S.backend.entitys.Veiculo;
import utfpr.OD46S.backend.enums.StatusVeiculo;

import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    Optional<Veiculo> findByLicensePlate(String licensePlate);
    boolean existsByLicensePlate(String licensePlate);
    
    // Métodos para paginação com filtros
    Page<Veiculo> findByLicensePlateContainingIgnoreCaseOrModelContainingIgnoreCase(
        String licensePlate, String model, Pageable pageable);
    
    Page<Veiculo> findByLicensePlateContainingIgnoreCaseOrModelContainingIgnoreCaseAndStatus(
        String licensePlate, String model, StatusVeiculo status, Pageable pageable);
    
    Page<Veiculo> findByLicensePlateContainingIgnoreCaseOrModelContainingIgnoreCaseAndActive(
        String licensePlate, String model, Boolean active, Pageable pageable);
    
    Page<Veiculo> findByLicensePlateContainingIgnoreCaseOrModelContainingIgnoreCaseAndStatusAndActive(
        String licensePlate, String model, StatusVeiculo status, Boolean active, Pageable pageable);
    
    Page<Veiculo> findByStatus(StatusVeiculo status, Pageable pageable);
    Page<Veiculo> findByActive(Boolean active, Pageable pageable);
    Page<Veiculo> findByStatusAndActive(StatusVeiculo status, Boolean active, Pageable pageable);
}


