package vn.sun.public_service_manager.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.sun.public_service_manager.dto.NotificationDTO;
import vn.sun.public_service_manager.entity.Citizen;
import vn.sun.public_service_manager.entity.Notification;
import vn.sun.public_service_manager.repository.CitizenRepository;
import vn.sun.public_service_manager.repository.NotificationRepository;
import vn.sun.public_service_manager.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private CitizenRepository citizenRepository;

    @Override
    public Page<NotificationDTO> getNotificationsByNationalId(String nationalId, Pageable pageable) {
        Citizen citizen = citizenRepository.findByNationalId(nationalId)
            .orElseThrow(() -> new RuntimeException("Công dân không tồn tại"));
        
        return notificationRepository.findByCitizen(citizen, pageable)
            .map(NotificationDTO::fromEntity);
    }

    @Override
    public Long getUnreadCount(String nationalId) {
        Citizen citizen = citizenRepository.findByNationalId(nationalId)
            .orElseThrow(() -> new RuntimeException("Công dân không tồn tại"));
        
        return notificationRepository.countByCitizenAndIsReadFalse(citizen);
    }

    @Override
    @Transactional
    public NotificationDTO markAsRead(Long notificationId, String nationalId) {
        Citizen citizen = citizenRepository.findByNationalId(nationalId)
            .orElseThrow(() -> new RuntimeException("Công dân không tồn tại"));
        
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Thông báo không tồn tại"));
        
        if (!notification.getCitizen().getId().equals(citizen.getId())) {
            throw new RuntimeException("Không có quyền truy cập thông báo này");
        }
        
        notification.setIsRead(true);
        Notification updatedNotification = notificationRepository.save(notification);
        
        return NotificationDTO.fromEntity(updatedNotification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String nationalId) {
        Citizen citizen = citizenRepository.findByNationalId(nationalId)
            .orElseThrow(() -> new RuntimeException("Công dân không tồn tại"));
        
        notificationRepository.markAllAsReadByCitizen(citizen.getId());
    }
}
