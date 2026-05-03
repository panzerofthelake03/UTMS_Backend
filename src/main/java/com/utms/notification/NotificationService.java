package com.utms.notification;

import com.utms.application.Application;
import com.utms.common.security.AuthenticatedUserService;
import com.utms.notification.dto.NotificationResponse;
import com.utms.notification.dto.UnreadCountResponse;
import com.utms.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public NotificationService(NotificationRepository notificationRepository,
                               AuthenticatedUserService authenticatedUserService) {
        this.notificationRepository = notificationRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    /**
     * Called by YgkService after finalising a decision (ACCEPTED or REJECTED).
     * Creates a notification for the student who owns the application.
     */
    @Transactional
    public void createApplicationResultNotification(Application application, String decision) {
        User student = application.getStudent().getUser();

        String title = "ACCEPTED".equals(decision)
                ? "Your application has been accepted"
                : "Your application has been rejected";
        String message = String.format(
                "Your application (term: %s) has been reviewed. Final decision: %s.",
                application.getTerm(), decision);

        Notification notification = new Notification();
        notification.setRecipient(student);
        notification.setApplication(application);
        notification.setNotificationType("APPLICATION_RESULT");
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setSentAt(Instant.now());

        notificationRepository.save(notification);
    }

    /**
     * Returns all notifications for the currently authenticated user,
     * newest first.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> listMyNotifications() {
        User currentUser = authenticatedUserService.getCurrentUser();
        return notificationRepository
                .findByRecipientIdOrderBySentAtDesc(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Returns the number of unread notifications for the current user.
     */
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount() {
        User currentUser = authenticatedUserService.getCurrentUser();
        long count = notificationRepository.countByRecipientIdAndReadFalse(currentUser.getId());
        return new UnreadCountResponse(count);
    }

    /**
     * Marks a single notification as read.
     * Only the owner of the notification may mark it.
     */
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        User currentUser = authenticatedUserService.getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Notification not found: " + notificationId));

        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only mark your own notifications as read");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(Instant.now());
            notificationRepository.save(notification);
        }

        return toResponse(notification);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getApplication() != null ? n.getApplication().getId() : null,
                n.getNotificationType(),
                n.getTitle(),
                n.getMessage(),
                n.isRead(),
                n.getReadAt(),
                n.getSentAt(),
                n.getCreatedAt()
        );
    }
}
