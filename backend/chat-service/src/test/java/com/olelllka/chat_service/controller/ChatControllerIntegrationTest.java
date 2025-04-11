package com.olelllka.chat_service.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.olelllka.chat_service.TestDataUtil;
import com.olelllka.chat_service.domain.dto.MessageDto;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.repository.MessageRepository;
import com.olelllka.chat_service.service.ChatService;
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
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;


@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class ChatControllerIntegrationTest {

    @RegisterExtension
    static WireMockExtension PROFILE_SERVICE = WireMockExtension.newInstance()
            .options(WireMockConfiguration.options().port(8001)).build();

    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:8.0");
    private String key = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    static {
        mongo.start();
    }

    @AfterAll
    static void tearDown() {
        mongo.stop();
        mongo.close();
    }

    private ChatService chatService;
    private MessageRepository repository;
    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    @Autowired
    public ChatControllerIntegrationTest(ChatService chatService,
                                         MessageRepository repository,
                                         MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        this.chatService = chatService;
        this.repository = repository;
        this.objectMapper = new ObjectMapper();
    }

    @Test
    public void testThatGetChatsByUserReturnsHttp200Ok() throws Exception {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + user1).willReturn(WireMock.ok()));
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + user2).willReturn(WireMock.ok()));
        ChatEntity saved = chatService.createNewChat(user1, user2);
        mockMvc.perform(MockMvcRequestBuilders.get("/chats/users/" + saved.getParticipants()[0])
                        .header("Authorization", "Bearer " + generateJwt(user1)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0]").exists());
    }

    @Test
    public void testThatGetChatsByUserReturnsHttp401Unauthorized() throws Exception {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + user1).willReturn(WireMock.ok()));
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + user2).willReturn(WireMock.ok()));
        ChatEntity saved = chatService.createNewChat(user1, user2);
        mockMvc.perform(MockMvcRequestBuilders.get("/chats/users/" + saved.getParticipants()[0])
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testThatDeleteChatWorks() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/chats/12345")
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testThatDeleteChatReturnsHttp401Unauthorized() throws Exception {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + user1).willReturn(WireMock.ok()));
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + user2).willReturn(WireMock.ok()));
        ChatEntity saved = chatService.createNewChat(user1, user2);
        mockMvc.perform(MockMvcRequestBuilders.delete("/chats/" + saved.getId())
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testThatDeleteChatReturnsHttp200Ok() throws Exception {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + user1).willReturn(WireMock.ok()));
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + user2).willReturn(WireMock.ok()));
        ChatEntity saved = chatService.createNewChat(user1, user2);
        mockMvc.perform(MockMvcRequestBuilders.delete("/chats/" + saved.getId())
                        .header("Authorization", "Bearer " + generateJwt(user1)))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testThatGetMessagesByChatIdReturnsHttp401Unauthorized() throws Exception {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + user1).willReturn(WireMock.ok()));
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + user2).willReturn(WireMock.ok()));
        ChatEntity chat = chatService.createNewChat(user1, user2);
        MessageEntity msg = TestDataUtil.createMessageEntity(chat.getId());
        repository.save(msg);
        mockMvc.perform(MockMvcRequestBuilders.get("/chats/" + chat.getId() + "/messages")
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testThatGetMessagesByChatIdReturnsPageOfMessages() throws Exception {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + user1).willReturn(WireMock.ok()));
        PROFILE_SERVICE.stubFor(WireMock.get("/profiles/" + user2).willReturn(WireMock.ok()));
        ChatEntity chat = chatService.createNewChat(user1, user2);
        mockMvc.perform(MockMvcRequestBuilders.get("/chats/" + chat.getId() + "/messages")
                        .header("Authorization", "Bearer " + generateJwt(user1)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(0));
    }

    @Test
    public void testThatUpdateSpecificMessageReturnsHttp400BadRequest() throws Exception {
        MessageDto dto = TestDataUtil.createMessageDto("123456");
        dto.setContent("");
        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/chats/messages/1234")
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatUpdateSpecificMessageReturnsHttp404IfMessageDoesNotExist() throws Exception {
        MessageDto dto = TestDataUtil.createMessageDto("12345");
        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/chats/messages/12345")
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testThatUpdateSpecificMessageReturnsHttp401Unauthorized() throws Exception {
        MessageDto dto = TestDataUtil.createMessageDto("123456");
        dto.setContent("UPDATED");
        MessageEntity original = repository.save(TestDataUtil.createMessageEntity("123456"));
        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/chats/messages/" + original.getId())
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testThatUpdateSpecificMessageReturnsHttp200OkAndUpdatesMessage() throws Exception {
        MessageDto dto = TestDataUtil.createMessageDto("123456");
        dto.setContent("UPDATED");
        MessageEntity original = repository.save(TestDataUtil.createMessageEntity("123456"));
        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/chats/messages/" + original.getId())
                        .header("Authorization", "Bearer " + generateJwt(original.getFrom()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").value("UPDATED"));
    }

    @Test
    public void testThatDeleteSpecificMessageReturnsHttp204NoContentIfThereIsNoMessage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/chats/messages/12345")
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testThatDeleteSpecificMessageReturnsHttp401UnauthorizedIfUnauthorized() throws Exception {
        MessageEntity msg = repository.save(TestDataUtil.createMessageEntity("123456"));
        mockMvc.perform(MockMvcRequestBuilders.delete("/chats/messages/" + msg.getId())
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testThatDeleteSpecificMessageReturnsHttp204NoContentIfMessageExistsAndAuthorized() throws Exception {
        MessageEntity msg = repository.save(TestDataUtil.createMessageEntity("123456"));
        mockMvc.perform(MockMvcRequestBuilders.delete("/chats/messages/" + msg.getId())
                        .header("Authorization", "Bearer " + generateJwt(msg.getFrom())))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
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
