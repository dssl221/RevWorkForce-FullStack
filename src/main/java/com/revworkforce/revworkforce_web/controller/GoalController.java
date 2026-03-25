package com.revworkforce.revworkforce_web.controller;

import com.revworkforce.revworkforce_web.model.Goal;
import com.revworkforce.revworkforce_web.service.GoalService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    public ResponseEntity<?> createGoal(@RequestBody Goal goal, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        try {
            goal.setEmployeeId(userId);
            Goal saved = goalService.createGoal(goal);
            return ResponseEntity.ok(Map.of("success", true, "message", "Goal created", "id", saved.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyGoals(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        return ResponseEntity.ok(goalService.getMyGoals(userId));
    }

    @GetMapping("/team")
    public ResponseEntity<?> getTeamGoals(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        return ResponseEntity.ok(goalService.getTeamGoals(userId));
    }

    @PutMapping("/{id}/progress")
    public ResponseEntity<?> updateProgress(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        try {
            goalService.updateProgress(id, body.get("progress"));
            return ResponseEntity.ok(Map.of("success", true, "message", "Progress updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/comment")
    public ResponseEntity<?> addComment(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            goalService.addComment(id, body.get("comment"));
            return ResponseEntity.ok(Map.of("success", true, "message", "Comment added"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@PathVariable Long id) {
        try {
            goalService.deleteGoal(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Goal deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
