package com.hris.controller;

import com.hris.model.ContractHistory;
import com.hris.repository.ContractHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Controller untuk file downloads
 */
@Controller
@RequiredArgsConstructor
public class FileController {

    private final ContractHistoryRepository contractHistoryRepository;

    /**
     * Generic file serving endpoint
     * Serves any uploaded file (photos, logos, stamps, etc.)
     */
    @GetMapping("/files/**")
    public ResponseEntity<Resource> serveFile() {
        // Extract the file path from the request
        String filePath = (String) org.springframework.web.servlet.HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE;

        if (filePath == null || filePath.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Remove the "/files/" prefix
        String actualPath = filePath.substring(7);

        try {
            Path path = Paths.get(actualPath);
            if (!Files.exists(path) || !Files.isReadable(path)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(path);

            // Determine content type
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(resource.contentLength())
                .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Download contract document
     */
    @GetMapping("/files/contract-docs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Resource> downloadContractDocument(@PathVariable Long id) {
        Optional<ContractHistory> historyOpt = contractHistoryRepository.findById(id);
        if (historyOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ContractHistory history = historyOpt.get();
        String documentPath = history.getDocumentPath();

        if (documentPath == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path path = Paths.get(documentPath);
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(path);
            String filename = "SK_PKWT_" + id + ".pdf";

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(resource.contentLength())
                .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
