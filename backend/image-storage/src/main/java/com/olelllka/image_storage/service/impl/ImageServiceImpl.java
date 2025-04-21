package com.olelllka.image_storage.service.impl;

import com.olelllka.image_storage.exceptions.InvalidTypeException;
import com.olelllka.image_storage.exceptions.NotFoundException;
import com.olelllka.image_storage.service.ImageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static com.olelllka.image_storage.controller.ImageController.UPLOAD_DIR;

@Service
public class ImageServiceImpl implements ImageService {

    @Override
    public String save(MultipartFile file) throws IOException {
        if (!file.getContentType().equals("image/png") && !file.getContentType().equals("image/jpeg")) {
            throw new InvalidTypeException("Only PNG and JPEG files are allowed");
        }
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR + fileName);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, file.getBytes());

        // npm localtunnel should be installed globally!!!!
        // Given you wrote this command: `npx localtunnel --port 8888 --subdomain linkup`
        return "https://linkup.loca.lt/images/" + fileName;
    }

    @Override
    public Resource getResource(String img) {
        Path filePath = Paths.get(UPLOAD_DIR + img);
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            throw new NotFoundException("Image was not found.");
        }
        return resource;
    }
}
