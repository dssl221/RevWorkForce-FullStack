package com.revworkforce.revworkforce_web.service;

import com.revworkforce.revworkforce_web.dao.NotificationDao;
import com.revworkforce.revworkforce_web.model.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationDao notificationDao;

    @InjectMocks
    private NotificationService notificationService;

    private Notification sampleNotification;

    @BeforeEach
    void setUp() {
        sampleNotification = Notification.builder()
                .id(1L)
                .userId(1L)
                .message("Test message")
                .read(false)
                .build();
    }

    @Test
    void createNotification_shouldSaveNotification() {
        when(notificationDao.save(1L, "Test message")).thenReturn(sampleNotification);

        Notification result = notificationService.createNotification(1L, "Test message");

        assertEquals("Test message", result.getMessage());
        verify(notificationDao).save(1L, "Test message");
    }

    @Test
    void getNotifications_shouldReturnNotifications() {
        when(notificationDao.findByUserId(1L)).thenReturn(List.of(sampleNotification));

        List<Notification> result = notificationService.getNotifications(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getUnreadNotifications_shouldReturnUnread() {
        when(notificationDao.findUnreadByUserId(1L)).thenReturn(List.of(sampleNotification));

        List<Notification> result = notificationService.getUnreadNotifications(1L);

        assertEquals(1, result.size());
    }

    @Test
    void countUnread_shouldReturnCount() {
        when(notificationDao.countUnread(1L)).thenReturn(5L);

        long result = notificationService.countUnread(1L);

        assertEquals(5L, result);
    }

    @Test
    void markRead_shouldCallDao() {
        notificationService.markRead(1L);

        verify(notificationDao).markRead(1L);
    }

    @Test
    void markAllRead_shouldCallDao() {
        notificationService.markAllRead(1L);

        verify(notificationDao).markAllRead(1L);
    }
}
