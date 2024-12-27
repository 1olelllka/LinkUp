package com.olelllka.chat_service.controller;

import com.olelllka.chat_service.TestcontainersConfiguration;
import com.olelllka.chat_service.service.ChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
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

    private ChatService service;
    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    @Autowired
    public ChatControllerIntegrationTest(ChatService chatService,
                                         MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        this.service = chatService;
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

}
