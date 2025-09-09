package utfpr.OD46S.backend.services.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import utfpr.OD46S.backend.entitys.Usuario;
import utfpr.OD46S.backend.entitys.login.AuthResponse;
import utfpr.OD46S.backend.entitys.login.LoginRequest;
import utfpr.OD46S.backend.entitys.login.RegisterRequest;
import utfpr.OD46S.backend.repositorys.UsuarioRepository;
import utfpr.OD46S.backend.utils.JwtUtils;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtil;

    public AuthResponse login(LoginRequest request) {
        Usuario admin = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getSenha())) {
            throw new RuntimeException("Senha inválida");
        }

        String token = jwtUtil.generateToken(admin.getEmail());
        return new AuthResponse(token, admin.getEmail(), admin.getNome());
    }

    public AuthResponse register(RegisterRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        Usuario user = new Usuario();
        user.setEmail(request.getEmail());
        user.setSenha(passwordEncoder.encode(request.getPassword()));
        user.setNome(request.getName());

        repository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getNome());
    }

}
