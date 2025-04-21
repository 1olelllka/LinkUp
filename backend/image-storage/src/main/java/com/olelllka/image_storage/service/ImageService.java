package com.olelllka.image_storage.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageService {
    String save(MultipartFile file) throws IOException;

    Resource getResource(String img);
}
