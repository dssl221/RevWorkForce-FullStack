package com.revworkforce.revworkforce_web.model;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {
    private Long id;
    private Long employeeId;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    @Builder.Default
    private LeaveStatus status = LeaveStatus.PENDING;
    private String managerComment;
    private LocalDate appliedDate;

    // Transient field for display
    private String employeeName;

    public enum LeaveStatus {
        PENDING, APPROVED, REJECTED, CANCELLED
    }
}
