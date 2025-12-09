package utfpr.OD46S.backend.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

/**
 * Utilitários para trabalhar com periodicity (formato cron)
 * Formato: "minuto hora * * dia_da_semana"
 * Exemplo: "0 8 * * 1" = Segunda-feira às 8:00
 */
public class PeriodicityUtils {
    
    /**
     * Verifica se a data atual é um dia permitido para a rota baseado na periodicity
     * @param periodicity Formato cron: "minuto hora * * dia_da_semana"
     * @param date Data a ser verificada (se null, usa a data atual)
     * @return true se a data é um dia permitido, false caso contrário
     */
    public static boolean isDateAllowed(String periodicity, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        
        if (periodicity == null || periodicity.trim().isEmpty()) {
            // Se não tem periodicity, permite qualquer dia
            return true;
        }
        
        try {
            String[] parts = periodicity.trim().split("\\s+");
            if (parts.length < 5) {
                // Formato inválido, permite por padrão
                return true;
            }
            
            String dayOfWeek = parts[4]; // Último campo é o dia da semana
            
            // Se for "*", permite todos os dias
            if (dayOfWeek.equals("*")) {
                return true;
            }
            
            // Pega o dia da semana da data (Java: 1=segunda, 7=domingo)
            DayOfWeek currentDayOfWeek = date.getDayOfWeek();
            
            // Converte para formato cron (0=domingo, 1=segunda, etc)
            // Java: MONDAY=1, TUESDAY=2, ..., SUNDAY=7
            // Cron: 0=Sunday, 1=Monday, ..., 6=Saturday
            int javaDayValue = currentDayOfWeek.getValue(); // 1-7
            int cronDayOfWeek = (javaDayValue == 7) ? 0 : javaDayValue; // 0-6
            
            // Verifica se o dia atual está na lista de dias permitidos
            if (dayOfWeek.contains(",")) {
                // Múltiplos dias separados por vírgula
                String[] allowedDays = dayOfWeek.split(",");
                for (String day : allowedDays) {
                    try {
                        if (Integer.parseInt(day.trim()) == cronDayOfWeek) {
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        // Ignora valores não numéricos
                    }
                }
                return false;
            } else if (dayOfWeek.contains("-")) {
                // Range de dias (ex: "1-5" = segunda a sexta)
                String[] range = dayOfWeek.split("-");
                if (range.length == 2) {
                    try {
                        int start = Integer.parseInt(range[0].trim());
                        int end = Integer.parseInt(range[1].trim());
                        return cronDayOfWeek >= start && cronDayOfWeek <= end;
                    } catch (NumberFormatException e) {
                        // Ignora se não conseguir parsear
                    }
                }
                return false;
            } else {
                // Dia único
                try {
                    return cronDayOfWeek == Integer.parseInt(dayOfWeek.trim());
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        } catch (Exception e) {
            // Em caso de erro no parse, permite por padrão
            return true;
        }
    }
    
    /**
     * Retorna os dias da semana permitidos baseado na periodicity
     * @param periodicity Formato cron
     * @return Set de DayOfWeek permitidos
     */
    public static Set<DayOfWeek> getAllowedDaysOfWeek(String periodicity) {
        Set<DayOfWeek> days = new HashSet<>();
        
        if (periodicity == null || periodicity.trim().isEmpty()) {
            // Se não tem periodicity, retorna todos os dias
            days.addAll(Arrays.asList(DayOfWeek.values()));
            return days;
        }
        
        try {
            String[] parts = periodicity.trim().split("\\s+");
            if (parts.length < 5) {
                return days;
            }
            
            String dayOfWeek = parts[4];
            
            // Mapeamento cron para DayOfWeek (cron: 0=domingo, 1=segunda, ..., 6=sábado)
            Map<Integer, DayOfWeek> cronDayMap = new HashMap<>();
            cronDayMap.put(0, DayOfWeek.SUNDAY);
            cronDayMap.put(1, DayOfWeek.MONDAY);
            cronDayMap.put(2, DayOfWeek.TUESDAY);
            cronDayMap.put(3, DayOfWeek.WEDNESDAY);
            cronDayMap.put(4, DayOfWeek.THURSDAY);
            cronDayMap.put(5, DayOfWeek.FRIDAY);
            cronDayMap.put(6, DayOfWeek.SATURDAY);
            
            if (dayOfWeek.equals("*")) {
                days.addAll(cronDayMap.values());
                return days;
            }
            
            if (dayOfWeek.contains(",")) {
                // Múltiplos dias separados por vírgula
                String[] dayNumbers = dayOfWeek.split(",");
                for (String dayNumStr : dayNumbers) {
                    dayNumStr = dayNumStr.trim();
                    try {
                        int dayNum = Integer.parseInt(dayNumStr);
                        if (cronDayMap.containsKey(dayNum)) {
                            days.add(cronDayMap.get(dayNum));
                        }
                    } catch (NumberFormatException e) {
                        // Ignora valores não numéricos
                    }
                }
            } else if (dayOfWeek.contains("-")) {
                // Range de dias
                String[] range = dayOfWeek.split("-");
                if (range.length == 2) {
                    try {
                        int start = Integer.parseInt(range[0].trim());
                        int end = Integer.parseInt(range[1].trim());
                        for (int i = start; i <= end; i++) {
                            if (cronDayMap.containsKey(i)) {
                                days.add(cronDayMap.get(i));
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Ignora se não conseguir parsear
                    }
                }
            } else {
                // Dia único
                try {
                    int dayNum = Integer.parseInt(dayOfWeek.trim());
                    if (cronDayMap.containsKey(dayNum)) {
                        days.add(cronDayMap.get(dayNum));
                    }
                } catch (NumberFormatException e) {
                    // Ignora se não conseguir parsear
                }
            }
        } catch (Exception e) {
            // Em caso de erro, retorna vazio
        }
        
        return days;
    }
    
    /**
     * Formata os dias da semana para exibição em mensagens
     * @param days Set de DayOfWeek
     * @return String formatada como "Segunda-feira, Quarta-feira, Sexta-feira"
     */
    public static String formatDaysOfWeek(Set<DayOfWeek> days) {
        if (days == null || days.isEmpty()) {
            return "Nenhum dia especificado";
        }
        
        Map<DayOfWeek, String> dayNames = new HashMap<>();
        dayNames.put(DayOfWeek.SUNDAY, "Domingo");
        dayNames.put(DayOfWeek.MONDAY, "Segunda-feira");
        dayNames.put(DayOfWeek.TUESDAY, "Terça-feira");
        dayNames.put(DayOfWeek.WEDNESDAY, "Quarta-feira");
        dayNames.put(DayOfWeek.THURSDAY, "Quinta-feira");
        dayNames.put(DayOfWeek.FRIDAY, "Sexta-feira");
        dayNames.put(DayOfWeek.SATURDAY, "Sábado");
        
        List<String> dayNameList = new ArrayList<>();
        for (DayOfWeek day : days) {
            dayNameList.add(dayNames.getOrDefault(day, day.toString()));
        }
        
        return String.join(", ", dayNameList);
    }
}

