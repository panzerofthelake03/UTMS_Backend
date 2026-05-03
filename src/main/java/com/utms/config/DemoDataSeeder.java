package com.utms.config;

import com.utms.common.security.RoleConstants;
import com.utms.student.Student;
import com.utms.student.StudentRepository;
import com.utms.user.Role;
import com.utms.user.RoleRepository;
import com.utms.user.User;
import com.utms.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

@Component
public class DemoDataSeeder implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "Demo123!";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(RoleRepository roleRepository,
                          UserRepository userRepository,
                          StudentRepository studentRepository,
                          PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role studentRole = ensureRole(RoleConstants.ROLE_STUDENT, "Student role");
        Role oidbRole = ensureRole(RoleConstants.ROLE_OIDB, "Administrative office role");
        Role ydyoRole = ensureRole(RoleConstants.ROLE_YDYO, "Foreign languages school role");
        Role ygkRole = ensureRole(RoleConstants.ROLE_YGK, "Evaluation committee role");
        Role intibakRole = ensureRole(RoleConstants.ROLE_INTIBAK, "Intibak committee role");
        Role adminRole = ensureRole(RoleConstants.ROLE_ADMIN, "System administrator role");

        User studentUser = ensureUser("demo.student@utms.local", "Demo", "Student", studentRole);
        ensureStudentProfile(studentUser);

        ensureUser("demo.oidb@utms.local", "Demo", "OIDB", oidbRole);
        ensureUser("demo.ydyo@utms.local", "Demo", "YDYO", ydyoRole);
        ensureUser("demo.ygk@utms.local", "Demo", "YGK", ygkRole);
        ensureUser("demo.intibak@utms.local", "Demo", "Intibak", intibakRole);
        ensureUser("demo.admin@utms.local", "Demo", "Admin", adminRole);
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

    private User ensureUser(String email, String firstName, String lastName, Role role) {
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

    private void ensureStudentProfile(User studentUser) {
        if (studentRepository.findByUserId(studentUser.getId()).isPresent()) {
            return;
        }

        Student student = new Student();
        student.setUser(studentUser);
        student.setStudentNumber("S20260001");
        student.setDepartment("Computer Engineering");
        student.setFaculty("Engineering");
        student.setGpa(new BigDecimal("3.20"));
        student.setCreatedBy("demo-seed");
        student.setUpdatedBy("demo-seed");
        studentRepository.save(student);
    }
}