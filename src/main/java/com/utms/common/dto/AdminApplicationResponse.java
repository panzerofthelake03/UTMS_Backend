package com.utms.common.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Enriched application view for staff roles (OIDB, YDYO, YGK, ADMIN).
 * Includes student identity fields alongside the application record.
 */
public class AdminApplicationResponse {

    private Long id;
    private String status;
    private String term;
    private String applicationNote;
    private Instant submittedAt;
    private Instant createdAt;
    private Instant updatedAt;

    // Student info
    private Long studentId;
    private String studentNumber;
    private String studentFirstName;
    private String studentLastName;
    private String studentEmail;
    private String department;
    private String faculty;
    private BigDecimal gpa;

    public AdminApplicationResponse() {}

    public AdminApplicationResponse(Long id, String status, String term, String applicationNote,
                                    Instant submittedAt, Instant createdAt, Instant updatedAt,
                                    Long studentId, String studentNumber,
                                    String studentFirstName, String studentLastName, String studentEmail,
                                    String department, String faculty, BigDecimal gpa) {
        this.id = id;
        this.status = status;
        this.term = term;
        this.applicationNote = applicationNote;
        this.submittedAt = submittedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.studentId = studentId;
        this.studentNumber = studentNumber;
        this.studentFirstName = studentFirstName;
        this.studentLastName = studentLastName;
        this.studentEmail = studentEmail;
        this.department = department;
        this.faculty = faculty;
        this.gpa = gpa;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }

    public String getApplicationNote() { return applicationNote; }
    public void setApplicationNote(String applicationNote) { this.applicationNote = applicationNote; }

    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public String getStudentFirstName() { return studentFirstName; }
    public void setStudentFirstName(String studentFirstName) { this.studentFirstName = studentFirstName; }

    public String getStudentLastName() { return studentLastName; }
    public void setStudentLastName(String studentLastName) { this.studentLastName = studentLastName; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }

    public BigDecimal getGpa() { return gpa; }
    public void setGpa(BigDecimal gpa) { this.gpa = gpa; }
}
