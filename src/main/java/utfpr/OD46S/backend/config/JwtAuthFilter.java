package utfpr.OD46S.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import utfpr.OD46S.backend.utils.JwtUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Ignorar endpoints públicos (login, refresh, health)
        String path = request.getRequestURI();
        if (path.startsWith("/api/v1/auth/login") || 
            path.startsWith("/api/v1/auth/refresh") ||
            path.startsWith("/api/v1/auth/health") ||
            path.startsWith("/health") ||
            path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                if (jwtUtils.validateToken(token)) {
                    String email = jwtUtils.getEmailFromToken(token);
                    String role = jwtUtils.getRoleFromToken(token);
                    
                    List<SimpleGrantedAuthority> authorities = role != null 
                        ? Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                        : Collections.emptyList();
                    
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            new User(email, "", authorities),
                            null,
                            authorities
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Se houver erro ao processar token, apenas continua sem autenticação
                // O Spring Security vai retornar 401 se a rota exigir autenticação
            }
        }

        filterChain.doFilter(request, response);
    }
}


