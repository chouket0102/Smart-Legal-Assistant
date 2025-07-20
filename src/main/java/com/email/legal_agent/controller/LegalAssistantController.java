package com.email.legal_agent.controller;

import com.email.legal_agent.dto.LegalAnalysisResult;
import com.email.legal_agent.dto.LegalQueryRequest;

import com.email.legal_agent.service.LegalTeamService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/legal-assistant")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LegalAssistantController {

    private static final Logger logger = LoggerFactory.getLogger(LegalAssistantController.class);

    @Autowired
    private LegalTeamService legalTeamService;

    @PostMapping("/analyze")


    public ResponseEntity<LegalAnalysisResult> analyzeLegalQuery(
            @Valid @RequestBody LegalQueryRequest request) {

        try {
            logger.info("Received legal query analysis request: {}", request.getQuery());

            // Generate session ID if not provided
            String sessionId = request.getSessionId() != null ?
                    request.getSessionId() :
                    UUID.randomUUID().toString();

            // Process the query through the legal team
            LegalAnalysisResult result = legalTeamService.processLegalQuery(
                    request.getQuery(),
                    sessionId
            );

            logger.info("Legal analysis completed for session: {}", sessionId);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error processing legal query", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LegalAnalysisResult.error("Failed to process query: " + e.getMessage()));
        }
    }

    @PostMapping("/analyze-document")

    public ResponseEntity<LegalAnalysisResult> analyzeDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "sessionId", required = false) String sessionId,
            @RequestParam(value = "analysisType", defaultValue = "COMPREHENSIVE") String analysisType) {

        try {
            logger.info("Received document analysis request: {}", file.getOriginalFilename());

            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(LegalAnalysisResult.error("No file provided"));
            }

            // Validate file type
            if (!isValidDocumentType(file.getContentType())) {
                return ResponseEntity.badRequest()
                        .body(LegalAnalysisResult.error("Invalid file type. Supported: PDF, DOC, DOCX, TXT"));
            }

            // Generate session ID if not provided
            if (sessionId == null) {
                sessionId = UUID.randomUUID().toString();
            }

            // Extract text from document
            String documentText = extractTextFromDocument(file);

            // Create query for document analysis
            String query = String.format(
                    "Analyze this %s document:\n\nFilename: %s\n\nContent:\n%s",
                    analysisType.toLowerCase(),
                    file.getOriginalFilename(),
                    documentText
            );

            // Process through legal team
            LegalAnalysisResult result = legalTeamService.processLegalQuery(query, sessionId);

            logger.info("Document analysis completed for: {}", file.getOriginalFilename());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error analyzing document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LegalAnalysisResult.error("Failed to analyze document: " + e.getMessage()));
        }
    }

    @GetMapping("/conversation/{sessionId}")

    public ResponseEntity<List<String>> getConversationHistory(
            @PathVariable String sessionId) {

        try {
            List<String> history = legalTeamService.getConversationHistory(sessionId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("Error retrieving conversation history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/conversation/{sessionId}")

    public ResponseEntity<Void> clearConversationHistory(
             @PathVariable String sessionId) {

        try {
            legalTeamService.clearConversationHistory(sessionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error clearing conversation history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/health")

    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Legal Assistant service is running");
    }

    @PostMapping("/quick-analysis")

    public ResponseEntity<String> quickAnalysis(@RequestParam String query) {
        try {
            logger.info("Quick analysis request: {}", query);

            // For quick analysis, just use one agent based on query type
            String result;
            String lowerQuery = query.toLowerCase();

            if (lowerQuery.contains("contract") || lowerQuery.contains("agreement")) {
                // Use contract analyst for quick contract questions
                result = "Quick Contract Analysis: " + query + "\n\n" +
                        "⚠️ For full analysis including compliance and strategic recommendations, use /analyze endpoint";
            } else if (lowerQuery.contains("compliance") || lowerQuery.contains("gdpr") || lowerQuery.contains("ccpa")) {
                result = "Quick Compliance Check: " + query + "\n\n" +
                        "⚠️ For comprehensive compliance analysis, use /analyze endpoint";
            } else {
                result = "Quick Legal Guidance: " + query + "\n\n" +
                        "💡 Recommendation: Use /analyze endpoint for comprehensive multi-agent analysis";
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error in quick analysis", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing quick analysis: " + e.getMessage());
        }
    }

    // Helper methods
    private boolean isValidDocumentType(String contentType) {
        if (contentType == null) return false;

        return contentType.equals("application/pdf") ||
                contentType.equals("application/msword") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.equals("text/plain") ||
                contentType.startsWith("text/");
    }

    private String extractTextFromDocument(MultipartFile file) throws Exception {
        String contentType = file.getContentType();
        byte[] bytes = file.getBytes();

        if (contentType == null) {
            throw new IllegalArgumentException("Unknown file type");
        }

        switch (contentType) {
            case "text/plain":
                return new String(bytes);
            case "application/pdf":
                // For PDF extraction, you would typically use a library like PDFBox
                // For now, return a placeholder
                return "[PDF Content - " + file.getOriginalFilename() + "]\n" +
                        "Note: PDF text extraction requires additional libraries like Apache PDFBox";
            case "application/msword":
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                // For Word document extraction, you would use Apache POI
                return "[Word Document Content - " + file.getOriginalFilename() + "]\n" +
                        "Note: Word document extraction requires Apache POI library";
            default:
                if (contentType.startsWith("text/")) {
                    return new String(bytes);
                }
                throw new IllegalArgumentException("Unsupported file type: " + contentType);
        }
    }

}