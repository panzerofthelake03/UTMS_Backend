package com.utms.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class VerificationEmailService {

    private static final Logger log = LoggerFactory.getLogger(VerificationEmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.from:no-reply@utms.local}")
    private String fromAddress;

    @Value("${app.auth.verification.dev-expose-code:false}")
    private boolean devExposeCode;

    public VerificationEmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    public String sendRegistrationCode(String email, String code) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        if (mailSender == null) {
            log.warn("JavaMailSender is not configured. Verification code for {} is {}", email, code);
            if (!devExposeCode) {
                throw new IllegalStateException("Email service is not configured");
            }
            return code;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(email);
            message.setSubject("UTMS Registration Verification Code");
            message.setText("Your verification code is: " + code + "\n\nThis code will expire shortly.");
            mailSender.send(message);
            return devExposeCode ? code : null;
        } catch (Exception ex) {
            log.error("Failed to send verification email to {}", email, ex);
            if (!devExposeCode) {
                throw new IllegalStateException("Failed to send verification email");
            }
            return code;
        }
    }
}
