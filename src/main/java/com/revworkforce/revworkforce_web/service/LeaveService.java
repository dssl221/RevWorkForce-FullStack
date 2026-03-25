package com.revworkforce.revworkforce_web.service;

import com.revworkforce.revworkforce_web.dao.*;
import com.revworkforce.revworkforce_web.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestDao leaveRequestDao;
    private final LeaveBalanceDao leaveBalanceDao;
    private final LeaveTypeDao leaveTypeDao;
    private final UserDao userDao;
    private final NotificationService notificationService;

    public LeaveRequest applyLeave(LeaveRequest request) {
        // Validate dates
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("Start date must be before end date");
        }
        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot apply leave for past dates");
        }

        // Check leave balance
        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        LeaveBalance balance = leaveBalanceDao
                .findByEmployeeIdAndLeaveType(request.getEmployeeId(), request.getLeaveType())
                .orElseThrow(() -> new RuntimeException("Leave type not configured for employee"));

        if (balance.getRemainingDays() < days) {
            throw new RuntimeException(
                    "Insufficient leave balance. Available: " + balance.getRemainingDays() + " days");
        }

        LeaveRequest saved = leaveRequestDao.save(request);

        // Notify manager
        User employee = userDao.findById(request.getEmployeeId()).orElse(null);
        if (employee != null && employee.getManagerId() != null) {
            notificationService.createNotification(employee.getManagerId(),
                    employee.getName() + " has applied for " + request.getLeaveType() +
                            " from " + request.getStartDate() + " to " + request.getEndDate());
        }

        return saved;
    }

    public List<LeaveRequest> getMyLeaves(Long employeeId) {
        return leaveRequestDao.findByEmployeeId(employeeId);
    }

    public LeaveRequest cancelLeave(Long leaveId, Long employeeId) {
        LeaveRequest leave = leaveRequestDao.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));
        if (!leave.getEmployeeId().equals(employeeId)) {
            throw new RuntimeException("Unauthorized");
        }
        if (leave.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new RuntimeException("Only pending leaves can be cancelled");
        }
        leaveRequestDao.cancelLeave(leaveId);
        leave.setStatus(LeaveRequest.LeaveStatus.CANCELLED);
        return leave;
    }

    public List<LeaveRequest> getTeamLeaves(Long managerId) {
        List<User> reportees = userDao.findByManagerId(managerId);
        List<Long> reporteeIds = reportees.stream().map(User::getId).collect(Collectors.toList());
        if (reporteeIds.isEmpty())
            return Collections.emptyList();
        return leaveRequestDao.findByEmployeeIdIn(reporteeIds);
    }

    public LeaveRequest approveLeave(Long leaveId, String comment) {
        LeaveRequest leave = leaveRequestDao.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));
        if (leave.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new RuntimeException("Leave is not pending");
        }

        long days = ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
        leaveRequestDao.approveLeave(leaveId, comment, days);

        // Notify employee
        notificationService.createNotification(leave.getEmployeeId(),
                "Your " + leave.getLeaveType() + " leave request has been approved.");

        leave.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        leave.setManagerComment(comment);
        return leave;
    }

    public LeaveRequest rejectLeave(Long leaveId, String comment) {
        LeaveRequest leave = leaveRequestDao.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));
        if (leave.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new RuntimeException("Leave is not pending");
        }
        if (comment == null || comment.trim().isEmpty()) {
            throw new RuntimeException("Comment is mandatory when rejecting leave");
        }
        leaveRequestDao.rejectLeave(leaveId, comment);

        // Notify employee
        notificationService.createNotification(leave.getEmployeeId(),
                "Your " + leave.getLeaveType() + " leave request has been rejected. Reason: " + comment);

        leave.setStatus(LeaveRequest.LeaveStatus.REJECTED);
        leave.setManagerComment(comment);
        return leave;
    }

    public List<LeaveBalance> getLeaveBalances(Long employeeId) {
        List<LeaveBalance> rawBalances = leaveBalanceDao.findByEmployeeId(employeeId);
        Map<String, LeaveBalance> merged = new LinkedHashMap<>();

        for (LeaveBalance b : rawBalances) {
            if (b.getLeaveType() == null) continue;
            String key = b.getLeaveType().trim().toLowerCase();
            if (!merged.containsKey(key)) {
                merged.put(key, LeaveBalance.builder()
                        .id(b.getId())
                        .employeeId(b.getEmployeeId())
                        .leaveType(b.getLeaveType().trim())
                        .totalDays(b.getTotalDays())
                        .usedDays(b.getUsedDays())
                        .build());
            }
        }

        return new ArrayList<>(merged.values());
    }


    // Admin methods
    public List<LeaveType> getAllLeaveTypes() {
        return leaveTypeDao.findAll();
    }

    public LeaveType addLeaveType(LeaveType leaveType) {
        if (leaveTypeDao.existsByName(leaveType.getName())) {
            throw new RuntimeException("Leave type already exists");
        }
        return leaveTypeDao.save(leaveType);
    }

    public LeaveType updateLeaveType(Long id, LeaveType updated) {
        leaveTypeDao.findById(id).orElseThrow(() -> new RuntimeException("Leave type not found"));
        leaveTypeDao.update(id, updated.getName(), updated.getDefaultDays());
        return updated;
    }

    public void deleteLeaveType(Long id) {
        leaveTypeDao.delete(id);
    }

    public LeaveBalance adjustLeaveBalance(Long employeeId, String leaveType, int totalDays) {
        leaveBalanceDao.adjustBalance(employeeId, leaveType, totalDays);
        return leaveBalanceDao.findByEmployeeIdAndLeaveType(employeeId, leaveType)
                .orElse(LeaveBalance.builder()
                        .employeeId(employeeId)
                        .leaveType(leaveType)
                        .totalDays(totalDays)
                        .usedDays(0)
                        .build());
    }

    public List<LeaveRequest> getAllLeaves() {
        return leaveRequestDao.findAll();
    }

    public long countPendingLeaves() {
        return leaveRequestDao.countByStatus("PENDING");
    }
}
