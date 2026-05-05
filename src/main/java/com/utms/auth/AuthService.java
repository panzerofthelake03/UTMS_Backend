package com.utms.auth;

import com.utms.auth.dto.AuthResponse;
import com.utms.auth.dto.LoginRequest;
import com.utms.auth.dto.RefreshTokenRequest;
import com.utms.auth.dto.RefreshTokenResponse;
import com.utms.auth.dto.RegisterRequest;
import com.utms.auth.jwt.JwtTokenProvider;
import com.utms.common.security.RoleConstants;
import com.utms.common.exception.ConflictException;
import com.utms.student.Student;
import com.utms.student.StudentRepository;
import com.utms.user.Role;
import com.utms.user.RoleRepository;
import com.utms.user.User;
import com.utms.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.authentication.BadCredentialsException;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final String DEFAULT_ROLE = RoleConstants.ROLE_STUDENT;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION_MINUTES = 30;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       StudentRepository studentRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered: " + request.email());
        }

        Role role = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalStateException("Default role not found in database"));

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setActive(true);
        user.getRoles().add(role);

        userRepository.save(user);
        ensureStudentProfileExists(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = tokenProvider.generateAccessToken(userDetails);
        String refreshToken = tokenProvider.generateRefreshToken(userDetails);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!user.isActive()) {
            throw new IllegalStateException("User account is disabled");
        }

        if (user.getLockTime() != null) {
            if (user.getLockTime().plusMinutes(LOCK_TIME_DURATION_MINUTES).isAfter(LocalDateTime.now())) {
                throw new IllegalStateException("Your account has been locked due to 5 failed login attempts. Please try again later.");
            } else {
                user.setLockTime(null);
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
            }
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException ex) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setLockTime(LocalDateTime.now());
                userRepository.save(user);
                throw new IllegalStateException("Your account has been locked due to 5 failed login attempts. Please try again later.");
            }
            userRepository.save(user);
            throw ex;
        }

        user.setFailedLoginAttempts(0);
        user.setLockTime(null);
        userRepository.save(user);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = tokenProvider.generateAccessToken(userDetails);
        String refreshToken = tokenProvider.generateRefreshToken(userDetails);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();
        if (!tokenProvider.validateToken(refreshToken) || !tokenProvider.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String email = tokenProvider.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found for refresh token"));

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .disabled(!user.isActive())
                .authorities(user.getRoles().stream().map(Role::getName).toArray(String[]::new))
                .build();

        String newAccessToken = tokenProvider.generateAccessToken(userDetails);
        String newRefreshToken = tokenProvider.generateRefreshToken(userDetails);
        return new RefreshTokenResponse(newAccessToken, newRefreshToken);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getFirstName(), user.getLastName(), roles);
    }

    private void ensureStudentProfileExists(User user) {
        if (studentRepository.findByUserId(user.getId()).isPresent()) {
            return;
        }

        Student student = new Student();
        student.setUser(user);
        student.setStudentNumber("AUTO-" + UUID.randomUUID().toString().substring(0, 8));
        student.setDepartment("UNDECLARED");
        student.setFaculty("UNASSIGNED");
        studentRepository.save(student);
    }
}
