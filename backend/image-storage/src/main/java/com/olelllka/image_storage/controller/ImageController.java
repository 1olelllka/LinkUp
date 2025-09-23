package com.olelllka.image_storage.controller;

import com.olelllka.image_storage.domain.ErrorDto;
import com.olelllka.image_storage.domain.UrlDto;
import com.olelllka.image_storage.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Custom Image API Endpoints")
public class ImageController {

    public static final String UPLOAD_DIR = "./uploads/";
    private final ImageService imageService;

    @Operation(summary = "Upload new image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully uploaded image"),
            @ApiResponse(responseCode = "400", description = "File is not supported", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
            })
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UrlDto> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String url = imageService.save(file);
        return new ResponseEntity<>(UrlDto.builder().url(url).build(), HttpStatus.OK);
    }

    @Operation(summary = "Get image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched image", content = {
                    @Content(mediaType = "image/png", schema = @Schema(implementation = Resource.class))
            }),
            @ApiResponse(responseCode = "404", description = "Image not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
            })
    })
    @GetMapping("/images/{img}")
    public ResponseEntity<Resource> getImage(@PathVariable String img) {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageService.getResource(img));
    }
}
