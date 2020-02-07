package com.sirma.employees.employee.controller;

import com.sirma.employees.employee.model.EmployeeResponse;
import com.sirma.employees.employee.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeResponse> getLongestWorkingEmployees(@RequestParam("file") MultipartFile file)  {
        EmployeeResponse employeeResponse = employeeService.getLongestWorkingEmployees(file);
        return ok(employeeResponse);
    }
}
