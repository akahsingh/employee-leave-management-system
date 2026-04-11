package com.elms.repository;

import com.elms.model.Employee;
import com.elms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUser(User user);
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByEmployeeCode(String employeeCode);
    List<Employee> findByManagerId(Long managerId);
    List<Employee> findByActiveTrue();
    boolean existsByEmail(String email);
    boolean existsByEmployeeCode(String employeeCode);

    @Query("SELECT e FROM Employee e WHERE e.user.role = 'ROLE_MANAGER' AND e.active = true")
    List<Employee> findAllManagers();
}
