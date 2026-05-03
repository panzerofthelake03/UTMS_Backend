package com.utms.ydyo.dto;

import jakarta.validation.constraints.NotBlank;

public class EnglishReviewRequest {

    /**
     * APPROVED   → English document accepted; application moves to UNDER_YGK_REVIEW.
     * EXAM_REQUIRED → Student needs to take language exam; application stays under review.
     */
    @NotBlank
    private String decision;

    private String reviewerNote;

    public EnglishReviewRequest() {}

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getReviewerNote() { return reviewerNote; }
    public void setReviewerNote(String reviewerNote) { this.reviewerNote = reviewerNote; }
}
