package com.elms.controller;

import com.elms.model.*;
import com.elms.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private final com.elms.repository.UserRepository userRepository;

    private Employee getCurrentEmployee(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
            .flatMap(employeeService::findByUser)
            .orElseThrow(() -> new RuntimeException("Employee profile not found"));
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Employee employee = getCurrentEmployee(userDetails);
        model.addAttribute("employee", employee);
        model.addAttribute("leaveBalances", leaveService.getLeaveBalances(employee));
        model.addAttribute("recentLeaves", leaveService.getLeaveRequestsByEmployee(employee).stream().limit(5).toList());
        model.addAttribute("pendingCount", leaveService.getLeaveRequestsByEmployee(employee)
            .stream().filter(l -> l.getStatus() == LeaveStatus.PENDING).count());
        return "employee/dashboard";
    }

    @GetMapping("/leaves")
    public String myLeaves(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Employee employee = getCurrentEmployee(userDetails);
        model.addAttribute("employee", employee);
        model.addAttribute("leaveRequests", leaveService.getLeaveRequestsByEmployee(employee));
        model.addAttribute("leaveTypes", leaveService.getAllActiveLeaveTypes());
        model.addAttribute("leaveBalances", leaveService.getLeaveBalances(employee));
        model.addAttribute("today", LocalDate.now());
        return "employee/leaves";
    }

    @PostMapping("/leaves/apply")
    public String applyLeave(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam Long leaveTypeId,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              @RequestParam String reason,
                              RedirectAttributes redirectAttributes) {
        try {
            if (endDate.isBefore(startDate)) {
                redirectAttributes.addFlashAttribute("error", "End date cannot be before start date.");
                return "redirect:/employee/leaves";
            }
            if (startDate.isBefore(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("error", "Cannot apply leave for past dates.");
                return "redirect:/employee/leaves";
            }
            Employee employee = getCurrentEmployee(userDetails);
            leaveService.applyLeave(employee, leaveTypeId, startDate, endDate, reason);
            redirectAttributes.addFlashAttribute("success", "Leave applied successfully! Waiting for manager approval.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/employee/leaves";
    }

    @PostMapping("/leaves/cancel/{id}")
    public String cancelLeave(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            Employee employee = getCurrentEmployee(userDetails);
            leaveService.cancelLeave(id, employee);
            redirectAttributes.addFlashAttribute("success", "Leave request cancelled.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/employee/leaves";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Employee employee = getCurrentEmployee(userDetails);
        model.addAttribute("employee", employee);
        model.addAttribute("leaveBalances", leaveService.getLeaveBalances(employee));
        return "employee/profile";
    }
}
