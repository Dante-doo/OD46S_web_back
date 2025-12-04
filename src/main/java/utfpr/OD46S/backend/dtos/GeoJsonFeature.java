package utfpr.OD46S.backend.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeoJsonFeature {
    private String type = "Feature";
    private Map<String, Object> properties;
    private Map<String, Object> geometry;
}

