package com.revworkforce.revworkforce_web.service;

import com.revworkforce.revworkforce_web.dao.AnnouncementDao;
import com.revworkforce.revworkforce_web.model.Announcement;
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
class AnnouncementServiceTest {

    @Mock
    private AnnouncementDao announcementDao;

    @InjectMocks
    private AnnouncementService announcementService;

    private Announcement sampleAnnouncement;

    @BeforeEach
    void setUp() {
        sampleAnnouncement = Announcement.builder()
                .id(1L)
                .title("Maintenance")
                .description("System maintenance this weekend")
                .build();
    }

    @Test
    void findAll_shouldReturnAnnouncements() {
        when(announcementDao.findAll()).thenReturn(List.of(sampleAnnouncement));

        List<Announcement> result = announcementService.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void save_shouldSaveAnnouncement() {
        when(announcementDao.save(sampleAnnouncement)).thenReturn(sampleAnnouncement);

        Announcement result = announcementService.save(sampleAnnouncement);

        assertEquals("Maintenance", result.getTitle());
        verify(announcementDao).save(sampleAnnouncement);
    }

    @Test
    void update_shouldCallDao() {
        announcementService.update(1L, "Updated Title", "Updated Desc");

        verify(announcementDao).update(1L, "Updated Title", "Updated Desc");
    }

    @Test
    void delete_shouldCallDao() {
        announcementService.delete(1L);

        verify(announcementDao).delete(1L);
    }
}
