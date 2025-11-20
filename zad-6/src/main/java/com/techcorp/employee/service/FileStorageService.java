package com.techcorp.employee.service;
import com.techcorp.employee.exception.FileStorageException;
import com.techcorp.employee.exception.FileNotFoundException;
import com.techcorp.employee.exception.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path fileStorageLocation;
    private final long maxFileSize;

    public FileStorageService(@Value("${app.upload.directory}") String uploadDirectory,
                              @Value("${spring.servlet.multipart.max-file-size}") String maxFileSizeString) {
        this.fileStorageLocation = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        // parse max file size from application.properties
        String maxFileSizeNonNull = Objects.requireNonNull(maxFileSizeString, "spring.servlet.multipart.max-file-size must not be null");
        this.maxFileSize = DataSize.parse(maxFileSizeNonNull).toBytes();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception exception) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", exception);
        }
    }
    
    public java.nio.file.Path getFilePath(String fileName) {
        return this.fileStorageLocation.resolve(fileName).normalize();
    }

    /**
     * Store file into a subdirectory under the configured storage location.
     * Returns the relative path (subdir/uniqueFileName) stored.
     */
    public String storeFileInSubDirectory(MultipartFile file, String subDir) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new InvalidFileException("File name is null or empty");
        }
        originalFileName = StringUtils.cleanPath(originalFileName);
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
            java.nio.file.Path dirPath = this.fileStorageLocation.resolve(subDir).toAbsolutePath().normalize();
            Files.createDirectories(dirPath);
            Path targetLocation = dirPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            // return path relative to storage root
            java.nio.file.Path relative = this.fileStorageLocation.relativize(targetLocation);
            return relative.toString().replace('\\', '/');
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }
    
    public String storeFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new InvalidFileException("File name is null or empty");
        }
        originalFileName = StringUtils.cleanPath(originalFileName);
        
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
            // This should never happen with UUID
            // If it does... lol
            if (Files.exists(targetLocation)) {
                throw new FileStorageException("File with the same name already exists?: " + uniqueFileName + " it's getting replaced lol");
            }
            
            validateFile(file, this.maxFileSize, new String[]{"jpg", "jpeg", "png", "gif", "pdf"});
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING); // Fuck your duplicate UUID
            return uniqueFileName;

    } catch (IOException exception) {
            throw new FileStorageException("Could not store file " + originalFileName + ". Please try again!", exception);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            java.net.URI uri = filePath.toUri();
            Resource resource = new UrlResource(Objects.requireNonNull(uri));
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found " + fileName);
            
            }
        } catch (MalformedURLException exception) {
            throw new FileNotFoundException("File not found " + fileName, exception);
        }
    }
    
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            
            } else {
                throw new FileNotFoundException("File not found " + fileName);
            
            }
        } catch (IOException exception) {
            throw new FileStorageException("Could not delete file " + fileName + ". Please try again!", exception);
        }
    }

    public void validateFile(MultipartFile file, long maxFileSize, String[] allowedExtensions) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            deleteFile(originalFileName);
            throw new InvalidFileException("File name was null or empty, deleted.");
        }

        if (file.isEmpty() || file.getSize() == 0) {
            // reject empty file
            deleteFile(originalFileName);
            throw new InvalidFileException("File was empty, deleted.");
        }

        originalFileName = StringUtils.cleanPath(originalFileName);
        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFileName.substring(dotIndex + 1).toLowerCase();
        }
        boolean isExtensionAllowed = false;
        for (String extension : allowedExtensions) {
            if (fileExtension.equals(extension.toLowerCase())) {
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
