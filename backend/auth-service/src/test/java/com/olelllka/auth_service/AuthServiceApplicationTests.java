package com.olelllka.auth_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class AuthServiceApplicationTests {

	@DynamicPropertySource
	static void registerEurekaProperties(DynamicPropertyRegistry registry) {
		registry.add("eureka.client.enabled", () -> false);
	}

	@Test
	void contextLoads() {
	}

}
