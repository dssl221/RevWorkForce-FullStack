package com.revworkforce.revworkforce_web.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance {
    private Long id;
    private Long employeeId;
    private String leaveType;
    private int totalDays;
    @Builder.Default
    private int usedDays = 0;

    public int getRemainingDays() {
        return totalDays - usedDays;
    }
}
