package utfpr.OD46S.backend.dtos;

import utfpr.OD46S.backend.enums.ExecutionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class RouteExecutionDTO {

    private Long id;
    private Long assignmentId;
    private AssignmentDetailsDTO assignment;
    private LocalDate executionDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ExecutionStatus status;
    private Integer initialKm;
    private Integer finalKm;
    private BigDecimal totalCollectedWeightKg;
    private Integer pointsVisited;
    private Integer pointsCollected;
    private String initialNotes;
    private String finalNotes;
    private String problemsFound;
    private String cancellationReason;
    private Integer driverRating;
    private LocalDateTime createdAt;

    // Constructors
    public RouteExecutionDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public AssignmentDetailsDTO getAssignment() {
        return assignment;
    }

    public void setAssignment(AssignmentDetailsDTO assignment) {
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
        this.driverRating = driverRating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Nested DTO for assignment details
    public static class AssignmentDetailsDTO {
        private Long id;
        private RouteBasicDTO route;
        private DriverBasicDTO driver;
        private VehicleBasicDTO vehicle;

        public AssignmentDetailsDTO() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public RouteBasicDTO getRoute() {
            return route;
        }

        public void setRoute(RouteBasicDTO route) {
            this.route = route;
        }

        public DriverBasicDTO getDriver() {
            return driver;
        }

        public void setDriver(DriverBasicDTO driver) {
            this.driver = driver;
        }

        public VehicleBasicDTO getVehicle() {
            return vehicle;
        }

        public void setVehicle(VehicleBasicDTO vehicle) {
            this.vehicle = vehicle;
        }
    }

    public static class RouteBasicDTO {
        private Long id;
        private String name;
        private String collectionType;

        public RouteBasicDTO() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCollectionType() {
            return collectionType;
        }

        public void setCollectionType(String collectionType) {
            this.collectionType = collectionType;
        }
    }

    public static class DriverBasicDTO {
        private Long id;
        private String name;
        private String email;

        public DriverBasicDTO() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class VehicleBasicDTO {
        private Long id;
        private String licensePlate;
        private String model;
        private String brand;

        public VehicleBasicDTO() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getLicensePlate() {
            return licensePlate;
        }

        public void setLicensePlate(String licensePlate) {
            this.licensePlate = licensePlate;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getBrand() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }
    }
}

