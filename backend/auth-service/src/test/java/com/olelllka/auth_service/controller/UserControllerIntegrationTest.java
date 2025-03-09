package com.olelllka.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.olelllka.auth_service.RabbitMQTestConfig;
import com.olelllka.auth_service.TestDataUtil;
import com.olelllka.auth_service.config.RabbitMQConfig;
import com.olelllka.auth_service.domain.dto.JWTToken;
import com.olelllka.auth_service.domain.dto.LoginUser;
import com.olelllka.auth_service.domain.dto.PatchUserDto;
import com.olelllka.auth_service.domain.dto.RegisterUserDto;
import com.olelllka.auth_service.domain.entity.UserEntity;
import com.olelllka.auth_service.repository.UserRepository;
import com.olelllka.auth_service.service.SHA256;
import com.olelllka.auth_service.service.impl.UserServiceImpl;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@Import(RabbitMQTestConfig.class)
public class UserControllerIntegrationTest {

    @ServiceConnection
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"));
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:8.0"));
    @ServiceConnection
    static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:7.2.6"));

    static {
        rabbitMQContainer.start();
        mongoDBContainer.start();
        redisContainer.start();
    }

    @AfterAll
    static void tearDown() {
        rabbitMQContainer.stop();
        rabbitMQContainer.close();
        mongoDBContainer.stop();
        mongoDBContainer.close();
        redisContainer.stop();
        redisContainer.close();
    }

    @AfterEach
    void refreshDB() {
        userRepository.deleteAll();
    }

    private final UserServiceImpl userService;
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final RabbitAdmin admin;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public UserControllerIntegrationTest(UserServiceImpl userService,
                                         UserRepository userRepository,
                                         RabbitAdmin admin,
                                         MockMvc mockMvc,
                                         RedisTemplate<String, String> redisTemplate) {
        this.userService = userService;
        this.mockMvc = mockMvc;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.admin = admin;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }

    @Test
    public void testThatRegisterANewUserReturnsHttp400BadRequestIfValidationFails() throws Exception {
        RegisterUserDto dto = TestDataUtil.createRegisterUserDto();
        dto.setPassword("");
        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                .contentType("application/json")
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatRegisterANewUserReturnsHttp409ConflictIfDuplicateExists() throws Exception {
        RegisterUserDto dto = TestDataUtil.createRegisterUserDto();
        userService.registerUser(dto);
        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType("application/json")
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    public void testThatRegisterANewUserReturnsHttp201CreatedIfSuccessful() throws Exception {
        RegisterUserDto dto = TestDataUtil.createRegisterUserDto();
        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType("application/json")
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isCreated());
        assertTrue(admin.getQueueInfo(RabbitMQConfig.create_user_queue).getMessageCount() > 0);
    }

    @Test
    public void testThatLoginUserReturnsHttp400BadRequestIfValidationFails() throws Exception {
        LoginUser loginUser = TestDataUtil.createLoginUser();
        loginUser.setEmail("");
        String json = objectMapper.writeValueAsString(loginUser);
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType("application/json")
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatLoginUserReturnsHttp403ForbiddenIfPasswordIsIncorrect() throws Exception {
        LoginUser loginUser = TestDataUtil.createLoginUser();
        loginUser.setPassword("incorrectPassword");
        RegisterUserDto dto = TestDataUtil.createRegisterUserDto();
        userService.registerUser(dto);
        String json = objectMapper.writeValueAsString(loginUser);
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType("application/json")
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void testThatLoginUserReturnsHttp200OkIfSuccessful() throws Exception {
        LoginUser loginUser = TestDataUtil.createLoginUser();
        RegisterUserDto dto = TestDataUtil.createRegisterUserDto();
        userService.registerUser(dto);
        String json = objectMapper.writeValueAsString(loginUser);
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType("application/json")
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists());
    }

    @Test
    public void testThatGetUserByJwtReturnsHttp200OkAndCacheWorks() throws Exception {
        LoginUser loginUser = TestDataUtil.createLoginUser();
        RegisterUserDto dto = TestDataUtil.createRegisterUserDto();
        userService.registerUser(dto);
        String json = objectMapper.writeValueAsString(loginUser);
        String content = mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType("application/json")
                        .content(json))
                .andReturn().getResponse().getContentAsString();
        JWTToken jwtToken = objectMapper.readValue(content, JWTToken.class);
        mockMvc.perform(MockMvcRequestBuilders.get("/auth/me")
                .header("Authorization", "Bearer " + jwtToken.getToken()))
                .andExpect(MockMvcResultMatchers.status().isOk());
        assertTrue(redisTemplate.hasKey("auth::" + SHA256.hash(loginUser.getEmail())));
    }

    @Test
    public void testThatPatchUserReturnsHttp200Ok() throws Exception {
        LoginUser loginUser = TestDataUtil.createLoginUser();
        RegisterUserDto dto = TestDataUtil.createRegisterUserDto();
        userService.registerUser(dto);
        String json = objectMapper.writeValueAsString(loginUser);
        String content = mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType("application/json")
                        .content(json))
                .andReturn().getResponse().getContentAsString();
        JWTToken jwtToken = objectMapper.readValue(content, JWTToken.class);
        PatchUserDto patchUserDto = PatchUserDto.builder().email("newemail@email.com").build();
        String patchJson = objectMapper.writeValueAsString(patchUserDto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/auth/me")
                .header("Authorization", "Bearer " + jwtToken.getToken())
                .contentType("application/json")
                .content(patchJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("newemail@email.com"));
        assertEquals(admin.getQueueInfo(RabbitMQConfig.update_user_queue).getMessageCount(), 1);
        assertTrue(redisTemplate.hasKey("auth::" + SHA256.hash(loginUser.getEmail())));
    }

    @Test
    public void testThatLogoutUserReturnsHttp200OkAndLogsOut() throws Exception {
        LoginUser loginUser = TestDataUtil.createLoginUser();
        RegisterUserDto dto = TestDataUtil.createRegisterUserDto();
        userService.registerUser(dto);
        String json = objectMapper.writeValueAsString(loginUser);
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType("application/json")
                        .content(json)).andReturn().getResponse().getContentAsString();
        JWTToken token = objectMapper.readValue(response, JWTToken.class);
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/logout")
                .header("Authorization", "Bearer " + token.getToken()))
                .andExpect(MockMvcResultMatchers.status().isOk());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

}
