package com.revworkforce.revworkforce_web.controller;

import com.revworkforce.revworkforce_web.model.User;
import com.revworkforce.revworkforce_web.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User registered = userService.register(user);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Registration successful");
            response.put("userId", registered.getId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials,
            HttpSession session, HttpServletRequest request) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            return userService.authenticate(email, password)
                    .map(user -> {
                        // Store user info in session
                        session.setAttribute("userId", user.getId());
                        session.setAttribute("userRole", user.getRole().name());
                        session.setAttribute("userName", user.getName());
                        session.setAttribute("userEmail", user.getEmail());

                        // Set Spring Security authentication context
                        List<SimpleGrantedAuthority> authorities = List.of(
                                new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                user.getEmail(), null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        // Persist SecurityContext in session so it survives across requests
                        session.setAttribute(
                                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                                SecurityContextHolder.getContext());

                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("message", "Login successful");
                        response.put("user", Map.of(
                                "id", user.getId(),
                                "name", user.getName(),
                                "email", user.getEmail(),
                                "role", user.getRole().name(),
                                "department", user.getDepartment() != null ? user.getDepartment() : "",
                                "designation", user.getDesignation() != null ? user.getDesignation() : "",
                                "employeeId", user.getEmployeeId() != null ? user.getEmployeeId() : ""));
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.badRequest()
                            .body(Map.of("success", false, "message", "Invalid email or password")));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("success", true, "message", "Logged out successfully"));
    }

    @GetMapping("/session")
    public ResponseEntity<?> getSession(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("authenticated", false));
        }
        return userService.findById(userId)
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("authenticated", true);
                    response.put("user", Map.of(
                            "id", user.getId(),
                            "name", user.getName(),
                            "email", user.getEmail(),
                            "role", user.getRole().name(),
                            "department", user.getDepartment() != null ? user.getDepartment() : "",
                            "designation", user.getDesignation() != null ? user.getDesignation() : "",
                            "employeeId", user.getEmployeeId() != null ? user.getEmployeeId() : ""));
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(401).body(Map.of("authenticated", false)));
    }
}
