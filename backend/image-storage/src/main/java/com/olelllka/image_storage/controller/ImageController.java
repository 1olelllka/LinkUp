package com.olelllka.image_storage.controller;

import lombok.extern.java.Log;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@Log
public class ImageController {

    private static final String UPLOAD_DIR = "./uploads/";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (!file.getContentType().equals("image/png") && !file.getContentType().equals("image/jpeg")) {
            return new ResponseEntity<>("Only PNG and JPG files are allowed", HttpStatus.BAD_REQUEST);
        }
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR + fileName);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, file.getBytes());

        String url = "https://cdn.linkup.com/images/" + fileName;
        return new ResponseEntity<>(url, HttpStatus.OK);
    }

    @GetMapping("/images/{img}")
    public ResponseEntity<Resource> getImage(@PathVariable String img) {
        Path filePath = Paths.get(UPLOAD_DIR + img);
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource);
    }
}
