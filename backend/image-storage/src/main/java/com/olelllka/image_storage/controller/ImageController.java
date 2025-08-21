package com.olelllka.image_storage.controller;

import com.olelllka.image_storage.domain.UrlDto;
import com.olelllka.image_storage.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600)
public class ImageController {

    public static final String UPLOAD_DIR = "./uploads/";
    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<UrlDto> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String url = imageService.save(file);
        return new ResponseEntity<>(UrlDto.builder().url(url).build(), HttpStatus.OK);
    }

    @GetMapping("/images/{img}")
    public ResponseEntity<Resource> getImage(@PathVariable String img) {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageService.getResource(img));
    }
}
