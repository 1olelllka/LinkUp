package com.olelllka.image_storage.controller;

import com.olelllka.image_storage.TestStorageConfig;
import com.olelllka.image_storage.service.ImageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.UUID;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@Import(TestStorageConfig.class)
public class ImageControllerIntegrationTest {
    private final MockMvc mockMvc;
    private final ImageService imageService;

    @Autowired
    public ImageControllerIntegrationTest(MockMvc mockMvc, ImageService imageService) {
        this.mockMvc = mockMvc;
        this.imageService = imageService;
    }

    @Test
    public void testThatUploadImageReturnsHttp400BadRequestIfImageIsNotPNGOrJPED() throws Exception {
        MockMultipartFile wrongFile = new MockMultipartFile("file", "wrong-file.heic", "image/heic", "content".getBytes());
        mockMvc.perform(MockMvcRequestBuilders.multipart("/upload")
                        .file(wrongFile))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Only PNG and JPEG files are allowed"));
    }

    @Test
    public void testThatUploadImageReturnsHttp200OkIfImageSuccessfullyUploaded() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "correct-file.jpg", "image/jpeg", "jpeg content".getBytes());
        mockMvc.perform(MockMvcRequestBuilders.multipart("/upload")
                        .file(file))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.url").exists());
    }

    @Test
    public void testThatGetImageReturnsHttp404NotFoundIfFileDoesNotExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/images/" + UUID.randomUUID()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testThatGetImageReturnsHttp200OkIfEverythingOkay() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "correct-file.jpg", "image/jpeg", "jpeg content".getBytes());
        String saved = imageService.save(file);
        mockMvc.perform(MockMvcRequestBuilders.get("/images/" + saved))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
