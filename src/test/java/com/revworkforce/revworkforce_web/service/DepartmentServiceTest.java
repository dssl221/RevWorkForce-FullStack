package com.revworkforce.revworkforce_web.service;

import com.revworkforce.revworkforce_web.dao.DepartmentDao;
import com.revworkforce.revworkforce_web.model.Department;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentDao departmentDao;

    @InjectMocks
    private DepartmentService departmentService;

    private Department sampleDepartment;

    @BeforeEach
    void setUp() {
        sampleDepartment = Department.builder()
                .id(1L)
                .name("Engineering")
                .build();
    }

    @Test
    void findAll_shouldReturnDepartments() {
        when(departmentDao.findAll()).thenReturn(List.of(sampleDepartment));

        List<Department> result = departmentService.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void save_shouldSaveDepartment() {
        when(departmentDao.save(sampleDepartment)).thenReturn(sampleDepartment);

        Department result = departmentService.save(sampleDepartment);

        assertEquals("Engineering", result.getName());
        verify(departmentDao).save(sampleDepartment);
    }

    @Test
    void update_shouldCallDao() {
        departmentService.update(1L, "HR");

        verify(departmentDao).update(1L, "HR");
    }

    @Test
    void delete_shouldCallDao() {
        departmentService.delete(1L);

        verify(departmentDao).delete(1L);
    }
}
