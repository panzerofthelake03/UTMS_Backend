package com.utms.intibak.dto;

import java.time.Instant;

public class CourseExemptionResponse {

    private Long id;
    private Long applicationId;
    private String studentCourseCode;
    private String studentCourseName;
    private Integer studentCourseCredits;
    private String studentCourseGrade;
    private String targetCourseCode;
    private String targetCourseName;
    private Integer targetCourseCredits;
    private String decision;
    private String decisionNote;
    private String decidedByEmail;
    private Instant decidedAt;
    private Instant createdAt;

    public CourseExemptionResponse() {}

    public CourseExemptionResponse(Long id, Long applicationId,
                                   String studentCourseCode, String studentCourseName,
                                   Integer studentCourseCredits, String studentCourseGrade,
                                   String targetCourseCode, String targetCourseName,
                                   Integer targetCourseCredits, String decision,
                                   String decisionNote, String decidedByEmail,
                                   Instant decidedAt, Instant createdAt) {
        this.id = id;
        this.applicationId = applicationId;
        this.studentCourseCode = studentCourseCode;
        this.studentCourseName = studentCourseName;
        this.studentCourseCredits = studentCourseCredits;
        this.studentCourseGrade = studentCourseGrade;
        this.targetCourseCode = targetCourseCode;
        this.targetCourseName = targetCourseName;
        this.targetCourseCredits = targetCourseCredits;
        this.decision = decision;
        this.decisionNote = decisionNote;
        this.decidedByEmail = decidedByEmail;
        this.decidedAt = decidedAt;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getStudentCourseCode() { return studentCourseCode; }
    public void setStudentCourseCode(String studentCourseCode) { this.studentCourseCode = studentCourseCode; }

    public String getStudentCourseName() { return studentCourseName; }
    public void setStudentCourseName(String studentCourseName) { this.studentCourseName = studentCourseName; }

    public Integer getStudentCourseCredits() { return studentCourseCredits; }
    public void setStudentCourseCredits(Integer studentCourseCredits) { this.studentCourseCredits = studentCourseCredits; }

    public String getStudentCourseGrade() { return studentCourseGrade; }
    public void setStudentCourseGrade(String studentCourseGrade) { this.studentCourseGrade = studentCourseGrade; }

    public String getTargetCourseCode() { return targetCourseCode; }
    public void setTargetCourseCode(String targetCourseCode) { this.targetCourseCode = targetCourseCode; }

    public String getTargetCourseName() { return targetCourseName; }
    public void setTargetCourseName(String targetCourseName) { this.targetCourseName = targetCourseName; }

    public Integer getTargetCourseCredits() { return targetCourseCredits; }
    public void setTargetCourseCredits(Integer targetCourseCredits) { this.targetCourseCredits = targetCourseCredits; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getDecisionNote() { return decisionNote; }
    public void setDecisionNote(String decisionNote) { this.decisionNote = decisionNote; }

    public String getDecidedByEmail() { return decidedByEmail; }
    public void setDecidedByEmail(String decidedByEmail) { this.decidedByEmail = decidedByEmail; }

    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
