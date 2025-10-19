package utfpr.OD46S.backend.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import utfpr.OD46S.backend.enums.CollectionFrequency;
import utfpr.OD46S.backend.enums.WasteType;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteCollectionPointDTO {
    private Long id;
    
    @JsonProperty("route_id")
    private Long routeId;
    
    @JsonProperty("sequence_order")
    private Integer sequenceOrder;
    
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    
    @JsonProperty("waste_type")
    private WasteType wasteType;
    
    @JsonProperty("estimated_capacity_kg")
    private BigDecimal estimatedCapacityKg;
    
    @JsonProperty("collection_frequency")
    private CollectionFrequency collectionFrequency;
    
    private String notes;
    private Boolean active;
}

