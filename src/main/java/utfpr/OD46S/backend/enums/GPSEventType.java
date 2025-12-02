package utfpr.OD46S.backend.enums;

public enum GPSEventType {
    START("Início da Coleta"),
    NORMAL("Percurso Normal"),
    STOP("Parada"),
    BREAK("Intervalo/Descanso"),
    FUEL("Abastecimento"),
    LUNCH("Almoço"),
    PROBLEM("Problema"),
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

