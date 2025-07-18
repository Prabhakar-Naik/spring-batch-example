package com.learn.springbatchexample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringBatchExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchExampleApplication.class, args);
        System.out.println("Spring Batch Example Application Started...!");
    }

}
