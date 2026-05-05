package com.utms.auth;

import com.utms.auth.dto.CaptchaChallengeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {

    private final SecureRandom random = new SecureRandom();
    private final Map<String, CaptchaChallenge> challenges = new ConcurrentHashMap<>();

    @Value("${app.auth.captcha.expiration-seconds:180}")
    private long captchaExpirationSeconds;

    public CaptchaChallengeResponse generateChallenge() {
        cleanupExpired();
        int left = random.nextInt(9) + 1;
        int right = random.nextInt(9) + 1;
        String challengeId = UUID.randomUUID().toString();
        String prompt = left + " + " + right + " = ?";

        CaptchaChallenge challenge = new CaptchaChallenge();
        challenge.answer = Integer.toString(left + right);
        challenge.expiresAt = Instant.now().plusSeconds(captchaExpirationSeconds);
        challenges.put(challengeId, challenge);

        return new CaptchaChallengeResponse(challengeId, prompt, captchaExpirationSeconds);
    }

    public void validateAndConsume(String captchaId, String answer) {
        cleanupExpired();
        if (captchaId == null || captchaId.isBlank()) {
            throw new IllegalArgumentException("captchaId is required");
        }
        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("captchaAnswer is required");
        }

        CaptchaChallenge challenge = challenges.remove(captchaId);
        if (challenge == null || challenge.expiresAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("CAPTCHA has expired. Please refresh and try again");
        }

        if (!challenge.answer.equals(answer.trim())) {
            throw new IllegalArgumentException("CAPTCHA answer is incorrect");
        }
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        challenges.entrySet().removeIf(entry -> entry.getValue().expiresAt.isBefore(now));
    }

    private static class CaptchaChallenge {
        private String answer;
        private Instant expiresAt;
    }
}
