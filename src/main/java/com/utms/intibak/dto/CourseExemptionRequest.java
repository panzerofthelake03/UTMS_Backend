package com.utms.intibak.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CourseExemptionRequest {

    @NotBlank
    private String studentCourseCode;

    @NotBlank
    private String studentCourseName;

    @NotNull
    @Positive
    private Integer studentCourseCredits;

    private String studentCourseGrade;
    private String targetCourseCode;
    private String targetCourseName;
    private Integer targetCourseCredits;

    public CourseExemptionRequest() {}

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
}
