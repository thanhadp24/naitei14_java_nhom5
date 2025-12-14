package vn.sun.public_service_manager.repository;

import vn.sun.public_service_manager.entity.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceType, Long> {

    List<ServiceType> findByCategoryContainingIgnoreCase(String categoryKeyword);

    Optional<ServiceType> findByCategoryIgnoreCase(String category);
}
