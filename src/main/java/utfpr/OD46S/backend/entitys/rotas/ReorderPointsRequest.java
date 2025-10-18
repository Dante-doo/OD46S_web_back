package utfpr.OD46S.backend.entitys.rotas;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReorderPointsRequest {
    private List<ReorderItem> points;

    @Getter
    @Setter
    public static class ReorderItem {
        private Long pointId;
        private Integer newSequence;
    }
}
