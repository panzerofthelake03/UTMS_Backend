package com.utms.student;

import com.utms.common.api.ApiResponse;
import com.utms.application.ApplicationService;
import com.utms.student.dto.StudentDashboardResponse;
import com.utms.student.dto.StudentProfileResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final ApplicationService applicationService;
    private final StudentService studentService;

    public StudentController(ApplicationService applicationService,
                             StudentService studentService) {
        this.applicationService = applicationService;
        this.studentService = studentService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentDashboardResponse>> dashboard() {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getStudentDashboard()));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> profile() {
        return ResponseEntity.ok(ApiResponse.success(studentService.getCurrentStudentProfile()));
    }
}
