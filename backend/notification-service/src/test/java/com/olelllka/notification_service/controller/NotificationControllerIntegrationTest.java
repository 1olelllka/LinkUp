package com.olelllka.notification_service.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.olelllka.notification_service.TestDataUtil;
import com.olelllka.notification_service.domain.entity.NotificationEntity;
import com.olelllka.notification_service.repository.NotificationRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
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

    @RegisterExtension
    static WireMockExtension PROFILE_SERVICE = WireMockExtension.newInstance()
            .options(WireMockConfiguration.options().port(8001)).build();

    static {
        mongoDBContainer.start();
    }

    @AfterAll
    static void tearDown() {
        mongoDBContainer.stop();
        mongoDBContainer.close();
    }

    private final MockMvc mockMvc;
    private final NotificationRepository repository;

    @Autowired
    public NotificationControllerIntegrationTest(MockMvc mockMvc,
                                                 NotificationRepository repository) {
        this.repository = repository;
        this.mockMvc = mockMvc;
    }
    @Test
    public void testThatGetListOfNotificationsForUserReturnsHttp200OkAndPage() throws Exception {
        UUID profileId = UUID.randomUUID();
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + profileId).willReturn(WireMock.ok()));

        mockMvc.perform(MockMvcRequestBuilders.get("/notifications/users/" + profileId))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testThatGetListOfNotificationsForUserReturnsHttp404NotFound() throws Exception {
        UUID profileId = UUID.randomUUID();
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + profileId).willReturn(WireMock.notFound()));

        mockMvc.perform(MockMvcRequestBuilders.get("/notifications/users/" + profileId))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
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
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + id).willReturn(WireMock.ok()));
        mockMvc.perform(MockMvcRequestBuilders.delete("/notifications/users/" + id))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        // check
        mockMvc.perform(MockMvcRequestBuilders.get("/notifications/users/" + id))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value("0"));
    }

    @Test
    public void testThatDeleteAllNotificationsReturnsHttp404NotFound() throws Exception {
        UUID id = UUID.randomUUID();
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + id).willReturn(WireMock.notFound()));
        mockMvc.perform(MockMvcRequestBuilders.delete("/notifications/users/" + id))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

}
