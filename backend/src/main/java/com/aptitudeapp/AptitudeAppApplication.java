package com.aptitudeapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AptitudeAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(AptitudeAppApplication.class, args);
    }
}
