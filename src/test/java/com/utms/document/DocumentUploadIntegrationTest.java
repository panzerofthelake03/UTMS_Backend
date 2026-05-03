package com.utms.document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentUploadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    private String registerAndGetToken() throws Exception {
        String email = "doc-test-" + UUID.randomUUID() + "@iyte.edu.tr";
        String body = "{" +
                "\"email\":\"" + email + "\"," +
                "\"password\":\"securePass123\"," +
                "\"firstName\":\"Test\"," +
                "\"lastName\":\"User\"" +
                "}";

        String resp = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(resp).path("data").path("accessToken").asText();
    }

    private long createDraftApplication(String token) throws Exception {
        String body = "{\"term\":\"2026-FALL\",\"applicationNote\":\"doc upload test\"}";
        String resp = mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(resp);
        return node.path("data").path("id").asLong();
    }

    // -------------------------------------------------------
    // Tests
    // -------------------------------------------------------

    @Test
    void uploadPdfDocument_succeeds() throws Exception {
        String token = registerAndGetToken();
        long appId = createDraftApplication(token);

        byte[] pdfBytes = "%PDF-1.4 minimal test content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "english-cert.pdf", "application/pdf", pdfBytes);

        mockMvc.perform(multipart("/api/applications/{id}/documents", appId)
                        .file(file)
                        .param("documentType", "ENGLISH_CERTIFICATE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.documentType").value("ENGLISH_CERTIFICATE"))
                .andExpect(jsonPath("$.data.mimeType").value("application/pdf"))
                .andExpect(jsonPath("$.data.scanStatus").value("CLEAN"))
                .andExpect(jsonPath("$.data.originalFilename").value("english-cert.pdf"));
    }

    @Test
    void uploadNonPdf_returns422() throws Exception {
        String token = registerAndGetToken();
        long appId = createDraftApplication(token);

        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "fake docx content".getBytes());

        mockMvc.perform(multipart("/api/applications/{id}/documents", appId)
                        .file(file)
                        .param("documentType", "ENGLISH_CERTIFICATE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void uploadOversizedPdf_returns422() throws Exception {
        String token = registerAndGetToken();
        long appId = createDraftApplication(token);

        // 3 MB — exceeds the 2 MB limit
        byte[] bigContent = new byte[3 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "file", "large.pdf", "application/pdf", bigContent);

        mockMvc.perform(multipart("/api/applications/{id}/documents", appId)
                        .file(file)
                        .param("documentType", "ENGLISH_CERTIFICATE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void uploadInfectedFile_returns422() throws Exception {
        String token = registerAndGetToken();
        long appId = createDraftApplication(token);

        MockMultipartFile file = new MockMultipartFile(
                "file", "malware.infected.pdf", "application/pdf",
                "%PDF-1.4 infected".getBytes());

        mockMvc.perform(multipart("/api/applications/{id}/documents", appId)
                        .file(file)
                        .param("documentType", "ENGLISH_CERTIFICATE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void listDocuments_returnsUploadedFiles() throws Exception {
        String token = registerAndGetToken();
        long appId = createDraftApplication(token);

        byte[] pdfBytes = "%PDF-1.4 list test".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "transcript.pdf", "application/pdf", pdfBytes);

        mockMvc.perform(multipart("/api/applications/{id}/documents", appId)
                        .file(file)
                        .param("documentType", "TRANSCRIPT")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/applications/{id}/documents", appId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].documentType").value("TRANSCRIPT"));
    }

    @Test
    void uploadWithoutAuth_returns401() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cert.pdf", "application/pdf", "%PDF-1.4".getBytes());

        mockMvc.perform(multipart("/api/applications/{id}/documents", 999L)
                        .file(file)
                        .param("documentType", "ENGLISH_CERTIFICATE"))
                .andExpect(status().isUnauthorized());
    }
}
