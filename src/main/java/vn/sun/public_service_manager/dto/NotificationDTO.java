package vn.sun.public_service_manager.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.sun.public_service_manager.entity.Notification;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    
    private Long id;
    private Long applicationId;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
    
    public static NotificationDTO fromEntity(Notification notification) {
        return NotificationDTO.builder()
            .id(notification.getId())
            .applicationId(notification.getApplicationId())
            .message(notification.getMessage())
            .isRead(notification.getIsRead())
            .createdAt(notification.getCreatedAt())
            .build();
    }
}
