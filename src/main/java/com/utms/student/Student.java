package com.utms.student;

import com.utms.common.entity.BaseEntity;
import com.utms.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "students")
public class Student extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "student_number", length = 50, nullable = false, unique = true)
    private String studentNumber;

    @Column(name = "department", length = 150, nullable = false)
    private String department;

    @Column(name = "faculty", length = 150)
    private String faculty;

    @Column(name = "gpa", precision = 4, scale = 2)
    private BigDecimal gpa;

    @Column(name = "nationality", length = 100)
    private String nationality;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "identity_document_type", length = 20)
    private String identityDocumentType;

    @Column(name = "tc_identity_number", length = 20)
    private String tcIdentityNumber;

    @Column(name = "identity_serial_no", length = 30)
    private String identitySerialNo;

    @Column(name = "passport_number", length = 30)
    private String passportNumber;

    @Column(name = "passport_expiration_date")
    private LocalDate passportExpirationDate;

    @Column(name = "current_program", length = 255)
    private String currentProgram;

    @Column(name = "current_university", length = 255)
    private String currentUniversity;

    public Student() {}

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public BigDecimal getGpa() {
        return gpa;
    }

    public void setGpa(BigDecimal gpa) {
        this.gpa = gpa;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getIdentityDocumentType() {
        return identityDocumentType;
    }

    public void setIdentityDocumentType(String identityDocumentType) {
        this.identityDocumentType = identityDocumentType;
    }

    public String getTcIdentityNumber() {
        return tcIdentityNumber;
    }

    public void setTcIdentityNumber(String tcIdentityNumber) {
        this.tcIdentityNumber = tcIdentityNumber;
    }

    public String getIdentitySerialNo() {
        return identitySerialNo;
    }

    public void setIdentitySerialNo(String identitySerialNo) {
        this.identitySerialNo = identitySerialNo;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public LocalDate getPassportExpirationDate() {
        return passportExpirationDate;
    }

    public void setPassportExpirationDate(LocalDate passportExpirationDate) {
        this.passportExpirationDate = passportExpirationDate;
    }

    public String getCurrentProgram() {
        return currentProgram;
    }

    public void setCurrentProgram(String currentProgram) {
        this.currentProgram = currentProgram;
    }

    public String getCurrentUniversity() {
        return currentUniversity;
    }

    public void setCurrentUniversity(String currentUniversity) {
        this.currentUniversity = currentUniversity;
    }
}
