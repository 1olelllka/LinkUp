package com.olelllka.image_storage;

import com.olelllka.image_storage.exceptions.InvalidTypeException;
import com.olelllka.image_storage.exceptions.NotFoundException;
import com.olelllka.image_storage.service.ImageService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

@TestConfiguration
public class TestStorageConfig {
    @Bean
    @Primary
    public ImageService testImageStorageService() {
        return new ImageService() {
            @Override
            public String save(MultipartFile file) {
                if (!file.getContentType().equals("image/png") && !file.getContentType().equals("image/jpeg")) {
                    throw new InvalidTypeException("Only PNG and JPEG files are allowed");
                }
                return "test-" + file.getOriginalFilename();
            }

            @Override
            public Resource getResource(String img) {
                if (!img.equals("test-correct-file.jpg")) {
                    throw new NotFoundException("Image was not found.");
                }
                return null;
            }
        };
    }
}