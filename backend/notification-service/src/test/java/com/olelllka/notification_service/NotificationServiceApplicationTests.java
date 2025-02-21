package com.olelllka.notification_service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Import(RabbitMQTestConfig.class)
class NotificationServiceApplicationTests {

	@ServiceConnection
	static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:8.0"));

	@ServiceConnection
	static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"));

	static {
		mongoDBContainer.start();
		rabbitMQContainer.start();
	}

	@AfterAll
	static void tearDown() {
		mongoDBContainer.stop();
		mongoDBContainer.close();
		rabbitMQContainer.stop();
		rabbitMQContainer.close();
	}

	@Test
	void contextLoads() {
	}

}
