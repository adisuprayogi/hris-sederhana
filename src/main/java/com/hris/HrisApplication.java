package com.hris;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main Application Class for HRIS Sederhana
 *
 * @author HRIS Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
public class HrisApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrisApplication.class, args);
    }
}
