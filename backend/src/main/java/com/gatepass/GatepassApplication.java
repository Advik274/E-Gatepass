package com.gatepass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GatepassApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatepassApplication.class, args);
    }
}