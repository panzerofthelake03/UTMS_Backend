package com.utms.application;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UC 1.6 - Tests for targetDepartment, phone, address, englishProficiencyOption fields.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApplicationContactFieldsTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void createApplication_withContactFields_persistsAndReturns() throws Exception {
        String token = registerStudentAndGetToken();

        String body = """
                {
                  "term": "2026-FALL",
                  "targetDepartment": "Computer Engineering",
                  "phone": "+90 555 123 4567",
                  "address": "123 Main Street, Izmir",
                  "englishProficiencyOption": "DOCUMENT"
                }
                """;

        mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.targetDepartment").value("Computer Engineering"))
                .andExpect(jsonPath("$.data.phone").value("+90 555 123 4567"))
                .andExpect(jsonPath("$.data.address").value("123 Main Street, Izmir"))
                .andExpect(jsonPath("$.data.englishProficiencyOption").value("DOCUMENT"));
    }

    @Test
    void createApplication_invalidPhone_returns400() throws Exception {
        String token = registerStudentAndGetToken();

        String body = """
                {
                  "term": "2026-FALL",
                  "phone": "not-a-phone"
                }
                """;

        mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateApplication_contactFields_updates() throws Exception {
        String token = registerStudentAndGetToken();

        // Create
        String createResp = mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"term":"2026-FALL","englishProficiencyOption":"YDYO_EXAM"}
                        """))
                .andReturn().getResponse().getContentAsString();

        long appId = objectMapper.readTree(createResp).path("data").path("id").asLong();

        // Update with contact fields
        mockMvc.perform(put("/api/applications/" + appId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "term": "2026-FALL",
                          "targetDepartment": "Mechanical Engineering",
                          "phone": "+90 555 987 6543",
                          "address": "456 New Street, Ankara",
                          "englishProficiencyOption": "DOCUMENT"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetDepartment").value("Mechanical Engineering"))
                .andExpect(jsonPath("$.data.englishProficiencyOption").value("DOCUMENT"));
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private String registerStudentAndGetToken() throws Exception {
        String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String email = "contact_student_" + unique + "@std.iyte.edu.tr";

        String startBody = String.format("""
            {
              "email": "%s",
              "password": "Test1234!",
              "firstName": "Test",
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
