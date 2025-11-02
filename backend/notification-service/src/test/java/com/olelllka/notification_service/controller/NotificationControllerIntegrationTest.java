package com.olelllka.notification_service.controller;

import com.olelllka.notification_service.RabbitMQTestConfig;
import com.olelllka.notification_service.TestDataUtil;
import com.olelllka.notification_service.domain.entity.NotificationEntity;
import com.olelllka.notification_service.repository.NotificationRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@Import(RabbitMQTestConfig.class)
public class NotificationControllerIntegrationTest {

    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:8.0"));

    @ServiceConnection
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"));

    @Value("${JWT_SECRET:0d9aa86975f076cbb84ab112f361a4b254c6f553d41da0918b439300e592ed3f}")
    private String key;

    static {
        mongoDBContainer.start();
        rabbitMQContainer.start();
    }

    @AfterAll
    static void tearDown() {
        mongoDBContainer.stop();
        mongoDBContainer.close();
        rabbitMQContainer.stop();
        rabbitMQContainer.close();
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
        mockMvc.perform(MockMvcRequestBuilders.get("/notifications/users/" + profileId)
                        .header("Authorization", "Bearer " + generateJwt(profileId)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testThatGetListOfNotificationsForUserReturnsHttp401Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/notifications/users/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testThatUpdateReadStatusesOfNotificationsReturnHttp403IfUserUnauthorizedToUpdateThem() throws Exception {
        UUID id = UUID.randomUUID();
        NotificationEntity notification = TestDataUtil.createNotificationEntity();
        notification.setUserId(id);
        notification = repository.save(notification);
        mockMvc.perform(MockMvcRequestBuilders.patch("/notifications/read?ids=" +notification.getId())
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void testThatUpdateReadStatusOfNotificationReturnsHttp200OkAndUpdatedReadStatus() throws Exception {
        UUID id = UUID.randomUUID();
        NotificationEntity notification = TestDataUtil.createNotificationEntity();
        notification.setUserId(id);
        notification = repository.save(notification);
        mockMvc.perform(MockMvcRequestBuilders.patch("/notifications/read?ids=" + notification.getId())
                        .header("Authorization", "Bearer " + generateJwt(id)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testThatDeleteSpecificNotificationReturnsHttp204NoContentIfNotificationDoesNotExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/notifications/" + ObjectId.getSmallestWithDate(new Date()))
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testThatDeleteSpecificNotificationReturnsHttp401Unauthorized() throws Exception {
        UUID id = UUID.randomUUID();
        NotificationEntity notification = TestDataUtil.createNotificationEntity();
        notification.setUserId(id);
        NotificationEntity saved = repository.save(notification);
        mockMvc.perform(MockMvcRequestBuilders.delete("/notifications/" + saved.getId())
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testThatDeleteSpecificNotificationReturnsHttp204NoContent() throws Exception {
        UUID id = UUID.randomUUID();
        NotificationEntity notification = TestDataUtil.createNotificationEntity();
        notification.setUserId(id);
        NotificationEntity saved = repository.save(notification);
        mockMvc.perform(MockMvcRequestBuilders.delete("/notifications/" + saved.getId())
                        .header("Authorization", "Bearer " + generateJwt(id)))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testThatDeleteAllNotificationsReturnsHttp401Unauthorized() throws Exception {
        UUID id = UUID.randomUUID();
        NotificationEntity entity1 = TestDataUtil.createNotificationEntity();
        entity1.setUserId(id);
        repository.save(entity1);
        mockMvc.perform(MockMvcRequestBuilders.delete("/notifications/users/" + id)
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
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
        mockMvc.perform(MockMvcRequestBuilders.delete("/notifications/users/" + id)
                        .header("Authorization", "Bearer " + generateJwt(id)))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        // check
        mockMvc.perform(MockMvcRequestBuilders.get("/notifications/users/" + id)
                        .header("Authorization", "Bearer " + generateJwt(id)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value("0"));
    }

    private String generateJwt(UUID id) {
        return Jwts.builder()
                .issuer("LinkUp")
                .subject(id.toString())
                .issuedAt(new Date())
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(key)))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1hr
                .compact();
    }

    private SecretKey securityKey() {
        byte[] decodedSecret = Base64.getDecoder().decode(key);
        return Keys.hmacShaKeyFor(decodedSecret);
    }

}
