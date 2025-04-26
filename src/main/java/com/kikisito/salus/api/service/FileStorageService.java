package com.kikisito.salus.api.service;

import com.kikisito.salus.api.config.FileStorageConfiguration;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


@Service
public class FileStorageService {
    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageConfiguration fileStorageConfiguration) {
        this.fileStorageLocation = fileStorageConfiguration.getUploadPath();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory at " + fileStorageLocation, ex);
        }
    }

    // Crea una ruta absoluta única para cada archivo
    private Path createFilePath(String originalFilename) {
        // Formato de la ruta: /uploadDir/{yyyy-MM-dd}/{uniqueId}_{filename}
        String today = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE);
        String uuid = UUID.randomUUID().toString();
        String filename = StringUtils.cleanPath(originalFilename);

        // Resuelve la ruta de la carpeta
        Path entityDir = fileStorageLocation.resolve(today);

        try {
            // Crea las carpetas si no existen
            Files.createDirectories(entityDir);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create directory for files", ex);
        }

        return entityDir.resolve(uuid + "_" + filename);
    }

    public String storeFile(MultipartFile file) {
        try {
            // Crea la ruta de destino del archivo
            Path targetLocation = this.createFilePath(file.getOriginalFilename());

            // Copia el archivo a la carpeta
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Determina la ruta relativa para almacenar en la base de datos
            return fileStorageLocation.relativize(targetLocation).toString();
        } catch (IOException e) {
            throw new RuntimeException("Could not store file", e);
        }
    }

    public byte[] loadFileAsBytes(String filePath) {
        try {
            Path fileLocation = fileStorageLocation.resolve(filePath);
            return Files.readAllBytes(fileLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not load file", e);
        }
    }

    public Resource loadFileAsResource(String filePath) {
        try {
            Path fileLocation = fileStorageLocation.resolve(filePath);
            Resource resource = new UrlResource(fileLocation.toUri());

            if(resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + filePath);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not load file", e);
        }
    }

    public boolean deleteFile(String filePath) {
        try {
            Path fileLocation = fileStorageLocation.resolve(filePath);
            return Files.deleteIfExists(fileLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file: " + filePath, e);
        }
    }
}