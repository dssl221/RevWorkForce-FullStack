package com.revworkforce.revworkforce_web.controller;

import com.revworkforce.revworkforce_web.service.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    @GetMapping
    public ResponseEntity<?> getAllHolidays() {
        return ResponseEntity.ok(holidayService.findAll());
    }
}
