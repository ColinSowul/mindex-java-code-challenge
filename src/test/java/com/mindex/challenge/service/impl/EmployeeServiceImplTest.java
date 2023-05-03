package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    private String employeeUrl;
    private String employeeIdUrl;
    private String reportingStructureUrl;
    private String compensationCreateUrl;
    private String compensationReadUrl;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
        reportingStructureUrl = "http://localhost:" + port + "/reportingStructure/{id}";
        compensationCreateUrl = "http://localhost:" + port + "/compensation";
        compensationReadUrl = "http://localhost:" + port + "/compensation/{id}";
    }

    @Test
    public void testCreateReadUpdate() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        // Create checks
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);


        // Read checks
        Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);


        // Update checks
        readEmployee.setPosition("Development Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(readEmployee, headers),
                        Employee.class,
                        readEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(readEmployee, updatedEmployee);
    }

    /* 
     * Reporting structure for test looks as follows:
     *       1
     *      / \
     *     2   3
     *      \ / \
     *       4   5
     */
    @Test
    public void testReportingStructureComputation() {
        Employee testEmployee1 = createBlankEmployee();
        Employee testEmployee2 = createBlankEmployee();
        Employee testEmployee3 = createBlankEmployee();
        Employee testEmployee4 = createBlankEmployee();
        Employee testEmployee5 = createBlankEmployee();
        testEmployee1.setDirectReports(Arrays.asList(testEmployee2, testEmployee3));
        testEmployee2.setDirectReports(Arrays.asList(testEmployee4));
        testEmployee3.setDirectReports(Arrays.asList(testEmployee4, testEmployee5));
        saveEmployeeList(testEmployee1, testEmployee2, testEmployee3, testEmployee4, testEmployee5);

        ReportingStructure output = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, testEmployee1.getEmployeeId()).getBody();
        assertEquals(testEmployee1.getEmployeeId(), output.getEmployee().getEmployeeId());
        assertEquals(4, output.getNumberOfReports());
    }

    @Test
    public void testCompensationCreateAndRead() {
        Compensation testCompensation = new Compensation();
        Employee testEmployee = createBlankEmployee();
        testCompensation.setEmployee(testEmployee);
        testCompensation.setSalary(new BigDecimal(60000.00));
        testCompensation.setEffectiveDate(LocalDate.parse("2020-12-20"));

        restTemplate.postForEntity(compensationCreateUrl, testCompensation, Compensation.class);

        Compensation readCompensation = restTemplate.getForEntity(compensationReadUrl, Compensation.class, testEmployee.getEmployeeId()).getBody();

        assertEquals(testCompensation.getEmployee().getEmployeeId(), readCompensation.getEmployee().getEmployeeId());
        assertEquals(testCompensation.getSalary(), readCompensation.getSalary());
        assertEquals(testCompensation.getEffectiveDate(), readCompensation.getEffectiveDate());
    }

    private Employee createBlankEmployee() {
        Employee newEmployee = new Employee();
        newEmployee.setEmployeeId(UUID.randomUUID().toString());
        return newEmployee;
    }

    private void saveEmployeeList(Employee... employees) {
        for(Employee employee : employees) {
            employeeRepository.insert(employee);
        }
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }
}
