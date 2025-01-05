package com.olelllka.stories_service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
class StoriesServiceApplicationTests {

	@ServiceConnection
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0");
	@ServiceConnection
	static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2.6")).withExposedPorts(6379);

	static {
		mongoDBContainer.start();;
		redisContainer.start();
	}

	@AfterAll
	static void tearDown() {
		mongoDBContainer.stop();
		mongoDBContainer.close();
		redisContainer.stop();
		redisContainer.close();
	}

	@Test
	void contextLoads() {
	}

}
