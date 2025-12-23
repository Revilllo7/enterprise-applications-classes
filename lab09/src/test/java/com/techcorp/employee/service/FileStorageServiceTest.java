package com.techcorp.employee.service;

import com.techcorp.employee.exception.InvalidFileException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    private final Path storage = Path.of("target/test-uploads");

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(storage)) {
            Files.walk(storage)
                    .sorted((a,b) -> b.compareTo(a))
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
        }
    }

    @Test
    void storeLoadDeleteFile() throws IOException {
        FileStorageService svc = new FileStorageService(storage.toString(), "10MB");

        byte[] data = "hello".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", data);

        String name = svc.storeFile(file);
        assertNotNull(name);
        assertTrue(name.contains("."));

        var resource = svc.loadFileAsResource(name);
        assertNotNull(resource);
        assertTrue(resource.exists());
        assertEquals(data.length, resource.contentLength());

        svc.deleteFile(name);
        assertFalse(Files.exists(storage.resolve(name)));
    }

    @Test
    void validateFile_invalidExtension_throws() {
        FileStorageService svc = new FileStorageService(storage.toString(), "10MB");
        MockMultipartFile file = new MockMultipartFile("file", "data.txt", "text/plain", "x".getBytes());
        long max = 10L * 1024L * 1024L;
        assertThrows(InvalidFileException.class, () -> svc.validateFile(file, max, new String[]{"csv"}));
    }

    @Test
    void validateFile_tooLarge_throws() {
        FileStorageService svc = new FileStorageService(storage.toString(), "1B");
        // make a 2 byte file
        MockMultipartFile file = new MockMultipartFile("file", "data.csv", "text/csv", new byte[]{1,2});
        long tiny = 1L;
        assertThrows(InvalidFileException.class, () -> svc.validateFile(file, tiny, new String[]{"csv"}));
    }
}
