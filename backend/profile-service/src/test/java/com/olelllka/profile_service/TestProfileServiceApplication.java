package com.olelllka.profile_service;

import org.springframework.boot.SpringApplication;

public class TestProfileServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(ProfileServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
