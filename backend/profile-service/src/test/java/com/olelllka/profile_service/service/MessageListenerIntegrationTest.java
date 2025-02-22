package com.olelllka.profile_service.service;

import com.olelllka.profile_service.TestDataUtil;
import com.olelllka.profile_service.configuration.RabbitMQConfig;
import com.olelllka.profile_service.domain.dto.ProfileDocumentDto;
import com.olelllka.profile_service.domain.entity.ProfileDocument;
import com.olelllka.profile_service.repository.ProfileDocumentRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MessageListenerIntegrationTest {

    @ServiceConnection
    static RabbitMQContainer rabbitContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"));
    @ServiceConnection
    static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:7.17.23"));

    static {
        rabbitContainer.start();
        elasticsearchContainer.start();
    }

    private final RabbitAdmin admin;
    private final MessagePublisher messagePublisher;
    private final ProfileDocumentRepository repository;

    @Autowired
    public MessageListenerIntegrationTest(RabbitAdmin admin,
                                          MessagePublisher messagePublisher,
                                          ProfileDocumentRepository repository) {
        this.messagePublisher = messagePublisher;
        this.admin = admin;
        this.repository = repository;
    }

    @AfterAll
    static void tearDown() {
        elasticsearchContainer.stop();
        elasticsearchContainer.close();
        rabbitContainer.stop();
        rabbitContainer.close();
    }

    @Test
    public void testThatCreateUpdateProfileOnElasticsearchListenerDoesItsJob() {
        // given
        ProfileDocumentDto dto = TestDataUtil.createNewProfileDocumentDto();
        ProfileDocument entity = TestDataUtil.createNewProfileDocument();
        entity.setId(dto.getId());
        // when
        messagePublisher.createUpdateProfile(dto);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> admin.getQueueInfo(RabbitMQConfig.create_update_queue).getMessageCount() == 0);
        // then
        assertTrue(repository.existsById(entity.getId()));
    }

    @Test
    public void testThatDeleteProfileOnElasticSearchDoesItsJob() {
        // given
        ProfileDocumentDto dto = TestDataUtil.createNewProfileDocumentDto();
        messagePublisher.createUpdateProfile(dto);
        // when
        messagePublisher.deleteProfile(dto.getId());
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> admin.getQueueInfo(RabbitMQConfig.delete_queue_elastic).getMessageCount() == 0);
        // then
        assertFalse(repository.existsById(dto.getId()));
        assertEquals(admin.getQueueInfo(RabbitMQConfig.delete_queue_post).getMessageCount(), 1);
    }

}
