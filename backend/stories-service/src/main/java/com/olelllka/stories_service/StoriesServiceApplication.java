package com.olelllka.stories_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class StoriesServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StoriesServiceApplication.class, args);
	}

}
