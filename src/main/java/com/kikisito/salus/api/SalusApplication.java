package com.kikisito.salus.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SalusApplication {
    public static void main(String[] args) {
        SpringApplication.run(SalusApplication.class, args);
    }
}
