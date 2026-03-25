package com.revworkforce.revworkforce_web.service;

import com.revworkforce.revworkforce_web.dao.DepartmentDao;
import com.revworkforce.revworkforce_web.model.Department;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentDao departmentDao;

    public List<Department> findAll() {
        return departmentDao.findAll();
    }

    public Department save(Department department) {
        return departmentDao.save(department);
    }

    public void update(Long id, String name) {
        departmentDao.update(id, name);
    }

    public void delete(Long id) {
        departmentDao.delete(id);
    }
}
