package com.utms.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends the password-reset OTP email (UC 1.3, step 4).
 *
 * <p>Mirrors {@link VerificationEmailService}: when no {@link JavaMailSender} is configured or the
 * send fails, and {@code dev-expose-code} is enabled, the code is returned for local development
 * instead of throwing. In production a failure surfaces as an exception so the caller can present
 * the UC 1.3 "Service temporarily unavailable" message (Exception 4b - SMTP Service Failure).</p>
 */
@Service
public class PasswordResetEmailService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetEmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.from:no-reply@utms.local}")
    private String fromAddress;

    @Value("${app.auth.verification.dev-expose-code:false}")
    private boolean devExposeCode;

    public PasswordResetEmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    /**
     * Sends the 6-digit OTP to the given email.
     *
     * @return the code itself when {@code dev-expose-code} is on (local dev), otherwise {@code null}.
     * @throws SmtpServiceException when the email cannot be sent and dev exposure is disabled.
     */
    public String sendResetCode(String email, String code) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        if (mailSender == null) {
            log.warn("JavaMailSender is not configured. Password reset code for {} is {}", email, code);
            if (!devExposeCode) {
                throw new SmtpServiceException("Email service is not configured");
            }
            return code;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(email);
            message.setSubject("UTMS Password Reset Code");
            message.setText(
                    "We received a request to reset your UTMS password.\n\n"
                    + "Your verification code is: " + code + "\n\n"
                    + "This code expires in 3 minutes. If you did not request a password reset, "
                    + "you can safely ignore this email.");
            mailSender.send(message);
            return devExposeCode ? code : null;
        } catch (Exception ex) {
            log.error("Failed to send password reset email to {}", email, ex);
            if (!devExposeCode) {
                throw new SmtpServiceException("Failed to send password reset email");
            }
            return code;
        }
    }
}
