package com.revworkforce.revworkforce_web.service;

import com.revworkforce.revworkforce_web.dao.HolidayDao;
import com.revworkforce.revworkforce_web.model.Holiday;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HolidayServiceTest {

    @Mock
    private HolidayDao holidayDao;

    @InjectMocks
    private HolidayService holidayService;

    private Holiday sampleHoliday;

    @BeforeEach
    void setUp() {
        sampleHoliday = Holiday.builder()
                .id(1L)
                .name("New Year")
                .holidayDate(LocalDate.of(2026, 1, 1))
                .build();
    }

    @Test
    void findAll_shouldReturnHolidays() {
        when(holidayDao.findAll()).thenReturn(List.of(sampleHoliday));

        List<Holiday> result = holidayService.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void save_shouldSaveHoliday() {
        when(holidayDao.save(sampleHoliday)).thenReturn(sampleHoliday);

        Holiday result = holidayService.save(sampleHoliday);

        assertEquals("New Year", result.getName());
        verify(holidayDao).save(sampleHoliday);
    }

    @Test
    void delete_shouldCallDao() {
        holidayService.delete(1L);

        verify(holidayDao).delete(1L);
    }
}
