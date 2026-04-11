package com.elms.repository;

import com.elms.model.Employee;
import com.elms.model.LeaveBalance;
import com.elms.model.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    List<LeaveBalance> findByEmployee(Employee employee);
    List<LeaveBalance> findByEmployeeAndYear(Employee employee, int year);
    Optional<LeaveBalance> findByEmployeeAndLeaveTypeAndYear(Employee employee, LeaveType leaveType, int year);
}
