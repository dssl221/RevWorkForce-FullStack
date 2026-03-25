package com.revworkforce.revworkforce_web.service;

import com.revworkforce.revworkforce_web.dao.*;
import com.revworkforce.revworkforce_web.model.*;
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
class LeaveServiceTest {

    @Mock
    private LeaveRequestDao leaveRequestDao;

    @Mock
    private LeaveBalanceDao leaveBalanceDao;

    @Mock
    private LeaveTypeDao leaveTypeDao;

    @Mock
    private UserDao userDao;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LeaveService leaveService;

    private User sampleEmployee;
    private LeaveRequest sampleLeaveRequest;
    private LeaveBalance sampleBalance;

    @BeforeEach
    void setUp() {
        sampleEmployee = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(User.Role.EMPLOYEE)
                .managerId(2L)
                .active(true)
                .build();

        sampleLeaveRequest = LeaveRequest.builder()
                .id(1L)
                .employeeId(1L)
                .leaveType("Sick Leave")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .reason("Not feeling well")
                .status(LeaveRequest.LeaveStatus.PENDING)
                .build();

        sampleBalance = LeaveBalance.builder()
                .id(1L)
                .employeeId(1L)
                .leaveType("Sick Leave")
                .totalDays(10)
                .usedDays(2)
                .build();
    }

    // ==================== applyLeave() ====================

    @Test
    void applyLeave_shouldSaveAndNotifyManager() {
        when(leaveBalanceDao.findByEmployeeIdAndLeaveType(1L, "Sick Leave"))
                .thenReturn(Optional.of(sampleBalance));
        when(leaveRequestDao.save(any(LeaveRequest.class))).thenReturn(sampleLeaveRequest);
        when(userDao.findById(1L)).thenReturn(Optional.of(sampleEmployee));

        LeaveRequest result = leaveService.applyLeave(sampleLeaveRequest);

        assertNotNull(result);
        verify(leaveRequestDao).save(any(LeaveRequest.class));
        verify(notificationService).createNotification(eq(2L), anyString());
    }

