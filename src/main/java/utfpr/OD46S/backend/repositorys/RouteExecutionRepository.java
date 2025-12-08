package utfpr.OD46S.backend.repositorys;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utfpr.OD46S.backend.entitys.RouteExecution;
import utfpr.OD46S.backend.enums.ExecutionStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RouteExecutionRepository extends JpaRepository<RouteExecution, Long> {

    @Query("SELECT re FROM RouteExecution re " +
            "JOIN FETCH re.assignment a " +
            "JOIN FETCH a.route r " +
            "JOIN FETCH a.driver d " +
            "JOIN FETCH a.vehicle v " +
            "WHERE (:assignmentId IS NULL OR a.id = :assignmentId) " +
            "AND (:driverId IS NULL OR d.id = :driverId) " +
            "AND (:status IS NULL OR re.status = :status) " +
            "AND (:startDate IS NULL OR re.executionDate >= :startDate) " +
            "AND (:endDate IS NULL OR re.executionDate <= :endDate)")
    Page<RouteExecution> findByFilters(
            @Param("assignmentId") Long assignmentId,
            @Param("driverId") Long driverId,
            @Param("status") ExecutionStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT re FROM RouteExecution re " +
            "JOIN FETCH re.assignment a " +
            "JOIN FETCH a.route r " +
            "JOIN FETCH a.driver d " +
            "JOIN FETCH a.vehicle v " +
            "WHERE re.id = :id")
    Optional<RouteExecution> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT re FROM RouteExecution re " +
            "JOIN FETCH re.assignment a " +
            "JOIN FETCH a.driver d " +
            "WHERE d.id = :driverId " +
            "AND re.status = 'IN_PROGRESS' " +
            "ORDER BY re.executionDate DESC, re.startTime DESC")
    Optional<RouteExecution> findCurrentExecutionByDriverId(@Param("driverId") Long driverId);

    @Query("SELECT re FROM RouteExecution re " +
            "JOIN FETCH re.assignment a " +
            "JOIN FETCH a.route r " +
            "JOIN FETCH a.driver d " +
            "JOIN FETCH a.vehicle v " +
            "WHERE re.executorId = :userId " +
            "AND re.status = 'IN_PROGRESS' " +
            "ORDER BY re.executionDate DESC, re.startTime DESC")
    Optional<RouteExecution> findCurrentExecutionByUserId(@Param("userId") Long userId);

    Optional<RouteExecution> findByAssignmentIdAndExecutionDate(Long assignmentId, LocalDate executionDate);

    List<RouteExecution> findByAssignmentId(Long assignmentId);

    @Query("SELECT COUNT(re) > 0 FROM RouteExecution re WHERE re.assignment.id = :assignmentId AND re.executionDate = :date")
    boolean existsByAssignmentIdAndDate(@Param("assignmentId") Long assignmentId, @Param("date") LocalDate date);

    /**
     * Verifica se existe uma execução não-cancelada para uma atribuição em uma data específica.
     * Permite iniciar novamente se a execução anterior foi cancelada.
     * 
     * @param assignmentId ID da atribuição
     * @param date Data da execução
     * @return true se existe uma execução com status IN_PROGRESS ou COMPLETED, false caso contrário
     */
    @Query("SELECT COUNT(re) > 0 FROM RouteExecution re " +
            "WHERE re.assignment.id = :assignmentId " +
            "AND re.executionDate = :date " +
            "AND re.status IN ('IN_PROGRESS', 'COMPLETED')")
    boolean existsNonCancelledByAssignmentIdAndDate(@Param("assignmentId") Long assignmentId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(re) > 0 FROM RouteExecution re JOIN re.assignment a WHERE a.driver.id = :driverId")
    boolean existsByDriverId(@Param("driverId") Long driverId);
}

