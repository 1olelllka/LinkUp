package com.olelllka.profile_service;

import org.junit.After;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
class ProfileServiceApplicationTests {

	@ServiceConnection
	static Neo4jContainer<?> neo4j = new Neo4jContainer<>(DockerImageName.parse("neo4j:latest"));

	static {
		neo4j.start();
	}

	@AfterAll
	static void tearDown() {
		neo4j.stop();
		neo4j.close();
	}

	@Test
	void contextLoads() {
	}

}
