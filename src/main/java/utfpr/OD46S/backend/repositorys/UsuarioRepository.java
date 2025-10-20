package utfpr.OD46S.backend.repositorys;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utfpr.OD46S.backend.entitys.Administrator;
import utfpr.OD46S.backend.entitys.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByCpf(String cpf);

    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
    
    Page<Usuario> findByActive(Boolean active, Pageable pageable);
}
