package vn.sun.public_service_manager.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.sun.public_service_manager.entity.Citizen;
import vn.sun.public_service_manager.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Page<Notification> findByCitizen(Citizen citizen, Pageable pageable);
    
    Long countByCitizenAndIsReadFalse(Citizen citizen);
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.citizen.id = :citizenId AND n.isRead = false")
    void markAllAsReadByCitizen(@Param("citizenId") Long citizenId);
}
