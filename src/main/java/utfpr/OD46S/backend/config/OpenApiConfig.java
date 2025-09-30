package utfpr.OD46S.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OD46S - API de Coleta de Lixo Urbano")
                        .description("Solução completa para otimização e monitoramento de rotas de coleta de lixo urbano. " +
                                   "O sistema oferece controle total sobre frotas, motoristas e trajetos através de uma " +
                                   "plataforma web para administradores e aplicativo mobile para motoristas.")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Equipe OD46S")
                                .email("team@od46s.com")
                                .url("https://github.com/OD46S"))
                        .license(new License()
                                .name("Licença MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desenvolvimento"),
                        new Server()
                                .url("https://api.od46s.com")
                                .description("Servidor de Produção")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT para autenticação da API")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
