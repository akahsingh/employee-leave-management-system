package com.elms.controller;

import com.elms.model.*;
import com.elms.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private final com.elms.repository.UserRepository userRepository;

    private Employee getCurrentEmployee(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
            .flatMap(employeeService::findByUser)
            .orElseThrow(() -> new RuntimeException("Manager profile not found"));
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Employee manager = getCurrentEmployee(userDetails);
        model.addAttribute("manager", manager);
        model.addAttribute("teamSize", employeeService.getTeamByManager(manager.getId()).size());
        model.addAttribute("pendingLeaves", leaveService.countPendingByManager(manager.getId()));
        model.addAttribute("recentRequests", leaveService.getAllRequestsByManager(manager.getId()).stream().limit(5).toList());
        return "manager/dashboard";
    }

    @GetMapping("/leaves")
    public String leaveRequests(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam(required = false) String filter,
                                 Model model) {
        Employee manager = getCurrentEmployee(userDetails);
        model.addAttribute("manager", manager);

        if ("pending".equals(filter)) {
            model.addAttribute("leaveRequests", leaveService.getPendingRequestsByManager(manager.getId()));
            model.addAttribute("filter", "pending");
        } else {
            model.addAttribute("leaveRequests", leaveService.getAllRequestsByManager(manager.getId()));
            model.addAttribute("filter", "all");
        }
        return "manager/leaves";
    }

    @PostMapping("/leaves/review/{id}")
    public String reviewLeave(@PathVariable Long id,
                               @RequestParam String action,
                               @RequestParam(required = false) String comments,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            Employee manager = getCurrentEmployee(userDetails);
            LeaveStatus status = "approve".equals(action) ? LeaveStatus.APPROVED : LeaveStatus.REJECTED;
            leaveService.reviewLeave(id, manager, status, comments);
            redirectAttributes.addFlashAttribute("success",
                "Leave request " + (status == LeaveStatus.APPROVED ? "approved" : "rejected") + " successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/manager/leaves";
    }

    @GetMapping("/team")
    public String team(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Employee manager = getCurrentEmployee(userDetails);
        model.addAttribute("manager", manager);
        model.addAttribute("team", employeeService.getTeamByManager(manager.getId()));
        return "manager/team";
    }
}
