package utfpr.OD46S.backend.utils;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    private String secret = "mySecretKey";
    Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private int jwtExpiration = 86400000; // 24 horas

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(key).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateTokenExpiration() {
        Date endDate = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(Jwts.builder().setExpiration(new Date()).compact())
                .getBody()
                .getExpiration();

        return !(endDate.before(new Date()));
    }

}
