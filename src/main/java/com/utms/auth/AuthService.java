package com.utms.auth;

import com.utms.auth.dto.AuthResponse;
import com.utms.auth.dto.CaptchaChallengeResponse;
import com.utms.auth.dto.LoginRequest;
import com.utms.auth.dto.RegisterCancelRequest;
import com.utms.auth.dto.RefreshTokenRequest;
import com.utms.auth.dto.RefreshTokenResponse;
import com.utms.auth.dto.RegisterRequest;
import com.utms.auth.dto.RegisterStartRequest;
import com.utms.auth.dto.RegisterStartResponse;
import com.utms.auth.dto.RegisterVerifyRequest;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final String DEFAULT_ROLE = RoleConstants.ROLE_STUDENT;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final CaptchaService captchaService;
    private final VerificationEmailService verificationEmailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.auth.verification.code-expiration-seconds:300}")
    private long codeExpirationSeconds;

    @Value("${app.auth.verification.max-attempts:5}")
    private int maxVerifyAttempts;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       StudentRepository studentRepository,
                       PendingRegistrationRepository pendingRegistrationRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       CaptchaService captchaService,
                       VerificationEmailService verificationEmailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.studentRepository = studentRepository;
        this.pendingRegistrationRepository = pendingRegistrationRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.captchaService = captchaService;
        this.verificationEmailService = verificationEmailService;
    }

    @Transactional
    public RegisterStartResponse registerStart(RegisterStartRequest startRequest) {
        captchaService.validateAndConsume(startRequest.captchaId(), startRequest.captchaAnswer());

        RegisterRequest request = toRegisterRequest(startRequest);

        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered: " + request.email());
        }

        validateStudentIdentityFields(request);
        invalidateExistingPendingRegistrations(request.email());

        String otpCode = generateOtpCode();

        PendingRegistration pending = new PendingRegistration();
        pending.setVerificationSessionId(UUID.randomUUID().toString());
        pending.setEmail(request.email().trim().toLowerCase());
        pending.setPasswordHash(passwordEncoder.encode(request.password()));
        pending.setFirstName(request.firstName().trim());
        pending.setLastName(request.lastName().trim());
        pending.setNationality(request.nationality().trim());
        pending.setDateOfBirth(request.dateOfBirth());
        pending.setIdentityDocumentType(request.identityDocumentType());
        pending.setTcIdentityNumber(blankToNull(request.tcIdentityNumber()));
        pending.setIdentitySerialNo(blankToNull(request.identitySerialNo()));
        pending.setPassportNumber(blankToNull(request.passportNumber()));
        pending.setPassportExpirationDate(request.passportExpirationDate());
        pending.setCurrentProgram(request.currentProgram().trim());
        pending.setCurrentUniversity(request.currentUniversity().trim());
        pending.setOtpHash(passwordEncoder.encode(otpCode));
        pending.setOtpExpiresAt(Instant.now().plusSeconds(codeExpirationSeconds));
        pending.setAttemptCount(0);
        pending.setStatus(RegistrationStatus.PENDING);

        pendingRegistrationRepository.save(pending);
        String devVerificationCode = verificationEmailService.sendRegistrationCode(request.email(), otpCode);

        return new RegisterStartResponse(
                pending.getVerificationSessionId(),
                maskEmail(request.email()),
                codeExpirationSeconds,
                devVerificationCode);
    }

    @Transactional
    public AuthResponse verifyRegistration(RegisterVerifyRequest request) {
        PendingRegistration pending = pendingRegistrationRepository.findByVerificationSessionId(request.verificationSessionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.GONE, "Verification session is invalid"));

        if (!RegistrationStatus.PENDING.equals(pending.getStatus())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Verification session is no longer active");
        }

        if (pending.getOtpExpiresAt().isBefore(Instant.now())) {
            pending.setStatus(RegistrationStatus.EXPIRED);
            pendingRegistrationRepository.save(pending);
            throw new ResponseStatusException(HttpStatus.GONE, "Verification code has expired");
        }

        if (pending.getAttemptCount() >= maxVerifyAttempts) {
            pending.setStatus(RegistrationStatus.CANCELED);
            pendingRegistrationRepository.save(pending);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many invalid verification attempts");
        }

        if (!passwordEncoder.matches(request.code(), pending.getOtpHash())) {
            pending.setAttemptCount(pending.getAttemptCount() + 1);
            pendingRegistrationRepository.save(pending);
            throw new IllegalArgumentException("Invalid verification code");
        }

        if (userRepository.existsByEmail(pending.getEmail())) {
            pending.setStatus(RegistrationStatus.CANCELED);
            pendingRegistrationRepository.save(pending);
            throw new ConflictException("Email already registered: " + pending.getEmail());
        }

        Role role = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalStateException("Default role not found in database"));

        User user = new User();
        user.setEmail(pending.getEmail());
        user.setPasswordHash(pending.getPasswordHash());
        user.setFirstName(pending.getFirstName());
        user.setLastName(pending.getLastName());
        user.setActive(true);
        user.getRoles().add(role);
        userRepository.save(user);

        ensureStudentProfileExistsFromPending(user, pending);

        pending.setStatus(RegistrationStatus.VERIFIED);
        pendingRegistrationRepository.save(pending);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .disabled(!user.isActive())
                .authorities(user.getRoles().stream().map(Role::getName).toArray(String[]::new))
                .build();

        String accessToken = tokenProvider.generateAccessToken(userDetails);
        String refreshToken = tokenProvider.generateRefreshToken(userDetails);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public void cancelRegistration(RegisterCancelRequest request) {
        pendingRegistrationRepository.findByVerificationSessionId(request.verificationSessionId())
                .ifPresent(pending -> {
                    if (RegistrationStatus.PENDING.equals(pending.getStatus())) {
                        pending.setStatus(RegistrationStatus.CANCELED);
                        pendingRegistrationRepository.save(pending);
                    }
                });
    }

    public CaptchaChallengeResponse getCaptchaChallenge() {
        return captchaService.generateChallenge();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = tokenProvider.generateAccessToken(userDetails);
        String refreshToken = tokenProvider.generateRefreshToken(userDetails);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("User disappeared after authentication"));

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

    private RegisterRequest toRegisterRequest(RegisterStartRequest request) {
        if (request.dateOfBirth() == null) {
            throw new IllegalArgumentException("dateOfBirth is required");
        }

        return new RegisterRequest(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName(),
                request.nationality(),
                request.dateOfBirth(),
                request.identityDocumentType(),
                request.tcIdentityNumber(),
                request.identitySerialNo(),
                request.passportNumber(),
                request.passportExpirationDate(),
                request.currentProgram(),
                request.currentUniversity());
    }

    private void invalidateExistingPendingRegistrations(String email) {
        List<PendingRegistration> pendingRegistrations = pendingRegistrationRepository.findByEmailAndStatus(
                email.trim().toLowerCase(),
                RegistrationStatus.PENDING);

        for (PendingRegistration pending : pendingRegistrations) {
            pending.setStatus(RegistrationStatus.CANCELED);
        }

        if (!pendingRegistrations.isEmpty()) {
            pendingRegistrationRepository.saveAll(pendingRegistrations);
        }
    }

    private String generateOtpCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private String maskEmail(String email) {
        String normalized = email.trim();
        int atIndex = normalized.indexOf('@');
        if (atIndex <= 1) {
            return "***" + normalized.substring(Math.max(atIndex, 0));
        }
        return normalized.charAt(0) + "***" + normalized.substring(atIndex - 1);
    }

    private void ensureStudentProfileExistsFromPending(User user, PendingRegistration pending) {
        if (studentRepository.findByUserId(user.getId()).isPresent()) {
            return;
        }

        Student student = new Student();
        student.setUser(user);
        student.setStudentNumber("AUTO-" + UUID.randomUUID().toString().substring(0, 8));
        student.setDepartment("UNDECLARED");
        student.setFaculty("UNASSIGNED");
        student.setNationality(pending.getNationality());
        student.setDateOfBirth(pending.getDateOfBirth());
        student.setIdentityDocumentType(pending.getIdentityDocumentType());
        student.setTcIdentityNumber(blankToNull(pending.getTcIdentityNumber()));
        student.setIdentitySerialNo(blankToNull(pending.getIdentitySerialNo()));
        student.setPassportNumber(blankToNull(pending.getPassportNumber()));
        student.setPassportExpirationDate(pending.getPassportExpirationDate());
        student.setCurrentProgram(pending.getCurrentProgram());
        student.setCurrentUniversity(pending.getCurrentUniversity());
        studentRepository.save(student);
    }

    private void validateStudentIdentityFields(RegisterRequest request) {
        LocalDate today = LocalDate.now();
        if (request.dateOfBirth().isAfter(today)) {
            throw new IllegalArgumentException("dateOfBirth cannot be in the future");
        }

        boolean isTurkish = "TURKISH".equalsIgnoreCase(request.nationality().trim());
        boolean tcSelected = "TC_ID".equals(request.identityDocumentType());
        boolean passportSelected = "PASSPORT".equals(request.identityDocumentType());

        if (isTurkish && !tcSelected) {
            throw new IllegalArgumentException("Turkish nationality must use identityDocumentType=TC_ID");
        }
        if (!isTurkish && !passportSelected) {
            throw new IllegalArgumentException("Non-Turkish nationality must use identityDocumentType=PASSPORT");
        }

        if (tcSelected) {
            String tc = blankToNull(request.tcIdentityNumber());
            if (tc == null || !tc.matches("\\d{11}")) {
                throw new IllegalArgumentException("tcIdentityNumber must be exactly 11 digits for TC_ID");
            }
            if (blankToNull(request.identitySerialNo()) == null) {
                throw new IllegalArgumentException("identitySerialNo is required for TC_ID");
            }
            if (blankToNull(request.passportNumber()) != null || request.passportExpirationDate() != null) {
                throw new IllegalArgumentException("passport fields must be empty for TC_ID");
            }
            return;
        }

        String passportNumber = blankToNull(request.passportNumber());
        if (passportNumber == null) {
            throw new IllegalArgumentException("passportNumber is required for PASSPORT");
        }
        if (request.passportExpirationDate() == null) {
            throw new IllegalArgumentException("passportExpirationDate is required for PASSPORT");
        }
        if (!request.passportExpirationDate().isAfter(today)) {
            throw new IllegalArgumentException("passportExpirationDate must be a future date");
        }
        if (blankToNull(request.tcIdentityNumber()) != null || blankToNull(request.identitySerialNo()) != null) {
            throw new IllegalArgumentException("TC identity fields must be empty for PASSPORT");
        }
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
