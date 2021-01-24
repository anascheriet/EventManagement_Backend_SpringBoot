package com.events.eventsmanagement.util;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class fileUploadService {

    //Save the uploaded file to this folder
    private static String UPLOAD_FOLDER = "/Users/anascheriet/Documents/Projects/Spring Boot/EventsManagement/src/main/resources/public/";

    public String singleFileUpload(MultipartFile file) {
        if (file.isEmpty()) {
            return "Please select a file to upload";
        }
        Path path = null;
        try {

            byte[] bytes = file.getBytes();
            path = Paths.get(UPLOAD_FOLDER + file.getOriginalFilename());
            Files.write(path, bytes);
            return path.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return UPLOAD_FOLDER + file.getOriginalFilename();
    }
}
