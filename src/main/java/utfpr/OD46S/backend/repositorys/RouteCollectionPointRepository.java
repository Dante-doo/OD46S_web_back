package utfpr.OD46S.backend.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import utfpr.OD46S.backend.entitys.RouteCollectionPoint;

import java.util.List;
import java.util.Optional;

public interface RouteCollectionPointRepository extends JpaRepository<RouteCollectionPoint, Long> {
    
    List<RouteCollectionPoint> findByRouteIdOrderBySequenceOrderAsc(Long routeId);
    
    Optional<RouteCollectionPoint> findByRouteIdAndSequenceOrder(Long routeId, Integer sequenceOrder);
    
    @Query("SELECT MAX(rcp.sequenceOrder) FROM RouteCollectionPoint rcp WHERE rcp.route.id = :routeId")
    Optional<Integer> findMaxSequenceOrderByRouteId(@Param("routeId") Long routeId);
    
    boolean existsByRouteIdAndSequenceOrder(Long routeId, Integer sequenceOrder);
}

