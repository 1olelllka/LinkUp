package com.olelllka.chat_service.controller;

import com.olelllka.chat_service.TestDataUtil;
import com.olelllka.chat_service.TestcontainersConfiguration;
import com.olelllka.chat_service.domain.dto.CreateChatDto;
import com.olelllka.chat_service.domain.dto.MessageDto;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.repository.MessageRepository;
import com.olelllka.chat_service.service.ChatService;
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


@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class ChatControllerIntegrationTest {

    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:8.0");

    static {
        mongo.start();
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
    public void testThatGetChatsByUserReturnsPageOfResults() throws Exception {
        chatService.createNewChat("1234", "5678");
        mockMvc.perform(MockMvcRequestBuilders.get("/chats/users/1234"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0]").exists());
    }

    @Test
    public void testThatCreateChatReturnsHttp400BadRequestIfValidationFails() throws Exception {
        CreateChatDto createChatDto = CreateChatDto.builder().user1Id("").user2Id("").build();
        String json = objectMapper.writeValueAsString(createChatDto);
        mockMvc.perform(MockMvcRequestBuilders.post("/chats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateChatReturnsHttp201Created() throws Exception {
        CreateChatDto createChatDto = CreateChatDto.builder().user1Id("1235").user2Id("8765").build();
        String json = objectMapper.writeValueAsString(createChatDto);
        mockMvc.perform(MockMvcRequestBuilders.post("/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.participants[0]").value("1235"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.participants[1]").value("8765"));
    }

    @Test
    public void testThatDeleteChatWorks() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/chats/12345"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testThatGetMessagesByChatIdReturnsPageOfMessages() throws Exception {
        ChatEntity chat = chatService.createNewChat("12345", "67890");
        mockMvc.perform(MockMvcRequestBuilders.get("/chats/" + chat.getId() + "/messages"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(0));
    }

    @Test
    public void testThatUpdateSpecificMessageReturnsHttp400BadRequest() throws Exception {
        MessageDto dto = TestDataUtil.createMessageDto("123456");
        dto.setContent("");
        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/chats/messages/1234")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatUpdateSpecificMessageReturnsHttp404IfMessageDoesNotExist() throws Exception {
        MessageDto dto = TestDataUtil.createMessageDto("12345");
        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/chats/messages/12345")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testThatUpdateSpecificMessageReturnsHttp200OkAndUpdatesMessage() throws Exception {
        MessageDto dto = TestDataUtil.createMessageDto("123456");
        dto.setContent("UPDATED");
        MessageEntity original = repository.save(TestDataUtil.createMessageEntity("123456"));
        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/chats/messages/" + original.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").value("UPDATED"));
    }

    @Test
    public void testThatDeleteSpecificMessageReturnsHttp204NoContentAndDeletesMsg() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/chats/messages/12345"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

}
