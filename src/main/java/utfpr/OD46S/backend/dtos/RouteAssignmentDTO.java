package utfpr.OD46S.backend.dtos;

import utfpr.OD46S.backend.enums.AssignmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RouteAssignmentDTO {

    private Long id;
    private RouteBasicDTO route;
    private DriverBasicDTO driver;
    private VehicleBasicDTO vehicle;
    private AssignmentStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Nested DTOs for related entities
    public static class RouteBasicDTO {
        private Long id;
        private String name;
        private String periodicity;
        private String collectionType;

        public RouteBasicDTO() {}

        public RouteBasicDTO(Long id, String name, String periodicity, String collectionType) {
            this.id = id;
            this.name = name;
            this.periodicity = periodicity;
            this.collectionType = collectionType;
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

        public String getPeriodicity() {
            return periodicity;
        }

        public void setPeriodicity(String periodicity) {
            this.periodicity = periodicity;
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
        private String licenseNumber;

        public DriverBasicDTO() {}

        public DriverBasicDTO(Long id, String name, String email, String licenseNumber) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.licenseNumber = licenseNumber;
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

        public String getLicenseNumber() {
            return licenseNumber;
        }

        public void setLicenseNumber(String licenseNumber) {
            this.licenseNumber = licenseNumber;
        }
    }

    public static class VehicleBasicDTO {
        private Long id;
        private String licensePlate;
        private String model;
        private Integer year;

        public VehicleBasicDTO() {}

        public VehicleBasicDTO(Long id, String licensePlate, String model, Integer year) {
            this.id = id;
            this.licensePlate = licensePlate;
            this.model = model;
            this.year = year;
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

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }
    }

    // Constructors
    public RouteAssignmentDTO() {}

    // Getters and Setters
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

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

