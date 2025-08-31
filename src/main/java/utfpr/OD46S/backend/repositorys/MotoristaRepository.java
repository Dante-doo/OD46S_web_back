package utfpr.OD46S.backend.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utfpr.OD46S.backend.entitys.Motorista;

@Repository
public interface MotoristaRepository extends JpaRepository<Motorista, Long> {
}
