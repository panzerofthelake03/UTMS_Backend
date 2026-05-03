package com.utms.document;

import com.utms.application.Application;
import com.utms.application.ApplicationStatus;
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
    private static final String EXAM_RESULT_TYPE = "ENGLISH_PROFICIENCY_EXAM_RESULT";

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
    *   5. Upload is allowed in DRAFT for owner only, or in WAITING_EXAM_RESULT for owner/YDYO
     */
    @Transactional
    public DocumentResponse uploadDocument(Long applicationId,
                                           String documentType,
                                           MultipartFile file) throws IOException {
        validateFile(file);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Application not found: " + applicationId));

        boolean isOwner = permissionChecker.isOwner(application.getStudent().getUser());
        boolean isYdyoStaff = permissionChecker.isYdyo() || permissionChecker.isAdmin();
        boolean isWaitingExamResult = ApplicationStatus.WAITING_EXAM_RESULT.equals(application.getStatus());

        if (isWaitingExamResult) {
            if (!isOwner && !isYdyoStaff) {
                throw new AccessDeniedException("Only the owner, YDYO, or admin can upload exam results in WAITING_EXAM_RESULT status");
            }
            if (!EXAM_RESULT_TYPE.equals(documentType)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Only ENGLISH_PROFICIENCY_EXAM_RESULT can be uploaded in WAITING_EXAM_RESULT status");
            }
        } else {
            if (!isOwner) {
                throw new AccessDeniedException("You can only upload documents for your own applications");
            }
            if (!ApplicationStatus.DRAFT.equals(application.getStatus())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Documents can only be uploaded while the application is in DRAFT status");
            }
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

    @Transactional(readOnly = true)
    public DownloadedDocument downloadDocument(Long applicationId, Long documentId) throws IOException {
        Document document = getViewableDocument(applicationId, documentId);
        return new DownloadedDocument(
                document.getOriginalFilename(),
                document.getMimeType(),
                storageService.readAllBytes(document.getStoragePath()));
    }

    @Transactional
    public void deleteDocument(Long applicationId, Long documentId) throws IOException {
        Document document = documentRepository.findByIdAndApplicationId(documentId, applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found: " + documentId));

        Application application = document.getApplication();
        if (!permissionChecker.isOwner(application.getStudent().getUser())) {
            throw new AccessDeniedException("You can only delete documents from your own applications");
        }

        if (!ApplicationStatus.DRAFT.equals(application.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Documents can only be deleted while the application is in DRAFT status");
        }

        storageService.delete(document.getStoragePath());
        documentRepository.delete(document);
    }

    private Document getViewableDocument(Long applicationId, Long documentId) {
        Document document = documentRepository.findByIdAndApplicationId(documentId, applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found: " + documentId));

        if (!permissionChecker.canViewApplication(document.getApplication().getStudent().getUser())) {
            throw new AccessDeniedException("You are not allowed to download this document");
        }
        return document;
    }

    public record DownloadedDocument(String fileName, String mimeType, byte[] content) {}

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
