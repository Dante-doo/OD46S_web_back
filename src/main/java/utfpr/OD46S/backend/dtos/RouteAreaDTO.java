package utfpr.OD46S.backend.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RouteAreaDTO {
    private Long id;
    private Long routeId;
    private String routeName;
    private String externalName;
    private String wasteType;
    private Map<String, Object> geometry; // GeoJSON geometry object
    private String strokeColor;
    private String fillColor;
    private BigDecimal fillOpacity;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

