package com.elms.repository;

import com.elms.model.Employee;
import com.elms.model.LeaveRequest;
import com.elms.model.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployee(Employee employee);
    List<LeaveRequest> findByStatus(LeaveStatus status);
    List<LeaveRequest> findByEmployeeAndStatus(Employee employee, LeaveStatus status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.manager.id = :managerId ORDER BY lr.appliedAt DESC")
    List<LeaveRequest> findByManagerId(Long managerId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.manager.id = :managerId AND lr.status = :status ORDER BY lr.appliedAt DESC")
    List<LeaveRequest> findByManagerIdAndStatus(Long managerId, LeaveStatus status);

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.status = 'PENDING'")
    long countPendingRequests();

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.employee.manager.id = :managerId AND lr.status = 'PENDING'")
    long countPendingByManager(Long managerId);
}
