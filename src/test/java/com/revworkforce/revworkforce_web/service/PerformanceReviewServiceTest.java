package com.revworkforce.revworkforce_web.service;

import com.revworkforce.revworkforce_web.dao.PerformanceReviewDao;
import com.revworkforce.revworkforce_web.dao.UserDao;
import com.revworkforce.revworkforce_web.model.PerformanceReview;
import com.revworkforce.revworkforce_web.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerformanceReviewServiceTest {

    @Mock
    private PerformanceReviewDao reviewDao;

    @Mock
    private UserDao userDao;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PerformanceReviewService performanceReviewService;

    private PerformanceReview sampleReview;
    private User sampleEmployee;

    @BeforeEach
    void setUp() {
        sampleReview = PerformanceReview.builder()
                .id(1L)
                .employeeId(1L)
                .deliverables("Completed API development")
                .accomplishments("Delivered 3 features")
                .improvements("Better time management")
                .selfRating(4)
                .status("DRAFT")
                .reviewYear(2026)
                .build();

        sampleEmployee = User.builder()
                .id(1L)
                .name("John Doe")
                .managerId(2L)
                .build();
    }

    @Test
    void createReview_shouldSaveReview() {
        when(reviewDao.save(sampleReview)).thenReturn(sampleReview);

        PerformanceReview result = performanceReviewService.createReview(sampleReview);

        assertNotNull(result);
        assertEquals("DRAFT", result.getStatus());
        verify(reviewDao).save(sampleReview);
    }

    @Test
    void getMyReviews_shouldReturnReviewsForEmployee() {
        when(reviewDao.findByEmployeeId(1L)).thenReturn(List.of(sampleReview));

        List<PerformanceReview> result = performanceReviewService.getMyReviews(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getTeamReviews_shouldReturnReviewsWithNames() {
        User reportee = User.builder().id(1L).name("John Doe").build();
        when(userDao.findByManagerId(2L)).thenReturn(List.of(reportee));
        when(reviewDao.findByEmployeeIdIn(List.of(1L))).thenReturn(List.of(sampleReview));

        List<PerformanceReview> result = performanceReviewService.getTeamReviews(2L);

        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getEmployeeName());
    }

    @Test
    void getTeamReviews_shouldReturnEmptyWhenNoReportees() {
        when(userDao.findByManagerId(2L)).thenReturn(Collections.emptyList());

        List<PerformanceReview> result = performanceReviewService.getTeamReviews(2L);

        assertTrue(result.isEmpty());
    }

    // ==================== submitReview() ====================

    @Test
    void submitReview_shouldSubmitDraftAndNotifyManager() {
        when(reviewDao.findById(1L)).thenReturn(Optional.of(sampleReview));
        when(userDao.findById(1L)).thenReturn(Optional.of(sampleEmployee));

        PerformanceReview result = performanceReviewService.submitReview(1L);

        assertEquals("SUBMITTED", result.getStatus());
        verify(reviewDao).submitReview(1L);
        verify(notificationService).createNotification(eq(2L), contains("submitted"));
    }

    @Test
    void submitReview_shouldThrowWhenNotDraft() {
        sampleReview.setStatus("SUBMITTED");
        when(reviewDao.findById(1L)).thenReturn(Optional.of(sampleReview));

        assertThrows(RuntimeException.class,
                () -> performanceReviewService.submitReview(1L));
    }

    @Test
    void submitReview_shouldThrowWhenReviewNotFound() {
        when(reviewDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> performanceReviewService.submitReview(99L));
    }

    // ==================== provideFeedback() ====================

    @Test
    void provideFeedback_shouldUpdateAndNotifyEmployee() {
        sampleReview.setStatus("SUBMITTED");
        when(reviewDao.findById(1L)).thenReturn(Optional.of(sampleReview));

        PerformanceReview result = performanceReviewService.provideFeedback(1L, 5, "Excellent work!");

        assertEquals("REVIEWED", result.getStatus());
        assertEquals(5, result.getManagerRating());
        assertEquals("Excellent work!", result.getManagerFeedback());
        verify(reviewDao).provideFeedback(1L, 5, "Excellent work!");
        verify(notificationService).createNotification(eq(1L), contains("feedback"));
    }

    @Test
    void provideFeedback_shouldThrowWhenNotSubmitted() {
        sampleReview.setStatus("DRAFT");
        when(reviewDao.findById(1L)).thenReturn(Optional.of(sampleReview));

        assertThrows(RuntimeException.class,
                () -> performanceReviewService.provideFeedback(1L, 4, "Feedback"));
    }

    @Test
    void provideFeedback_shouldThrowWhenReviewNotFound() {
        when(reviewDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> performanceReviewService.provideFeedback(99L, 4, "Feedback"));
    }
}
