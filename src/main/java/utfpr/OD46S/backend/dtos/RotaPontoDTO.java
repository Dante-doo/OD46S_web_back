package utfpr.OD46S.backend.dtos;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RotaPontoDTO {
    private Long id;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer sequence;
    private String status;
    private String neighborhood;
    private String observations;




}
