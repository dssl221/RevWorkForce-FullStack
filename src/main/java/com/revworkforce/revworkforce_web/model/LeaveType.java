package com.revworkforce.revworkforce_web.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveType {
    private Long id;
    private String name;
    private int defaultDays;
}
