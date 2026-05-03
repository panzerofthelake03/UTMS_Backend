package com.utms.oidb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utms.common.security.RoleConstants;
import com.utms.user.Role;
import com.utms.user.RoleRepository;
import com.utms.user.User;
import com.utms.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Phase4AdminWorkflowIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired PasswordEncoder passwordEncoder;

    // -------------------------------------------------------
    // Full workflow: Student → OIDB → YDYO → YGK
    // -------------------------------------------------------

    @Test
    void fullAdminWorkflow_studentSubmit_oidbForward_ydyoApprove_ygkAccept() throws Exception {

        // 1. Student submits application
        String studentToken = registerStudentAndGetToken();
        long applicationId = createAndSubmitApplication(studentToken);

        // 2. Create staff users
        String oidbEmail = "oidb-" + UUID.randomUUID() + "@utms.local";
        String ydyoEmail = "ydyo-" + UUID.randomUUID() + "@utms.local";
        String ygkEmail  = "ygk-"  + UUID.randomUUID() + "@utms.local";
        createStaffUser(oidbEmail, RoleConstants.ROLE_OIDB);
        createStaffUser(ydyoEmail, RoleConstants.ROLE_YDYO);
        createStaffUser(ygkEmail,  RoleConstants.ROLE_YGK);

        String oidbToken = loginAndGetToken(oidbEmail);
        String ydyoToken = loginAndGetToken(ydyoEmail);
        String ygkToken  = loginAndGetToken(ygkEmail);

        // 3. OIDB lists submitted applications
        mockMvc.perform(get("/api/oidb/applications")
                        .header("Authorization", "Bearer " + oidbToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 4. OIDB takes review → UNDER_OIDB_REVIEW
        mockMvc.perform(post("/api/oidb/applications/{id}/take-review", applicationId)
                        .header("Authorization", "Bearer " + oidbToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNDER_OIDB_REVIEW"));

        // 5. OIDB forwards to YDYO → UNDER_YDYO_REVIEW
        mockMvc.perform(post("/api/oidb/applications/{id}/forward-ydyo", applicationId)
                        .header("Authorization", "Bearer " + oidbToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"Documents look complete, forwarding to YDYO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNDER_YDYO_REVIEW"));

        // 6. YDYO reviews English document → APPROVED → UNDER_YGK_REVIEW
        mockMvc.perform(post("/api/ydyo/applications/{id}/english-review", applicationId)
                        .header("Authorization", "Bearer " + ydyoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"APPROVED\",\"reviewerNote\":\"TOEFL score valid\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNDER_YGK_REVIEW"));

        // 7. YGK submits evaluation with composite score → ACCEPTED
        mockMvc.perform(post("/api/ygk/applications/{id}/evaluate", applicationId)
                        .header("Authorization", "Bearer " + ygkToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"languageScore\":85,\"adjustment\":2,\"evaluatorNote\":\"Strong candidate\",\"decision\":\"ACCEPTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.decision").value("ACCEPTED"))
                .andExpect(jsonPath("$.data.compositeScore").isNumber());

        // 8. Student timeline now shows full history
        mockMvc.perform(get("/api/applications/{id}/timeline", applicationId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(6));
    }

    @Test
    void oidbEndpoints_rejectStudentRole() throws Exception {
        String studentToken = registerStudentAndGetToken();
        mockMvc.perform(get("/api/oidb/applications")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void ydyoReview_rejectInvalidDecision() throws Exception {
        String studentToken = registerStudentAndGetToken();
        long appId = createAndSubmitApplication(studentToken);

        String oidbEmail = "oidb-inv-" + UUID.randomUUID() + "@utms.local";
        String ydyoEmail = "ydyo-inv-" + UUID.randomUUID() + "@utms.local";
        createStaffUser(oidbEmail, RoleConstants.ROLE_OIDB);
        createStaffUser(ydyoEmail, RoleConstants.ROLE_YDYO);

        String oidbToken = loginAndGetToken(oidbEmail);
        String ydyoToken = loginAndGetToken(ydyoEmail);

        // Move to YDYO review via OIDB
        mockMvc.perform(post("/api/oidb/applications/{id}/take-review", appId)
                        .header("Authorization", "Bearer " + oidbToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/oidb/applications/{id}/forward-ydyo", appId)
                        .header("Authorization", "Bearer " + oidbToken))
                .andExpect(status().isOk());

        // Invalid decision value
        mockMvc.perform(post("/api/ydyo/applications/{id}/english-review", appId)
                        .header("Authorization", "Bearer " + ydyoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"INVALID_DECISION\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void compositeScore_calculatedCorrectly() throws Exception {
        // GPA=3.60, language=80, adjustment=0 → (3.60/4.00)*60 + (80/100)*40 = 54 + 32 = 86.00
        String studentToken = registerStudentAndGetToken();
        long appId = createAndSubmitApplication(studentToken);

        String oidbEmail = "oidb-score-" + UUID.randomUUID() + "@utms.local";
        String ydyoEmail = "ydyo-score-" + UUID.randomUUID() + "@utms.local";
        String ygkEmail  = "ygk-score-"  + UUID.randomUUID() + "@utms.local";
        createStaffUser(oidbEmail, RoleConstants.ROLE_OIDB);
        createStaffUser(ydyoEmail, RoleConstants.ROLE_YDYO);
        createStaffUser(ygkEmail,  RoleConstants.ROLE_YGK);

        String oidbToken = loginAndGetToken(oidbEmail);
        String ydyoToken = loginAndGetToken(ydyoEmail);
        String ygkToken  = loginAndGetToken(ygkEmail);

        mockMvc.perform(post("/api/oidb/applications/{id}/take-review", appId)
                        .header("Authorization", "Bearer " + oidbToken)).andExpect(status().isOk());
        mockMvc.perform(post("/api/oidb/applications/{id}/forward-ydyo", appId)
                        .header("Authorization", "Bearer " + oidbToken)).andExpect(status().isOk());
        mockMvc.perform(post("/api/ydyo/applications/{id}/english-review", appId)
                        .header("Authorization", "Bearer " + ydyoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"APPROVED\"}")).andExpect(status().isOk());

        // GPA defaults to 0 for auto-created student; expect (0/4)*60 + (80/100)*40 = 32.00
        String resp = mockMvc.perform(post("/api/ygk/applications/{id}/evaluate", appId)
                        .header("Authorization", "Bearer " + ygkToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"languageScore\":80,\"adjustment\":0,\"decision\":\"REJECTED\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode eval = objectMapper.readTree(resp).path("data");
        double score = eval.path("compositeScore").asDouble();
        assert score == 32.00 : "Expected composite score 32.00 but got " + score;
    }

    @Test
    void intibak_addAndDecideExemption() throws Exception {
        String studentToken = registerStudentAndGetToken();
        long appId = createAndSubmitApplication(studentToken);

        String oidbEmail = "oidb-intibak-" + UUID.randomUUID() + "@utms.local";
        createStaffUser(oidbEmail, RoleConstants.ROLE_OIDB);
        String oidbToken = loginAndGetToken(oidbEmail);

        // Add a course mapping
        String addBody = """
                {
                  "studentCourseCode":"CENG101",
                  "studentCourseName":"Intro to Programming",
                  "studentCourseCredits":3,
                  "studentCourseGrade":"A",
                  "targetCourseCode":"BIL101",
                  "targetCourseName":"Introduction to Computer Science",
                  "targetCourseCredits":3
                }
                """;

        String addResp = mockMvc.perform(post("/api/intibak/{id}/exemptions", appId)
                        .header("Authorization", "Bearer " + oidbToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.decision").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        long exemptionId = objectMapper.readTree(addResp).path("data").path("id").asLong();

        // Record decision
        mockMvc.perform(put("/api/intibak/{appId}/exemptions/{id}/decide", appId, exemptionId)
                        .header("Authorization", "Bearer " + oidbToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"EXEMPT\",\"decisionNote\":\"Equivalent course confirmed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.decision").value("EXEMPT"))
                .andExpect(jsonPath("$.data.decidedByEmail").isString());
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    private String registerStudentAndGetToken() throws Exception {
        String email = "phase4-student-" + UUID.randomUUID() + "@iyte.edu.tr";
        String body = "{\"email\":\"" + email + "\",\"password\":\"securePass123\"," +
                "\"firstName\":\"Test\",\"lastName\":\"Student\"}";
        String resp = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).path("data").path("accessToken").asText();
    }

    private long createAndSubmitApplication(String studentToken) throws Exception {
        String createBody = "{\"term\":\"2026-FALL\",\"applicationNote\":\"Phase 4 test\"}";
        String createResp = mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long appId = objectMapper.readTree(createResp).path("data").path("id").asLong();

        mockMvc.perform(post("/api/applications/{id}/submit", appId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());
        return appId;
    }

    private void createStaffUser(String email, String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException(roleName + " not seeded"));
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("StaffPass123!"));
        user.setFirstName("Staff");
        user.setLastName("User");
        user.setActive(true);
        user.getRoles().add(role);
        userRepository.save(user);
    }

    private String loginAndGetToken(String email) throws Exception {
        String body = "{\"email\":\"" + email + "\",\"password\":\"StaffPass123!\"}";
        String resp = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).path("data").path("accessToken").asText();
    }
}
