package com.events.eventsmanagement.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class fileUploadService {

    //Save the uploaded file to this folder
    private static String UPLOAD_FOLDER = "/Users/anascheriet/Desktop/EVENTOR/api/EventManagement_Backend_SpringBoot/src/main/resources/public/";

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
