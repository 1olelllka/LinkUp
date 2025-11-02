package com.olelllka.stories_service.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.olelllka.stories_service.RabbitMQConfig;
import com.olelllka.stories_service.TestDataUtil;
import com.olelllka.stories_service.domain.dto.CreateStoryDto;
import com.olelllka.stories_service.domain.dto.ProfileDto;
import com.olelllka.stories_service.domain.entity.StoryEntity;
import com.olelllka.stories_service.service.SHA256;
import com.olelllka.stories_service.service.StoryService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@Import(RabbitMQConfig.class)
public class StoryControllerIntegrationTests {

    @RegisterExtension
    static WireMockExtension PROFILE_SERVICE = WireMockExtension.newInstance()
            .options(WireMockConfiguration.options().port(8001)).build();

    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0");
    @ServiceConnection
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2.6")).withExposedPorts(6379);
    @ServiceConnection
    static RabbitMQContainer rabbitContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"));

    private MockMvc mockMvc;
    private StoryService service;
    private ObjectMapper objectMapper;
    private RedisTemplate<String, String> redisTemplate;
    private RabbitAdmin rabbitAdmin;

    @Value("${JWT_SECRET:0d9aa86975f076cbb84ab112f361a4b254c6f553d41da0918b439300e592ed3f}")
    private String key;

    @Autowired
    public StoryControllerIntegrationTests(MockMvc mockMvc, StoryService service,
                                           RedisTemplate<String, String> redisTemplate,
                                           RabbitAdmin rabbitAdmin) {
        this.mockMvc = mockMvc;
        this.service = service;
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.rabbitAdmin = rabbitAdmin;
    }

    static {
        mongoDBContainer.start();
        redisContainer.start();
        rabbitContainer.start();
    }

    @AfterAll
    static void tearDown() {
        mongoDBContainer.stop();
        mongoDBContainer.close();
        redisContainer.stop();
        redisContainer.close();
        rabbitContainer.stop();
        rabbitContainer.close();
    }

