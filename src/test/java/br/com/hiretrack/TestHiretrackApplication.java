package br.com.hiretrack;

import org.springframework.boot.SpringApplication;

public class TestHiretrackApplication {

    public static void main(String[] args) {
        SpringApplication.from(HiretrackApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
