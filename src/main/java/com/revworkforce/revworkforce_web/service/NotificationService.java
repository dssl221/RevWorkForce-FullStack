package com.revworkforce.revworkforce_web.service;

import com.revworkforce.revworkforce_web.dao.NotificationDao;
import com.revworkforce.revworkforce_web.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationDao notificationDao;

    public Notification createNotification(Long userId, String message) {
        return notificationDao.save(userId, message);
    }

    public List<Notification> getNotifications(Long userId) {
        return notificationDao.findByUserId(userId);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationDao.findUnreadByUserId(userId);
    }

    public long countUnread(Long userId) {
        return notificationDao.countUnread(userId);
    }

    public void markRead(Long notifId) {
        notificationDao.markRead(notifId);
    }

    public void markAllRead(Long userId) {
        notificationDao.markAllRead(userId);
    }
}
