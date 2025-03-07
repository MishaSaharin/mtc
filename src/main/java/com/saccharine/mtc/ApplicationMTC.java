package com.saccharine.mtc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
public class ApplicationMTC {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationMTC.class, args);
    }
}