package com.utms.notification;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Phase5NotificationIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired PasswordEncoder passwordEncoder;

    // -------------------------------------------------------
    // Tests
    // -------------------------------------------------------

    @Test
    void ygkDecision_createsNotificationForStudent() throws Exception {
        String studentToken = registerStudentAndGetToken();
        long appId = createAndSubmitApplication(studentToken);
        runFullPipeline(appId, "ACCEPTED");

        // Student checks their notifications
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].notificationType").value("APPLICATION_RESULT"))
                .andExpect(jsonPath("$.data[0].read").value(false));
    }

    @Test
    void unreadCount_reflectsNewNotification() throws Exception {
        String studentToken = registerStudentAndGetToken();
        long appId = createAndSubmitApplication(studentToken);
        runFullPipeline(appId, "REJECTED");

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(1));
    }

    @Test
    void markAsRead_updatesReadStatus() throws Exception {
        String studentToken = registerStudentAndGetToken();
        long appId = createAndSubmitApplication(studentToken);
        runFullPipeline(appId, "ACCEPTED");

        // Get the notification id
        String listResp = mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long notifId = objectMapper.readTree(listResp).path("data").get(0).path("id").asLong();

        // Mark as read
        mockMvc.perform(post("/api/notifications/{id}/read", notifId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.read").value(true))
                .andExpect(jsonPath("$.data.readAt").isNotEmpty());

        // Unread count is now 0
        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(0));
    }

    @Test
    void anotherUser_cannotMarkOtherUsersNotification() throws Exception {
        String studentToken = registerStudentAndGetToken();
        long appId = createAndSubmitApplication(studentToken);
        runFullPipeline(appId, "ACCEPTED");

        String listResp = mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + studentToken))
                .andReturn().getResponse().getContentAsString();
        long notifId = objectMapper.readTree(listResp).path("data").get(0).path("id").asLong();

        // Different student tries to mark it
        String otherStudentToken = registerStudentAndGetToken();
        mockMvc.perform(post("/api/notifications/{id}/read", notifId)
                        .header("Authorization", "Bearer " + otherStudentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void swaggerUiIsAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    /**
     * Runs the full OIDB → YDYO → YGK pipeline for an already-submitted application.
     */
    private void runFullPipeline(long appId, String finalDecision) throws Exception {
        String oidbEmail = "oidb-p5-" + UUID.randomUUID() + "@utms.local";
        String ydyoEmail = "ydyo-p5-" + UUID.randomUUID() + "@utms.local";
        String ygkEmail  = "ygk-p5-"  + UUID.randomUUID() + "@utms.local";
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
                        .content("{\"decision\":\"APPROVED\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/ygk/applications/{id}/evaluate", appId)
                        .header("Authorization", "Bearer " + ygkToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"languageScore\":70,\"adjustment\":0,\"decision\":\"" + finalDecision + "\"}"))
                .andExpect(status().isOk());
    }

    private String registerStudentAndGetToken() throws Exception {
        String email = "p5-student-" + UUID.randomUUID() + "@iyte.edu.tr";
        String body = "{\"email\":\"" + email + "\",\"password\":\"securePass123\"," +
                "\"firstName\":\"Test\",\"lastName\":\"Student\"}";
        String resp = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).path("data").path("accessToken").asText();
    }

    private long createAndSubmitApplication(String studentToken) throws Exception {
        String createBody = "{\"term\":\"2026-FALL\",\"applicationNote\":\"Phase 5 test\"}";
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
