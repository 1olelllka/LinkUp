package com.olelllka.profile_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.olelllka.profile_service.TestDataUtil;
import com.olelllka.profile_service.TestcontainersConfiguration;
import com.olelllka.profile_service.domain.dto.CreateProfileDto;
import com.olelllka.profile_service.domain.dto.PatchProfileDto;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.service.ProfileService;
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
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.utility.DockerImageName;
import java.util.UUID;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class ProfileControllerIntegrationTest {

    @ServiceConnection
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>(DockerImageName.parse("neo4j:5.26.0"));

    static {
        neo4j.start();
    }

    private MockMvc mockMvc;
    private ProfileService profileService;
    private ObjectMapper objectMapper;

    @Autowired
    public ProfileControllerIntegrationTest(MockMvc mockMvc, ProfileService profileService) {
        this.mockMvc = mockMvc;
        this.profileService = profileService;
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testThatCreateNewProfileReturnsHttp400BadRequestIfInvalidData() throws Exception {
        CreateProfileDto dto = TestDataUtil.createNewCreateProfileDto();
        dto.setUsername("");
        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(MockMvcRequestBuilders.post("/profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateNewProfileReturnsHttp201CreatedAndCreatedProfile() throws Exception {
        CreateProfileDto dto = TestDataUtil.createNewCreateProfileDto();
        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(MockMvcRequestBuilders.post("/profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value(dto.getUsername()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }

    @Test
    public void testThatGetProfileByIdReturnsHttp404NotFoundIfProfileDoesNotExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/profiles/" + UUID.randomUUID()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testThatGetProfileReturnsHttp200OkIfProfileExists() throws Exception {
        ProfileEntity profile = profileService.createProfile(TestDataUtil.createNewProfileEntity());
        mockMvc.perform(MockMvcRequestBuilders.get("/profiles/" + profile.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testThatUpdateProfileByIdReturnsHttp400BadRequestIfInvalidData() throws Exception {
        PatchProfileDto patchProfileDto = TestDataUtil.createPatchProfileDto();
        patchProfileDto.setUsername("");
        String json = objectMapper.writeValueAsString(patchProfileDto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/profiles/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatUpdateProfileByIdReturnsHttp404NotFoundIfProfileDoesNotExist() throws Exception {
        PatchProfileDto patchProfileDto = TestDataUtil.createPatchProfileDto();
        String json = objectMapper.writeValueAsString(patchProfileDto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/profiles/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testThatUpdateProfileByIdReturnsHttp200OkAndUpdatedProfile() throws Exception {
        ProfileEntity profile = profileService.createProfile(TestDataUtil.createNewProfileEntity());
        PatchProfileDto patchProfileDto = TestDataUtil.createPatchProfileDto();
        patchProfileDto.setName("UPDATED NAME");
        String json = objectMapper.writeValueAsString(patchProfileDto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/profiles/" + profile.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("UPDATED NAME"));
    }

    @Test
    public void testThatDeleteProfileByIdReturnsHttp204NoContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/profiles/" + UUID.randomUUID()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

}
