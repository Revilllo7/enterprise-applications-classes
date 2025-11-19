
// Należy utworzyć serwis FileStorageService z adnotacją @Service, który będzie odpowiedzialny za operacje na plikach. Serwis powinien enkapsulować logikę zapisywania plików na dysku, generowania unikalnych nazw plików aby uniknąć konfliktów, walidacji typów i rozmiarów plików oraz obsługi błędów związanych z operacjami na systemie plików.

// Serwis powinien oferować metody do zapisywania pliku przyjmującego obiekt MultipartFile i zwracającego nazwę zapisanego pliku, metodę do odczytywania pliku z dysku zwracającą obiekt Resource, metodę do usuwania pliku oraz metodę walidującą czy plik spełnia wymagania dotyczące rozszerzenia i rozmiaru. Ścieżki do katalogów powinny być wstrzykiwane z application.properties używając adnotacji @Value.

package com.techcorp.employee.service;
import com.techcorp.employee.exception.FileStorageException;
import com.techcorp.employee.exception.FileNotFoundException;
import com.techcorp.employee.exception.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
@Service
public class FileStorageService {
    private final Path fileStorageLocation;
    @Value("${app.upload.directory}")
    public FileStorageService(String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }
    public String storeFile(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (originalFileName.contains("..")) {
                throw new InvalidFileException("Invalid path sequence in file name: " + originalFileName);
            }
            String fileExtension = "";
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex > 0) {
                fileExtension = originalFileName.substring(dotIndex);
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return uniqueFileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileNotFoundException("File not found " + fileName, ex);
        }
    }
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file " + fileName + ". Please try again!", ex);
        }
    }
    public void validateFile(MultipartFile file, long maxFileSize, String[] allowedExtensions) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFileName.substring(dotIndex + 1).toLowerCase();
        }
        boolean isExtensionAllowed = false;
        for (String ext : allowedExtensions) {
            if (fileExtension.equals(ext.toLowerCase())) {
                isExtensionAllowed = true;
                break;
            }
        }
        if (!isExtensionAllowed) {
            throw new InvalidFileException("File type not allowed: " + fileExtension);
        }
        if (file.getSize() > maxFileSize) {
            throw new InvalidFileException("File size exceeds the maximum limit of " + maxFileSize + " bytes");
        }
    }
}