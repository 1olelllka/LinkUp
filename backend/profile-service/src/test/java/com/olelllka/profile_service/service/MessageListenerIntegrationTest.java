package com.olelllka.profile_service.service;

import com.olelllka.profile_service.RabbitMQTestConfig;
import com.olelllka.profile_service.TestDataUtil;
import com.olelllka.profile_service.configuration.RabbitMQConfig;
import com.olelllka.profile_service.domain.dto.ProfileDocumentDto;
import com.olelllka.profile_service.domain.dto.UserMessageDto;
import com.olelllka.profile_service.domain.entity.ProfileDocument;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.repository.ProfileDocumentRepository;
import com.olelllka.profile_service.repository.ProfileRepository;
import com.redis.testcontainers.RedisContainer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(RabbitMQTestConfig.class)
public class MessageListenerIntegrationTest {

    @ServiceConnection
    static RabbitMQContainer rabbitContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"));
    @ServiceConnection
    static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:7.17.23"));
    @ServiceConnection
    static Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>(DockerImageName.parse("neo4j:latest"));
    @ServiceConnection
    static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:7.2.6"));

    static {
        rabbitContainer.start();
        elasticsearchContainer.start();
        neo4jContainer.start();
        redisContainer.start();
    }

    private final RabbitAdmin admin;
    private final MessagePublisher messagePublisher;
    private final ProfileDocumentRepository repository;
    private final ProfileRepository profileRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, ProfileEntity> redisTemplate;

    @Autowired
    public MessageListenerIntegrationTest(RabbitAdmin admin,
                                          MessagePublisher messagePublisher,
                                          RabbitTemplate rabbitTemplate,
                                          ProfileRepository profileRepository,
                                          ProfileDocumentRepository repository,
                                          RedisTemplate<String, ProfileEntity> redisTemplate) {
        this.messagePublisher = messagePublisher;
        this.admin = admin;
        this.repository = repository;
        this.profileRepository = profileRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;
    }

    @AfterEach
    void refreshDBs() {
        repository.deleteAll();
        profileRepository.deleteAll();
    }

    @AfterAll
    static void tearDown() {
        elasticsearchContainer.stop();
        elasticsearchContainer.close();
        rabbitContainer.stop();
        rabbitContainer.close();
        neo4jContainer.stop();
        neo4jContainer.close();
        redisContainer.stop();
        redisContainer.close();
    }

    @Test
    public void testThatCreateProfileFromAuthServiceWorks() throws InterruptedException {
        // given
        UUID profileId = UUID.randomUUID();
        UserMessageDto userMessageDto = TestDataUtil.createUserMessageDto();
        userMessageDto.setProfileId(profileId);
        // when
        rabbitTemplate.convertAndSend(RabbitMQTestConfig.create_user_exchange, "create.user", userMessageDto);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> admin.getQueueInfo(RabbitMQTestConfig.create_user_queue).getMessageCount() == 0);
        Thread.sleep(Duration.of(2, ChronoUnit.SECONDS));
        // then
        assertTrue(profileRepository.existsById(profileId));
        assertTrue(repository.existsById(profileId));
    }

    @Test
    public void testThatUpdateProfileFromAuthServiceWorks() throws InterruptedException {
        UUID profileId = UUID.randomUUID();
        UserMessageDto userMessageDto = TestDataUtil.createUserMessageDto();
        userMessageDto.setProfileId(profileId);
        // create profile
        rabbitTemplate.convertAndSend(RabbitMQTestConfig.create_user_exchange, "create.user", userMessageDto);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> admin.getQueueInfo(RabbitMQTestConfig.create_user_queue).getMessageCount() == 0);
        Thread.sleep(Duration.of(2, ChronoUnit.SECONDS));
        // update
        userMessageDto.setUsername("UPDATED");
        rabbitTemplate.convertAndSend(RabbitMQTestConfig.update_user_exchange, "update.user", userMessageDto);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> admin.getQueueInfo(RabbitMQTestConfig.update_user_queue).getMessageCount() == 0);
        Thread.sleep(Duration.of(2, ChronoUnit.SECONDS));
        // then
        assertEquals(profileRepository.findById(profileId).get().getName(), userMessageDto.getName());
        assertEquals(profileRepository.findById(profileId).get().getUsername(), "UPDATED");
        assertEquals(repository.findById(profileId).get().getName(), userMessageDto.getName());
        assertEquals(repository.findById(profileId).get().getUsername(), "UPDATED");
        assertTrue(redisTemplate.hasKey("profile::" + SHA256.hash(profileId.toString())));
    }

    @Test
    public void testThatUpdateProfileOnElasticsearchListenerDoesItsJob() {
        // given
        ProfileDocumentDto dto = TestDataUtil.createNewProfileDocumentDto();
        ProfileDocument entity = TestDataUtil.createNewProfileDocument();
        entity.setId(dto.getId());
        // when
        messagePublisher.updateProfile(dto);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> admin.getQueueInfo(RabbitMQConfig.update_elastic_queue).getMessageCount() == 0);
        // then
        assertTrue(repository.existsById(entity.getId()));
        assertEquals(repository.findById(entity.getId()).get().getName(), dto.getName());
    }

    @Test
    public void testThatDeleteProfileOnElasticSearchDoesItsJob() {
        // given
        ProfileDocumentDto dto = TestDataUtil.createNewProfileDocumentDto();
        messagePublisher.updateProfile(dto);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> admin.getQueueInfo(RabbitMQConfig.update_elastic_queue).getMessageCount() == 0);
        // when
        messagePublisher.deleteProfile(dto.getId());
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> admin.getQueueInfo(RabbitMQConfig.delete_queue_elastic).getMessageCount() == 0);
        // then
        assertFalse(repository.existsById(dto.getId()));
        assertEquals(admin.getQueueInfo(RabbitMQConfig.delete_queue_post).getMessageCount(), 1);
    }

}
