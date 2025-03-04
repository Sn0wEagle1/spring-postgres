package com.postgresql.springlab;

import com.postgresql.springlab.model.*;
import com.postgresql.springlab.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.LocalDate;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.postgresql.springlab.repository")
@EntityScan(basePackages = "com.postgresql.springlab.model")
public class SpringlabApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringlabApplication.class, args);
	}
}