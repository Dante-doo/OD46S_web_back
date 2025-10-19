package utfpr.OD46S.backend.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import utfpr.OD46S.backend.enums.CollectionType;
import utfpr.OD46S.backend.enums.Priority;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteDTO {
    private Long id;
    private String name;
    private String description;
    
    @JsonProperty("collection_type")
    private CollectionType collectionType;
    
    private String periodicity;
    private Priority priority;
    
    @JsonProperty("estimated_time_minutes")
    private Integer estimatedTimeMinutes;
    
    @JsonProperty("distance_km")
    private BigDecimal distanceKm;
    
    private Boolean active;
    private String notes;
    
    @JsonProperty("created_by")
    private Long createdBy;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    @JsonProperty("collection_points")
    private List<RouteCollectionPointDTO> collectionPoints;
    
    @JsonProperty("collection_points_count")
    private Integer collectionPointsCount;
}

