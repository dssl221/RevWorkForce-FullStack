package com.revworkforce.revworkforce_web.model;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String employeeId;
    private Role role;
    private String department;
    private String designation;
    private Long managerId;
    private String phone;
    private String address;
    private String emergencyContact;
    private LocalDate joiningDate;
    @Builder.Default
    private boolean active = true;
    private Double salary;

    public enum Role {
        ADMIN, MANAGER, EMPLOYEE
    }
}
