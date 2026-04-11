package com.elms.controller;

import com.elms.model.*;
import com.elms.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private final DepartmentService departmentService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Employee> employees = employeeService.getAllActiveEmployees();
        model.addAttribute("totalEmployees", employees.size());
        model.addAttribute("totalManagers", employeeService.getAllManagers().size());
        model.addAttribute("pendingLeaves", leaveService.countPendingRequests());
        model.addAttribute("recentLeaves", leaveService.getAllLeaveRequests().stream().limit(5).toList());
        model.addAttribute("departments", departmentService.getAllDepartments().size());
        return "admin/dashboard";
    }

    // ---- EMPLOYEES ----
    @GetMapping("/employees")
    public String employees(Model model) {
        model.addAttribute("employees", employeeService.getAllEmployees());
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("managers", employeeService.getAllManagers());
        return "admin/employees";
    }

    @PostMapping("/employees/add")
    public String addEmployee(@RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String email,
                               @RequestParam String phone,
                               @RequestParam String designation,
                               @RequestParam String employeeCode,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate joiningDate,
                               @RequestParam(required = false) Long departmentId,
                               @RequestParam(required = false) Long managerId,
                               @RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String role,
                               RedirectAttributes redirectAttributes) {
        try {
            if (employeeService.existsByEmail(email)) {
                redirectAttributes.addFlashAttribute("error", "Email already exists!");
                return "redirect:/admin/employees";
            }
            if (employeeService.existsByUsername(username)) {
                redirectAttributes.addFlashAttribute("error", "Username already exists!");
                return "redirect:/admin/employees";
            }
            Role empRole = Role.valueOf(role);
            employeeService.createEmployee(firstName, lastName, email, phone, designation,
                employeeCode, joiningDate, departmentId, managerId, username, password, empRole);
            redirectAttributes.addFlashAttribute("success", "Employee added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/employees";
    }

    @PostMapping("/employees/update/{id}")
    public String updateEmployee(@PathVariable Long id,
                                  @RequestParam String firstName,
                                  @RequestParam String lastName,
                                  @RequestParam String email,
                                  @RequestParam String phone,
                                  @RequestParam String designation,
                                  @RequestParam(required = false) Long departmentId,
                                  @RequestParam(required = false) Long managerId,
                                  RedirectAttributes redirectAttributes) {
        try {
            employeeService.updateEmployee(id, firstName, lastName, email, phone, designation, departmentId, managerId);
            redirectAttributes.addFlashAttribute("success", "Employee updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/employees";
    }

    @PostMapping("/employees/deactivate/{id}")
    public String deactivateEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            employeeService.deactivateEmployee(id);
            redirectAttributes.addFlashAttribute("success", "Employee deactivated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/employees";
    }

    // ---- LEAVE REQUESTS ----
    @GetMapping("/leaves")
    public String leaves(Model model) {
        model.addAttribute("leaveRequests", leaveService.getAllLeaveRequests());
        return "admin/leaves";
    }

    // ---- LEAVE TYPES ----
    @GetMapping("/leave-types")
    public String leaveTypes(Model model) {
        model.addAttribute("leaveTypes", leaveService.getAllLeaveTypes());
        return "admin/leave-types";
    }

    @PostMapping("/leave-types/add")
    public String addLeaveType(@RequestParam String name,
                                @RequestParam String description,
                                @RequestParam int maxDaysPerYear,
                                RedirectAttributes redirectAttributes) {
        try {
            LeaveType lt = LeaveType.builder()
                .name(name)
                .description(description)
                .maxDaysPerYear(maxDaysPerYear)
                .build();
            leaveService.saveLeaveType(lt);
            redirectAttributes.addFlashAttribute("success", "Leave type added!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/leave-types";
    }

    // ---- DEPARTMENTS ----
    @GetMapping("/departments")
    public String departments(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "admin/departments";
    }

    @PostMapping("/departments/add")
    public String addDepartment(@RequestParam String name,
                                 @RequestParam String description,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (departmentService.existsByName(name)) {
                redirectAttributes.addFlashAttribute("error", "Department already exists!");
                return "redirect:/admin/departments";
            }
            Department dept = Department.builder().name(name).description(description).build();
            departmentService.save(dept);
            redirectAttributes.addFlashAttribute("success", "Department added!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/departments";
    }
}
