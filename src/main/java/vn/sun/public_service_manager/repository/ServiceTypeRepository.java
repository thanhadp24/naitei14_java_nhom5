package vn.sun.public_service_manager.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.sun.public_service_manager.entity.ServiceType;

@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceType, Long> {
    
    Page<ServiceType> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    Page<ServiceType> findByCodeContainingIgnoreCase(String code, Pageable pageable);
    
    Page<ServiceType> findByResponsibleDepartmentId(Long departmentId, Pageable pageable);
}
