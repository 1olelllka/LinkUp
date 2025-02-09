package com.olelllka.notification_service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
class NotificationServiceApplicationTests {

	@ServiceConnection
	static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:8.0"));

	static {
		mongoDBContainer.start();
	}

	@AfterAll
	static void tearDown() {
		mongoDBContainer.stop();
		mongoDBContainer.close();
	}

	@Test
	void contextLoads() {
	}

}
