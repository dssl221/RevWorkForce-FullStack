package com.revworkforce.revworkforce_web.model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    private Long id;
    private Long userId;
    private String message;
    @Builder.Default
    private boolean read = false;
    private LocalDateTime createdAt;
}
