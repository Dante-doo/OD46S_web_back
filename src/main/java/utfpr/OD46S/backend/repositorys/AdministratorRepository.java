package utfpr.OD46S.backend.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utfpr.OD46S.backend.entitys.Administrator;

import java.util.Optional;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, Long> {

    Optional<Administrator> findByEmail(String email);

    Optional<Administrator> findByCpf(String cpf);

    boolean existsByEmail(String email);

}
