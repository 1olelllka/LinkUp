package com.olelllka.profile_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.olelllka.profile_service.RabbitMQTestConfig;
import com.olelllka.profile_service.TestDataUtil;
import com.olelllka.profile_service.configuration.RabbitMQConfig;
import com.olelllka.profile_service.domain.dto.FollowDto;
import com.olelllka.profile_service.domain.dto.PatchProfileDto;
import com.olelllka.profile_service.domain.dto.UserMessageDto;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.repository.ProfileDocumentRepository;
import com.olelllka.profile_service.repository.ProfileRepository;
import com.olelllka.profile_service.service.ProfileService;
import com.redis.testcontainers.RedisContainer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
@Import(RabbitMQTestConfig.class)
class ProfileControllerIntegrationTest {

    @ServiceConnection
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>(DockerImageName.parse("neo4j:5.26.0"));

    @ServiceConnection
    static RabbitMQContainer rabbitContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"));

    @ServiceConnection
    static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:7.2.6"));

    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:8.0"));

    @Value("${JWT_SECRET:0d9aa86975f076cbb84ab112f361a4b254c6f553d41da0918b439300e592ed3f}")
    private String key;

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

    private final MockMvc mockMvc;
    private final ProfileService profileService;
    private final ProfileRepository profileRepository;
    private final ProfileDocumentRepository documentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin rabbitAdmin;
    private final ObjectMapper objectMapper;

    @Autowired
    public ProfileControllerIntegrationTest(MockMvc mockMvc,
                                            ProfileRepository profileRepository,
                                            ProfileService profileService,
                                            ProfileDocumentRepository documentRepository,
                                            RabbitTemplate rabbitTemplate,
                                            RabbitAdmin rabbitAdmin) {
        this.mockMvc = mockMvc;
        this.profileService = profileService;
        this.profileRepository = profileRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = new ObjectMapper();
        this.documentRepository = documentRepository;
        this.rabbitAdmin = rabbitAdmin;
        objectMapper.registerModule(new JavaTimeModule());
    }

    @AfterEach
    void tearingDown() {
        profileRepository.deleteAll();
        documentRepository.deleteAll();
    }

