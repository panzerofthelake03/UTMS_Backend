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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StudentWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void dashboardRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/student/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void studentCanCreateUpdateSubmitAndTrackTimeline() throws Exception {
        String token = registerAndGetToken();

        String createBody = """
                {
                  "term":"2026-FALL",
                  "applicationNote":"Initial draft"
                }
                """;

        String createResponse = mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode created = objectMapper.readTree(createResponse);
        long applicationId = created.path("data").path("id").asLong();

        String updateBody = """
                {
                  "term":"2026-FALL",
                  "applicationNote":"Updated draft note"
                }
                """;

        mockMvc.perform(put("/api/applications/{id}", applicationId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.applicationNote").value("Updated draft note"));

        mockMvc.perform(post("/api/applications/{id}/submit", applicationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

        mockMvc.perform(get("/api/applications/{id}/timeline", applicationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].toStatus").value("DRAFT"))
                .andExpect(jsonPath("$.data[1].toStatus").value("SUBMITTED"));

        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalApplications").value(1))
                .andExpect(jsonPath("$.data.latestApplicationStatus").value("SUBMITTED"))
                .andExpect(jsonPath("$.data.hasDraftApplication").value(false));
    }

    private String registerAndGetToken() throws Exception {
        String email = "phase3-student-" + UUID.randomUUID() + "@iyte.edu.tr";
        String registerBody = "{" +
                "\"email\":\"" + email + "\"," +
                "\"password\":\"securePass123\"," +
                "\"firstName\":\"Ali\"," +
                "\"lastName\":\"Yilmaz\"" +
                "}";

        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(registerResponse)
                .path("data")
                .path("accessToken")
                .asText();
    }
}
