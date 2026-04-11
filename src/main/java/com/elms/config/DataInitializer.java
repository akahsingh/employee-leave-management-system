package com.elms.config;

import com.elms.model.*;
import com.elms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        // --- Departments ---
        Department hr = departmentRepository.save(Department.builder().name("Human Resources").description("HR Department").build());
        Department it = departmentRepository.save(Department.builder().name("Information Technology").description("IT Department").build());
        departmentRepository.save(Department.builder().name("Finance").description("Finance Department").build());
        departmentRepository.save(Department.builder().name("Operations").description("Operations Department").build());

        // --- Leave Types ---
        LeaveType casual = leaveTypeRepository.save(LeaveType.builder().name("Casual Leave").description("For personal/casual reasons").maxDaysPerYear(12).build());
        LeaveType sick = leaveTypeRepository.save(LeaveType.builder().name("Sick Leave").description("For medical/health reasons").maxDaysPerYear(10).build());
        LeaveType annual = leaveTypeRepository.save(LeaveType.builder().name("Annual Leave").description("Earned/annual leave").maxDaysPerYear(15).build());
        LeaveType maternity = leaveTypeRepository.save(LeaveType.builder().name("Maternity Leave").description("For maternity purposes").maxDaysPerYear(180).build());
        LeaveType lop = leaveTypeRepository.save(LeaveType.builder().name("Loss of Pay").description("Unpaid leave").maxDaysPerYear(30).build());

        List<LeaveType> leaveTypes = List.of(casual, sick, annual, maternity, lop);

        // --- Admin User ---
        User adminUser = userRepository.save(User.builder()
            .username("admin")
            .password(passwordEncoder.encode("admin@123"))
            .role(Role.ROLE_ADMIN)
            .build());

        Employee admin = employeeRepository.save(Employee.builder()
            .firstName("Super")
            .lastName("Admin")
            .email("admin@elms.com")
            .phone("9999999999")
            .employeeCode("EMP-001")
            .joiningDate(LocalDate.of(2020, 1, 1))
            .designation("System Administrator")
            .department(it)
            .user(adminUser)
            .build());

        // --- Manager Users ---
        User mgr1User = userRepository.save(User.builder()
            .username("manager1")
            .password(passwordEncoder.encode("manager@123"))
            .role(Role.ROLE_MANAGER)
            .build());

        Employee manager1 = employeeRepository.save(Employee.builder()
            .firstName("Rajan")
            .lastName("Kumar")
            .email("rajan.kumar@elms.com")
            .phone("9876543210")
            .employeeCode("EMP-002")
            .joiningDate(LocalDate.of(2021, 3, 15))
            .designation("IT Manager")
            .department(it)
            .user(mgr1User)
            .build());

        User mgr2User = userRepository.save(User.builder()
            .username("manager2")
            .password(passwordEncoder.encode("manager@123"))
            .role(Role.ROLE_MANAGER)
            .build());

        Employee manager2 = employeeRepository.save(Employee.builder()
            .firstName("Priya")
            .lastName("Sharma")
            .email("priya.sharma@elms.com")
            .phone("9876543211")
            .employeeCode("EMP-003")
            .joiningDate(LocalDate.of(2021, 6, 1))
            .designation("HR Manager")
            .department(hr)
            .user(mgr2User)
            .build());

        // --- Employee Users ---
        User emp1User = userRepository.save(User.builder()
            .username("emp1")
            .password(passwordEncoder.encode("emp@123"))
            .role(Role.ROLE_EMPLOYEE)
            .build());

        Employee emp1 = employeeRepository.save(Employee.builder()
            .firstName("Akash")
            .lastName("Singh")
            .email("akash.singh@elms.com")
            .phone("9123456789")
            .employeeCode("EMP-004")
            .joiningDate(LocalDate.of(2022, 1, 10))
            .designation("Software Developer")
            .department(it)
            .manager(manager1)
            .user(emp1User)
            .build());

        User emp2User = userRepository.save(User.builder()
            .username("emp2")
            .password(passwordEncoder.encode("emp@123"))
            .role(Role.ROLE_EMPLOYEE)
            .build());

        Employee emp2 = employeeRepository.save(Employee.builder()
            .firstName("Neha")
            .lastName("Verma")
            .email("neha.verma@elms.com")
            .phone("9123456788")
            .employeeCode("EMP-005")
            .joiningDate(LocalDate.of(2022, 5, 20))
            .designation("HR Executive")
            .department(hr)
            .manager(manager2)
            .user(emp2User)
            .build());

        // --- Initialize Leave Balances ---
        int year = LocalDate.now().getYear();
        for (Employee emp : List.of(admin, manager1, manager2, emp1, emp2)) {
            for (LeaveType lt : leaveTypes) {
                leaveBalanceRepository.save(LeaveBalance.builder()
                    .employee(emp)
                    .leaveType(lt)
                    .year(year)
                    .totalDays(lt.getMaxDaysPerYear())
                    .build());
            }
        }

        System.out.println("==============================================");
        System.out.println("  ELMS App Started! Default Login Credentials");
        System.out.println("  Admin    -> admin / admin@123");
        System.out.println("  Manager1 -> manager1 / manager@123");
        System.out.println("  Manager2 -> manager2 / manager@123");
        System.out.println("  Employee -> emp1 / emp@123");
        System.out.println("==============================================");
    }
}