    @Test
    public void testThatGetArchivedStoriesForUserReturnsHttp401Unauthorized() throws Exception {
        UUID profileId = UUID.randomUUID();
        mockMvc.perform(MockMvcRequestBuilders.get("/stories/archive/" + profileId)
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testThatGetArchivedStoriesForUserReturnsHttp200Ok() throws Exception {
        UUID profileId = UUID.randomUUID();
        mockMvc.perform(MockMvcRequestBuilders.get("/stories/archive/" + profileId)
                .header("Authorization", "Bearer " + generateJwt(profileId)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testThatCreateStoryForUserReturnsHttp400BadRequestIfIncorrectJson() throws Exception {
        CreateStoryDto createStoryDto = CreateStoryDto.builder().image("").build();
        String json = objectMapper.writeValueAsString(createStoryDto);
        mockMvc.perform(MockMvcRequestBuilders.post("/stories/users/1234")
                .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        assertFalse(redisTemplate.hasKey("story-feed:" + SHA256.generate("1234")));
    }

    @Test
    public void testThatCreateStoryForUserReturnsHttp201CreatedIfSuccessful() throws Exception {
        UUID profileId = UUID.randomUUID();
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + profileId).willReturn(WireMock.ok()));
        CreateStoryDto createStoryDto = CreateStoryDto.builder().image("New Image url").build();
        ProfileDto dto = getProfileDto(profileId);
        String json = objectMapper.writeValueAsString(createStoryDto);
        mockMvc.perform(MockMvcRequestBuilders.post("/stories/users/" + profileId)
                        .header("Authorization", "Bearer " + generateJwt(profileId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.available").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.image").value("New Image url"));
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> rabbitAdmin.getQueueInfo(RabbitMQConfig.CREATE_STORY_QUEUE).getMessageCount() == 0);
        assertTrue(redisTemplate.hasKey("story-feed:" + SHA256.generate(dto.getId().toString())));
    }

    @Test
    public void testThatCreateStoryForUserReturnsHttp401Unauthorized() throws Exception {
        UUID profileId = UUID.randomUUID();
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + profileId).willReturn(WireMock.ok()));
        CreateStoryDto createStoryDto = CreateStoryDto.builder().image("New Image url").build();
        String json = objectMapper.writeValueAsString(createStoryDto);
        mockMvc.perform(MockMvcRequestBuilders.post("/stories/users/" + profileId)
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        assertFalse(redisTemplate.hasKey("story-feed:" + SHA256.generate(profileId.toString())));
    }

    @Test
    public void testThatCreateStoryForUserTriggersCircuitBreaker() throws Exception {
        UUID profileId = UUID.randomUUID();
        CreateStoryDto createStoryDto = CreateStoryDto.builder().image("New Image url").build();
        String json = objectMapper.writeValueAsString(createStoryDto);
        mockMvc.perform(MockMvcRequestBuilders.post("/stories/users/" + profileId)
                        .header("Authorization", "Bearer " + generateJwt(profileId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isServiceUnavailable());
    }

    @Test
    public void testThatCreateStoryForUserReturnsHttp404NotFoundIfUserDoesNotExist() throws Exception {
        UUID profileId = UUID.randomUUID();
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + profileId).willReturn(WireMock.notFound()));
        CreateStoryDto createStoryDto = CreateStoryDto.builder().image("New Image url").build();
        String json = objectMapper.writeValueAsString(createStoryDto);
        mockMvc.perform(MockMvcRequestBuilders.post("/stories/users/" + profileId)
                        .header("Authorization", "Bearer " + generateJwt(profileId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testThatUpdateStoryReturnsHttp400BadRequestIfIncorrectJson() throws Exception {
        CreateStoryDto createStoryDto = CreateStoryDto.builder().image("").build();
        String json = objectMapper.writeValueAsString(createStoryDto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/stories/1234")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatUpdateStoryReturnsHttp404IfStoryDoesNotExist() throws Exception {
        CreateStoryDto createStoryDto = CreateStoryDto.builder().image("Updated url").build();
        String json = objectMapper.writeValueAsString(createStoryDto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/stories/1234")
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testThatGettingStoriesFeedForUserReturnsHttp401Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/stories/users/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testThatGettingStoriesFeedForUserReturnsHttp200Ok() throws Exception {
        UUID profileId = UUID.randomUUID();
        mockMvc.perform(MockMvcRequestBuilders.get("/stories/users/" + profileId)
                .header("Authorization", "Bearer " + generateJwt(profileId)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testThatUpdateStoryReturnsHttp200IfSuccessful() throws Exception {
        UUID profileId = UUID.randomUUID();
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + profileId).willReturn(WireMock.ok()));
        ProfileDto dto = getProfileDto(profileId);
        StoryEntity entity = service.createStory(profileId, TestDataUtil.createStoryEntity(), generateJwt(profileId));
        CreateStoryDto createStoryDto = CreateStoryDto.builder().image("Updated url").build();
        String json = objectMapper.writeValueAsString(createStoryDto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/stories/" + entity.getId())
                        .header("Authorization", "Bearer " + generateJwt(profileId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.image").value("Updated url"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.available").value(true));
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> rabbitAdmin.getQueueInfo(RabbitMQConfig.CREATE_STORY_QUEUE).getMessageCount() == 0);
        assertTrue(redisTemplate.hasKey("story-feed:" + SHA256.generate(dto.getId().toString())));
    }

    @Test
    public void testThatUpdateStoryReturnsHttp401Unauthorized() throws Exception {
        UUID profileId = UUID.randomUUID();
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + profileId).willReturn(WireMock.ok()));
        ProfileDto dto = getProfileDto(profileId);
        StoryEntity entity = service.createStory(profileId, TestDataUtil.createStoryEntity(), generateJwt(profileId));
        CreateStoryDto createStoryDto = CreateStoryDto.builder().image("Updated url").build();
        String json = objectMapper.writeValueAsString(createStoryDto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/stories/" + entity.getId())
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> rabbitAdmin.getQueueInfo(RabbitMQConfig.CREATE_STORY_QUEUE).getMessageCount() == 0);
        assertTrue(redisTemplate.hasKey("story-feed:" + SHA256.generate(dto.getId().toString())));
    }

    @Test
    public void testThatDeleteStoryReturnsHttp204NoContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/stories/1234")
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testThatDeleteStoryReturnsHttp401Unauthorized() throws Exception {
        UUID profileId = UUID.randomUUID();
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + profileId).willReturn(WireMock.ok()));
        ProfileDto dto = getProfileDto(profileId);
        StoryEntity entity = service.createStory(profileId, TestDataUtil.createStoryEntity(), generateJwt(profileId));
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> rabbitAdmin.getQueueInfo(RabbitMQConfig.CREATE_STORY_QUEUE).getMessageCount() == 0);
        mockMvc.perform(MockMvcRequestBuilders.delete("/stories/" + entity.getId())
                .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        assertTrue(redisTemplate.hasKey("story-feed:" + SHA256.generate(dto.getId().toString())));
    }

    private @NotNull ProfileDto getProfileDto(UUID profileId) throws JsonProcessingException {
        Pageable pageable = PageRequest.of(0, 1);
        ProfileDto dto = TestDataUtil.createProfileDto();
        Page<ProfileDto> stubBody = new PageImpl<>(List.of(dto), pageable, 1);
        PROFILE_SERVICE.stubFor(WireMock.get(WireMock.urlMatching("/profiles/" + profileId + "/followees.*"))
                .willReturn(WireMock.ok(objectMapper.writeValueAsString(stubBody))
                        .withHeader("Content-Type", "application/json")));
        return dto;
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
