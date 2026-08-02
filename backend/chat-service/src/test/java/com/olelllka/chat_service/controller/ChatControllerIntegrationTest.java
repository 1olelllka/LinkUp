package com.olelllka.chat_service.controller;

import com.olelllka.chat_service.TestDataUtil;
import com.olelllka.chat_service.domain.dto.MessageDto;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.repository.MessageRepository;
import com.olelllka.chat_service.repository.ProfileDocumentRepository;
import com.olelllka.chat_service.service.ChatService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;


@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
class ChatControllerIntegrationTest {

    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:8.0");

    @Value("${JWT_SECRET:0d9aa86975f076cbb84ab112f361a4b254c6f553d41da0918b439300e592ed3f}")
    private String key;

    static {
        mongo.start();
    }

    @AfterAll
    static void tearDown() {
        mongo.stop();
        mongo.close();
    }

    private ChatService chatService;
    @MockitoBean
    private ProfileDocumentRepository documentRepository;
    private ChatRepository chatRepository;
    private MessageRepository repository;
    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    @Autowired
    public ChatControllerIntegrationTest(ChatService chatService,
                                         MessageRepository repository,
                                         ChatRepository chatRepository,
                                         MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        this.chatService = chatService;
        this.repository = repository;
        this.chatRepository = chatRepository;
        this.objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearingDown() {
        chatRepository.deleteAll();
    }

    @Test
    void testThatGetChatsByUserReturnsHttp200Ok() throws Exception {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        when(documentRepository.findById(user1)).thenReturn(Optional.of(TestDataUtil.createProfileDocument(user1)));
        when(documentRepository.findById(user2)).thenReturn(Optional.of(TestDataUtil.createProfileDocument(user2)));
        ChatEntity saved = chatService.createNewChat(user1, user2);
        mockMvc.perform(MockMvcRequestBuilders.get("/chats/users/" + saved.getParticipants()[0].getId())
                        .header("Authorization", "Bearer " + generateJwt(user1)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0]").exists());
    }

    @Test
    void testThatGetChatByTwoUsersReturnsHttp404NotFound() throws Exception {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        when(documentRepository.findById(user1)).thenReturn(Optional.of(TestDataUtil.createProfileDocument(user1)));
        when(documentRepository.findById(user2)).thenReturn(Optional.of(TestDataUtil.createProfileDocument(user2)));
        ChatEntity saved = chatService.createNewChat(user1, user2);
        mockMvc.perform(MockMvcRequestBuilders.get("/chats?user1=" + saved.getParticipants()[0].getId() + "&user2=" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + generateJwt(user1)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testThatGetChatByTwoUsersReturnsHttp401UnauthorizedIfTokenInvalid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/chats?user1=" + UUID.randomUUID() + "&user2=" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testThatGetChatByTwoUsersReturnsHttp200Ok() throws Exception {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        when(documentRepository.findById(user1)).thenReturn(Optional.of(TestDataUtil.createProfileDocument(user1)));
        when(documentRepository.findById(user2)).thenReturn(Optional.of(TestDataUtil.createProfileDocument(user2)));
        chatService.createNewChat(user1, user2);
        mockMvc.perform(MockMvcRequestBuilders.get("/chats?user1=" + user1 + "&user2=" + user2)
                        .header("Authorization", "Bearer " + generateJwt(user1)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }

    @Test
    void testThatGetChatsByUserReturnsHttp401Unauthorized() throws Exception {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        when(documentRepository.findById(user1)).thenReturn(Optional.of(TestDataUtil.createProfileDocument(user1)));
        when(documentRepository.findById(user2)).thenReturn(Optional.of(TestDataUtil.createProfileDocument(user2)));
        ChatEntity saved = chatService.createNewChat(user1, user2);
        mockMvc.perform(MockMvcRequestBuilders.get("/chats/users/" + saved.getParticipants()[0].getId())
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void testThatDeleteChatWorks() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/chats/12345")
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void testThatDeleteChatReturnsHttp401Unauthorized() throws Exception {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        when(documentRepository.findById(user1)).thenReturn(Optional.of(TestDataUtil.createProfileDocument(user1)));
        when(documentRepository.findById(user2)).thenReturn(Optional.of(TestDataUtil.createProfileDocument(user2)));
        ChatEntity saved = chatService.createNewChat(user1, user2);
        mockMvc.perform(MockMvcRequestBuilders.delete("/chats/" + saved.getId())
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void testThatDeleteChatReturnsHttp204NoContent() throws Exception {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        when(documentRepository.findById(user1)).thenReturn(Optional.of(TestDataUtil.createProfileDocument(user1)));
        when(documentRepository.findById(user2)).thenReturn(Optional.of(TestDataUtil.createProfileDocument(user2)));
        ChatEntity saved = chatService.createNewChat(user1, user2);
        mockMvc.perform(MockMvcRequestBuilders.delete("/chats/" + saved.getId())
                        .header("Authorization", "Bearer " + generateJwt(user1)))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testThatGetMessagesByChatIdReturnsHttp401Unauthorized() throws Exception {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        when(documentRepository.findById(user1)).thenReturn(Optional.of(TestDataUtil.createProfileDocument(user1)));
        when(documentRepository.findById(user2)).thenReturn(Optional.of(TestDataUtil.createProfileDocument(user2)));
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
        when(documentRepository.findById(user1)).thenReturn(Optional.of(TestDataUtil.createProfileDocument(user1)));
        when(documentRepository.findById(user2)).thenReturn(Optional.of(TestDataUtil.createProfileDocument(user2)));
        ChatEntity chat = chatService.createNewChat(user1, user2);
        mockMvc.perform(MockMvcRequestBuilders.get("/chats/" + chat.getId() + "/messages")
                        .header("Authorization", "Bearer " + generateJwt(user1)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(0));
    }

    @Test
    void testThatUpdateSpecificMessageReturnsHttp400BadRequest() throws Exception {
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
    void testThatUpdateSpecificMessageReturnsHttp404IfMessageDoesNotExist() throws Exception {
        MessageDto dto = TestDataUtil.createMessageDto("12345");
        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/chats/messages/12345")
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testThatUpdateSpecificMessageReturnsHttp401Unauthorized() throws Exception {
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
    void testThatUpdateSpecificMessageReturnsHttp200OkAndUpdatesMessage() throws Exception {
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
    void testThatDeleteSpecificMessageReturnsHttp204NoContentIfThereIsNoMessage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/chats/messages/12345")
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void testThatDeleteSpecificMessageReturnsHttp401UnauthorizedIfUnauthorized() throws Exception {
        MessageEntity msg = repository.save(TestDataUtil.createMessageEntity("123456"));
        mockMvc.perform(MockMvcRequestBuilders.delete("/chats/messages/" + msg.getId())
                        .header("Authorization", "Bearer " + generateJwt(UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void testThatDeleteSpecificMessageReturnsHttp204NoContentIfMessageExistsAndAuthorized() throws Exception {
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
