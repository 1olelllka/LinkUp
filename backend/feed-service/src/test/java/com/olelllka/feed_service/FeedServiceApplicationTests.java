package com.olelllka.feed_service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@Import(RabbitMQConfiguration.class)
@SpringBootTest
class FeedServiceApplicationTests {

	@ServiceConnection
	static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2.6")).withExposedPorts(6379);

	@ServiceConnection
	static RabbitMQContainer rabbitContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:latest"));

	static {
		redisContainer.start();
		rabbitContainer.start();
	}

	@AfterAll
	static void tearDown() {
		redisContainer.stop();
		redisContainer.close();
		rabbitContainer.stop();
		rabbitContainer.close();
	}

	@Test
	void contextLoads() {
	}

}
