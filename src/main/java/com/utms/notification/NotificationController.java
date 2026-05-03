package com.utms.notification;

import com.utms.common.api.ApiResponse;
import com.utms.notification.dto.NotificationResponse;
import com.utms.notification.dto.UnreadCountResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** GET /api/notifications — all notifications for the current user, newest first */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> listMyNotifications() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.listMyNotifications()));
    }

    /** GET /api/notifications/unread-count — badge count for the current user */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount()));
    }

    /** POST /api/notifications/{id}/read — mark a notification as read */
    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.markAsRead(id)));
    }
}
