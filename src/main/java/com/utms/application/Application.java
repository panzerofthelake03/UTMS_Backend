package com.utms.application;

import com.utms.common.entity.BaseEntity;
import com.utms.student.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "applications")
public class Application extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "term", length = 50, nullable = false)
    private String term;

    @Column(name = "application_note", columnDefinition = "TEXT")
    private String applicationNote;

    @Column(name = "target_department", length = 255)
    private String targetDepartment;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    /** UC 1.6: "DOCUMENT" | "YDYO_EXAM" */
    @Column(name = "english_proficiency_option", length = 30)
    private String englishProficiencyOption;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    public Application() {}

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getApplicationNote() {
        return applicationNote;
    }

    public void setApplicationNote(String applicationNote) {
        this.applicationNote = applicationNote;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getTargetDepartment() { return targetDepartment; }
    public void setTargetDepartment(String targetDepartment) { this.targetDepartment = targetDepartment; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEnglishProficiencyOption() { return englishProficiencyOption; }
    public void setEnglishProficiencyOption(String englishProficiencyOption) {
        this.englishProficiencyOption = englishProficiencyOption;
    }
}
