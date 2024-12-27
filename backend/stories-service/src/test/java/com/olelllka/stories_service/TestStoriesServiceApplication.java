package com.olelllka.stories_service;

import org.springframework.boot.SpringApplication;

public class TestStoriesServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(StoriesServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
