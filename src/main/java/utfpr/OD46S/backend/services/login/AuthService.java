package utfpr.OD46S.backend.services.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utfpr.OD46S.backend.entitys.Usuario;
import utfpr.OD46S.backend.entitys.login.AuthResponse;
import utfpr.OD46S.backend.entitys.login.LoginRequest;
import utfpr.OD46S.backend.repositorys.AdministratorRepository;
import utfpr.OD46S.backend.repositorys.MotoristaRepository;
import utfpr.OD46S.backend.repositorys.UsuarioRepository;
import utfpr.OD46S.backend.utils.JwtUtils;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private MotoristaRepository motoristaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtil;

    public AuthResponse login(LoginRequest request) {
        Usuario user = null;

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            user = usuarioRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado por email"));
        } else if (request.getCpf() != null && !request.getCpf().isEmpty()) {
            user = usuarioRepository.findByCpf(request.getCpf())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado por CPF"));
        } else {
            throw new RuntimeException("Necessário informar email ou CPF");
        }

        if (!user.getActive()) {
            throw new RuntimeException("Usuário inativo");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Senha inválida");
        }

        // Determinar role do usuário e obter IDs específicos
        String role = "USER";
        Long driverId = null;
        Long adminId = null;
        
        if (administratorRepository.findById(user.getId()).isPresent()) {
            role = "ADMIN";
            adminId = user.getId();
        } else if (motoristaRepository.findById(user.getId()).isPresent()) {
            role = "DRIVER";
            driverId = user.getId();
        }

        String token = jwtUtil.generateToken(user.getEmail(), role);
        return new AuthResponse(token, user.getEmail(), user.getName(), role, user.getId(), driverId, adminId);
    }


    public AuthResponse refreshToken(String token) {
        try {
            // Validar o token atual
            if (!jwtUtil.validateToken(token)) {
                throw new RuntimeException("Token inválido ou expirado");
            }

            // Extrair email do token
            String email = jwtUtil.getEmailFromToken(token);
            
            // Buscar usuário pelo email
            Usuario user = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            if (!user.getActive()) {
                throw new RuntimeException("Usuário inativo");
            }

            // Determinar role do usuário e obter IDs específicos
            String role = "USER";
            Long driverId = null;
            Long adminId = null;
            
            if (administratorRepository.findById(user.getId()).isPresent()) {
                role = "ADMIN";
                adminId = user.getId();
            } else if (motoristaRepository.findById(user.getId()).isPresent()) {
                role = "DRIVER";
                driverId = user.getId();
            }

            // Gerar novo token
            String newToken = jwtUtil.generateToken(user.getEmail(), role);
            return new AuthResponse(newToken, user.getEmail(), user.getName(), role, user.getId(), driverId, adminId);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao renovar token: " + e.getMessage());
        }
    }
}