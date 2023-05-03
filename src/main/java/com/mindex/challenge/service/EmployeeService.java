package com.mindex.challenge.service;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;

public interface EmployeeService {
    Employee create(Employee employee);
    Employee read(String id);
    Employee update(Employee employee);
    ReportingStructure computeReportingStructure(String employeeId);
    Compensation saveCompensation(Compensation compensation);
    Compensation getCompensationForEmployee(String employeeId);
}
