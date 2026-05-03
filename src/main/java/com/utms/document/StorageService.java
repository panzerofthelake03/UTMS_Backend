package com.utms.document;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Handles saving uploaded files to local disk storage.
 * The storage directory is configurable via app.document.storage-dir.
 */
@Service
public class StorageService {

    private final Path storageRoot;

    public StorageService(@Value("${app.document.storage-dir}") String storageDir) throws IOException {
        this.storageRoot = Paths.get(storageDir);
        Files.createDirectories(this.storageRoot);
    }

    /**
     * Persists the uploaded file under a unique name and returns its storage path.
     *
     * @param applicationId the owning application's ID (used as sub-directory)
     * @param file          the multipart file to store
     * @return the relative path stored in the database
     */
    public String store(Long applicationId, MultipartFile file) throws IOException {
        String uniqueName = UUID.randomUUID() + "_" + sanitiseFilename(file.getOriginalFilename());
        Path destination = storageRoot.resolve(String.valueOf(applicationId)).resolve(uniqueName);
        Files.createDirectories(destination.getParent());
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return applicationId + "/" + uniqueName;
    }

    private String sanitiseFilename(String filename) {
        if (filename == null) return "upload";
        // Strip path traversal characters
        return filename.replaceAll("[^a-zA-Z0-9._\\-]", "_");
    }
}
