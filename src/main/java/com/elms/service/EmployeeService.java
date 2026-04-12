package com.elms.service;

import com.elms.model.*;
import com.elms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Employee> getAllActiveEmployees() {
        return employeeRepository.findByActiveTrue();
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    public Optional<Employee> findByUser(User user) {
        return employeeRepository.findByUser(user);
    }

    public List<Employee> getAllManagers() {
        return employeeRepository.findAllManagers();
    }

    public List<Employee> getTeamByManager(Long managerId) {
        return employeeRepository.findByManagerId(managerId);
    }

    @Transactional
    public Employee createEmployee(String firstName, String lastName, String email,
                                   String phone, String designation, String employeeCode,
                                   LocalDate joiningDate, Long departmentId, Long managerId,
                                   String username, String password, Role role) {

        User user = User.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .role(role)
            .build();
        userRepository.save(user);

        Department department = departmentId != null
            ? departmentRepository.findById(departmentId).orElse(null) : null;

        Employee manager = managerId != null
            ? employeeRepository.findById(managerId).orElse(null) : null;

        Employee employee = Employee.builder()
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .phone(phone)
            .designation(designation)
            .employeeCode(employeeCode)
            .joiningDate(joiningDate)
            .department(department)
            .manager(manager)
            .user(user)
            .build();

        Employee saved = employeeRepository.save(employee);
        initializeLeaveBalances(saved);
        return saved;
    }

    @Transactional
    public Employee updateEmployee(Long id, String firstName, String lastName, String email,
                                    String phone, String designation, Long departmentId, Long managerId) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(email);
        employee.setPhone(phone);
        employee.setDesignation(designation);

        if (departmentId != null) {
            departmentRepository.findById(departmentId).ifPresent(employee::setDepartment);
        }
        if (managerId != null) {
            employeeRepository.findById(managerId).ifPresent(employee::setManager);
        }

        return employeeRepository.save(employee);
    }

    @Transactional
    public void deactivateEmployee(Long id) {
        employeeRepository.findById(id).ifPresent(emp -> {
            emp.setActive(false);
            emp.getUser().setActive(false);
            employeeRepository.save(emp);
        });
    }

    private void initializeLeaveBalances(Employee employee) {
        int currentYear = LocalDate.now().getYear();
        List<LeaveType> leaveTypes = leaveTypeRepository.findByActiveTrue();
        for (LeaveType lt : leaveTypes) {
            LeaveBalance balance = LeaveBalance.builder()
                .employee(employee)
                .leaveType(lt)
                .year(currentYear)
                .totalDays(lt.getMaxDaysPerYear())
                .build();
            leaveBalanceRepository.save(balance);
        }
    }

    public boolean existsByEmail(String email) {
        return employeeRepository.existsByEmail(email);
    }

    public boolean existsByEmployeeCode(String code) {
        return employeeRepository.existsByEmployeeCode(code);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }
}
