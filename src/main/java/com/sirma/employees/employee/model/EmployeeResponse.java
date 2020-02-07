package com.sirma.employees.employee.model;

public class EmployeeResponse {
    private long firstEmployeeId;
    private long secondEmployeeId;
    private long projectId;
    private int daysWorked = 0; // There is no employee with less days worked

    public EmployeeResponse(long firstEmployeeId, long secondEmployeeId, long projectId, int daysWorked) {
        this.firstEmployeeId = firstEmployeeId;
        this.secondEmployeeId = secondEmployeeId;
        this.projectId = projectId;
        this.daysWorked = daysWorked;
    }

    public EmployeeResponse() {}

    public int getDaysWorked() {
        return daysWorked;
    }

    public long getFirstEmployeeId() {
        return firstEmployeeId;
    }

    public long getSecondEmployeeId() {
        return secondEmployeeId;
    }

    public void setFirstEmployeeId(long firstEmployeeId) {
        this.firstEmployeeId = firstEmployeeId;
    }

    public void setSecondEmployeeId(long secondEmployeeId) {
        this.secondEmployeeId = secondEmployeeId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public void setDaysWorked(int daysWorked) {
        this.daysWorked = daysWorked;
    }

    public long getProjectId() {
        return projectId;
    }
}
