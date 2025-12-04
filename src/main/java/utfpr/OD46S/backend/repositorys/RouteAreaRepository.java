package utfpr.OD46S.backend.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utfpr.OD46S.backend.entitys.RouteArea;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteAreaRepository extends JpaRepository<RouteArea, Long> {
    
    List<RouteArea> findByRouteId(Long routeId);
    
    List<RouteArea> findByRouteIdAndActive(Long routeId, Boolean active);
    
    List<RouteArea> findByWasteType(String wasteType);
    
    List<RouteArea> findByActive(Boolean active);
    
    @Query("SELECT ra FROM RouteArea ra WHERE ra.route.id = :routeId AND ra.externalName = :externalName")
    Optional<RouteArea> findByRouteIdAndExternalName(@Param("routeId") Long routeId, @Param("externalName") String externalName);
    
    @Query("SELECT ra FROM RouteArea ra WHERE ra.active = :active " +
           "AND (:wasteType IS NULL OR ra.wasteType = :wasteType) " +
           "AND (:routeId IS NULL OR ra.route.id = :routeId)")
    List<RouteArea> findWithFilters(@Param("active") Boolean active, 
                                     @Param("wasteType") String wasteType, 
                                     @Param("routeId") Long routeId);
}

