package com.olelllka.profile_service;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Import(RabbitMQTestConfig.class)
class ProfileServiceApplicationTests {

	@ServiceConnection
	static Neo4jContainer<?> neo4j = new Neo4jContainer<>(DockerImageName.parse("neo4j:5.26.0"));

	@ServiceConnection
	static RabbitMQContainer rabbitContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"));

	@ServiceConnection
	static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:7.2.6"));

	@ServiceConnection
	static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:8.0"));


	static {
		neo4j.start();
		rabbitContainer.start();
		redisContainer.start();
		mongoDBContainer.start();
	}

	@AfterAll
	static void tearDown() {
		neo4j.stop();
		neo4j.close();
		rabbitContainer.stop();
		rabbitContainer.close();
		redisContainer.stop();
		redisContainer.close();
		mongoDBContainer.stop();
		mongoDBContainer.close();
	}

	@Test
	void contextLoads() {
	}

}