    @Test
    void testThatGetProfileByIdReturnsHttp404NotFoundIfProfileDoesNotExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/profiles/" + UUID.randomUUID()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testThatGetProfileReturnsHttp200OkIfProfileExists() throws Exception {
        ProfileEntity profile = TestDataUtil.createNewProfileEntity();
        profile.setId(UUID.randomUUID());
        profileRepository.save(profile);
        mockMvc.perform(MockMvcRequestBuilders.get("/profiles/" + profile.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testThatUpdateProfileByIdReturnsHttp400BadRequestIfInvalidData() throws Exception {
        PatchProfileDto patchProfileDto = TestDataUtil.createPatchProfileDto();
        patchProfileDto.setDateOfBirth(LocalDate.of(2026, 1, 1));
        String json = objectMapper.writeValueAsString(patchProfileDto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/profiles/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testThatUpdateProfileByIdReturnsHttp404NotFoundIfProfileDoesNotExist() throws Exception {
        PatchProfileDto patchProfileDto = TestDataUtil.createPatchProfileDto();
        UUID id = UUID.randomUUID();
        String json = objectMapper.writeValueAsString(patchProfileDto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/profiles/" + id)
                        .header("Authorization", "Bearer " + generateJwt(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testThatUpdateProfileByIdReturnsHttp200OkAndUpdatedProfile() throws Exception {
        UserMessageDto messageDto = TestDataUtil.createUserMessageDto();
        messageDto.setProfileId(UUID.randomUUID());
        createNewUser(messageDto);
        assertTrue(profileRepository.existsById(messageDto.getProfileId()));
        PatchProfileDto patchProfileDto = TestDataUtil.createPatchProfileDto();
        patchProfileDto.setName("UPDATED NAME");
        String json = objectMapper.writeValueAsString(patchProfileDto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/profiles/" + messageDto.getProfileId())
                        .header("Authorization", "Bearer " + generateJwt(messageDto.getProfileId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("UPDATED NAME"));
    }

    @Test
    void testThatDeleteProfileByIdReturnsHttp401UnauthorizedIfJWTInvalid() throws Exception {
        UUID profileId = UUID.randomUUID();
        mockMvc.perform(MockMvcRequestBuilders.delete("/profiles/" + profileId)
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void testThatDeleteProfileByIdReturnsHttp204NoContent() throws Exception {
        UUID profileId = UUID.randomUUID();
        UserMessageDto userMessageDto = TestDataUtil.createUserMessageDto();
        userMessageDto.setProfileId(profileId);
        createNewUser(userMessageDto);
        mockMvc.perform(MockMvcRequestBuilders.delete("/profiles/" + profileId)
                        .header("Authorization", "Bearer " + generateJwt(profileId)))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void testThatFollowProfileReturnsHttp400BadRequestIfValidationFails() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/profiles/follow")
                        .contentType("application/json")
                        .content(""))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testThatFollowProfileReturnsHttp400BadRequestIfTheSameIds() throws Exception {
        UUID id = UUID.randomUUID();
        FollowDto followDto = TestDataUtil.createFollowDto(id, id);
        String json = objectMapper.writeValueAsString(followDto);
        mockMvc.perform(MockMvcRequestBuilders.post("/profiles/follow")
                        .contentType("application/json")
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testThatFollowProfileReturnsHttp401UnauthorizedIfJWTInvalid() throws Exception {
        rabbitAdmin.purgeQueue(RabbitMQConfig.notification_queue);
        UserMessageDto messageDto1 = TestDataUtil.createUserMessageDto();
        messageDto1.setProfileId(UUID.randomUUID());
        createNewUser(messageDto1);
        UserMessageDto messageDto2 = TestDataUtil.createUserMessageDto();
        messageDto2.setProfileId(UUID.randomUUID());
        createNewUser(messageDto2);
        FollowDto followDto = TestDataUtil.createFollowDto(messageDto1.getProfileId(), messageDto2.getProfileId());
        String json = objectMapper.writeValueAsString(followDto);
        mockMvc.perform(MockMvcRequestBuilders.post("/profiles/follow")
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID()))
                        .contentType("application/json")
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void testThatFollowProfileReturnsHttp400BadRequestIfAlreadyFollowed() throws Exception {
        rabbitAdmin.purgeQueue(RabbitMQConfig.notification_queue);
        UserMessageDto messageDto1 = TestDataUtil.createUserMessageDto();
        messageDto1.setProfileId(UUID.randomUUID());
        createNewUser(messageDto1);
        UserMessageDto messageDto2 = TestDataUtil.createUserMessageDto();
        messageDto2.setProfileId(UUID.randomUUID());
        createNewUser(messageDto2);
        FollowDto followDto = TestDataUtil.createFollowDto(messageDto1.getProfileId(), messageDto2.getProfileId());
        String json = objectMapper.writeValueAsString(followDto);
        profileService.followNewProfile(messageDto1.getProfileId(), messageDto2.getProfileId(), generateJwt(messageDto1.getProfileId()));
        mockMvc.perform(MockMvcRequestBuilders.post("/profiles/follow")
                        .header("Authorization", "Bearer " + generateJwt(messageDto1.getProfileId()))
                        .contentType("application/json")
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testThatFollowProfileReturnsHttp200OkIfSuccessful() throws Exception {
        rabbitAdmin.purgeQueue(RabbitMQConfig.notification_queue);
        UserMessageDto messageDto1 = TestDataUtil.createUserMessageDto();
        messageDto1.setProfileId(UUID.randomUUID());
        createNewUser(messageDto1);
        UserMessageDto messageDto2 = TestDataUtil.createUserMessageDto();
        messageDto2.setProfileId(UUID.randomUUID());
        createNewUser(messageDto2);
        FollowDto followDto = TestDataUtil.createFollowDto(messageDto1.getProfileId(), messageDto2.getProfileId());
        String json = objectMapper.writeValueAsString(followDto);
        mockMvc.perform(MockMvcRequestBuilders.post("/profiles/follow")
                        .header("Authorization", "Bearer " + generateJwt(messageDto1.getProfileId()))
                        .contentType("application/json")
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk());
        assertEquals(1, rabbitAdmin.getQueueInfo(RabbitMQConfig.notification_queue).getMessageCount());
    }

    @Test
    void testThatUnfollowProfileReturnsHttp400BadRequestIfValidationFails() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/profiles/unfollow")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testThatUnfollowProfileReturnsHttp400BadRequestIfTheSameIds() throws Exception {
        UUID id = UUID.randomUUID();
        FollowDto followDto = TestDataUtil.createFollowDto(id, id);
        String json = objectMapper.writeValueAsString(followDto);
        mockMvc.perform(MockMvcRequestBuilders.delete("/profiles/unfollow")
                        .contentType("application/json")
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testThatUnfollowProfileReturnsHttp401UnauthorizedIfJWTInvalid() throws Exception {
        UserMessageDto messageDto1 = TestDataUtil.createUserMessageDto();
        messageDto1.setProfileId(UUID.randomUUID());
        createNewUser(messageDto1);
        UserMessageDto messageDto2 = TestDataUtil.createUserMessageDto();
        messageDto2.setProfileId(UUID.randomUUID());
        createNewUser(messageDto2);
        FollowDto followDto = TestDataUtil.createFollowDto(messageDto1.getProfileId(), messageDto2.getProfileId());
        String json = objectMapper.writeValueAsString(followDto);
        mockMvc.perform(MockMvcRequestBuilders.delete("/profiles/unfollow")
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID()))
                        .contentType("application/json")
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void testThatUnfollowProfileReturnsHttp400BadRequestIfAlreadyUnfollowed() throws Exception {
        UserMessageDto messageDto1 = TestDataUtil.createUserMessageDto();
        messageDto1.setProfileId(UUID.randomUUID());
        createNewUser(messageDto1);
        UserMessageDto messageDto2 = TestDataUtil.createUserMessageDto();
        messageDto2.setProfileId(UUID.randomUUID());
        createNewUser(messageDto2);
        FollowDto followDto = TestDataUtil.createFollowDto(messageDto1.getProfileId(), messageDto2.getProfileId());
        String json = objectMapper.writeValueAsString(followDto);
        mockMvc.perform(MockMvcRequestBuilders.delete("/profiles/unfollow")
                        .header("Authorization", "Bearer " + generateJwt(messageDto1.getProfileId()))
                        .contentType("application/json")
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testThatUnfollowProfileReturnsHttp200OkIfSuccessful() throws Exception {
        UserMessageDto messageDto1 = TestDataUtil.createUserMessageDto();
        messageDto1.setProfileId(UUID.randomUUID());
        createNewUser(messageDto1);
        UserMessageDto messageDto2 = TestDataUtil.createUserMessageDto();
        messageDto2.setProfileId(UUID.randomUUID());
        createNewUser(messageDto2);
        profileService.followNewProfile(messageDto1.getProfileId(), messageDto2.getProfileId(), generateJwt(messageDto1.getProfileId()));
        FollowDto followDto = TestDataUtil.createFollowDto(messageDto1.getProfileId(), messageDto2.getProfileId());
        String json = objectMapper.writeValueAsString(followDto);
        mockMvc.perform(MockMvcRequestBuilders.delete("/profiles/unfollow")
                        .header("Authorization", "Bearer " + generateJwt(messageDto1.getProfileId()))
                        .contentType("application/json")
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testThatGetFollowersByIdReturnsHttp200Ok() throws Exception {
        UserMessageDto messageDto1 = TestDataUtil.createUserMessageDto();
        messageDto1.setProfileId(UUID.randomUUID());
        messageDto1.setUsername("TEST");
        createNewUser(messageDto1);
        UserMessageDto messageDto2 = TestDataUtil.createUserMessageDto();
        messageDto2.setProfileId(UUID.randomUUID());
        createNewUser(messageDto2);
        profileService.followNewProfile(messageDto1.getProfileId(), messageDto2.getProfileId(), generateJwt(messageDto1.getProfileId()));
        mockMvc.perform(MockMvcRequestBuilders.get("/profiles/" + messageDto2.getProfileId() + "/followers"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].username").value("TEST"));
    }

    @Test
    void testThatGetFolloweesByIdReturnsHttp200Ok() throws Exception {
        UserMessageDto messageDto1 = TestDataUtil.createUserMessageDto();
        messageDto1.setProfileId(UUID.randomUUID());
        createNewUser(messageDto1);
        UserMessageDto messageDto2 = TestDataUtil.createUserMessageDto();
        messageDto2.setUsername("TEST");
        messageDto2.setProfileId(UUID.randomUUID());
        createNewUser(messageDto2);
        profileService.followNewProfile(messageDto1.getProfileId(), messageDto2.getProfileId(), generateJwt(messageDto1.getProfileId()));
        mockMvc.perform(MockMvcRequestBuilders.get("/profiles/" + messageDto1.getProfileId() + "/followees"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].username").value("TEST"));
    }

    @Test
    void testThatSearchProfilesByElasticSearchReturnsHttp200Ok() throws Exception {
        UUID profileId = UUID.randomUUID();
        UserMessageDto messageDto = TestDataUtil.createUserMessageDto();
        messageDto.setProfileId(profileId);
        createNewUser(messageDto);
        mockMvc.perform(MockMvcRequestBuilders.get("/profiles?search=F"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].username").value(messageDto.getUsername()));
    }

    @Test
    void testThatSearchProfilesByNeo4jReturnsHttp200OkIfElasticsearchIsDown() throws Exception {
        UserMessageDto messageDto = TestDataUtil.createUserMessageDto();
        messageDto.setProfileId(UUID.randomUUID());
        createNewUser(messageDto);
        mockMvc.perform(MockMvcRequestBuilders.get("/profiles?search=" + messageDto.getUsername()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].username").value(messageDto.getUsername()));
    }

    @Test
    void testThatCheckFollowStatusReturnsHttp200OKIfSuccessful() throws Exception {
        rabbitAdmin.purgeQueue(RabbitMQConfig.notification_queue);
        UserMessageDto messageDto1 = TestDataUtil.createUserMessageDto();
        messageDto1.setProfileId(UUID.randomUUID());
        createNewUser(messageDto1);
        UserMessageDto messageDto2 = TestDataUtil.createUserMessageDto();
        messageDto2.setProfileId(UUID.randomUUID());
        createNewUser(messageDto2);
        FollowDto followDto = TestDataUtil.createFollowDto(messageDto1.getProfileId(), messageDto2.getProfileId());
        String json = objectMapper.writeValueAsString(followDto);
        mockMvc.perform(MockMvcRequestBuilders.post("/profiles/follow")
                        .header("Authorization", "Bearer " + generateJwt(messageDto1.getProfileId()))
                        .contentType("application/json")
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk());
        assertEquals(1, rabbitAdmin.getQueueInfo(RabbitMQConfig.notification_queue).getMessageCount());
        mockMvc.perform(MockMvcRequestBuilders.get("/profiles/follow-status?from=" + messageDto1.getProfileId()
                                                                + "&to=" + messageDto2.getProfileId()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testThatCheckFollowStatusReturnsHttp404NotFoundIfRelationshipDoesNotExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/profiles/follow-status?from=" + UUID.randomUUID() + "&to=" + UUID.randomUUID()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testThatCheckUsernameAvailabilityReturnsHttp200Ok() throws Exception {
        UserMessageDto messageDto1 = TestDataUtil.createUserMessageDto();
        messageDto1.setProfileId(UUID.randomUUID());
        messageDto1.setUsername("TEST");
        createNewUser(messageDto1);
        mockMvc.perform(MockMvcRequestBuilders.get("/profiles/username-availability?username="+messageDto1.getUsername()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testThatCheckUsernameAvailabilityReturnsHttp404NotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/profiles/username-availability?username=random"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    private void createNewUser(UserMessageDto messageDto) throws InterruptedException {
        rabbitTemplate.convertAndSend(RabbitMQTestConfig.create_user_exchange, "create.user", messageDto);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> rabbitAdmin.getQueueInfo(RabbitMQTestConfig.create_user_queue).getMessageCount() == 0);
        Thread.sleep(Duration.of(1, ChronoUnit.SECONDS));
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

}
