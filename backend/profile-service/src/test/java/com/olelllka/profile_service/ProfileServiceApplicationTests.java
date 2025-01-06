package com.olelllka.profile_service;

import org.junit.After;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
class ProfileServiceApplicationTests {

	@ServiceConnection
	static Neo4jContainer<?> neo4j = new Neo4jContainer<>(DockerImageName.parse("neo4j:5.26.0"));

	@ServiceConnection
	static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:7.17.23"));

	@ServiceConnection
	static RabbitMQContainer rabbitContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"));

	static {
		neo4j.start();
		elasticsearchContainer.start();
		rabbitContainer.start();
	}

	@AfterAll
	static void tearDown() {
		neo4j.stop();
		neo4j.close();
		elasticsearchContainer.stop();
		elasticsearchContainer.close();
		rabbitContainer.stop();
		rabbitContainer.close();
	}

	@Test
	void contextLoads() {
	}

}
