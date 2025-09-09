package utfpr.OD46S.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utfpr.OD46S.backend.dtos.UsuarioDTO;
import utfpr.OD46S.backend.entitys.Usuario;
import utfpr.OD46S.backend.repositorys.UsuarioRepository;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public boolean existsById(Integer id) {
        return usuarioRepository.existsById(id);
    }

    public Usuario findById(Integer id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    /**
     * Salvar ou atualizar um usuário.
     * @param usuario O usuário a ser salvo ou atualizado. No caso ainda so ta salvando kkkk
     * @return O usuário salvo ou atualizado.
     */
    public UsuarioDTO saveOrUpdate(UsuarioDTO usuarioDTO) {


        Usuario usuario = usuarioDTO.toEntity();

        Usuario save = usuarioRepository.save(usuario);

        return UsuarioDTO.fromEntity(save);

    }

    public void deleteById(Integer id) {
        usuarioRepository.deleteById(id);
    }

}
