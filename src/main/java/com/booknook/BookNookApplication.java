package com.booknook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Book Nook application.
 * @SpringBootApplication does three things:
 *   1. Marks this as a Spring configuration class
 *   2. Enables component scanning (finds all @Service, @Controller, etc.)
 *   3. Enables Spring Boot auto-configuration (H2, JPA, Web setup)
 */
@SpringBootApplication
public class BookNookApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookNookApplication.class, args);
        System.out.println("\n✨ Book Nook is running! Visit http://localhost:8080\n");
    }
}
