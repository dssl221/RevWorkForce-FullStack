package com.revworkforce.revworkforce_web.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceReview {
    private Long id;
    private Long employeeId;
    private String deliverables;
    private String accomplishments;
    private String improvements;
    private Integer selfRating;
    private Integer managerRating;
    private String managerFeedback;
    @Builder.Default
    private String status = "DRAFT";
    private Integer reviewYear;

    // Transient field for display
    private String employeeName;
}
