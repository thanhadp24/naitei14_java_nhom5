package vn.sun.public_service_manager.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import vn.sun.public_service_manager.exception.FileException;

@Component
public class FileUtil {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public void createDirectoryIfNotExists(String folder) {
        Path path = Path.of(uploadDir + "/" + folder);
        File file = path.toFile();
        if (!file.isDirectory()) {
            try {
                Files.createDirectories(path);
                System.out.println("Directory created or already exists: " + path.toAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Directory already exists: " + path.toAbsolutePath());
        }
    }

    public void saveFiles(MultipartFile[] files, String folder) {
        for (MultipartFile file : files) {
            try {
                Path path = Path.of(uploadDir + "/" + folder + "/" + file.getOriginalFilename());
                try (InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
                }
                System.out.println("File saved: " + path.toAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void validateFileExtensions(MultipartFile[] files, List<String> allowedExtensions) throws FileException {
        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            if (allowedExtensions.stream().noneMatch(ext -> filename != null && filename.endsWith("." + ext))) {
                throw new FileException(
                        "File extension not allowed, only " + allowedExtensions.toString() + " are accepted.");
            }
        }
    }
}
