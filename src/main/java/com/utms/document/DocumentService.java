package com.utms.document;

import com.utms.application.Application;
import com.utms.application.ApplicationRepository;
import com.utms.common.security.AuthenticatedUserService;
import com.utms.common.security.PermissionChecker;
import com.utms.document.dto.DocumentResponse;
import com.utms.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Service
public class DocumentService {

    private static final String PDF_MIME = "application/pdf";

    private final long maxFileSizeBytes;
    private final DocumentRepository documentRepository;
    private final ApplicationRepository applicationRepository;
    private final StorageService storageService;
    private final ScanSimulatorService scanSimulatorService;
    private final AuthenticatedUserService authenticatedUserService;
    private final PermissionChecker permissionChecker;

    public DocumentService(
            @Value("${app.document.max-file-size-bytes:2097152}") long maxFileSizeBytes,
            DocumentRepository documentRepository,
            ApplicationRepository applicationRepository,
            StorageService storageService,
            ScanSimulatorService scanSimulatorService,
            AuthenticatedUserService authenticatedUserService,
            PermissionChecker permissionChecker) {
        this.maxFileSizeBytes = maxFileSizeBytes;
        this.documentRepository = documentRepository;
        this.applicationRepository = applicationRepository;
        this.storageService = storageService;
        this.scanSimulatorService = scanSimulatorService;
        this.authenticatedUserService = authenticatedUserService;
        this.permissionChecker = permissionChecker;
    }

    /**
     * Uploads and validates a document for the given application.
     * Validation rules:
     *   1. File must not be empty
     *   2. MIME type must be application/pdf
     *   3. File size must not exceed 2 MB
     *   4. Virus scan simulation must return CLEAN
     *   5. The authenticated user must own the application
     */
    @Transactional
    public DocumentResponse uploadDocument(Long applicationId,
                                           String documentType,
                                           MultipartFile file) throws IOException {
        validateFile(file);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Application not found: " + applicationId));

        User currentUser = authenticatedUserService.getCurrentUser();
        if (!permissionChecker.isOwner(application.getStudent().getUser())) {
            throw new AccessDeniedException("You can only upload documents for your own applications");
        }

        // Virus scan simulation
        String scanResult = scanSimulatorService.scan(file);
        if (ScanStatus.INFECTED.equals(scanResult)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "File failed virus scan and was rejected");
        }

        String storagePath = storageService.store(applicationId, file);

        Document document = new Document();
        document.setApplication(application);
        document.setDocumentType(documentType);
        document.setOriginalFilename(file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.pdf");
        document.setStoragePath(storagePath);
        document.setMimeType(file.getContentType() != null ? file.getContentType() : PDF_MIME);
        document.setFileSizeBytes(file.getSize());
        document.setScanStatus(scanResult);

        Document saved = documentRepository.save(document);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> listDocuments(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Application not found: " + applicationId));

        User currentUser = authenticatedUserService.getCurrentUser();
        if (!permissionChecker.canViewApplication(application.getStudent().getUser())) {
            throw new AccessDeniedException("You are not allowed to view documents for this application");
        }

        return documentRepository.findByApplicationIdOrderByCreatedAtAsc(applicationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file must not be empty");
        }

        String contentType = file.getContentType();
        if (!PDF_MIME.equalsIgnoreCase(contentType)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Only PDF files are accepted (received: " + contentType + ")");
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "File size exceeds the maximum allowed limit of " + (maxFileSizeBytes / 1_048_576) + " MB");
        }
    }

    private DocumentResponse toResponse(Document doc) {
        return new DocumentResponse(
                doc.getId(),
                doc.getApplication().getId(),
                doc.getDocumentType(),
                doc.getOriginalFilename(),
                doc.getMimeType(),
                doc.getFileSizeBytes(),
                doc.getScanStatus(),
                doc.getCreatedAt()
        );
    }
}
