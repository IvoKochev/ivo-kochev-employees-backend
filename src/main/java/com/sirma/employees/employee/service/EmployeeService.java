package com.sirma.employees.employee.service;

import com.sirma.employees.employee.model.EmployeeResponse;
import org.springframework.web.multipart.MultipartFile;

public interface EmployeeService {
    EmployeeResponse getLongestWorkingEmployees(MultipartFile file);
}
