package vn.sun.public_service_manager.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.sun.public_service_manager.dto.NotificationDTO;

public interface NotificationService {
    
    Page<NotificationDTO> getNotificationsByNationalId(String nationalId, Pageable pageable);
    
    Long getUnreadCount(String nationalId);
    
    NotificationDTO markAsRead(Long notificationId, String nationalId);
    
    void markAllAsRead(String nationalId);
}
