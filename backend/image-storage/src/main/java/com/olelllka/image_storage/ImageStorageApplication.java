package com.olelllka.image_storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ImageStorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImageStorageApplication.class, args);
	}

}
