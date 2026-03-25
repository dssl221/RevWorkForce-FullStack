package com.revworkforce.revworkforce_web.service;

import com.revworkforce.revworkforce_web.dao.HolidayDao;
import com.revworkforce.revworkforce_web.model.Holiday;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayDao holidayDao;

    public List<Holiday> findAll() {
        List<Holiday> raw = holidayDao.findAll();
        Map<String, Holiday> deduped = new LinkedHashMap<>();
        for (Holiday h : raw) {
            String key = (h.getName() != null ? h.getName().trim() : "") + "|" +
                        (h.getHolidayDate() != null ? h.getHolidayDate().toString() : "");
            if (!deduped.containsKey(key)) {
                deduped.put(key, h);
            }
        }
        return new ArrayList<>(deduped.values());
    }


    public Holiday save(Holiday holiday) {
        return holidayDao.save(holiday);
    }

    public void delete(Long id) {
        holidayDao.delete(id);
    }
}
