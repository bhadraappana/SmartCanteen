package com.canteen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.h2.H2ConsoleAutoConfiguration;

@SpringBootApplication(exclude = { H2ConsoleAutoConfiguration.class })
public class CanteenBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(CanteenBackendApplication.class, args);
    }
}