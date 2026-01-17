package com.hris.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private UserDetailsService userDetailsService;
    private PasswordEncoder passwordEncoder;

    public CustomAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();
        
        log.info("==== AUTHENTICATION DEBUG ====");
        log.info("Email: '{}', length: {}", email, email.length());
        log.info("Password: '{}' (hidden), length: {}", password, password.length());
        log.info("Password bytes: {}", bytesToHex(password.getBytes()));
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String dbHash = userDetails.getPassword();
        
        log.info("DB Hash: {}", dbHash.substring(0, Math.min(30, dbHash.length())));
        
        boolean matches = passwordEncoder.matches(password, dbHash);
        log.info("Password matches: {}", matches);
        
        // Test with hardcoded password
        boolean testMatches = passwordEncoder.matches("test123", dbHash);
        log.info("Hardcoded 'test123' matches: {}", testMatches);
        
        if (!matches) {
            log.error("Authentication FAILED for email: {}", email);
            throw new BadCredentialsException("Invalid credentials");
        }
        
        log.info("Authentication SUCCESS for email: {}", email);
        log.info("==== END AUTHENTICATION DEBUG ====");
        
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                password,
                userDetails.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
