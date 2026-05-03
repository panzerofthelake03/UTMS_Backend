package com.utms.intibak;

import com.utms.application.Application;
import com.utms.common.entity.BaseEntity;
import com.utms.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "course_exemptions")
public class CourseExemption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "student_course_code", length = 50, nullable = false)
    private String studentCourseCode;

    @Column(name = "student_course_name", length = 200, nullable = false)
    private String studentCourseName;

    @Column(name = "student_course_credits", nullable = false)
    private Integer studentCourseCredits;

    @Column(name = "student_course_grade", length = 10)
    private String studentCourseGrade;

    @Column(name = "target_course_code", length = 50)
    private String targetCourseCode;

    @Column(name = "target_course_name", length = 200)
    private String targetCourseName;

    @Column(name = "target_course_credits")
    private Integer targetCourseCredits;

    @Column(name = "decision", length = 20, nullable = false)
    private String decision = "PENDING";

    @Column(name = "decision_note", columnDefinition = "TEXT")
    private String decisionNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by_user_id")
    private User decidedBy;

    @Column(name = "decided_at")
    private Instant decidedAt;

    public CourseExemption() {}

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

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

    public User getDecidedBy() { return decidedBy; }
    public void setDecidedBy(User decidedBy) { this.decidedBy = decidedBy; }

    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }
}
