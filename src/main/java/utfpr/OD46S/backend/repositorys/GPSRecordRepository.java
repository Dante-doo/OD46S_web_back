package utfpr.OD46S.backend.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utfpr.OD46S.backend.entitys.GPSRecord;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GPSRecordRepository extends JpaRepository<GPSRecord, Long> {

    @Query("SELECT g FROM GPSRecord g WHERE g.execution.id = :executionId ORDER BY g.gpsTimestamp ASC")
    List<GPSRecord> findByExecutionIdOrderByTimestamp(@Param("executionId") Long executionId);

    @Query("SELECT g FROM GPSRecord g WHERE g.execution.id = :executionId " +
           "AND g.gpsTimestamp BETWEEN :startTime AND :endTime " +
           "ORDER BY g.gpsTimestamp ASC")
    List<GPSRecord> findByExecutionIdAndTimestampBetween(
            @Param("executionId") Long executionId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT g FROM GPSRecord g WHERE g.execution.id = :executionId " +
           "AND g.eventType = :eventType ORDER BY g.gpsTimestamp ASC")
    List<GPSRecord> findByExecutionIdAndEventType(
            @Param("executionId") Long executionId,
            @Param("eventType") String eventType
    );

    @Query("SELECT COUNT(g) FROM GPSRecord g WHERE g.execution.id = :executionId")
    long countByExecutionId(@Param("executionId") Long executionId);

    @Query("SELECT g FROM GPSRecord g WHERE g.execution.id = :executionId " +
           "ORDER BY g.gpsTimestamp DESC LIMIT 1")
    GPSRecord findLatestByExecutionId(@Param("executionId") Long executionId);
}

