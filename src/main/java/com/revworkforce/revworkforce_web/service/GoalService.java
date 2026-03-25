package com.revworkforce.revworkforce_web.service;

import com.revworkforce.revworkforce_web.dao.GoalDao;
import com.revworkforce.revworkforce_web.dao.UserDao;
import com.revworkforce.revworkforce_web.model.Goal;
import com.revworkforce.revworkforce_web.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalDao goalDao;
    private final UserDao userDao;
    private final NotificationService notificationService;

    public Goal createGoal(Goal goal) {
        return goalDao.save(goal);
    }

    public List<Goal> getMyGoals(Long employeeId) {
        return goalDao.findByEmployeeId(employeeId);
    }

    public List<Goal> getTeamGoals(Long managerId) {
        List<User> reportees = userDao.findByManagerId(managerId);
        List<Long> reporteeIds = reportees.stream().map(User::getId).collect(Collectors.toList());
        if (reporteeIds.isEmpty())
            return Collections.emptyList();
        List<Goal> goals = goalDao.findByEmployeeIdIn(reporteeIds);
        // Set employee names
        Map<Long, String> nameMap = reportees.stream()
                .collect(Collectors.toMap(User::getId, User::getName));
        goals.forEach(g -> g.setEmployeeName(nameMap.get(g.getEmployeeId())));
        return goals;
    }

    public Goal updateProgress(Long goalId, int progress) {
        Goal goal = goalDao.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        goalDao.updateProgress(goalId, progress);
        goal.setProgress(progress);
        return goal;
    }

    public Goal addComment(Long goalId, String comment) {
        Goal goal = goalDao.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        goalDao.addComment(goalId, comment);
        goal.setManagerComment(comment);

        // Notify employee
        notificationService.createNotification(goal.getEmployeeId(),
                "Your manager commented on your goal: " + comment);

        return goal;
    }

    public void deleteGoal(Long goalId) {
        goalDao.delete(goalId);
    }
}
