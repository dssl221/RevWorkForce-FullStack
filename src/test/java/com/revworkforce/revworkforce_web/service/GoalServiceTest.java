package com.revworkforce.revworkforce_web.service;

import com.revworkforce.revworkforce_web.dao.GoalDao;
import com.revworkforce.revworkforce_web.dao.UserDao;
import com.revworkforce.revworkforce_web.model.Goal;
import com.revworkforce.revworkforce_web.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalDao goalDao;

    @Mock
    private UserDao userDao;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private GoalService goalService;

    private Goal sampleGoal;

    @BeforeEach
    void setUp() {
        sampleGoal = Goal.builder()
                .id(1L)
                .employeeId(1L)
                .description("Complete Spring Boot project")
                .deadline(LocalDate.now().plusDays(30))
                .priority("HIGH")
                .progress(0)
                .build();
    }

    @Test
    void createGoal_shouldSaveGoal() {
        when(goalDao.save(sampleGoal)).thenReturn(sampleGoal);

        Goal result = goalService.createGoal(sampleGoal);

        assertNotNull(result);
        assertEquals("Complete Spring Boot project", result.getDescription());
        verify(goalDao).save(sampleGoal);
    }

    @Test
    void getMyGoals_shouldReturnGoalsForEmployee() {
        when(goalDao.findByEmployeeId(1L)).thenReturn(List.of(sampleGoal));

        List<Goal> result = goalService.getMyGoals(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getTeamGoals_shouldReturnGoalsWithEmployeeNames() {
        User reportee = User.builder().id(1L).name("John Doe").build();
        when(userDao.findByManagerId(2L)).thenReturn(List.of(reportee));
        when(goalDao.findByEmployeeIdIn(List.of(1L))).thenReturn(List.of(sampleGoal));

        List<Goal> result = goalService.getTeamGoals(2L);

        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getEmployeeName());
    }

    @Test
    void getTeamGoals_shouldReturnEmptyWhenNoReportees() {
        when(userDao.findByManagerId(2L)).thenReturn(Collections.emptyList());

        List<Goal> result = goalService.getTeamGoals(2L);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateProgress_shouldUpdateAndReturn() {
        when(goalDao.findById(1L)).thenReturn(Optional.of(sampleGoal));

        Goal result = goalService.updateProgress(1L, 50);

        assertEquals(50, result.getProgress());
        verify(goalDao).updateProgress(1L, 50);
    }

    @Test
    void updateProgress_shouldThrowWhenGoalNotFound() {
        when(goalDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> goalService.updateProgress(99L, 50));
    }

    @Test
    void addComment_shouldAddCommentAndNotifyEmployee() {
        when(goalDao.findById(1L)).thenReturn(Optional.of(sampleGoal));

        Goal result = goalService.addComment(1L, "Good progress!");

        assertEquals("Good progress!", result.getManagerComment());
        verify(goalDao).addComment(1L, "Good progress!");
        verify(notificationService).createNotification(eq(1L), contains("commented"));
    }

    @Test
    void addComment_shouldThrowWhenGoalNotFound() {
        when(goalDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> goalService.addComment(99L, "Comment"));
    }

    @Test
    void deleteGoal_shouldCallDao() {
        goalService.deleteGoal(1L);

        verify(goalDao).delete(1L);
    }
}
