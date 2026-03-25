package com.revworkforce.revworkforce_web.service;

import com.revworkforce.revworkforce_web.dao.AnnouncementDao;
import com.revworkforce.revworkforce_web.model.Announcement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementDao announcementDao;

    public List<Announcement> findAll() {
        List<Announcement> raw = announcementDao.findAll();
        Map<String, Announcement> deduped = new LinkedHashMap<>();
        for (Announcement a : raw) {
            String key = (a.getTitle() != null ? a.getTitle().trim() : "") + "|" +
                        (a.getDescription() != null ? a.getDescription().trim() : "");
            if (!deduped.containsKey(key)) {
                deduped.put(key, a);
            }
        }
        return new ArrayList<>(deduped.values());
    }


    public Announcement save(Announcement announcement) {
        return announcementDao.save(announcement);
    }

    public void update(Long id, String title, String description) {
        announcementDao.update(id, title, description);
    }

    public void delete(Long id) {
        announcementDao.delete(id);
    }
}
