package com.ppg.iicsdoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for IICS Documentation Generator
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
public class IICSDocGenApplication {
    public static void main(String[] args) {
        SpringApplication.run(IICSDocGenApplication.class, args);
    }
}