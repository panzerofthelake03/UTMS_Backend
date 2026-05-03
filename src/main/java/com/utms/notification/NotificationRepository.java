package com.utms.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderBySentAtDesc(Long recipientId);

    List<Notification> findByRecipientIdAndReadFalseOrderBySentAtDesc(Long recipientId);

    long countByRecipientIdAndReadFalse(Long recipientId);
}
