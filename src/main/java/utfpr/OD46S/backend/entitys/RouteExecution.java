package utfpr.OD46S.backend.entitys;

import jakarta.persistence.*;
import utfpr.OD46S.backend.enums.ExecutionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "route_executions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"assignment_id", "execution_date"})
})
public class RouteExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private RouteAssignment assignment;

    @Column(name = "execution_date", nullable = false)
    private LocalDate executionDate;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExecutionStatus status = ExecutionStatus.IN_PROGRESS;

    @Column(name = "initial_km")
    private Integer initialKm;

    @Column(name = "final_km")
    private Integer finalKm;

    @Column(name = "total_collected_weight_kg", precision = 10, scale = 2)
    private BigDecimal totalCollectedWeightKg;

    @Column(name = "points_visited")
    private Integer pointsVisited = 0;

    @Column(name = "points_collected")
    private Integer pointsCollected = 0;

    @Column(name = "initial_notes", columnDefinition = "TEXT")
    private String initialNotes;

    @Column(name = "final_notes", columnDefinition = "TEXT")
    private String finalNotes;

    @Column(name = "problems_found", columnDefinition = "TEXT")
    private String problemsFound;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "driver_rating")
    private Integer driverRating;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public RouteExecution() {
    }

    public RouteExecution(RouteAssignment assignment, LocalDate executionDate) {
        this.assignment = assignment;
        this.executionDate = executionDate;
        this.status = ExecutionStatus.IN_PROGRESS;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RouteAssignment getAssignment() {
        return assignment;
    }

    public void setAssignment(RouteAssignment assignment) {
        this.assignment = assignment;
    }

    public LocalDate getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(LocalDate executionDate) {
        this.executionDate = executionDate;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public Integer getInitialKm() {
        return initialKm;
    }

    public void setInitialKm(Integer initialKm) {
        this.initialKm = initialKm;
    }

    public Integer getFinalKm() {
        return finalKm;
    }

    public void setFinalKm(Integer finalKm) {
        this.finalKm = finalKm;
    }

    public BigDecimal getTotalCollectedWeightKg() {
        return totalCollectedWeightKg;
    }

    public void setTotalCollectedWeightKg(BigDecimal totalCollectedWeightKg) {
        this.totalCollectedWeightKg = totalCollectedWeightKg;
    }

    public Integer getPointsVisited() {
        return pointsVisited;
    }

    public void setPointsVisited(Integer pointsVisited) {
        this.pointsVisited = pointsVisited;
    }

    public Integer getPointsCollected() {
        return pointsCollected;
    }

    public void setPointsCollected(Integer pointsCollected) {
        this.pointsCollected = pointsCollected;
    }

    public String getInitialNotes() {
        return initialNotes;
    }

    public void setInitialNotes(String initialNotes) {
        this.initialNotes = initialNotes;
    }

    public String getFinalNotes() {
        return finalNotes;
    }

    public void setFinalNotes(String finalNotes) {
        this.finalNotes = finalNotes;
    }

    public String getProblemsFound() {
        return problemsFound;
    }

    public void setProblemsFound(String problemsFound) {
        this.problemsFound = problemsFound;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Integer getDriverRating() {
        return driverRating;
    }

    public void setDriverRating(Integer driverRating) {
        if (driverRating != null && (driverRating < 1 || driverRating > 5)) {
            throw new IllegalArgumentException("Driver rating must be between 1 and 5");
        }
        this.driverRating = driverRating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

