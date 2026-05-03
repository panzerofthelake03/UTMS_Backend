package com.utms.document;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Simulates an antivirus/malware scan for uploaded files.
 * In production this would delegate to ClamAV or a cloud scanning API.
 *
 * Simulation rules:
 * - Files whose original name ends with ".infected" → INFECTED (for test scenarios)
 * - All other valid PDFs → CLEAN
 */
@Service
public class ScanSimulatorService {

    public String scan(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.toLowerCase().contains(".infected")) {
            return ScanStatus.INFECTED;
        }
        return ScanStatus.CLEAN;
    }
}
