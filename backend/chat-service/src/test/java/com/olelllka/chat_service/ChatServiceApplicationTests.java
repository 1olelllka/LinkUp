package com.olelllka.chat_service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MongoDBContainer;

@SpringBootTest
class ChatServiceApplicationTests {

	@ServiceConnection
	static MongoDBContainer mongo = new MongoDBContainer("mongo:8.0");

	static {
		mongo.start();
	}

	@AfterAll
	static void tearDown() {
		mongo.stop();
		mongo.close();
	}

	@Test
	void contextLoads() {
	}

}