    @Test
    void applyLeave_shouldThrowWhenStartDateAfterEndDate() {
        sampleLeaveRequest.setStartDate(LocalDate.now().plusDays(5));
        sampleLeaveRequest.setEndDate(LocalDate.now().plusDays(1));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.applyLeave(sampleLeaveRequest));
        assertEquals("Start date must be before end date", ex.getMessage());
    }

    @Test
    void applyLeave_shouldThrowWhenStartDateInPast() {
        sampleLeaveRequest.setStartDate(LocalDate.now().minusDays(1));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.applyLeave(sampleLeaveRequest));
        assertEquals("Cannot apply leave for past dates", ex.getMessage());
    }

    @Test
    void applyLeave_shouldThrowWhenInsufficientBalance() {
        LeaveBalance lowBalance = LeaveBalance.builder()
                .employeeId(1L)
                .leaveType("Sick Leave")
                .totalDays(2)
                .usedDays(2)
                .build();

        when(leaveBalanceDao.findByEmployeeIdAndLeaveType(1L, "Sick Leave"))
                .thenReturn(Optional.of(lowBalance));

        assertThrows(RuntimeException.class,
                () -> leaveService.applyLeave(sampleLeaveRequest));
    }

    @Test
    void applyLeave_shouldThrowWhenLeaveTypeNotConfigured() {
        when(leaveBalanceDao.findByEmployeeIdAndLeaveType(1L, "Sick Leave"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> leaveService.applyLeave(sampleLeaveRequest));
    }

    // ==================== cancelLeave() ====================

    @Test
    void cancelLeave_shouldCancelPendingLeave() {
        when(leaveRequestDao.findById(1L)).thenReturn(Optional.of(sampleLeaveRequest));

        LeaveRequest result = leaveService.cancelLeave(1L, 1L);

        assertEquals(LeaveRequest.LeaveStatus.CANCELLED, result.getStatus());
        verify(leaveRequestDao).cancelLeave(1L);
    }

    @Test
    void cancelLeave_shouldThrowWhenLeaveNotFound() {
        when(leaveRequestDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> leaveService.cancelLeave(99L, 1L));
    }

    @Test
    void cancelLeave_shouldThrowWhenUnauthorized() {
        when(leaveRequestDao.findById(1L)).thenReturn(Optional.of(sampleLeaveRequest));

        assertThrows(RuntimeException.class, () -> leaveService.cancelLeave(1L, 99L));
    }

    @Test
    void cancelLeave_shouldThrowWhenNotPending() {
        sampleLeaveRequest.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        when(leaveRequestDao.findById(1L)).thenReturn(Optional.of(sampleLeaveRequest));

        assertThrows(RuntimeException.class, () -> leaveService.cancelLeave(1L, 1L));
    }

    // ==================== approveLeave() ====================

    @Test
    void approveLeave_shouldApproveAndNotifyEmployee() {
        when(leaveRequestDao.findById(1L)).thenReturn(Optional.of(sampleLeaveRequest));

        LeaveRequest result = leaveService.approveLeave(1L, "Approved");

        assertEquals(LeaveRequest.LeaveStatus.APPROVED, result.getStatus());
        assertEquals("Approved", result.getManagerComment());
        verify(leaveRequestDao).approveLeave(eq(1L), eq("Approved"), anyLong());
        verify(notificationService).createNotification(eq(1L), contains("approved"));
    }

    @Test
    void approveLeave_shouldThrowWhenNotPending() {
        sampleLeaveRequest.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        when(leaveRequestDao.findById(1L)).thenReturn(Optional.of(sampleLeaveRequest));

        assertThrows(RuntimeException.class, () -> leaveService.approveLeave(1L, "OK"));
    }

    // ==================== rejectLeave() ====================

    @Test
    void rejectLeave_shouldRejectAndNotifyEmployee() {
        when(leaveRequestDao.findById(1L)).thenReturn(Optional.of(sampleLeaveRequest));

        LeaveRequest result = leaveService.rejectLeave(1L, "Not enough coverage");

        assertEquals(LeaveRequest.LeaveStatus.REJECTED, result.getStatus());
        verify(leaveRequestDao).rejectLeave(1L, "Not enough coverage");
        verify(notificationService).createNotification(eq(1L), contains("rejected"));
    }

    @Test
    void rejectLeave_shouldThrowWhenCommentIsEmpty() {
        when(leaveRequestDao.findById(1L)).thenReturn(Optional.of(sampleLeaveRequest));

        assertThrows(RuntimeException.class, () -> leaveService.rejectLeave(1L, ""));
    }

    @Test
    void rejectLeave_shouldThrowWhenCommentIsNull() {
        when(leaveRequestDao.findById(1L)).thenReturn(Optional.of(sampleLeaveRequest));

        assertThrows(RuntimeException.class, () -> leaveService.rejectLeave(1L, null));
    }

    @Test
    void rejectLeave_shouldThrowWhenNotPending() {
        sampleLeaveRequest.setStatus(LeaveRequest.LeaveStatus.REJECTED);
        when(leaveRequestDao.findById(1L)).thenReturn(Optional.of(sampleLeaveRequest));

        assertThrows(RuntimeException.class, () -> leaveService.rejectLeave(1L, "Reason"));
    }

    // ==================== getTeamLeaves() ====================

    @Test
    void getTeamLeaves_shouldReturnLeavesForReportees() {
        User reportee = User.builder().id(3L).name("Reportee").build();
        when(userDao.findByManagerId(2L)).thenReturn(List.of(reportee));
        when(leaveRequestDao.findByEmployeeIdIn(List.of(3L))).thenReturn(List.of(sampleLeaveRequest));

        List<LeaveRequest> result = leaveService.getTeamLeaves(2L);

        assertEquals(1, result.size());
    }

    @Test
    void getTeamLeaves_shouldReturnEmptyWhenNoReportees() {
        when(userDao.findByManagerId(2L)).thenReturn(Collections.emptyList());

        List<LeaveRequest> result = leaveService.getTeamLeaves(2L);

        assertTrue(result.isEmpty());
    }

    // ==================== Leave Type CRUD ====================

    @Test
    void addLeaveType_shouldSaveWhenNameIsNew() {
        LeaveType lt = LeaveType.builder().name("Paternity").defaultDays(5).build();
        when(leaveTypeDao.existsByName("Paternity")).thenReturn(false);
        when(leaveTypeDao.save(lt)).thenReturn(lt);

        LeaveType result = leaveService.addLeaveType(lt);

        assertNotNull(result);
        verify(leaveTypeDao).save(lt);
    }

    @Test
    void addLeaveType_shouldThrowWhenNameExists() {
        LeaveType lt = LeaveType.builder().name("Sick Leave").build();
        when(leaveTypeDao.existsByName("Sick Leave")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> leaveService.addLeaveType(lt));
    }

    @Test
    void getLeaveBalances_shouldReturnBalances() {
        when(leaveBalanceDao.findByEmployeeId(1L)).thenReturn(List.of(sampleBalance));

        List<LeaveBalance> result = leaveService.getLeaveBalances(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getLeaveBalances_shouldDeduplicateLeaveTypes() {
        LeaveBalance duplicate = LeaveBalance.builder()
                .id(2L)
                .employeeId(1L)
                .leaveType("Sick Leave")
                .totalDays(10)
                .usedDays(1)
                .build();

        when(leaveBalanceDao.findByEmployeeId(1L)).thenReturn(List.of(sampleBalance, duplicate));

        List<LeaveBalance> result = leaveService.getLeaveBalances(1L);

        assertEquals(1, result.size());
        assertEquals("Sick Leave", result.get(0).getLeaveType());
        assertEquals(10, result.get(0).getTotalDays());
        assertEquals(2, result.get(0).getUsedDays());
    }


    @Test
    void getAllLeaves_shouldReturnAll() {
        when(leaveRequestDao.findAll()).thenReturn(List.of(sampleLeaveRequest));

        List<LeaveRequest> result = leaveService.getAllLeaves();

        assertEquals(1, result.size());
    }

    @Test
    void countPendingLeaves_shouldReturnCount() {
        when(leaveRequestDao.countByStatus("PENDING")).thenReturn(5L);

        long count = leaveService.countPendingLeaves();

        assertEquals(5, count);
    }
}
