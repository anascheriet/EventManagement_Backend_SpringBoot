package com.events.eventsmanagement.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import com.events.eventsmanagement.models.AppUser;

public class BaseController {

    public AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (AppUser) authentication.getPrincipal();
    }

    @Value("${file.upload-dir}")
    private String IMAGE_FOLDER_DESTINATION;

    public void saveUploadedFile(MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(IMAGE_FOLDER_DESTINATION + file.getOriginalFilename());
            Files.write(path, bytes);
        }
    }
}
