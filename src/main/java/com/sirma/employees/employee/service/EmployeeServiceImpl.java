package com.sirma.employees.employee.service;

import com.sirma.employees.date.DateUtils;
import com.sirma.employees.employee.model.Employee;
import com.sirma.employees.employee.model.EmployeeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private static final String FILE_NOT_EXISTS = "File does not exists";
    private static final String WRONG_DATA = "Wrong data in the file";
    private static final String INVALID_DATE = "Invalid date";
    private static final String INVALID_ID = "Invalid id";
    private static final String ERROR_WHILE_READING_THE_FILE = "Error while reading the file";
    private static final String NO_EMPLOYEES = "There are no two employees working on the same project";
    private static final String COMMA = ",";
    private static final String EMPTY_FILE = "File is empty";
    private static final int EMPLOYEES_LENGTH_WITHOUT_DATE_TO = 3;
    private static final int EMPLOYEES_LENGTH_WITH_DATE_TO = 4;
    private static final int EMPLOYEE_ID_INDEX = 0;
    private static final int EMPLOYEE_PROJECT_ID_INDEX = 1;
    private static final int EMPLOYEE_DATE_FROM_INDEX = 2;
    private static final int EMPLOYEE_DATE_TO_INDEX = 3;
    private static final int MIN_EMPLOYEES_SIZE = 2;

    @Override
    public EmployeeResponse getLongestWorkingEmployees(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EMPTY_FILE);
        }
        BufferedReader bufferedReader = null;
        try {
            InputStream inputStream = multipartFile.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            return mapEmployees(bufferedReader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILE_NOT_EXISTS);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              ERROR_WHILE_READING_THE_FILE);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private EmployeeResponse mapEmployees(BufferedReader reader) throws IOException {
        Map<Long, List<Employee>> employees = new HashMap<>();

        String line = reader.readLine();
        Employee currentEmployee; // Current created employee
        Employee checkedEmployee; // Same employee object, that has worked for this project in several periods of time

        while (line != null) {
            String[] employeeFields = line.split(COMMA);
            if (employeeFields.length == EMPLOYEES_LENGTH_WITHOUT_DATE_TO ||
                employeeFields.length == EMPLOYEES_LENGTH_WITH_DATE_TO) {
                currentEmployee = createEmployee(employeeFields);

                // If there is no project key like this, then add it
                employees.putIfAbsent(currentEmployee.getProjectId(), new ArrayList<>());
                checkedEmployee = findEmployeeById(currentEmployee.getId(),
                                                   employees.get(currentEmployee.getProjectId()));

                // If checked employee is null, then add the current one
                if (checkedEmployee == null) {
                    employees.get(currentEmployee.getProjectId())
                             .add(currentEmployee);
                } else { // If there is an employee who has worked on a project for several periods of time
                    checkedEmployee.addDays(checkedEmployee.getDays());
                }
            } else {
                // If employeeFields length is different than 3 or 4 -> wrong data format
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, WRONG_DATA);
            }

            line = reader.readLine();
        }

        return computeEmployeeResponse(employees);
    }

    private static Employee createEmployee(String[] employeeFields) {
        Employee employee = new Employee();

        try {
            employee.setId(Long.parseLong(employeeFields[EMPLOYEE_ID_INDEX].trim()));
            employee.setProjectId(Long.parseLong(employeeFields[EMPLOYEE_PROJECT_ID_INDEX].trim()));
            employee.setDays(calculateDaysWorked(employeeFields));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_ID);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_DATE);
        }

        return employee;
    }

    // Calculating days worked by current employee
    private static int calculateDaysWorked(String[] employeeFields) {
        LocalDate fromDate = DateUtils.parseDate(employeeFields[EMPLOYEE_DATE_FROM_INDEX].trim());
        LocalDate dateTo;

        if (hasDateTo(employeeFields.length)) {
            dateTo = DateUtils.parseDate(employeeFields[EMPLOYEE_DATE_TO_INDEX].trim());
        } else {
            dateTo = LocalDate.now();
        }

        // DAYS.between() returns long, but no employee can work more days than int max value
        return (int) DAYS.between(fromDate, dateTo);
    }

    private static boolean hasDateTo(int size) {
        return size == EMPLOYEES_LENGTH_WITH_DATE_TO;
    }

    private static Employee findEmployeeById(long id, List<Employee> employees) {
        if (employees.isEmpty()) {
            return null;
        }

        return employees.stream()
                        .filter(e -> e.getId() == id)
                        .findAny()
                        .orElse(null);
    }

    private static EmployeeResponse computeEmployeeResponse(Map<Long, List<Employee>> employees) {
        EmployeeResponse employeeResponse = new EmployeeResponse();
        boolean employeeExists = false;
        for (List<Employee> employeeList : employees.values()) {
            EmployeeResponse computedEmployee = createMaxWorkedEmployeeResponse(employeeList);

            if (computedEmployee != null &&
                computedEmployee.getDaysWorked() > employeeResponse.getDaysWorked()) {
                employeeResponse = computedEmployee;
                employeeExists = true;
            }
        }
        // If there are no two employees working on the same project
        if (!employeeExists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, NO_EMPLOYEES);
        }
        return employeeResponse;
    }

    private static EmployeeResponse createMaxWorkedEmployeeResponse(List<Employee> employees) {
        if (employees.size() < MIN_EMPLOYEES_SIZE) {
            return null;
        }
        Employee firstEmployee = getEmployeeWithMaxDays(employees);
        Employee secondEmployee = getEmployeeWithMaxDays(employees);

        return new EmployeeResponse(firstEmployee.getId(), secondEmployee.getId(),
                                    firstEmployee.getProjectId(),
                                    firstEmployee.getDays() + secondEmployee.getDays());
    }

    private static Employee getEmployeeWithMaxDays(List<Employee> employees) {
        int maxDays = 0;
        int index = -1;
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i)
                         .getDays() > maxDays) {
                maxDays = employees.get(i)
                                   .getDays();
                index = i;
            }
        }

        // Removing that employee, so we can get the second one with max worked days
        return employees.remove(index);
    }

}
