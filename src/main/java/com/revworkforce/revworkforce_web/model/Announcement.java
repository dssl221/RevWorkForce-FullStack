package com.revworkforce.revworkforce_web.model;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Announcement {
    private Long id;
    private String title;
    private String description;
    private LocalDate createdDate;
}
