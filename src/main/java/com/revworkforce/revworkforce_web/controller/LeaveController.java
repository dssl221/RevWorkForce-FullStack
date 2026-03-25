package com.revworkforce.revworkforce_web.controller;

import com.revworkforce.revworkforce_web.model.LeaveBalance;
import com.revworkforce.revworkforce_web.model.LeaveRequest;
import com.revworkforce.revworkforce_web.service.LeaveService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    public ResponseEntity<?> applyLeave(@RequestBody LeaveRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        try {
            request.setEmployeeId(userId);
            LeaveRequest saved = leaveService.applyLeave(request);
            return ResponseEntity
                    .ok(Map.of("success", true, "message", "Leave applied successfully", "id", saved.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyLeaves(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        return ResponseEntity.ok(leaveService.getMyLeaves(userId));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelLeave(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        try {
            leaveService.cancelLeave(id, userId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Leave cancelled"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/team")
    public ResponseEntity<?> getTeamLeaves(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        return ResponseEntity.ok(leaveService.getTeamLeaves(userId));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveLeave(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            leaveService.approveLeave(id, body.get("comment"));
            return ResponseEntity.ok(Map.of("success", true, "message", "Leave approved"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectLeave(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            leaveService.rejectLeave(id, body.get("comment"));
            return ResponseEntity.ok(Map.of("success", true, "message", "Leave rejected"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getLeaveBalance(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        return ResponseEntity.ok(leaveService.getLeaveBalances(userId));
    }

    @GetMapping("/balance/{employeeId}")
    public ResponseEntity<?> getEmployeeLeaveBalance(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveService.getLeaveBalances(employeeId));
    }

    @GetMapping("/types")
    public ResponseEntity<?> getLeaveTypes() {
        return ResponseEntity.ok(leaveService.getAllLeaveTypes());
    }
}
