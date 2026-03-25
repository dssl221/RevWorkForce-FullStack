package com.revworkforce.revworkforce_web.service;

import com.revworkforce.revworkforce_web.dao.*;
import com.revworkforce.revworkforce_web.model.*;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final LeaveBalanceDao leaveBalanceDao;
    private final LeaveTypeDao leaveTypeDao;


    public User register(User user) {
        if (userDao.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        // Store password as plain text
        user.setActive(true);
        if (user.getJoiningDate() == null) {
            user.setJoiningDate(LocalDate.now());
        }
        if (user.getRole() == null) {
            user.setRole(User.Role.EMPLOYEE);
        }
        if (user.getEmployeeId() == null || user.getEmployeeId().isEmpty()) {
            user.setEmployeeId("EMP" + String.format("%05d", System.currentTimeMillis() % 100000));
        }
        User saved = userDao.save(user);

        // Initialize leave balances for the new employee
        List<LeaveType> leaveTypes = leaveTypeDao.findAll();
        for (LeaveType lt : leaveTypes) {
            LeaveBalance lb = LeaveBalance.builder()
                    .employeeId(saved.getId())
                    .leaveType(lt.getName())
                    .totalDays(lt.getDefaultDays())
                    .usedDays(0)
                    .build();
            leaveBalanceDao.save(lb);
        }

        return saved;
    }

    public Optional<User> authenticate(String email, String password) {
        Optional<User> userOpt = userDao.findByEmail(email);
        if (userOpt.isPresent() && password.equals(userOpt.get().getPassword())) {
            if (!userOpt.get().isActive()) {
                throw new RuntimeException("Account is deactivated");
            }
            return userOpt;
        }
        return Optional.empty();
    }

    public Optional<User> findById(Long id) {
        return userDao.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    public List<User> findAll() {
        return userDao.findAll();
    }

    public List<User> findByManagerId(Long managerId) {
        return userDao.findByManagerId(managerId);
    }

    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return userDao.findAll();
        }
        return userDao.searchUsers(query.trim());
    }

    public User updateEmployee(Long id, User updated) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        if (updated.getName() != null)
            user.setName(updated.getName());
        if (updated.getDepartment() != null)
            user.setDepartment(updated.getDepartment());
        if (updated.getDesignation() != null)
            user.setDesignation(updated.getDesignation());
        if (updated.getPhone() != null)
            user.setPhone(updated.getPhone());
        if (updated.getAddress() != null)
            user.setAddress(updated.getAddress());
        if (updated.getEmergencyContact() != null)
            user.setEmergencyContact(updated.getEmergencyContact());
        if (updated.getManagerId() != null)
            user.setManagerId(updated.getManagerId());
        if (updated.getRole() != null)
            user.setRole(updated.getRole());
        if (updated.getSalary() != null)
            user.setSalary(updated.getSalary());
        userDao.update(user);
        return user;
    }

    public User updateProfile(Long id, User updated) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        userDao.updateProfile(id,
                updated.getPhone() != null ? updated.getPhone() : user.getPhone(),
                updated.getAddress() != null ? updated.getAddress() : user.getAddress(),
                updated.getEmergencyContact() != null ? updated.getEmergencyContact() : user.getEmergencyContact());
        return userDao.findById(id).orElse(user);
    }

    public void deactivate(Long id) {
        userDao.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
        userDao.deactivate(id);
    }

    public void reactivate(Long id) {
        userDao.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
        userDao.reactivate(id);
    }

    public void assignManager(Long employeeId, Long managerId) {
        userDao.findById(employeeId).orElseThrow(() -> new RuntimeException("Employee not found"));
        userDao.assignManager(employeeId, managerId);
    }

    public long countActiveEmployees() {
        return userDao.countByActive(true);
    }

    public long countByRole(User.Role role) {
        return userDao.countByRole(role);
    }

    public List<User> getManagers() {
        return userDao.findAll().stream()
                .filter(u -> u.getRole() == User.Role.MANAGER || u.getRole() == User.Role.ADMIN)
                .toList();
    }
}
