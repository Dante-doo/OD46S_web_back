package utfpr.OD46S.backend.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utfpr.OD46S.backend.entitys.Administrator;
import utfpr.OD46S.backend.entitys.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {


    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

}
