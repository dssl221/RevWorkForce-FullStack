package com.revworkforce.revworkforce_web.service;

import com.revworkforce.revworkforce_web.dao.PerformanceReviewDao;
import com.revworkforce.revworkforce_web.dao.UserDao;
import com.revworkforce.revworkforce_web.model.PerformanceReview;
import com.revworkforce.revworkforce_web.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PerformanceReviewService {

    private final PerformanceReviewDao reviewDao;
    private final UserDao userDao;
    private final NotificationService notificationService;

    public PerformanceReview createReview(PerformanceReview review) {
        return reviewDao.save(review);
    }

    public List<PerformanceReview> getMyReviews(Long employeeId) {
        return reviewDao.findByEmployeeId(employeeId);
    }

    public List<PerformanceReview> getTeamReviews(Long managerId) {
        List<User> reportees = userDao.findByManagerId(managerId);
        List<Long> reporteeIds = reportees.stream().map(User::getId).collect(Collectors.toList());
        if (reporteeIds.isEmpty())
            return Collections.emptyList();
        List<PerformanceReview> reviews = reviewDao.findByEmployeeIdIn(reporteeIds);
        // Set employee names
        Map<Long, String> nameMap = reportees.stream()
                .collect(Collectors.toMap(User::getId, User::getName));
        reviews.forEach(r -> r.setEmployeeName(nameMap.get(r.getEmployeeId())));
        return reviews;
    }

    public PerformanceReview submitReview(Long reviewId) {
        PerformanceReview review = reviewDao.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        if (!"DRAFT".equals(review.getStatus())) {
            throw new RuntimeException("Only draft reviews can be submitted");
        }
        reviewDao.submitReview(reviewId);
        review.setStatus("SUBMITTED");

        // Notify manager
        User employee = userDao.findById(review.getEmployeeId()).orElse(null);
        if (employee != null && employee.getManagerId() != null) {
            notificationService.createNotification(employee.getManagerId(),
                    employee.getName() + " has submitted a performance review for your feedback.");
        }

        return review;
    }

    public PerformanceReview provideFeedback(Long reviewId, int managerRating, String feedback) {
        PerformanceReview review = reviewDao.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        if (!"SUBMITTED".equals(review.getStatus())) {
            throw new RuntimeException("Review must be submitted before providing feedback");
        }
        reviewDao.provideFeedback(reviewId, managerRating, feedback);
        review.setManagerRating(managerRating);
        review.setManagerFeedback(feedback);
        review.setStatus("REVIEWED");

        // Notify employee
        notificationService.createNotification(review.getEmployeeId(),
                "Your manager has provided feedback on your performance review.");

        return review;
    }
}
