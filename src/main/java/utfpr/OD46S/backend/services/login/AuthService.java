package utfpr.OD46S.backend.services.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import utfpr.OD46S.backend.dtos.AdministradorDTO;
import utfpr.OD46S.backend.entitys.Administrator;
import utfpr.OD46S.backend.entitys.Usuario;
import utfpr.OD46S.backend.entitys.login.AuthResponse;
import utfpr.OD46S.backend.entitys.login.LoginRequest;
import utfpr.OD46S.backend.entitys.login.RegisterRequest;
import utfpr.OD46S.backend.repositorys.AdministratorRepository;
import utfpr.OD46S.backend.repositorys.UsuarioRepository;
import utfpr.OD46S.backend.utils.JwtUtils;

@Service
public class AuthService {

    @Autowired
    private AdministratorRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtil;

    public AuthResponse login(LoginRequest request) {
        Usuario admin = null;

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            admin = repository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Administrador não encontrado por email"));

        } else if (request.getCpf() != null && !request.getCpf().isEmpty()) {
            admin = repository.findByCpf(request.getCpf())
                    .orElseThrow(() -> new RuntimeException("Administrador não encontrado por CPF"));
        } else {
            throw new RuntimeException("Necessário informar email ou CPF");
        }

        if (!passwordEncoder.matches(request.getSenha(), admin.getSenha())) {
            throw new RuntimeException("Senha inválida");
        }

        String token = jwtUtil.generateToken(admin.getEmail());
        return new AuthResponse(token, admin.getEmail(), admin.getNome());
    }

    public AuthResponse register(RegisterRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        AdministradorDTO Admin = new AdministradorDTO();
        Admin.setEmail(request.getEmail());
        Admin.setSenha(passwordEncoder.encode(request.getSenha()));
        Admin.setNome(request.getNome());
        Admin.setCpf(request.getCpf());
        Admin.setNivelAcesso("A");

        Administrator adminEntity = Admin.toEntity();
        repository.save(adminEntity);

        String token = jwtUtil.generateToken(Admin.getEmail());
        return new AuthResponse(token, Admin.getEmail(), Admin.getNome());
    }

}
