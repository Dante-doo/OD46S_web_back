package utfpr.OD46S.backend.utils;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {

    // CRITICAL SECURITY: Use environment variables in production!
    // Example: @Value("${jwt.secret}") private String secret;
    private String secret = "mySecretKey_CHANGE_IN_PRODUCTION_USE_ENV_VAR";
    private int jwtExpiration = 86400000; // 24 horas (86400 seconds)

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateTokenExpiration() {
        Date endDate = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(Jwts.builder().setExpiration(new Date()).compact())
                .getBody()
                .getExpiration();

        return !(endDate.before(new Date()));
    }

}
