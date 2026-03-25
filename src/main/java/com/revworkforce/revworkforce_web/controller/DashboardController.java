package com.revworkforce.revworkforce_web.controller;

import com.revworkforce.revworkforce_web.model.User;
import com.revworkforce.revworkforce_web.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final LeaveService leaveService;
    private final AnnouncementService announcementService;
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> getDashboard(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");
        if (userId == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        Map<String, Object> data = new HashMap<>();
        data.put("role", role);
        data.put("announcements", announcementService.findAll());
        data.put("leaveBalances", leaveService.getLeaveBalances(userId));
        data.put("unreadNotifications", notificationService.countUnread(userId));

        if ("ADMIN".equals(role)) {
            data.put("totalEmployees", userService.countActiveEmployees());
            data.put("totalManagers", userService.countByRole(User.Role.MANAGER));
            data.put("pendingLeaves", leaveService.countPendingLeaves());
            data.put("departments", userService.findAll().stream()
                    .map(User::getDepartment).filter(Objects::nonNull).distinct().count());
        } else if ("MANAGER".equals(role)) {
            List<User> team = userService.findByManagerId(userId);
            data.put("teamSize", team.size());
            long pendingTeamLeaves = leaveService.getTeamLeaves(userId).stream()
                    .filter(l -> "PENDING".equals(l.getStatus().name())).count();
            data.put("pendingTeamLeaves", pendingTeamLeaves);
        }

        return ResponseEntity.ok(data);
    }
}
