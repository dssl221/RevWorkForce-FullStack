package com.revworkforce.revworkforce_web.controller;

import com.revworkforce.revworkforce_web.model.Announcement;
import com.revworkforce.revworkforce_web.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(announcementService.findAll());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Announcement announcement) {
        try {
            Announcement saved = announcementService.save(announcement);
            return ResponseEntity.ok(Map.of("success", true, "message", "Announcement created", "id", saved.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Announcement announcement) {
        try {
            announcementService.update(id, announcement.getTitle(), announcement.getDescription());
            return ResponseEntity.ok(Map.of("success", true, "message", "Announcement updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            announcementService.delete(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Announcement deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
