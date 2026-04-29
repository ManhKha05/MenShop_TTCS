package com.ttcs.menshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class MenshopApplication {

	public static void main(String[] args) {
		SpringApplication.run(MenshopApplication.class, args);
	}

}
