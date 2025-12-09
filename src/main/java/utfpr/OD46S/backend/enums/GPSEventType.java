package utfpr.OD46S.backend.enums;

public enum GPSEventType {
    // Eventos de Percurso
    START("Início da Coleta"),
    NORMAL("Percurso Normal"),
    STOP("Parada"),
    BREAK("Intervalo/Descanso"),
    
    // Eventos de Coleta em Pontos
    POINT_COLLECTED("Ponto Coletado com Sucesso"),
    
    // Eventos Gerais
    PROBLEM("Problema Geral"),
    END("Fim da Coleta");

    private final String description;

    GPSEventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
    
    /**
     * Retorna o nome do enum como string (para uso na API)
     * Garante consistência com o mobile que usa os nomes dos enums
     */
    public String getApiValue() {
        return this.name();
    }
    
    /**
     * Valida se uma string corresponde a um valor válido do enum
     * @param value String a ser validada
     * @return true se for um valor válido, false caso contrário
     */
    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            valueOf(value.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Converte uma string para o enum correspondente
     * @param value String a ser convertida
     * @return O enum correspondente ou null se não for válido
     */
    public static GPSEventType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

