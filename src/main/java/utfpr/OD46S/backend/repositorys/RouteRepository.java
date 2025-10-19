package utfpr.OD46S.backend.repositorys;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import utfpr.OD46S.backend.entitys.Route;
import utfpr.OD46S.backend.enums.CollectionType;
import utfpr.OD46S.backend.enums.Priority;

import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {
    
    Optional<Route> findByIdAndActiveTrue(Long id);
    
    @Query("SELECT r FROM Route r WHERE " +
           "(:search IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:collectionType IS NULL OR r.collectionType = :collectionType) AND " +
           "(:priority IS NULL OR r.priority = :priority) AND " +
           "(:active IS NULL OR r.active = :active)")
    Page<Route> findByFilters(
        @Param("search") String search,
        @Param("collectionType") CollectionType collectionType,
        @Param("priority") Priority priority,
        @Param("active") Boolean active,
        Pageable pageable
    );
}

