package com.revworkforce.revworkforce_web.model;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal {
    private Long id;
    private Long employeeId;
    private String description;
    private LocalDate deadline;
    @Builder.Default
    private String priority = "MEDIUM";
    @Builder.Default
    private int progress = 0;
    private String managerComment;

    // Transient field for display
    private String employeeName;
}
