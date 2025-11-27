package utfpr.OD46S.backend.repositorys;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utfpr.OD46S.backend.entitys.RouteAssignment;
import utfpr.OD46S.backend.enums.AssignmentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RouteAssignmentRepository extends JpaRepository<RouteAssignment, Long> {

    @Query("SELECT ra FROM RouteAssignment ra " +
           "LEFT JOIN FETCH ra.route r " +
           "LEFT JOIN FETCH ra.driver d " +
           "LEFT JOIN FETCH ra.vehicle v " +
           "WHERE (:routeId IS NULL OR r.id = :routeId) " +
           "AND (:driverId IS NULL OR d.id = :driverId) " +
           "AND (:vehicleId IS NULL OR v.id = :vehicleId) " +
           "AND (:status IS NULL OR ra.status = :status)")
    Page<RouteAssignment> findAllWithFilters(
            @Param("routeId") Long routeId,
            @Param("driverId") Long driverId,
            @Param("vehicleId") Long vehicleId,
            @Param("status") AssignmentStatus status,
            Pageable pageable);

    @Query("SELECT ra FROM RouteAssignment ra " +
           "LEFT JOIN FETCH ra.route r " +
           "LEFT JOIN FETCH ra.driver d " +
           "LEFT JOIN FETCH ra.vehicle v " +
           "LEFT JOIN FETCH ra.createdBy " +
           "WHERE ra.id = :id")
    Optional<RouteAssignment> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT ra FROM RouteAssignment ra " +
           "WHERE ra.driver.id = :driverId " +
           "AND ra.status = 'ACTIVE' " +
           "AND ra.startDate <= :currentDate " +
           "AND (ra.endDate IS NULL OR ra.endDate >= :currentDate)")
    List<RouteAssignment> findActiveAssignmentsByDriverId(
            @Param("driverId") Long driverId,
            @Param("currentDate") LocalDate currentDate);

    @Query("SELECT COUNT(ra) > 0 FROM RouteAssignment ra " +
           "WHERE ra.driver.id = :driverId " +
           "AND ra.status = 'ACTIVE' " +
           "AND ra.startDate <= :endDate " +
           "AND (ra.endDate IS NULL OR ra.endDate >= :startDate)")
    boolean existsActiveAssignmentForDriverInPeriod(
            @Param("driverId") Long driverId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(ra) > 0 FROM RouteAssignment ra " +
           "WHERE ra.vehicle.id = :vehicleId " +
           "AND ra.status = 'ACTIVE' " +
           "AND ra.startDate <= :endDate " +
           "AND (ra.endDate IS NULL OR ra.endDate >= :startDate)")
    boolean existsActiveAssignmentForVehicleInPeriod(
            @Param("vehicleId") Long vehicleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<RouteAssignment> findByDriverIdAndStatus(Long driverId, AssignmentStatus status);

    List<RouteAssignment> findByVehicleIdAndStatus(Long vehicleId, AssignmentStatus status);

    List<RouteAssignment> findByRouteIdAndStatus(Long routeId, AssignmentStatus status);

    @Query("SELECT COUNT(ra) > 0 FROM RouteAssignment ra WHERE ra.driver.id = :driverId")
    boolean existsByDriverId(@Param("driverId") Long driverId);
}

