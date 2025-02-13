package com.olelllka.profile_service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

// Commented something for now, later I'll integrate it
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	ElasticsearchContainer elasticsearchContainer() {
		return new ElasticsearchContainer(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:7.17.23"));
	}

	@Bean
	@ServiceConnection
	Neo4jContainer<?> neo4jContainer() {
		return new Neo4jContainer<>(DockerImageName.parse("neo4j:latest"));
	}
	@Bean
	@ServiceConnection
	RabbitMQContainer rabbitContainer() {
		return new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"));
	}

//	@Bean
//	@ServiceConnection(name = "redis")
//	GenericContainer<?> redisContainer() {
//		return new GenericContainer<>(DockerImageName.parse("redis:latest")).withExposedPorts(6379);
//	}

}
