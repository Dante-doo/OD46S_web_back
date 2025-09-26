package utfpr.OD46S.backend.services.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utfpr.OD46S.backend.entitys.Administrator;
import utfpr.OD46S.backend.entitys.Motorista;
import utfpr.OD46S.backend.entitys.Usuario;
import utfpr.OD46S.backend.entitys.login.AuthResponse;
import utfpr.OD46S.backend.entitys.login.LoginRequest;
import utfpr.OD46S.backend.entitys.login.RegisterRequest;
import utfpr.OD46S.backend.enums.CategoriaCNH;
import utfpr.OD46S.backend.repositorys.AdministratorRepository;
import utfpr.OD46S.backend.repositorys.MotoristaRepository;
import utfpr.OD46S.backend.repositorys.UsuarioRepository;
import utfpr.OD46S.backend.utils.JwtUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

        // Determinar tipo de usuário
        String userType = "USER";
        if (administratorRepository.findById(user.getId()).isPresent()) {
            userType = "ADMIN";
        } else if (motoristaRepository.findById(user.getId()).isPresent()) {
            userType = "DRIVER";
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getName(), userType);
    }

    public AuthResponse register(RegisterRequest request) {
        // Validações
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        if (usuarioRepository.existsByCpf(request.getCpf())) {
            throw new RuntimeException("CPF já cadastrado");
        }

        // Validações específicas para DRIVER
        if ("DRIVER".equals(request.getType())) {
            if (request.getLicenseNumber() == null || request.getLicenseCategory() == null || request.getLicenseExpiry() == null) {
                throw new RuntimeException("Dados da CNH são obrigatórios para motoristas");
            }
            
            if (motoristaRepository.existsByLicenseNumber(request.getLicenseNumber())) {
                throw new RuntimeException("Número da CNH já cadastrado");
            }
        }

        // Criar usuário base
        Usuario user = new Usuario();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setCpf(request.getCpf());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);

        // Salvar usuário base
        user = usuarioRepository.save(user);

        // Criar entidade específica
        if ("ADMIN".equals(request.getType())) {
            Administrator admin = new Administrator();
            admin.setId(user.getId());
            admin.setAccessLevel(request.getAccessLevel() != null ? request.getAccessLevel() : "ADMIN");
            admin.setDepartment(request.getDepartment());
            admin.setCorporatePhone(request.getCorporatePhone());
            administratorRepository.save(admin);
        } else if ("DRIVER".equals(request.getType())) {
            Motorista driver = new Motorista();
            driver.setId(user.getId());
            driver.setLicenseNumber(request.getLicenseNumber());
            driver.setLicenseCategory(CategoriaCNH.valueOf(request.getLicenseCategory()));
            driver.setLicenseExpiry(LocalDate.parse(request.getLicenseExpiry(), DateTimeFormatter.ISO_LOCAL_DATE));
            driver.setPhone(request.getPhone());
            driver.setEnabled(true);
            motoristaRepository.save(driver);
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getName(), request.getType());
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

            // Determinar tipo de usuário
            String userType = "USER";
            if (administratorRepository.findById(user.getId()).isPresent()) {
                userType = "ADMIN";
            } else if (motoristaRepository.findById(user.getId()).isPresent()) {
                userType = "DRIVER";
            }

            // Gerar novo token
            String newToken = jwtUtil.generateToken(user.getEmail());
            return new AuthResponse(newToken, user.getEmail(), user.getName(), userType);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao renovar token: " + e.getMessage());
        }
    }
}