package com.utms.intibak.dto;

import jakarta.validation.constraints.NotBlank;

public class ExemptionDecisionRequest {

    /**
     * EXEMPT | PARTIAL | REJECTED
     */
    @NotBlank
    private String decision;

    private String decisionNote;

    public ExemptionDecisionRequest() {}

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getDecisionNote() { return decisionNote; }
    public void setDecisionNote(String decisionNote) { this.decisionNote = decisionNote; }
}
