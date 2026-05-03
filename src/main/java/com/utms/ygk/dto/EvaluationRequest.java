package com.utms.ygk.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class EvaluationRequest {

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    private BigDecimal languageScore;

    /** Manual policy adjustment in range −5 to +5. */
    private BigDecimal adjustment;

    private String evaluatorNote;

    /**
     * Final YGK decision: ACCEPTED or REJECTED.
     */
    @NotBlank
    private String decision;

    public EvaluationRequest() {}

    public BigDecimal getLanguageScore() { return languageScore; }
    public void setLanguageScore(BigDecimal languageScore) { this.languageScore = languageScore; }

    public BigDecimal getAdjustment() { return adjustment; }
    public void setAdjustment(BigDecimal adjustment) { this.adjustment = adjustment; }

    public String getEvaluatorNote() { return evaluatorNote; }
    public void setEvaluatorNote(String evaluatorNote) { this.evaluatorNote = evaluatorNote; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
}
