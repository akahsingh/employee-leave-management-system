package com.elms.service;

import com.elms.model.*;
import com.elms.repository.LeaveBalanceRepository;
import com.elms.repository.LeaveRequestRepository;
import com.elms.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }

    public List<LeaveRequest> getLeaveRequestsByEmployee(Employee employee) {
        return leaveRequestRepository.findByEmployee(employee);
    }

    public List<LeaveRequest> getPendingRequestsByManager(Long managerId) {
        return leaveRequestRepository.findByManagerIdAndStatus(managerId, LeaveStatus.PENDING);
    }

    public List<LeaveRequest> getAllRequestsByManager(Long managerId) {
        return leaveRequestRepository.findByManagerId(managerId);
    }

    public Optional<LeaveRequest> findById(Long id) {
        return leaveRequestRepository.findById(id);
    }

    public List<LeaveBalance> getLeaveBalances(Employee employee) {
        return leaveBalanceRepository.findByEmployeeAndYear(employee, LocalDate.now().getYear());
    }

    public List<LeaveType> getAllActiveLeaveTypes() {
        return leaveTypeRepository.findByActiveTrue();
    }

    @Transactional
    public LeaveRequest applyLeave(Employee employee, Long leaveTypeId,
                                    LocalDate startDate, LocalDate endDate, String reason) {
        LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
            .orElseThrow(() -> new RuntimeException("Leave type not found"));

        int totalDays = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;

        // Check balance
        LeaveBalance balance = leaveBalanceRepository
            .findByEmployeeAndLeaveTypeAndYear(employee, leaveType, LocalDate.now().getYear())
            .orElseThrow(() -> new RuntimeException("No leave balance found"));

        if (balance.getRemainingDays() < totalDays) {
            throw new RuntimeException("Insufficient leave balance. Available: " + balance.getRemainingDays() + " days");
        }

        LeaveRequest request = LeaveRequest.builder()
            .employee(employee)
            .leaveType(leaveType)
            .startDate(startDate)
            .endDate(endDate)
            .totalDays(totalDays)
            .reason(reason)
            .build();

        return leaveRequestRepository.save(request);
    }

    @Transactional
    public LeaveRequest reviewLeave(Long requestId, Employee reviewer, LeaveStatus status, String comments) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Leave request has already been reviewed");
        }

        request.setStatus(status);
        request.setReviewedBy(reviewer);
        request.setReviewerComments(comments);
        request.setReviewedAt(LocalDateTime.now());

        if (status == LeaveStatus.APPROVED) {
            // Deduct from balance
            LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeAndLeaveTypeAndYear(
                    request.getEmployee(), request.getLeaveType(), LocalDate.now().getYear())
                .orElseThrow(() -> new RuntimeException("Leave balance not found"));
            balance.setUsedDays(balance.getUsedDays() + request.getTotalDays());
            leaveBalanceRepository.save(balance);
        }

        return leaveRequestRepository.save(request);
    }

    @Transactional
    public LeaveRequest cancelLeave(Long requestId, Employee employee) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (!request.getEmployee().getId().equals(employee.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be cancelled");
        }

        request.setStatus(LeaveStatus.CANCELLED);
        return leaveRequestRepository.save(request);
    }

    public long countPendingRequests() {
        return leaveRequestRepository.countPendingRequests();
    }

    public long countPendingByManager(Long managerId) {
        return leaveRequestRepository.countPendingByManager(managerId);
    }

    public List<LeaveType> getAllLeaveTypes() {
        return leaveTypeRepository.findAll();
    }

    @Transactional
    public LeaveType saveLeaveType(LeaveType leaveType) {
        return leaveTypeRepository.save(leaveType);
    }
}
