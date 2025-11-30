package vn.sun.public_service_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.sun.public_service_manager.entity.ApplicationStatus;

@Repository
public interface ApplicationStatusRepository extends JpaRepository<ApplicationStatus, Long> {

}
