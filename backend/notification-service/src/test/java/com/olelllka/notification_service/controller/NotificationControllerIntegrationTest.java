package com.olelllka.notification_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olelllka.notification_service.TestDataUtil;
import com.olelllka.notification_service.domain.entity.NotificationEntity;
import com.olelllka.notification_service.repository.NotificationRepository;
import com.olelllka.notification_service.service.NotificationService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Date;
import java.util.UUID;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class NotificationControllerIntegrationTest {

    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:8.0"));

    static {
        mongoDBContainer.start();
    }

    private final NotificationService service;
    private final MockMvc mockMvc;
    private final NotificationRepository repository; // temporary

    @Autowired
    public NotificationControllerIntegrationTest(NotificationService service,
                                                 MockMvc mockMvc,
                                                 NotificationRepository repository) {
        this.repository = repository;
        this.mockMvc = mockMvc;
        this.service = service;
    }

    @Test
    public void testThatGetListOfNotificationsForUserReturnsHttp200OkAndPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/notifications/users/" + UUID.randomUUID()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testThatUpdateReadStatusOfNotificationReturnsHttp404IfSuchNotificationDoesNotExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/notifications/" + ObjectId.getSmallestWithDate(new Date())))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testThatDeleteSpecificNotificationReturnsHttp204NoContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/notifications/" + ObjectId.getSmallestWithDate(new Date())))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testThatDeleteAllNotificationsReturnsHttp204NoContent() throws Exception {
        UUID id = UUID.randomUUID();
        NotificationEntity entity1 = TestDataUtil.createNotificationEntity();
        entity1.setUserId(id);
        NotificationEntity entity2 = TestDataUtil.createNotificationEntity();
        entity2.setUserId(id);
        repository.save(entity1);
        repository.save(entity2);
        mockMvc.perform(MockMvcRequestBuilders.delete("/notifications/users/" + id))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        // check
        mockMvc.perform(MockMvcRequestBuilders.get("/notifications/users/" + id))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value("0"));
    }

}
