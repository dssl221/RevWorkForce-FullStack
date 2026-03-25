package com.revworkforce.revworkforce_web.controller;

import com.revworkforce.revworkforce_web.model.PerformanceReview;
import com.revworkforce.revworkforce_web.service.PerformanceReviewService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/performance-reviews")
@RequiredArgsConstructor
public class PerformanceReviewController {

    private final PerformanceReviewService reviewService;

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody PerformanceReview review, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        try {
            review.setEmployeeId(userId);
            PerformanceReview saved = reviewService.createReview(review);
            return ResponseEntity.ok(Map.of("success", true, "message", "Review created", "id", saved.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyReviews(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        return ResponseEntity.ok(reviewService.getMyReviews(userId));
    }

    @GetMapping("/team")
    public ResponseEntity<?> getTeamReviews(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        return ResponseEntity.ok(reviewService.getTeamReviews(userId));
    }

    @PutMapping("/{id}/submit")
    public ResponseEntity<?> submitReview(@PathVariable Long id) {
        try {
            reviewService.submitReview(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Review submitted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/feedback")
    public ResponseEntity<?> provideFeedback(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            int rating = Integer.parseInt(body.get("managerRating").toString());
            String feedback = (String) body.get("managerFeedback");
            reviewService.provideFeedback(id, rating, feedback);
            return ResponseEntity.ok(Map.of("success", true, "message", "Feedback provided"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
