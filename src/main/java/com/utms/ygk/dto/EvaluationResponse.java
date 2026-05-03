package com.utms.ygk.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class EvaluationResponse {

    private Long id;
    private Long applicationId;
    private BigDecimal compositeScore;
    private String evaluatorNote;
    private String decision;
    private String ydyoDecision;
    private String ydyoNote;
    private Instant createdAt;
    private Instant updatedAt;

    public EvaluationResponse() {}

    public EvaluationResponse(Long id, Long applicationId, BigDecimal compositeScore,
                              String evaluatorNote, String decision,
                              String ydyoDecision, String ydyoNote,
                              Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.applicationId = applicationId;
        this.compositeScore = compositeScore;
        this.evaluatorNote = evaluatorNote;
        this.decision = decision;
        this.ydyoDecision = ydyoDecision;
        this.ydyoNote = ydyoNote;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public BigDecimal getCompositeScore() { return compositeScore; }
    public void setCompositeScore(BigDecimal compositeScore) { this.compositeScore = compositeScore; }

    public String getEvaluatorNote() { return evaluatorNote; }
    public void setEvaluatorNote(String evaluatorNote) { this.evaluatorNote = evaluatorNote; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getYdyoDecision() { return ydyoDecision; }
    public void setYdyoDecision(String ydyoDecision) { this.ydyoDecision = ydyoDecision; }

    public String getYdyoNote() { return ydyoNote; }
    public void setYdyoNote(String ydyoNote) { this.ydyoNote = ydyoNote; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
