package utfpr.OD46S.backend.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import utfpr.OD46S.backend.entitys.Veiculo;

import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    Optional<Veiculo> findByLicensePlate(String licensePlate);
    boolean existsByLicensePlate(String licensePlate);
}


