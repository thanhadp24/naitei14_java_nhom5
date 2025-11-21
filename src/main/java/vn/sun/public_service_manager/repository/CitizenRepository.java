package vn.sun.public_service_manager.repository;

import vn.sun.public_service_manager.entity.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CitizenRepository extends JpaRepository<Citizen, Long> {
    boolean existsByNationalId(String nationalId);
    boolean existsByEmail(String email);
    Optional<Citizen> findByNationalId(String nationalId); // Dùng cho đăng nhập
}