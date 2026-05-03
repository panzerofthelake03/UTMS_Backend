package com.utms.auth;

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
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private RoleRepository roleRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

    @Test
    void loginReturnsAccessAndRefreshTokens() throws Exception {
        String email = "phase2-login-" + UUID.randomUUID() + "@iyte.edu.tr";
        registerStudent(email, "securePass123");
        String loginBody = "{" +
                "\"email\":\"" + email + "\"," +
                "\"password\":\"securePass123\"" +
                "}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString());
    }

    @Test
    void refreshReturnsNewAccessAndRefreshTokens() throws Exception {
        String email = "phase2-refresh-" + UUID.randomUUID() + "@iyte.edu.tr";
        registerStudent(email, "securePass123");
        String loginBody = "{" +
                "\"email\":\"" + email + "\"," +
                "\"password\":\"securePass123\"" +
                "}";

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String refreshToken = loginJson.path("data").path("refreshToken").asText();

        String refreshBody = "{" + "\"refreshToken\":\"" + refreshToken + "\"}";

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString());
    }

    @Test
    void protectedRouteRejectsMissingAndInvalidTokens() throws Exception {
        mockMvc.perform(get("/api/protected/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/protected/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void roleBasedRoutesReturnForbiddenForWrongRoleAndOkForCorrectRole() throws Exception {
        String uniqueEmail = "student-" + UUID.randomUUID() + "@iyte.edu.tr";
        String registerBody = "{" +
                "\"email\":\"" + uniqueEmail + "\"," +
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

        JsonNode registerJson = objectMapper.readTree(registerResponse);
        String studentAccessToken = registerJson.path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/protected/admin")
                        .header("Authorization", "Bearer " + studentAccessToken))
                .andExpect(status().isForbidden());

        String adminEmail = "admin-test-" + UUID.randomUUID() + "@utms.local";
        String adminPassword = "Admin1234!";
        createAdminUser(adminEmail, adminPassword);
        String loginBody = "{" +
                "\"email\":\"" + adminEmail + "\"," +
                "\"password\":\"" + adminPassword + "\"" +
                "}";

        String adminLoginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String adminAccessToken = objectMapper.readTree(adminLoginResponse)
                .path("data")
                .path("accessToken")
                .asText();

        mockMvc.perform(get("/api/protected/admin")
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("admin access granted"));
    }

        private void registerStudent(String email, String password) throws Exception {
                String registerBody = "{" +
                                "\"email\":\"" + email + "\"," +
                                "\"password\":\"" + password + "\"," +
                                "\"firstName\":\"Ali\"," +
                                "\"lastName\":\"Yilmaz\"" +
                                "}";

                mockMvc.perform(post("/api/auth/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(registerBody))
                                .andExpect(status().isCreated());
        }

        private void createAdminUser(String email, String rawPassword) {
                Role adminRole = roleRepository.findByName(RoleConstants.ROLE_ADMIN)
                                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found"));

                User user = new User();
                user.setEmail(email);
                user.setPasswordHash(passwordEncoder.encode(rawPassword));
                user.setFirstName("Admin");
                user.setLastName("Tester");
                user.setActive(true);
                user.getRoles().add(adminRole);
                userRepository.save(user);
        }
}
