package com.revworkforce.revworkforce_web.model;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holiday {
    private Long id;
    private String name;
    private LocalDate holidayDate;
}
