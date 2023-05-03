package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CompensationRepository compensationRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Creating employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    @Override
    public ReportingStructure computeReportingStructure(String employeeId) {
        Employee root = employeeRepository.findByEmployeeId(employeeId);
        return new ReportingStructure(root, getAllReportsForEmployee(root, new HashSet<String>()).size());
    }

    @Override
    public Compensation saveCompensation(Compensation compensation) {
        compensationRepository.insert(compensation);
        return compensation;
    }

    @Override
    public Compensation getCompensationForEmployee(String employeeId) {
        Compensation example = new Compensation();
        Employee exampleEmployee = new Employee();
        exampleEmployee.setEmployeeId(employeeId);
        example.setEmployee(exampleEmployee);
        Example<Compensation> queryByExample = Example.of(example);

        return compensationRepository.findAll(queryByExample).get(0);
    }

    // Using set to handle possibility of single employee reporting directly to two different managers
    // Id must be used because reports with same employee ID would not have unique object identity
    private HashSet<String> getAllReportsForEmployee(Employee employee, HashSet<String> reports) {
        if(employee.getDirectReports() != null && !employee.getDirectReports().isEmpty()) {
            for(Employee directReport : employee.getDirectReports()) {
                reports.add(directReport.getEmployeeId());
                getAllReportsForEmployee(directReport, reports);
            }
        }
        return reports;
    }
}
