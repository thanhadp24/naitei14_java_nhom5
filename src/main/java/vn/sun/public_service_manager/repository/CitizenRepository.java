package vn.sun.public_service_manager.repository;

import vn.sun.public_service_manager.entity.Citizen;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CitizenRepository extends JpaRepository<Citizen, Long> {
    boolean existsByNationalId(String nationalId);

    boolean existsByEmail(String email);

    Optional<Citizen> findByNationalId(String nationalId); // Dùng cho đăng nhập

    @Query("SELECT c FROM Citizen c WHERE " +
            "c.fullName LIKE %?1% OR " +
            "c.nationalId LIKE %?1% OR " +
            "c.email LIKE %?1% OR " +
            "c.phone LIKE %?1%")
    Page<Citizen> findByKeyword(String keyword, Pageable pageable);
}