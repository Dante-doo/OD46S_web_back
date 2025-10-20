package utfpr.OD46S.backend.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Inicializador para carregar variáveis do .env ANTES do Spring processar application.properties
 * 
 * Esta classe é executada MUITO CEDO no ciclo de vida do Spring, garantindo que
 * as variáveis estejam disponíveis quando o application.properties for processado.
 */
public class DotenvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        System.out.println("🚀 DOTENV INITIALIZER - Carregando .env...");
        System.out.println("🔧 Diretório atual: " + System.getProperty("user.dir"));
        System.out.println("🔧 Arquivo .env existe: " + new java.io.File(".env").exists());
        
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .filename(".env")
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();

            System.out.println("📊 Total de variáveis encontradas: " + dotenv.entries().size());
            
            // Carregar variáveis como System Properties
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                
                if (System.getProperty(key) == null) {
                    System.setProperty(key, value);
                    System.out.println("✅ Carregada: " + key + " = " + value);
                } else {
                    System.out.println("⚠️ Já existe: " + key + " (ignorando .env)");
                }
            });

            System.out.println("✅ Arquivo .env carregado com sucesso!");
            System.out.println("🔍 LOG_PATTERN após carregamento: " + System.getProperty("LOG_PATTERN"));
            
        } catch (Exception e) {
            System.out.println("⚠️ Erro ao carregar .env: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

