package com.hris.security;

import com.hris.model.Employee;
import com.hris.model.enums.EmployeeStatus;
import com.hris.model.enums.RoleType;
import com.hris.repository.EmployeeRepository;
import com.hris.repository.EmployeeRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Custom User Details Service
 * Loads user-specific data for authentication
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeRoleRepository employeeRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Loading user by email: {}", email);
        
        Employee employee = employeeRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        log.info("User found: {}, status: {}", employee.getEmail(), employee.getStatus());
        log.info("Password hash from DB: {}", employee.getPassword().substring(0, Math.min(30, employee.getPassword().length())));

        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new UsernameNotFoundException("User account is not active: " + email);
        }

        // Load user roles
        Set<RoleType> roles = new HashSet<>(employeeRoleRepository.findRolesByEmployeeId(employee.getId()));
        log.info("User roles: {}", roles);

        return new CustomUserDetails(
                employee.getId(),
                employee.getEmail(),
                employee.getPassword(),
                employee.getFullName(),
                roles,
                employee.getStatus() == EmployeeStatus.ACTIVE
        );
    }
}
