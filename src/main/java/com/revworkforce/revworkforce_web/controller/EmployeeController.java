package com.revworkforce.revworkforce_web.controller;

import com.revworkforce.revworkforce_web.model.User;
import com.revworkforce.revworkforce_web.service.UserService;
import com.revworkforce.revworkforce_web.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final UserService userService;
    private final NotificationService notificationService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        return userService.findById(userId)
                .map(user -> {
                    Map<String, Object> response = userToMap(user);
                    // Get manager details
                    if (user.getManagerId() != null) {
                        userService.findById(user.getManagerId()).ifPresent(manager -> {
                            response.put("managerName", manager.getName());
                            response.put("managerEmail", manager.getEmail());
                        });
                    }
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(@RequestBody User updated, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        try {
            userService.updateProfile(userId, updated);
            return ResponseEntity.ok(Map.of("success", true, "message", "Profile updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployee(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> ResponseEntity.ok(userToMap(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/directory")
    public ResponseEntity<?> getDirectory(@RequestParam(required = false) String search, HttpSession session) {
        try {
            List<User> users;
            if (search != null && !search.trim().isEmpty()) {
                users = userService.searchUsers(search);
            } else {
                users = userService.findAll();
            }
            // Admin sees all users (including inactive), others see only active
            String role = (String) session.getAttribute("userRole");
            if (!"ADMIN".equals(role)) {
                users = users.stream().filter(User::isActive).collect(Collectors.toList());
            }
            List<Map<String, Object>> result = users.stream()
                    .map(this::userToMap)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error",
                    e.getMessage() != null ? e.getMessage() : "Unknown error", "type", e.getClass().getName()));
        }
    }

    @GetMapping("/team")
    public ResponseEntity<?> getTeam(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        List<User> team = userService.findByManagerId(userId);
        List<Map<String, Object>> result = team.stream()
                .map(this::userToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/managers")
    public ResponseEntity<?> getManagers() {
        List<User> managers = userService.getManagers();
        List<Map<String, Object>> result = managers.stream()
                .map(u -> Map.<String, Object>of("id", u.getId(), "name", u.getName(), "email", u.getEmail() != null ? u.getEmail() : ""))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        return ResponseEntity.ok(notificationService.getNotifications(userId));
    }

    @GetMapping("/notifications/count")
    public ResponseEntity<?> getNotificationCount(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        return ResponseEntity.ok(Map.of("count", notificationService.countUnread(userId)));
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<?> markNotificationRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/notifications/read-all")
    public ResponseEntity<?> markAllRead(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        notificationService.markAllRead(userId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    private Map<String, Object> userToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        map.put("employeeId", user.getEmployeeId() != null ? user.getEmployeeId() : "");
        map.put("role", user.getRole().name());
        map.put("department", user.getDepartment() != null ? user.getDepartment() : "");
        map.put("designation", user.getDesignation() != null ? user.getDesignation() : "");
        map.put("phone", user.getPhone() != null ? user.getPhone() : "");
        map.put("address", user.getAddress() != null ? user.getAddress() : "");
        map.put("emergencyContact", user.getEmergencyContact() != null ? user.getEmergencyContact() : "");
        map.put("joiningDate", user.getJoiningDate() != null ? user.getJoiningDate().toString() : "");
        map.put("active", user.isActive());
        map.put("salary", user.getSalary() != null ? user.getSalary() : 0);
        map.put("managerId", user.getManagerId());
        return map;
    }
}
