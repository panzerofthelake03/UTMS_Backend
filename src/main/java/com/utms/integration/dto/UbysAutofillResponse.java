package com.utms.integration.dto;

import java.math.BigDecimal;
import java.util.List;

public class UbysAutofillResponse {

    private String studentNumber;
    private String department;
    private String faculty;
    private BigDecimal gpa;
    private int completedCredits;
    private List<String> completedCourses;
    private boolean profileUpdated;

    public UbysAutofillResponse() {}

    public UbysAutofillResponse(String studentNumber, String department, String faculty,
                                BigDecimal gpa, int completedCredits,
                                List<String> completedCourses, boolean profileUpdated) {
        this.studentNumber = studentNumber;
        this.department = department;
        this.faculty = faculty;
        this.gpa = gpa;
        this.completedCredits = completedCredits;
        this.completedCourses = completedCourses;
        this.profileUpdated = profileUpdated;
    }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }

    public BigDecimal getGpa() { return gpa; }
    public void setGpa(BigDecimal gpa) { this.gpa = gpa; }

    public int getCompletedCredits() { return completedCredits; }
    public void setCompletedCredits(int completedCredits) { this.completedCredits = completedCredits; }

    public List<String> getCompletedCourses() { return completedCourses; }
    public void setCompletedCourses(List<String> completedCourses) { this.completedCourses = completedCourses; }

    public boolean isProfileUpdated() { return profileUpdated; }
    public void setProfileUpdated(boolean profileUpdated) { this.profileUpdated = profileUpdated; }
}
