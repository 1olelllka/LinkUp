package com.olelllka.stories_service.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.olelllka.stories_service.TestDataUtil;
import com.olelllka.stories_service.domain.dto.CreateStoryDto;
import com.olelllka.stories_service.domain.entity.StoryEntity;
import com.olelllka.stories_service.service.SHA256;
import com.olelllka.stories_service.service.StoryService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class StoryControllerIntegrationTests {

    @RegisterExtension
    static WireMockExtension PROFILE_SERVICE = WireMockExtension.newInstance()
            .options(WireMockConfiguration.options().port(8001)).build();

    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0");
    @ServiceConnection
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2.6")).withExposedPorts(6379);

    private MockMvc mockMvc;
    private StoryService service;
    private ObjectMapper objectMapper;
    private RedisTemplate<String, String> redisTemplate;
    private String key = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    @Autowired
    public StoryControllerIntegrationTests(MockMvc mockMvc, StoryService service, RedisTemplate<String, String> redisTemplate) {
        this.mockMvc = mockMvc;
        this.service = service;
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    static {
        mongoDBContainer.start();
        redisContainer.start();
    }

    @AfterAll
    static void tearDown() {
        mongoDBContainer.stop();
        mongoDBContainer.close();
        redisContainer.stop();
        redisContainer.close();
    }

    @Test
    public void testThatGetAllStoriesForUserReturnsHttp401Unauthorized() throws Exception {
        UUID profileId = UUID.randomUUID();
        mockMvc.perform(MockMvcRequestBuilders.get("/stories/users/" + profileId)
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testThatGetAllStoriesForUserReturnsHttp200Ok() throws Exception {
        UUID profileId = UUID.randomUUID();
        mockMvc.perform(MockMvcRequestBuilders.get("/stories/users/" + profileId)
                .header("Authorization", "Bearer " + generateJwt(profileId)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testThatGetSpecificStoryReturnsHttp404NotFoundIfDoesNotExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/stories/1234")
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testThatGetSpecificStoryForOthersReturnsHttp200OkIfExistsAndAvailableAndThenCacheWorks() throws Exception {
        UUID profileId = UUID.randomUUID();
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + profileId).willReturn(WireMock.ok()));
        StoryEntity story = service.createStory(profileId, TestDataUtil.createStoryEntity(), generateJwt(profileId));
        mockMvc.perform(MockMvcRequestBuilders.get("/stories/" + story.getId())
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isOk());
        assertTrue(redisTemplate.hasKey("story::"+SHA256.generate(story.getId())));
    }

    @Test
    public void testThatGetSpecificStoryForOwnerReturnsHttp200OkIfExistsAndThenCacheWorks() throws Exception {
        UUID profileId = UUID.randomUUID();
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + profileId).willReturn(WireMock.ok()));
        StoryEntity story = service.createStory(profileId, TestDataUtil.createStoryEntity(), generateJwt(profileId));
        mockMvc.perform(MockMvcRequestBuilders.get("/stories/" + story.getId())
                        .header("Authorization", "Bearer " + generateJwt(profileId)))
                .andExpect(MockMvcResultMatchers.status().isOk());
        assertTrue(redisTemplate.hasKey("story::"+SHA256.generate(story.getId())));
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
    }

    @Test
    public void testThatCreateStoryForUserReturnsHttp201CreatedIfSuccessful() throws Exception {
        UUID profileId = UUID.randomUUID();
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + profileId).willReturn(WireMock.ok()));
        CreateStoryDto createStoryDto = CreateStoryDto.builder().image("New Image url").build();
        String json = objectMapper.writeValueAsString(createStoryDto);
        mockMvc.perform(MockMvcRequestBuilders.post("/stories/users/" + profileId)
                        .header("Authorization", "Bearer " + generateJwt(profileId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.available").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.image").value("New Image url"));
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
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.available").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.image").value("circuit-breaker.url"));
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
    public void testThatUpdateStoryReturnsHttp200IfSuccessful() throws Exception {
        UUID profileId = UUID.randomUUID();
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + profileId).willReturn(WireMock.ok()));
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
        assertTrue(redisTemplate.hasKey("story::"+SHA256.generate(entity.getId())));
    }

    @Test
    public void testThatUpdateStoryReturnsHttp401Unauthorized() throws Exception {
        UUID profileId = UUID.randomUUID();
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + profileId).willReturn(WireMock.ok()));
        StoryEntity entity = service.createStory(profileId, TestDataUtil.createStoryEntity(), generateJwt(profileId));
        CreateStoryDto createStoryDto = CreateStoryDto.builder().image("Updated url").build();
        String json = objectMapper.writeValueAsString(createStoryDto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/stories/" + entity.getId())
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testThatDeleteStoryReturnsHttp204NoContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/stories/1234")
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        assertFalse(redisTemplate.hasKey("story::"+SHA256.generate("1234")));
    }

    @Test
    public void testThatDeleteStoryReturnsHttp401Unauthorized() throws Exception {
        UUID profileId = UUID.randomUUID();
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + profileId).willReturn(WireMock.ok()));
        StoryEntity entity = service.createStory(profileId, TestDataUtil.createStoryEntity(), generateJwt(profileId));
        mockMvc.perform(MockMvcRequestBuilders.delete("/stories/" + entity.getId())
                .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
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
