package com.olelllka.profile_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.olelllka.profile_service.TestDataUtil;
import com.olelllka.profile_service.configuration.RabbitMQConfig;
import com.olelllka.profile_service.domain.dto.CreateProfileDto;
import com.olelllka.profile_service.domain.dto.PatchProfileDto;
import com.olelllka.profile_service.domain.dto.ProfileDto;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.repository.ProfileDocumentRepository;
import com.olelllka.profile_service.service.ProfileService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
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
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class ProfileControllerIntegrationTest {

    @ServiceConnection
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>(DockerImageName.parse("neo4j:5.26.0"));

    @ServiceConnection
    static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:7.17.23"));

    @ServiceConnection
    static RabbitMQContainer rabbitContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"));

    static {
        neo4j.start();
        elasticsearchContainer.start();
        rabbitContainer.start();
    }

    @AfterAll
    static void tearDown() {
        neo4j.stop();
        neo4j.close();
        elasticsearchContainer.stop();
        elasticsearchContainer.close();
        rabbitContainer.stop();
        rabbitContainer.close();
    }

    private MockMvc mockMvc;
    private ProfileService profileService;
    private ProfileDocumentRepository documentRepository;
    private RabbitAdmin rabbitAdmin;
    private RabbitListenerEndpointRegistry registry;
    private ObjectMapper objectMapper;

    @Autowired
    public ProfileControllerIntegrationTest(MockMvc mockMvc,
                                            ProfileService profileService,
                                            ProfileDocumentRepository documentRepository,
                                            RabbitAdmin rabbitAdmin,
                                            RabbitListenerEndpointRegistry registry) {
        this.mockMvc = mockMvc;
        this.profileService = profileService;
        this.documentRepository = documentRepository;
        this.objectMapper = new ObjectMapper();
        this.rabbitAdmin = rabbitAdmin;
        this.registry = registry;
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
        registry.stop();
        CreateProfileDto dto = TestDataUtil.createNewCreateProfileDto();
        String json = objectMapper.writeValueAsString(dto);
        registry.getListenerContainer("cu-profile").start();
        String result = mockMvc.perform(MockMvcRequestBuilders.post("/profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value(dto.getUsername()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();
        ProfileDto resultDto = objectMapper.readValue(result, ProfileDto.class);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> rabbitAdmin.getQueueInfo(RabbitMQConfig.create_update_queue).getMessageCount() == 0);
        assertTrue(documentRepository.existsById(resultDto.getId()));
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
        registry.stop();
        ProfileEntity profile = profileService.createProfile(TestDataUtil.createNewProfileEntity());
        PatchProfileDto patchProfileDto = TestDataUtil.createPatchProfileDto();
        patchProfileDto.setName("UPDATED NAME");
        String json = objectMapper.writeValueAsString(patchProfileDto);
        registry.getListenerContainer("cu-profile").start();
        String result = mockMvc.perform(MockMvcRequestBuilders.patch("/profiles/" + profile.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("UPDATED NAME"))
                .andReturn().getResponse().getContentAsString();
        ProfileDto resultDto = objectMapper.readValue(result, ProfileDto.class);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> rabbitAdmin.getQueueInfo(RabbitMQConfig.create_update_queue).getMessageCount() == 0);
        assertEquals(documentRepository.findById(resultDto.getId()).get().getName(), patchProfileDto.getName());
    }

    @Test
    public void testThatDeleteProfileByIdReturnsHttp204NoContent() throws Exception {
        registry.stop();
        registry.getListenerContainer("cu-profile").start();
        ProfileEntity profile = profileService.createProfile(TestDataUtil.createNewProfileEntity());
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> rabbitAdmin.getQueueInfo(RabbitMQConfig.create_update_queue).getMessageCount() == 0);
        assertTrue(documentRepository.existsById(profile.getId()));
        registry.getListenerContainer("d-profile").start();
        mockMvc.perform(MockMvcRequestBuilders.delete("/profiles/" + profile.getId()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> rabbitAdmin.getQueueInfo(RabbitMQConfig.delete_queue_elastic).getMessageCount() == 0);
        assertFalse(documentRepository.existsById(profile.getId()));
        assertTrue(rabbitAdmin.getQueueInfo(RabbitMQConfig.delete_queue_post).getMessageCount() == 1);
        assertTrue(rabbitAdmin.getQueueInfo(RabbitMQConfig.delete_queue_feed).getMessageCount() == 1);
        assertTrue(rabbitAdmin.getQueueInfo(RabbitMQConfig.delete_queue_story).getMessageCount() == 1);
        assertTrue(rabbitAdmin.getQueueInfo(RabbitMQConfig.delete_queue_notification).getMessageCount() == 1);
    }

    @Test
    public void testThatFollowProfileReturnsHttp400BadRequestIfTheSameIds() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(MockMvcRequestBuilders.post("/profiles/" + id + "/follow?user=" + id))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatFollowProfileReturnsHttp400BadRequestIfAlreadyFollowed() throws Exception {
        rabbitAdmin.purgeQueue(RabbitMQConfig.notification_queue);
        ProfileEntity profile1 = TestDataUtil.createNewProfileEntity();
        profileService.createProfile(profile1);
        ProfileEntity profile2 = TestDataUtil.createNewProfileEntity();
        profileService.createProfile(profile2);
        profileService.followNewProfile(profile1.getId(), profile2.getId());
        mockMvc.perform(MockMvcRequestBuilders.post("/profiles/" + profile1.getId() + "/follow?user=" + profile2.getId()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatFollowProfileReturnsHttp200OkIfSuccessful() throws Exception {
        registry.stop();
        rabbitAdmin.purgeQueue(RabbitMQConfig.notification_queue);
        ProfileEntity profile1 = TestDataUtil.createNewProfileEntity();
        profileService.createProfile(profile1);
        ProfileEntity profile2 = TestDataUtil.createNewProfileEntity();
        profileService.createProfile(profile2);
        mockMvc.perform(MockMvcRequestBuilders.post("/profiles/" + profile1.getId() + "/follow?user=" + profile2.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk());
        assertEquals(1, rabbitAdmin.getQueueInfo(RabbitMQConfig.notification_queue).getMessageCount());
    }

    @Test
    public void testThatUnfollowProfileReturnsHttp400BadRequestIfTheSameIds() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(MockMvcRequestBuilders.delete("/profiles/" + id + "/unfollow?user=" + id))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatUnfollowProfileReturnsHttp400BadRequestIfAlreadyFollowed() throws Exception {
        ProfileEntity profile1 = TestDataUtil.createNewProfileEntity();
        profileService.createProfile(profile1);
        ProfileEntity profile2 = TestDataUtil.createNewProfileEntity();
        profileService.createProfile(profile2);
        mockMvc.perform(MockMvcRequestBuilders.delete("/profiles/" + profile1.getId() + "/unfollow?user=" + profile2.getId()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatUnfollowProfileReturnsHttp200OktIfSuccessful() throws Exception {
        ProfileEntity profile1 = TestDataUtil.createNewProfileEntity();
        profileService.createProfile(profile1);
        ProfileEntity profile2 = TestDataUtil.createNewProfileEntity();
        profileService.createProfile(profile2);
        profileService.followNewProfile(profile1.getId(), profile2.getId());
        mockMvc.perform(MockMvcRequestBuilders.delete("/profiles/" + profile1.getId() + "/unfollow?user=" + profile2.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testThatGetFollowersByIdReturnsHttp200Ok() throws Exception {
        ProfileEntity profile1 = TestDataUtil.createNewProfileEntity();
        profile1.setUsername("TEST");
        profileService.createProfile(profile1);
        ProfileEntity profile2 = TestDataUtil.createNewProfileEntity();
        profileService.createProfile(profile2);
        profileService.followNewProfile(profile1.getId(), profile2.getId());
        mockMvc.perform(MockMvcRequestBuilders.get("/profiles/" + profile2.getId() + "/followers"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].username").value("TEST"));
    }

    @Test
    public void testThatGetFolloweesByIdReturnsHttp200Ok() throws Exception {
        ProfileEntity profile1 = TestDataUtil.createNewProfileEntity();
        profileService.createProfile(profile1);
        ProfileEntity profile2 = TestDataUtil.createNewProfileEntity();
        profile2.setUsername("TEST");
        profileService.createProfile(profile2);
        profileService.followNewProfile(profile1.getId(), profile2.getId());
        mockMvc.perform(MockMvcRequestBuilders.get("/profiles/" + profile1.getId() + "/followees"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].username").value("TEST"));
    }

    @Test
    public void testThatSearchProfilesByElasticSearchReturnsHttp200Ok() throws Exception {
        registry.stop();
        ProfileEntity profile = TestDataUtil.createNewProfileEntity();
        registry.getListenerContainer("cu-profile").start();
        profileService.createProfile(profile);
        mockMvc.perform(MockMvcRequestBuilders.get("/profiles?search=F"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].username").value(profile.getUsername()));
    }

    @Test
    public void testThatSearchProfilesByNeo4jReturnsHttp200OkIfElasticsearchIsDown() throws Exception {
        registry.stop();
        ProfileEntity profile = TestDataUtil.createNewProfileEntity();
        registry.getListenerContainer("cu-profile").start();
        profileService.createProfile(profile);
        elasticsearchContainer.stop();
        mockMvc.perform(MockMvcRequestBuilders.get("/profiles?search=username"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].username").value(profile.getUsername()));
    }

}
