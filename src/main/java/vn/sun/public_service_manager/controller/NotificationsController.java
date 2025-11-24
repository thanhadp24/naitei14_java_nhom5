package vn.sun.public_service_manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.sun.public_service_manager.dto.NotificationDTO;
import vn.sun.public_service_manager.service.NotificationService;
import vn.sun.public_service_manager.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationsController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    @ApiMessage("Lấy danh sách thông báo thành công")
    public ResponseEntity<Page<NotificationDTO>> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        String nationalId = authentication.getName();
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<NotificationDTO> notifications = notificationService
            .getNotificationsByNationalId(nationalId, pageable);
        
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    @ApiMessage("Lấy số lượng thông báo chưa đọc thành công")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        String nationalId = authentication.getName();
        Long unreadCount = notificationService.getUnreadCount(nationalId);
        
        return ResponseEntity.ok(unreadCount);
    }

    @PutMapping("/{id}/mark-as-read")
    @ApiMessage("Đánh dấu thông báo đã đọc thành công")
    public ResponseEntity<NotificationDTO> markAsRead(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String nationalId = authentication.getName();
        NotificationDTO notification = notificationService.markAsRead(id, nationalId);
        
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/mark-all-as-read")
    @ApiMessage("Đánh dấu tất cả thông báo đã đọc thành công")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        String nationalId = authentication.getName();
        notificationService.markAllAsRead(nationalId);
        
        return ResponseEntity.ok().build();
    }
}
