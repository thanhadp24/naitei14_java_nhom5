package vn.sun.public_service_manager.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.sun.public_service_manager.entity.Citizen;

public interface CitizenRepository extends JpaRepository<Citizen, Long> {
        boolean existsByNationalId(String nationalId);

        boolean existsByEmail(String email);

        boolean existsByPhone(String phone);

        Optional<Citizen> findByNationalId(String nationalId); // Dùng cho đăng nhập

        @Query("SELECT c FROM Citizen c WHERE " +
                        "c.fullName LIKE %?1% OR " +
                        "c.nationalId LIKE %?1% OR " +
                        "c.email LIKE %?1% OR " +
                        "c.phone LIKE %?1%")
        Page<Citizen> findByKeyword(String keyword, Pageable pageable);

        @Query("SELECT c FROM Citizen c " +
                        "WHERE :search IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(c.nationalId) LIKE LOWER(CONCAT('%', :search, '%'))")
        Page<Citizen> findWithSearch(@Param("search") String search, Pageable pageable);

}