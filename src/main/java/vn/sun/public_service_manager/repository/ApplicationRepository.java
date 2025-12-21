package vn.sun.public_service_manager.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.sun.public_service_manager.entity.Application;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long>, JpaSpecificationExecutor<Application> {

        @Query("SELECT a FROM Application a " +
                        "WHERE a.citizen.id = :citizendId AND " +
                        "(LOWER(a.note) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "OR LOWER(a.service.name) LIKE LOWER(CONCAT('%', :keyword, '%')) )")
        Page<Application> findByKeyword(Long citizendId, String keyword, Pageable pageable);

        @Query("SELECT a FROM Application a " +
                        "LEFT JOIN FETCH a.service s " +
                        "LEFT JOIN FETCH a.statuses st " +
                        "WHERE a.citizen.id = :citizenId " +
                        "ORDER BY a.submittedAt DESC")
        Page<Application> findByCitizenId(@Param("citizenId") Long citizenId, Pageable pageable);

        @EntityGraph(attributePaths = { "statuses", "service", "service.serviceType", "citizen", "assignedStaff" })
        @Query("SELECT a FROM Application a WHERE a.citizen.id = :citizenId")
        Page<Application> findByCitizenIdWithStatuses(@Param("citizenId") Long citizenId, Pageable pageable);

        @Query("SELECT a FROM Application a " +
                        "LEFT JOIN FETCH a.service s " +
                        "LEFT JOIN FETCH a.citizen c " +
                        "LEFT JOIN FETCH a.assignedStaff " +
                        "WHERE a.id = :id AND c.id = :citizenId")
        Optional<Application> findByIdWithDetails(@Param("id") Long id, Long citizenId);

}
