package com.utms.document;

import com.utms.common.api.ApiResponse;
import com.utms.document.dto.DocumentResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/applications/{applicationId}/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * POST /api/applications/{applicationId}/documents
     * Accepts a multipart PDF (max 2 MB). Validates MIME type and runs scan simulation.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @PathVariable Long applicationId,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file) throws IOException {
        DocumentResponse response = documentService.uploadDocument(applicationId, documentType, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * GET /api/applications/{applicationId}/documents
     * Lists all documents associated with an application.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> listDocuments(@PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.success(documentService.listDocuments(applicationId)));
    }

    @GetMapping("/{documentId}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long applicationId,
                                                   @PathVariable Long documentId) throws IOException {
        DocumentService.DownloadedDocument download = documentService.downloadDocument(applicationId, documentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + download.fileName() + "\"")
                .contentType(MediaType.parseMediaType(download.mimeType()))
                .body(download.content());
    }

    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable Long applicationId,
                                                            @PathVariable Long documentId) throws IOException {
        documentService.deleteDocument(applicationId, documentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
