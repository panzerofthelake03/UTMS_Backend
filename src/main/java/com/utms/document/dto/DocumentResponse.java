package com.utms.document.dto;

import java.time.Instant;

public class DocumentResponse {

    private Long id;
    private Long applicationId;
    private String documentType;
    private String originalFilename;
    private String mimeType;
    private Long fileSizeBytes;
    private String scanStatus;
    private Instant createdAt;

    public DocumentResponse() {}

    public DocumentResponse(Long id, Long applicationId, String documentType,
                            String originalFilename, String mimeType,
                            Long fileSizeBytes, String scanStatus, Instant createdAt) {
        this.id = id;
        this.applicationId = applicationId;
        this.documentType = documentType;
        this.originalFilename = originalFilename;
        this.mimeType = mimeType;
        this.fileSizeBytes = fileSizeBytes;
        this.scanStatus = scanStatus;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getScanStatus() { return scanStatus; }
    public void setScanStatus(String scanStatus) { this.scanStatus = scanStatus; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
