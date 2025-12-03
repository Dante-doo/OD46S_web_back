package utfpr.OD46S.backend.enums;

public enum GPSEventType {
    // Eventos de Percurso
    START("Início da Coleta"),
    NORMAL("Percurso Normal"),
    STOP("Parada"),
    BREAK("Intervalo/Descanso"),
    FUEL("Abastecimento"),
    LUNCH("Almoço"),
    
    // Eventos de Coleta em Pontos
    POINT_ARRIVAL("Chegada no Ponto de Coleta"),
    POINT_COLLECTED("Ponto Coletado com Sucesso"),
    POINT_SKIPPED("Ponto Não Coletado"),
    POINT_PROBLEM("Problema no Ponto de Coleta"),
    
    // Eventos Gerais
    PROBLEM("Problema Geral"),
    OBSERVATION("Observação"),
    PHOTO("Registro Fotográfico"),
    END("Fim da Coleta");

    private final String description;

    GPSEventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

