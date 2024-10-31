package com.bfp.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.bfp")
@EnableJpaRepositories(basePackages = "com.bfp.filemanagement.dao")  // Adjust package name
@EntityScan(basePackages = "com.bfp.filemanagement.dao")
public class BasicFilePermissionsApplication {
    public static void main(String[] args) {
        SpringApplication.run(BasicFilePermissionsApplication.class, args);
    }
}
