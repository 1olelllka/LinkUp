package com.olelllka.chat_service.controller;

import com.olelllka.chat_service.TestDataUtil;
import com.olelllka.chat_service.TestcontainersConfiguration;
import com.olelllka.chat_service.domain.dto.MessageDto;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.service.ChatService;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Log
public class ChatControllerIntegrationTest {

    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:8.0");

    static {
        mongo.start();
    }

    private ChatService service;
    private ObjectMapper objectMapper;
    private ChatRepository repository; // temporary
    private MockMvc mockMvc;

    @Autowired
    public ChatControllerIntegrationTest(ChatService chatService,
                                         ChatRepository chatRepository,
                                         MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        this.service = chatService;
        this.repository = chatRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Test
    public void testThatGetChatsByUserReturnsPageOfResults() throws Exception {
        service.createNewChat("1234", "5678");
        mockMvc.perform(MockMvcRequestBuilders.get("/chats/users/1234"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0]").exists());
    }

    @Test
    public void testThatCreateChatWorks() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/chats?userId1=1234&userId2=5678"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.participants[0]").value("1234"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.participants[1]").value("5678"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.messages").isArray());
    }

    @Test
    public void testThatDeleteChatWorks() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/chats/12345"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testThatGetMessagesByChatIdReturnsPageOfMessages() throws Exception {
        ChatEntity chat = service.createNewChat("12345", "67890");
        mockMvc.perform(MockMvcRequestBuilders.get("/chats/" + chat.getId() + "/messages"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(0));
    }

    @Test
    public void testThatUpdateSpecificMessageReturnsHttp400BadRequest() throws Exception {
        MessageDto dto = TestDataUtil.createMessageDto();
        dto.setContent("");
        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/chats/12345/messages/1234")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatUpdateSpecificMessageReturnsHttp404IfMessageDoesNotExist() throws Exception {
        MessageDto dto = TestDataUtil.createMessageDto();
        ChatEntity chat = service.createNewChat("1235", "6789");
        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/chats/" + chat.getId() + "/messages/" + chat.getId()) // chat.getId() just to get hex24 string
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testThatUpdateSpecificMessageReturnsHttp200OkAndUpdatesMessage() throws Exception {
        MessageDto dto = TestDataUtil.createMessageDto();
        dto.setContent("UPDATED");
        ChatEntity chat = service.createNewChat("12345", "13456");
        MessageEntity message = TestDataUtil.createMessageEntity();
        message.setIdIfNotPresent();
        chat.setMessages(List.of(message));
        ChatEntity finalChat = repository.save(chat);
        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/chats/" + finalChat.getId() + "/messages/" + finalChat.getMessages().getFirst().getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").value("UPDATED"));
    }

}
