package vn.sun.public_service_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.sun.public_service_manager.entity.Application;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

}
