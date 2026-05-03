package com.utms.integration.dto;

public class YoksisLookupResponse {

    private String studentNumber;
    private String firstName;
    private String lastName;
    private String nationality;
    private boolean enrollmentVerified;
    private String message;

    public YoksisLookupResponse() {}

    public YoksisLookupResponse(String studentNumber, String firstName, String lastName,
                                String nationality, boolean enrollmentVerified, String message) {
        this.studentNumber = studentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationality = nationality;
        this.enrollmentVerified = enrollmentVerified;
        this.message = message;
    }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public boolean isEnrollmentVerified() { return enrollmentVerified; }
    public void setEnrollmentVerified(boolean enrollmentVerified) { this.enrollmentVerified = enrollmentVerified; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
