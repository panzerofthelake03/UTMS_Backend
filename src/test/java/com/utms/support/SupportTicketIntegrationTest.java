package com.utms.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UC 1.7 - Support Ticket integration tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SupportTicketIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void createTicket_requiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/support/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"subject":"Test","category":"Transfer","message":"Hello"}
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTicket_success() throws Exception {
        String token = registerStudentAndGetToken();

        mockMvc.perform(post("/api/support/tickets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "subject": "Question about transfer",
                          "category": "Transfer",
                          "message": "When is the deadline for transfer applications?"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.ticketStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.subject").value("Question about transfer"));
    }

    @Test
    void createTicket_rateLimiting_blocksSecondRequestWithin2Minutes() throws Exception {
        String token = registerStudentAndGetToken();

        // First request - should succeed
        mockMvc.perform(post("/api/support/tickets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"subject":"First msg","category":"Transfer","message":"First message body here"}
                        """))
                .andExpect(status().isCreated());

        // Second request immediately after - should be rate limited
        mockMvc.perform(post("/api/support/tickets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"subject":"Second msg","category":"Exam","message":"Second message body here"}
                        """))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void createTicket_validationErrors() throws Exception {
        String token = registerStudentAndGetToken();

        // Empty subject
        mockMvc.perform(post("/api/support/tickets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"subject":"","category":"Transfer","message":"Some message"}
                        """))
                .andExpect(status().isBadRequest());

        // Message too long (> 1000 chars)
        String longMsg = "A".repeat(1001);
        mockMvc.perform(post("/api/support/tickets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {"subject":"Test","category":"Transfer","message":"%s"}
                        """, longMsg)))
                .andExpect(status().isBadRequest());
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private String registerStudentAndGetToken() throws Exception {
        String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String email = "support_student_" + unique + "@std.iyte.edu.tr";

        // Register
        String startBody = String.format("""
            {
              "email": "%s",
              "password": "Test1234!",
              "firstName": "Support",
              "lastName": "Student",
              "nationality": "Turkish",
              "dateOfBirth": "2000-01-01",
              "identityDocumentType": "TC_ID",
              "tcIdentityNumber": "12345678901",
              "currentProgram": "Computer Engineering",
              "currentUniversity": "IYTE",
              "captchaId": "00000000-0000-0000-0000-000000000000",
              "captchaAnswer": "AAAA"
            }
            """, email);

        String startResp = mockMvc.perform(post("/api/auth/register/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(startBody))
                .andReturn().getResponse().getContentAsString();
        JsonNode startNode = objectMapper.readTree(startResp);
        String sessionId = startNode.path("data").path("verificationSessionId").asText();
        String devCode = startNode.path("data").path("devVerificationCode").asText();

        mockMvc.perform(post("/api/auth/register/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {"verificationSessionId":"%s","code":"%s"}
                        """, sessionId, devCode)));

        String loginResp = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {"email":"%s","password":"Test1234!"}
                        """, email)))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(loginResp).path("data").path("accessToken").asText();
    }
}
