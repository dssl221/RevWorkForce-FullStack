package com.revworkforce.revworkforce_web.controller;

import com.revworkforce.revworkforce_web.model.*;
import com.revworkforce.revworkforce_web.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final LeaveService leaveService;
    private final DepartmentService departmentService;
    private final HolidayService holidayService;

    // --- Employee Management ---
    @PostMapping("/employees")
    public ResponseEntity<?> addEmployee(@RequestBody User user) {
        try {
            User saved = userService.register(user);
            return ResponseEntity.ok(Map.of("success", true, "message", "Employee added", "id", saved.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @RequestBody User user) {
        try {
            userService.updateEmployee(id, user);
            return ResponseEntity.ok(Map.of("success", true, "message", "Employee updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/employees/{id}/deactivate")
    public ResponseEntity<?> deactivateEmployee(@PathVariable Long id) {
        try {
            userService.deactivate(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Employee deactivated"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/employees/{id}/reactivate")
    public ResponseEntity<?> reactivateEmployee(@PathVariable Long id) {
        try {
            userService.reactivate(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Employee reactivated"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/employees/{employeeId}/manager/{managerId}")
    public ResponseEntity<?> assignManager(@PathVariable Long employeeId, @PathVariable Long managerId) {
        try {
            userService.assignManager(employeeId, managerId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Manager assigned"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/employees")
    public ResponseEntity<?> getAllEmployees(@RequestParam(required = false) String search) {
        List<User> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userService.searchUsers(search);
        } else {
            users = userService.findAll();
        }
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("name", u.getName());
            map.put("email", u.getEmail());
            map.put("employeeId", u.getEmployeeId() != null ? u.getEmployeeId() : "");
            map.put("role", u.getRole().name());
            map.put("department", u.getDepartment() != null ? u.getDepartment() : "");
            map.put("designation", u.getDesignation() != null ? u.getDesignation() : "");
            map.put("phone", u.getPhone() != null ? u.getPhone() : "");
            map.put("active", u.isActive());
            map.put("managerId", u.getManagerId());
            map.put("salary", u.getSalary() != null ? u.getSalary() : 0);
            map.put("joiningDate", u.getJoiningDate() != null ? u.getJoiningDate().toString() : "");
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // --- Leave Management ---
    @GetMapping("/leaves")
    public ResponseEntity<?> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    @PostMapping("/leave-types")
    public ResponseEntity<?> addLeaveType(@RequestBody LeaveType leaveType) {
        try {
            LeaveType saved = leaveService.addLeaveType(leaveType);
            return ResponseEntity.ok(Map.of("success", true, "message", "Leave type added", "id", saved.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/leave-types/{id}")
    public ResponseEntity<?> updateLeaveType(@PathVariable Long id, @RequestBody LeaveType leaveType) {
        try {
            leaveService.updateLeaveType(id, leaveType);
            return ResponseEntity.ok(Map.of("success", true, "message", "Leave type updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/leave-types/{id}")
    public ResponseEntity<?> deleteLeaveType(@PathVariable Long id) {
        try {
            leaveService.deleteLeaveType(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Leave type deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/leave-balance")
    public ResponseEntity<?> adjustLeaveBalance(@RequestBody Map<String, Object> body) {
        try {
            Long empId = Long.parseLong(body.get("employeeId").toString());
            String leaveType = (String) body.get("leaveType");
            int totalDays = Integer.parseInt(body.get("totalDays").toString());
            leaveService.adjustLeaveBalance(empId, leaveType, totalDays);
            return ResponseEntity.ok(Map.of("success", true, "message", "Leave balance adjusted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // --- Departments ---
    @GetMapping("/departments")
    public ResponseEntity<?> getDepartments() {
        return ResponseEntity.ok(departmentService.findAll());
    }

    @PostMapping("/departments")
    public ResponseEntity<?> addDepartment(@RequestBody Department department) {
        try {
            Department saved = departmentService.save(department);
            return ResponseEntity.ok(Map.of("success", true, "message", "Department added", "id", saved.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<?> updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        try {
            departmentService.update(id, department.getName());
            return ResponseEntity.ok(Map.of("success", true, "message", "Department updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        try {
            departmentService.delete(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Department deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // --- Holidays ---
    @GetMapping("/holidays")
    public ResponseEntity<?> getHolidays() {
        return ResponseEntity.ok(holidayService.findAll());
    }

    @PostMapping("/holidays")
    public ResponseEntity<?> addHoliday(@RequestBody Holiday holiday) {
        try {
            Holiday saved = holidayService.save(holiday);
            return ResponseEntity.ok(Map.of("success", true, "message", "Holiday added", "id", saved.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/holidays/{id}")
    public ResponseEntity<?> deleteHoliday(@PathVariable Long id) {
        try {
            holidayService.delete(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Holiday deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
