package com.hris.config;

import com.hris.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Security Configuration
 * Configures authentication and authorization rules
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /**
     * Password Encoder Bean
     * Uses BCrypt for secure password hashing
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Provider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new com.hris.security.CustomAuthenticationProvider(userDetailsService, passwordEncoder());
    }

    /**
     * Authentication Manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Security Filter Chain
     * Configures HTTP security rules
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for HTMX (can be enabled later with proper headers)
            .csrf(csrf -> csrf.disable())

            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public pages
                .requestMatchers(
                    "/",
                    "/auth/login",
                    "/auth/login-error",
                    "/css/**",
                    "/js/**",
                    "/dist/**",
                    "/images/**",
                    "/webjars/**",
                    "/favicon.ico",
                    "/error",
                    "/api/test/**",
                    "/api/debug/**",
                    "/api/encoder-test/**",
                    "/api/debug-login/**"
                ).permitAll()

                // Role selection & confirmation (requires authentication, any role)
                .requestMatchers("/auth/select-role", "/auth/confirm-role").authenticated()

                // Role-based access
                .requestMatchers("/dashboard/admin").hasRole("ADMIN")
                .requestMatchers("/dashboard/hr").hasAnyRole("ADMIN", "HR")
                .requestMatchers("/dashboard/employee").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers("/dashboard/dosen").hasAnyRole("ADMIN", "HR", "DOSEN")

                // Company management (Admin only)
                .requestMatchers("/company/**").hasRole("ADMIN")

                // User management (Admin only)
                .requestMatchers("/users/**").hasRole("ADMIN")

                // Employee management (Admin, HR)
                .requestMatchers("/employees/**").hasAnyRole("ADMIN", "HR")

                // Department & Position (Admin, HR)
                .requestMatchers("/departments/**", "/positions/**").hasAnyRole("ADMIN", "HR")

                // Lecturer management (Admin, HR)
                .requestMatchers("/lecturers/**").hasAnyRole("ADMIN", "HR")

                // Attendance (All authenticated users)
                .requestMatchers("/attendance/**").authenticated()

                // Leave (All authenticated users)
                .requestMatchers("/leave/**").authenticated()

                // Payroll (Admin, HR)
                .requestMatchers("/payroll/**").hasAnyRole("ADMIN", "HR")

                // Lecturer payroll (Admin, HR)
                .requestMatchers("/lecturer-payroll/**").hasAnyRole("ADMIN", "HR")

                // Activity log (Admin only)
                .requestMatchers("/activity-log/**").hasRole("ADMIN")

                // All other requests require authentication
                .anyRequest().authenticated()
            )

            // Configure login
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .defaultSuccessUrl("/auth/select-role", true)
                .failureUrl("/auth/login?error=true")
                .usernameParameter("email")
                .passwordParameter("password")
                .permitAll()
            )

            // Configure logout
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout"))
                .logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "HRIS_SESSION")
                .permitAll()
            )

            // Configure session management
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/auth/login?expired=true")
            );

        return http.build();
    }
}
