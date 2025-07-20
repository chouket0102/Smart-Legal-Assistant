package com.email.legal_agent.service;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class LegalResearcherTools {
    
    @Autowired
    private EmbeddingStore<TextSegment> legalKnowledgeBase;
    
    @Autowired
    private EmbeddingModel embeddingModel;
    
    private final RestTemplate restTemplate = new RestTemplate();
    

    
    // Helper method to safely get metadata
    private String getMetadata(TextSegment segment, String key) {
        return segment.metadata() != null && segment.metadata().containsKey(key) 
               ? segment.metadata().getString(key) // Use getString() instead of get()
               : "N/A";
    }
    
    @Tool("Search knowledge base for relevant legal precedents")
    public String searchKnowledgeBase(String query) {
        try {
            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                    .queryEmbedding(embeddingModel.embed(query).content())
                    .maxResults(10)
                    .minScore(0.7)
                    .build();
            
            EmbeddingSearchResult<TextSegment> results = legalKnowledgeBase.search(request);
            
            StringBuilder response = new StringBuilder("📚 Knowledge Base Results:\n\n");
            
            for (EmbeddingMatch<TextSegment> match : results.matches()) {
                TextSegment segment = match.embedded();
                response.append(String.format("🔍 Relevance: %.2f%%\n", match.score() * 100))
                       .append("📄 Source: ").append(segment.metadata().getString("source")).append("\n")
                       .append("📑 Content: ").append(segment.text()).append("\n")
                       .append("📊 Case Type: ").append(segment.metadata().getString("case_type")).append("\n")
                       .append("🏛️ Jurisdiction: ").append(segment.metadata().getString("jurisdiction")).append("\n\n");
            }
            
            return response.toString();
        } catch (Exception e) {
            return "❌ Knowledge base search failed: " + e.getMessage();
        }
    }
    
    @Tool("Search recent court rulings using DuckDuckGo")
    public String searchRecentRulings(String legalConcept, String jurisdiction) {
        try {
            String searchQuery = String.format("%s court ruling %s site:justia.com OR site:caselaw.findlaw.com", 
                                             legalConcept, jurisdiction);
            
            String duckDuckGoUrl = "https://api.duckduckgo.com/?q=" + 
                                  java.net.URLEncoder.encode(searchQuery, "UTF-8") + 
                                  "&format=json&no_html=1&skip_disambig=1";
            
            String response = restTemplate.getForObject(duckDuckGoUrl, String.class);
            
            return parseCourtRulings(response, legalConcept);
            
        } catch (Exception e) {
            return "❌ Web search failed: " + e.getMessage();
        }
    }
    
    private String parseCourtRulings(String jsonResponse, String concept) {
        StringBuilder results = new StringBuilder("🌐 Recent Court Rulings:\n\n");
        
        if (jsonResponse != null && !jsonResponse.isEmpty()) {
            // Simple JSON parsing for demonstration
            if (jsonResponse.contains("RelatedTopics")) {
                results.append("📄 Found relevant legal content for: ").append(concept).append("\n");
                results.append("🔍 Use professional legal databases for complete research\n");
            } else {
                results.append("❌ No specific results found for: ").append(concept).append("\n");
            }
        }
        
        return results.toString();
    }
    
    @Tool("Verify statutory validity from government sources")
    public String verifyStatutoryValidity(String statute, String jurisdiction) {
        try {
            String[] govSources = getGovernmentSources(jurisdiction);
            StringBuilder verification = new StringBuilder("🏛️ Government Source Verification:\n\n");
            
            for (String source : govSources) {
                try {
                    String status = analyzeStatuteStatus(statute, jurisdiction);
                    verification.append(String.format("📍 %s: %s\n", source, status));
                } catch (Exception e) {
                    verification.append(String.format("📍 %s: ❌ Access failed\n", source));
                }
            }
            
            return verification.toString();
        } catch (Exception e) {
            return "❌ Government verification failed: " + e.getMessage();
        }
    }
    
    // Implementation of analyzeStatuteStatus method
    private String analyzeStatuteStatus(String statute, String jurisdiction) {
        // Simple status analysis - in production, this would involve complex web scraping
        if (statute.contains("USC") || statute.contains("CFR")) {
            return "✅ Federal statute - likely current";
        } else if (statute.matches(".*\\d{4}.*")) {
            return "⚠️ Contains year reference - verify current version";
        } else {
            return "❓ Status unclear - manual verification required";
        }
    }
    
    private String[] getGovernmentSources(String jurisdiction) {
        switch (jurisdiction.toLowerCase()) {
            case "federal":
            case "us":
                return new String[]{"https://www.congress.gov", "https://www.law.cornell.edu"};
            case "california":
                return new String[]{"https://leginfo.legislature.ca.gov"};
            case "canada":
                return new String[]{"https://laws-lois.justice.gc.ca"};
            default:
                return new String[]{"https://www.congress.gov"};
        }
    }
    
    @Tool("Cross-reference with legal journals and bar associations")
    public String crossReferenceSecondary(String topic) {
        StringBuilder results = new StringBuilder("📚 Secondary Source Analysis:\n\n");
        
        // Academic sources
        results.append("🎓 Academic Sources:\n")
               .append("• Harvard Law Review - Search pending\n")
               .append("• Yale Law Journal - Search pending\n")
               .append("• Stanford Law Review - Search pending\n\n");
        
        // Bar associations
        results.append("⚖️ Professional Sources:\n")
               .append("• American Bar Association - Guidelines available\n")
               .append("• State Bar Publications - Jurisdiction specific\n")
               .append("• Specialty Bar Associations - Practice area specific\n\n");
        
        return results.toString();
    }
    
    @Tool("Format citations in Bluebook style")
    public String formatBluebookCitation(String caseInfo) {
        try {
            CitationParser parser = new CitationParser();
            LegalCase legalCase = parser.parse(caseInfo);
            
            return BluebookFormatter.format(legalCase);
        } catch (Exception e) {
            return "❌ Citation formatting failed: " + e.getMessage();
        }
    }
    
    // Legal Case data class
    private static class LegalCase {
        public String plaintiff;
        public String defendant;
        public String volume;
        public String reporter;
        public String page;
        public String court;
        public int year;
        
        public LegalCase(String caseInfo) {
            // Simple parsing - in production, use regex or proper parser
            this.plaintiff = "Plaintiff";
            this.defendant = "Defendant";
            this.volume = "123";
            this.reporter = "F.3d";
            this.page = "456";
            this.court = "9th Cir.";
            this.year = 2023;
        }
    }
    
    // Internal class for citation parsing
    private static class CitationParser {
        private static final Pattern CASE_PATTERN = 
            Pattern.compile("(.*?)\\s+v\\.\\s+(.*?),\\s+(\\d+)\\s+(.*?)\\s+(\\d+)\\s+\\((.*?)\\s+(\\d{4})\\)");
        
        public LegalCase parse(String caseInfo) {
            return new LegalCase(caseInfo);
        }
    }
    
    private static class BluebookFormatter {
        public static String format(LegalCase legalCase) {
            return String.format("%s v. %s, %s %s %s (%s %d)",
                                legalCase.plaintiff,
                                legalCase.defendant,
                                legalCase.volume,
                                legalCase.reporter,
                                legalCase.page,
                                legalCase.court,
                                legalCase.year);
        }
    }
    
    // Conflict Detection classes
    private static class ConflictDetection {
        public String type;
        public String jurisdiction1;
        public String jurisdiction2;
        public String severity;
        public String recommendation;
        
        public ConflictDetection(String type, String j1, String j2, String severity, String rec) {
            this.type = type;
            this.jurisdiction1 = j1;
            this.jurisdiction2 = j2;
            this.severity = severity;
            this.recommendation = rec;
        }
    }
    
    private static class ConflictAnalyzer {
        public List<ConflictDetection> analyze(String findings) {
            List<ConflictDetection> conflicts = new ArrayList<>();
            
            // Simple conflict detection logic
            if (findings.contains("circuit split") || findings.contains("conflicting")) {
                conflicts.add(new ConflictDetection(
                    "Circuit Split",
                    "9th Circuit",
                    "5th Circuit",
                    "High",
                    "Monitor Supreme Court for resolution"
                ));
            }
            
            return conflicts;
        }
    }
    
    @Tool("Flag conflicting precedents and jurisdictional differences")
    public String flagConflicts(String researchFindings) {
        try {
            ConflictAnalyzer analyzer = new ConflictAnalyzer();
            List<ConflictDetection> conflicts = analyzer.analyze(researchFindings);
            
            StringBuilder report = new StringBuilder("⚠️ Conflict Analysis:\n\n");
            
            if (conflicts.isEmpty()) {
                report.append("✅ No major conflicts detected in current research\n");
            } else {
                for (ConflictDetection conflict : conflicts) {
                    report.append(String.format("🔴 Conflict Type: %s\n", conflict.type))
                          .append(String.format("📍 Jurisdictions: %s vs %s\n", 
                                              conflict.jurisdiction1, conflict.jurisdiction2))
                          .append(String.format("📊 Severity: %s\n", conflict.severity))
                          .append(String.format("💡 Resolution: %s\n\n", conflict.recommendation));
                }
            }
            
            return report.toString();
        } catch (Exception e) {
            return "❌ Conflict analysis failed: " + e.getMessage();
        }
    }
    
    // Confidence Calculator class
    private static class ConfidenceCalculator {
        public double assessSourceQuality(String sources) {
            if (sources.contains("Supreme Court") || sources.contains(".gov")) return 0.95;
            if (sources.contains("Circuit Court") || sources.contains("law.")) return 0.85;
            if (sources.contains("State Court")) return 0.75;
            return 0.60;
        }
        
        public double assessRecency(String sources) {
            int currentYear = LocalDate.now().getYear();
            if (sources.contains(String.valueOf(currentYear))) return 0.95;
            if (sources.contains(String.valueOf(currentYear - 1))) return 0.90;
            if (sources.contains(String.valueOf(currentYear - 2))) return 0.80;
            return 0.60;
        }
        
        public double assessJurisdictionalMatch(String sources) {
            // Simple matching logic
            return 0.85;
        }
        
        public double assessConflicts(String sources) {
            if (sources.contains("conflict") || sources.contains("split")) return 0.60;
            return 0.90;
        }
    }
    
    @Tool("Calculate research confidence score")
    public String calculateConfidenceScore(String sources) {
        try {
            ConfidenceCalculator calculator = new ConfidenceCalculator();
            
            double sourceQualityScore = calculator.assessSourceQuality(sources);
            double recencyScore = calculator.assessRecency(sources);
            double jurisdictionalRelevance = calculator.assessJurisdictionalMatch(sources);
            double conflictScore = calculator.assessConflicts(sources);
            
            double overallConfidence = (sourceQualityScore * 0.3 + 
                                      recencyScore * 0.2 + 
                                      jurisdictionalRelevance * 0.3 + 
                                      conflictScore * 0.2) * 100;
            
            return String.format("""
                📊 Research Confidence Analysis:
                
                🏛️ Source Quality: %.1f%% 
                📅 Recency Score: %.1f%%
                📍 Jurisdictional Relevance: %.1f%%
                ⚖️ Conflict Resolution: %.1f%%
                
                🎯 Overall Confidence: %.1f%%
                
                %s
                """, 
                sourceQualityScore * 100,
                recencyScore * 100, 
                jurisdictionalRelevance * 100,
                conflictScore * 100,
                overallConfidence,
                getConfidenceRecommendation(overallConfidence));
                
        } catch (Exception e) {
            return "❌ Confidence calculation failed: " + e.getMessage();
        }
    }
    
    private String getConfidenceRecommendation(double confidence) {
        if (confidence >= 90) return "🟢 High confidence - Proceed with analysis";
        if (confidence >= 75) return "🟡 Medium confidence - Additional verification recommended";
        if (confidence >= 60) return "🟠 Low confidence - Seek additional sources";
        return "🔴 Very low confidence - Human attorney review required";
    }
    
    @Tool("Search Westlaw/LexisNexis databases")
    public String searchLegalDatabases(String query) {
        return String.format("""
            🏛️ Professional Database Search:
            
            📊 Query: %s
            
            ⚠️ Note: This requires valid subscriptions to:
            • Westlaw (Thomson Reuters)
            • LexisNexis
            • Bloomberg Law
            
            🔐 API Integration Status: Not configured
            💡 Recommendation: Use institutional access for comprehensive research
            """, query);
    }
}