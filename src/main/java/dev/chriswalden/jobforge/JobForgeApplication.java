package dev.chriswalden.jobforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class JobForgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobForgeApplication.class, args);
	}

}
