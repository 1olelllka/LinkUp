package com.olelllka.feed_service.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.olelllka.feed_service.TestDataUtil;
import com.olelllka.feed_service.domain.dto.PostDto;
import com.olelllka.feed_service.service.SHA256;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class FeedControllerIntegrationTest {

    @RegisterExtension
    static WireMockExtension POSTS_SERVICE = WireMockExtension.newInstance()
            .options(WireMockConfiguration.options().port(8000)).build();

    @ServiceConnection
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2.6")).withExposedPorts(6379);

    static {
        redisContainer.start();
    }

    @AfterAll
    static void tearDown() {
        redisContainer.stop();
        redisContainer.close();
    }

    private final MockMvc mockMvc;
    private final RedisTemplate<String, String> redisTemplate;
    private String key = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    @Autowired
    public FeedControllerIntegrationTest(MockMvc mockMvc,
                                         RedisTemplate<String, String> redisTemplate) {
        this.mockMvc = mockMvc;
        this.redisTemplate = redisTemplate;
    }

    @Test
    public void testThatGetFeedForSpecificUserReturnsHttp200OkAndSomeMockedData() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        UUID profileId = UUID.randomUUID();
        PostDto postDto = TestDataUtil.createPostDto(UUID.randomUUID());
        String jsonPost = objectMapper.writeValueAsString(postDto);
        redisTemplate.opsForList().leftPush("feed:profile:"+ SHA256.hash(profileId.toString()), "1");
        POSTS_SERVICE.stubFor(WireMock.get("/posts/1")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonPost)));

        mockMvc.perform(MockMvcRequestBuilders.get("/feeds/" + profileId)
                        .header("Authorization", "Bearer " + generateJwt(profileId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].image").value("img"));
    }

    @Test
    public void testWhenGetFeedForSpecificUserReturnsActivatesCircuitBreaker() throws Exception {
        UUID profileId = UUID.randomUUID();
        redisTemplate.opsForList().leftPush("feed:profile:"+SHA256.hash(profileId.toString()), "123");
        POSTS_SERVICE.stubFor(WireMock.get("/posts/123").willReturn(WireMock.serverError()));
        mockMvc.perform(MockMvcRequestBuilders.get("/feeds/" + profileId)
                        .header("Authorization", "Bearer " + generateJwt(profileId)))
                .andExpect(MockMvcResultMatchers.status().isServiceUnavailable());
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
