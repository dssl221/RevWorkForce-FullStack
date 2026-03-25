package com.revworkforce.revworkforce_web.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LeaveBalanceModelTest {

    @Test
    void getRemainingDays_shouldReturnCorrectDifference() {
        LeaveBalance balance = LeaveBalance.builder()
                .totalDays(15)
                .usedDays(5)
                .build();

        int remaining = balance.getRemainingDays();

        assertEquals(10, remaining);
    }

    @Test
    void getRemainingDays_shouldHandleZeroUsed() {
        LeaveBalance balance = LeaveBalance.builder()
                .totalDays(15)
                .usedDays(0)
                .build();

        int remaining = balance.getRemainingDays();

        assertEquals(15, remaining);
    }

    @Test
    void getRemainingDays_shouldHandleNegativeRemainingIfUsedExceedsTotal() {
        LeaveBalance balance = LeaveBalance.builder()
                .totalDays(10)
                .usedDays(12)
                .build();

        int remaining = balance.getRemainingDays();

        assertEquals(-2, remaining);
    }
}
