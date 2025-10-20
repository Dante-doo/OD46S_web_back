package utfpr.OD46S.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "utfpr.OD46S.backend")
public class BackendApplication {

	static {
		// Carregar .env ANTES de qualquer coisa - usando static block
		System.out.println("🚀 CARREGANDO .env NO STATIC BLOCK...");
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

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
