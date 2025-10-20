package utfpr.OD46S.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utfpr.OD46S.backend.dtos.UsuarioDTO;
import utfpr.OD46S.backend.entitys.Administrator;
import utfpr.OD46S.backend.entitys.Motorista;
import utfpr.OD46S.backend.entitys.Usuario;
import utfpr.OD46S.backend.repositorys.AdministratorRepository;
import utfpr.OD46S.backend.repositorys.MotoristaRepository;
import utfpr.OD46S.backend.repositorys.UsuarioRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private MotoristaRepository motoristaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Page<UsuarioDTO> listarUsuarios(String search, String type, Boolean active, Pageable pageable) {
        Page<Usuario> usuarios;
        
        // Aplicar filtros básicos
        if (active != null) {
            usuarios = usuarioRepository.findByActive(active, pageable);
        } else {
            usuarios = usuarioRepository.findAll(pageable);
        }
        
        // Converter para DTOs e aplicar filtros
        Page<UsuarioDTO> result = usuarios.map(usuario -> {
            UsuarioDTO dto = UsuarioDTO.fromEntity(usuario);
            
            // Determinar tipo do usuário
            if (administratorRepository.existsById(usuario.getId())) {
                dto.setType("ADMIN");
                Optional<Administrator> admin = administratorRepository.findById(usuario.getId());
                if (admin.isPresent()) {
                    dto.setAccessLevel(admin.get().getAccessLevel());
                    dto.setDepartment(admin.get().getDepartment());
                    dto.setCorporatePhone(admin.get().getCorporatePhone());
                }
            } else if (motoristaRepository.existsById(usuario.getId())) {
                dto.setType("DRIVER");
                Optional<Motorista> motorista = motoristaRepository.findById(usuario.getId());
                if (motorista.isPresent()) {
                    dto.setLicenseNumber(motorista.get().getLicenseNumber());
                    dto.setLicenseCategory(motorista.get().getLicenseCategory().toString());
                    dto.setLicenseExpiry(motorista.get().getLicenseExpiry().toString());
                    dto.setEnabled(motorista.get().getEnabled());
                    dto.setPhone(motorista.get().getPhone());
                }
            }
            
            return dto;
        });
        
        // Aplicar filtros adicionais se necessário
        if ((search != null && !search.trim().isEmpty()) || (type != null && !type.trim().isEmpty())) {
            List<UsuarioDTO> filteredList = result.getContent().stream()
                .filter(dto -> {
                    // Aplicar filtro de busca
                    if (search != null && !search.trim().isEmpty()) {
                        String searchLower = search.toLowerCase();
                        boolean matchesSearch = (dto.getName() != null && dto.getName().toLowerCase().contains(searchLower)) ||
                                              (dto.getEmail() != null && dto.getEmail().toLowerCase().contains(searchLower));
                        if (!matchesSearch) {
                            return false;
                        }
                    }
                    
                    // Aplicar filtro de tipo
                    if (type != null && !type.trim().isEmpty()) {
                        if (!type.equals(dto.getType())) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
            
            // Criar nova Page com os dados filtrados
            return new PageImpl<>(filteredList, pageable, filteredList.size());
        }
        
        return result;
    }

    public UsuarioDTO obterUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        UsuarioDTO dto = UsuarioDTO.fromEntity(usuario);
        
        // Determinar tipo do usuário e adicionar campos específicos
        if (administratorRepository.existsById(id)) {
            dto.setType("ADMIN");
            Optional<Administrator> admin = administratorRepository.findById(id);
            if (admin.isPresent()) {
                dto.setAccessLevel(admin.get().getAccessLevel());
                dto.setDepartment(admin.get().getDepartment());
                dto.setCorporatePhone(admin.get().getCorporatePhone());
            }
        } else if (motoristaRepository.existsById(id)) {
            dto.setType("DRIVER");
            Optional<Motorista> motorista = motoristaRepository.findById(id);
            if (motorista.isPresent()) {
                dto.setLicenseNumber(motorista.get().getLicenseNumber());
                if (motorista.get().getLicenseCategory() != null) {
                    dto.setLicenseCategory(motorista.get().getLicenseCategory().toString());
                }
                if (motorista.get().getLicenseExpiry() != null) {
                    dto.setLicenseExpiry(motorista.get().getLicenseExpiry().toString());
                }
                dto.setEnabled(motorista.get().getEnabled());
                dto.setPhone(motorista.get().getPhone());
            }
        }
        
        return dto;
    }

    public UsuarioDTO criarUsuario(UsuarioDTO dto) {
        try {
            // Validar se email já existe
            if (usuarioRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email já cadastrado");
            }

            // Validar se CPF já existe
            if (usuarioRepository.existsByCpf(dto.getCpf())) {
                throw new RuntimeException("CPF já cadastrado");
            }

            // Validar se licenseNumber já existe (para DRIVER)
            if ("DRIVER".equals(dto.getType()) && dto.getLicenseNumber() != null && 
                motoristaRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
                throw new RuntimeException("Número de licença já cadastrado");
            }

        // Criar usuário base
        Usuario usuario = dto.toEntity();
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuarioRepository.save(usuario);

        // Criar registro específico baseado no tipo
        if ("ADMIN".equals(dto.getType())) {
            Administrator admin = new Administrator();
            admin.setId(usuario.getId());
            admin.setAccessLevel(dto.getAccessLevel() != null ? dto.getAccessLevel() : "ADMIN");
            admin.setDepartment(dto.getDepartment());
            admin.setCorporatePhone(dto.getCorporatePhone());
            administratorRepository.save(admin);
        } else if ("DRIVER".equals(dto.getType())) {
            Motorista motorista = new Motorista();
            motorista.setId(usuario.getId());
            motorista.setLicenseNumber(dto.getLicenseNumber());
            motorista.setLicenseCategory(parseLicenseCategory(dto.getLicenseCategory()));
            if (dto.getLicenseExpiry() != null && !dto.getLicenseExpiry().trim().isEmpty()) {
                motorista.setLicenseExpiry(LocalDate.parse(dto.getLicenseExpiry()));
            }
            motorista.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
            motorista.setPhone(dto.getPhone());
            motoristaRepository.save(motorista);
        }

        return obterUsuario(usuario.getId());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar usuário: " + e.getMessage(), e);
        }
    }

    public UsuarioDTO atualizarUsuario(Long id, UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Verificar se email já existe em outro usuário
        if (!usuario.getEmail().equals(dto.getEmail()) && usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        // Verificar se CPF já existe em outro usuário
        if (!usuario.getCpf().equals(dto.getCpf()) && usuarioRepository.existsByCpf(dto.getCpf())) {
            throw new RuntimeException("CPF já cadastrado");
        }

        // Atualizar campos básicos
        if (dto.getName() != null) {
            usuario.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            usuario.setEmail(dto.getEmail());
        }
        if (dto.getCpf() != null) {
            usuario.setCpf(dto.getCpf());
        }
        if (dto.getActive() != null) {
            usuario.setActive(dto.getActive());
        }

        // Atualizar senha se fornecida
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        usuarioRepository.save(usuario);

        // Atualizar campos específicos do tipo
        if (administratorRepository.existsById(id)) {
            Optional<Administrator> adminOpt = administratorRepository.findById(id);
            if (adminOpt.isPresent()) {
                Administrator admin = adminOpt.get();
                if (dto.getAccessLevel() != null) admin.setAccessLevel(dto.getAccessLevel());
                if (dto.getDepartment() != null) admin.setDepartment(dto.getDepartment());
                if (dto.getCorporatePhone() != null) admin.setCorporatePhone(dto.getCorporatePhone());
                administratorRepository.save(admin);
            }
        } else if (motoristaRepository.existsById(id)) {
            Optional<Motorista> motoristaOpt = motoristaRepository.findById(id);
            if (motoristaOpt.isPresent()) {
                Motorista motorista = motoristaOpt.get();
                if (dto.getLicenseNumber() != null) motorista.setLicenseNumber(dto.getLicenseNumber());
                if (dto.getLicenseCategory() != null) motorista.setLicenseCategory(parseLicenseCategory(dto.getLicenseCategory()));
                if (dto.getLicenseExpiry() != null) motorista.setLicenseExpiry(LocalDate.parse(dto.getLicenseExpiry()));
                if (dto.getEnabled() != null) motorista.setEnabled(dto.getEnabled());
                if (dto.getPhone() != null) motorista.setPhone(dto.getPhone());
                motoristaRepository.save(motorista);
            }
        }

        return obterUsuario(id);
    }

    public void removerUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuário não encontrado");
        }

        // Remover registros específicos primeiro
        if (administratorRepository.existsById(id)) {
            administratorRepository.deleteById(id);
        } else if (motoristaRepository.existsById(id)) {
            motoristaRepository.deleteById(id);
        }

        // Remover usuário base
        usuarioRepository.deleteById(id);
    }

    private utfpr.OD46S.backend.enums.CategoriaCNH parseLicenseCategory(String category) {
        if (category == null) return null;
        try {
            return utfpr.OD46S.backend.enums.CategoriaCNH.valueOf(category);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Categoria de CNH inválida: " + category);
        }
    }
}
