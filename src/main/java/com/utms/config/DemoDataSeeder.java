package com.utms.config;

import com.utms.application.ApplicationRepository;
import com.utms.common.security.RoleConstants;
import com.utms.student.Student;
import com.utms.student.StudentRepository;
import com.utms.user.Role;
import com.utms.user.RoleRepository;
import com.utms.user.User;
import com.utms.user.UserRepository;
import com.utms.ygk.EvaluationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Component
public class DemoDataSeeder implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "Demo123!";
    private static final BigDecimal DEMO_YKS_SCORE          = new BigDecimal("420.00");
    /** Normalized to 0-100 for the composite formula: 420 / 5 = 84 */
    private static final BigDecimal DEMO_LANGUAGE_SCORE      = new BigDecimal("84.00");

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ApplicationRepository applicationRepository;
    private final EvaluationRepository evaluationRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(RoleRepository roleRepository,
                          UserRepository userRepository,
                          StudentRepository studentRepository,
                          ApplicationRepository applicationRepository,
                          EvaluationRepository evaluationRepository,
                          PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.applicationRepository = applicationRepository;
        this.evaluationRepository = evaluationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        ensureRole(RoleConstants.ROLE_STUDENT, "Student role");
        ensureRole(RoleConstants.ROLE_OIDB, "Administrative office role");
        ensureRole(RoleConstants.ROLE_YDYO, "Foreign languages school role");
        ensureRole(RoleConstants.ROLE_YGK, "Evaluation committee role");
        ensureRole(RoleConstants.ROLE_INTIBAK, "Intibak committee role");
        ensureRole(RoleConstants.ROLE_ADMIN, "System administrator role");
        ensureRole(RoleConstants.ROLE_DEAN, "Dean office role");

        ensureUser("demo.oidb@utms.local",    "Demo", "OIDB",    RoleConstants.ROLE_OIDB);
        ensureUser("demo.ydyo@utms.local",    "Demo", "YDYO",    RoleConstants.ROLE_YDYO);
        ensureUser("demo.ygk@utms.local",     "Demo", "YGK",     RoleConstants.ROLE_YGK);
        ensureUser("demo.intibak@utms.local", "Demo", "Intibak", RoleConstants.ROLE_INTIBAK);
        ensureUser("demo.admin@utms.local",   "Demo", "Admin",   RoleConstants.ROLE_ADMIN);
        ensureUser("demo.dean@utms.local",    "Demo", "Dean",    RoleConstants.ROLE_DEAN);

        User studentUser = ensureUser("demo.student@utms.local", "Demo", "Student", RoleConstants.ROLE_STUDENT);
        Student student = ensureStudentProfile(studentUser);

        // Back-fill language_score for any evaluation that was created before yks_score was assigned
        applicationRepository.findByStudentId(student.getId()).forEach(app ->
            evaluationRepository.findByApplicationId(app.getId()).ifPresent(eval -> {
                if (eval.getLanguageScore() == null) {
                    eval.setLanguageScore(DEMO_LANGUAGE_SCORE);
                    evaluationRepository.save(eval);
                }
            })
        );
    }

    private Role ensureRole(String name, String description) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            role.setCreatedBy("demo-seed");
            role.setUpdatedBy("demo-seed");
            return roleRepository.save(role);
        });
    }

    private User ensureUser(String email, String firstName, String lastName, String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName));
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setActive(true);
            user.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
            user.setRoles(Set.of(role));
            user.setCreatedBy("demo-seed");
            user.setUpdatedBy("demo-seed");
            return userRepository.save(user);
        });
    }

    private Student ensureStudentProfile(User studentUser) {
        Student student = studentRepository.findByUserId(studentUser.getId()).orElseGet(Student::new);
        student.setUser(studentUser);
        if (student.getStudentNumber() == null)       student.setStudentNumber("S20260001");
        if (student.getDepartment() == null)          student.setDepartment("Computer Engineering");
        if (student.getFaculty() == null)             student.setFaculty("Engineering");
        if (student.getGpa() == null)                 student.setGpa(new BigDecimal("3.20"));
        if (student.getNationality() == null)         student.setNationality("TURKISH");
        if (student.getDateOfBirth() == null)         student.setDateOfBirth(LocalDate.of(2000, 1, 15));
        if (student.getIdentityDocumentType() == null) student.setIdentityDocumentType("TC_ID");
        if (student.getTcIdentityNumber() == null)    student.setTcIdentityNumber("10000000001");
        if (student.getIdentitySerialNo() == null)    student.setIdentitySerialNo("A12B34567");
        if (student.getCurrentProgram() == null)      student.setCurrentProgram("Computer Engineering");
        if (student.getCurrentUniversity() == null)   student.setCurrentUniversity("Izmir Institute of Technology");
        if (student.getYksScore() == null)            student.setYksScore(DEMO_YKS_SCORE);
        student.setPassportNumber(null);
        student.setPassportExpirationDate(null);
        student.setCreatedBy("demo-seed");
        student.setUpdatedBy("demo-seed");
        studentRepository.save(student);
        return student;
    }
}
