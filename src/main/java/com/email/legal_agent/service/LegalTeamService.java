package com.email.legal_agent.service;

import com.email.legal_agent.dto.LegalAnalysisResult;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class LegalTeamService {
    private static final Logger logger = LoggerFactory.getLogger(LegalTeamService.class);

    private final ComplianceAgent complianceAgent;
    private final ContractAnalyst contractAnalyst;
    private final LegalResearcher legalResearcher;
    private final LegalStrategist legalStrategist;
    private final TeamCoordinator teamCoordinator;

    // Conversation memory for maintaining context across agents
    private final Map<String, List<String>> conversationHistory = new HashMap<>();
    private final Map<String, AgentResponse> agentResponses = new HashMap<>();

    public LegalTeamService(@Value("${groq.api.key}") String apiKey,
                            @Value("${groq.api.base-url}") String baseUrl,
                            @Value("${groq.model.name}") String modelName,
                            @Value("${groq.model.temperature:0.1}") Double temperature,
                            @Autowired LegalResearcherTools legalResearcherTools) {

        logger.info("Initializing LegalTeamService with Groq - baseUrl: {}, model: {}", baseUrl, modelName);

        var chatLanguageModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(60))
                .build();

        // Initialize all agents with shared chat model and memory
        legalResearcher = AiServices.builder(LegalResearcher.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .tools(legalResearcherTools)
                .build();

        contractAnalyst = AiServices.builder(ContractAnalyst.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .build();

        complianceAgent = AiServices.builder(ComplianceAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .build();

        legalStrategist = AiServices.builder(LegalStrategist.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .build();

        teamCoordinator = AiServices.builder(TeamCoordinator.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(30)) // Coordinator needs more memory
                .build();

        logger.info("All legal agents initialized successfully with Groq");
    }

    /**
     * Process a legal query through the appropriate agent workflow
     */
    public LegalAnalysisResult processLegalQuery(String query, String sessionId) {
        logger.info("Processing legal query for session: {}", sessionId);

        try {
            // Initialize session history if needed
            conversationHistory.putIfAbsent(sessionId, new ArrayList<>());
            agentResponses.putIfAbsent(sessionId, new AgentResponse());

            // Add query to conversation history
            conversationHistory.get(sessionId).add("USER: " + query);

            // Step 1: Determine query type and route to appropriate agents
            QueryType queryType = determineQueryType(query);
            logger.info("Query type determined: {}", queryType);

            // Step 2: Process query through relevant agents
            AgentResponse responses = processWithAgents(query, queryType, sessionId);

            // Step 3: Coordinate and synthesize responses
            String coordinatedResponse = teamCoordinator.processQuery(
                    buildCoordinatorInput(query, responses, sessionId)
            );

            // Step 4: Build comprehensive result
            LegalAnalysisResult result = buildAnalysisResult(query, queryType, responses, coordinatedResponse, sessionId);

            // Update conversation history
            conversationHistory.get(sessionId).add("ASSISTANT: " + result.getExecutiveSummary());

            return result;

        } catch (Exception e) {
            logger.error("Error processing legal query", e);
            return LegalAnalysisResult.error("Failed to process query: " + e.getMessage());
        }
    }

    /**
     * Determine the type of legal query to route to appropriate agents
     */
    private QueryType determineQueryType(String query) {
        String lowerQuery = query.toLowerCase();

        // Contract-related queries
        if (containsAny(lowerQuery, "contract", "agreement", "nda", "msa", "terms", "clause", "indemnity")) {
            return QueryType.CONTRACT_ANALYSIS;
        }

        // Compliance-related queries
        if (containsAny(lowerQuery, "gdpr", "ccpa", "hipaa", "compliance", "regulation", "violation", "penalty")) {
            return QueryType.COMPLIANCE;
        }

        // Research-related queries
        if (containsAny(lowerQuery, "case law", "precedent", "court", "ruling", "statute", "legal research")) {
            return QueryType.LEGAL_RESEARCH;
        }

        // Strategy-related queries
        if (containsAny(lowerQuery, "strategy", "litigation", "settlement", "negotiation", "options", "recommend")) {
            return QueryType.LEGAL_STRATEGY;
        }

        // Default to comprehensive analysis
        return QueryType.COMPREHENSIVE;
    }

    /**
     * Process query through relevant agents based on type
     */
    private AgentResponse processWithAgents(String query, QueryType queryType, String sessionId) {
        AgentResponse response = agentResponses.get(sessionId);

        switch (queryType) {
            case CONTRACT_ANALYSIS:
                response.contractAnalysis = contractAnalyst.processQuery(query);
                response.complianceAnalysis = complianceAgent.processQuery(buildComplianceQuery(query, response.contractAnalysis));
                response.legalStrategy = legalStrategist.processQuery(buildStrategyQuery(query, response.contractAnalysis, response.complianceAnalysis));
                break;

            case COMPLIANCE:
                response.complianceAnalysis = complianceAgent.processQuery(query);
                response.legalStrategy = legalStrategist.processQuery(buildStrategyQuery(query, null, response.complianceAnalysis));
                break;

            case LEGAL_RESEARCH:
                response.researchFindings = legalResearcher.processQuery(query);
                response.legalStrategy = legalStrategist.processQuery(buildStrategyQuery(query, null, null));
                break;

            case LEGAL_STRATEGY:
                // Strategy queries may need background research
                response.researchFindings = legalResearcher.processQuery(buildResearchQuery(query));
                response.legalStrategy = legalStrategist.processQuery(query);
                break;

            case COMPREHENSIVE:
            default:
                // Full workflow for comprehensive analysis
                response.researchFindings = legalResearcher.processQuery(query);
                response.contractAnalysis = contractAnalyst.processQuery(query);
                response.complianceAnalysis = complianceAgent.processQuery(query);
                response.legalStrategy = legalStrategist.processQuery(
                        buildStrategyQuery(query, response.contractAnalysis, response.complianceAnalysis)
                );
                break;
        }

        return response;
    }

    /**
     * Build input for team coordinator to synthesize responses
     */
    private String buildCoordinatorInput(String originalQuery, AgentResponse responses, String sessionId) {
        StringBuilder input = new StringBuilder();
        input.append("Original Query: ").append(originalQuery).append("\n\n");

        List<String> history = conversationHistory.get(sessionId);
        if (history.size() > 1) {
            input.append("Previous Context:\n");
            history.stream()
                    .skip(Math.max(0, history.size() - 6)) // Last 6 entries
                    .forEach(entry -> input.append(entry).append("\n"));
            input.append("\n");
        }

        input.append("AGENT RESPONSES TO SYNTHESIZE:\n\n");

        if (responses.researchFindings != null) {
            input.append("=== LEGAL RESEARCH FINDINGS ===\n")
                    .append(responses.researchFindings).append("\n\n");
        }

        if (responses.contractAnalysis != null) {
            input.append("=== CONTRACT ANALYSIS ===\n")
                    .append(responses.contractAnalysis).append("\n\n");
        }

        if (responses.complianceAnalysis != null) {
            input.append("=== COMPLIANCE ANALYSIS ===\n")
                    .append(responses.complianceAnalysis).append("\n\n");
        }

        if (responses.legalStrategy != null) {
            input.append("=== STRATEGIC RECOMMENDATIONS ===\n")
                    .append(responses.legalStrategy).append("\n\n");
        }

        return input.toString();
    }

    /**
     * Build comprehensive analysis result
     */
    private LegalAnalysisResult buildAnalysisResult(String query, QueryType queryType,
                                                    AgentResponse responses, String coordinatedResponse,
                                                    String sessionId) {
        LegalAnalysisResult result = new LegalAnalysisResult();
        result.setQuery(query);
        result.setQueryType(queryType);
        result.setSessionId(sessionId);
        result.setExecutiveSummary(coordinatedResponse);
        result.setResearchFindings(responses.researchFindings);
        result.setContractAnalysis(responses.contractAnalysis);
        result.setComplianceAnalysis(responses.complianceAnalysis);
        result.setStrategicRecommendations(responses.legalStrategy);
        result.setTimestamp(new Date());

        // Extract key insights
        result.setKeyInsights(extractKeyInsights(responses));
        result.setRiskAssessment(extractRiskAssessment(responses));
        result.setActionItems(extractActionItems(coordinatedResponse));
        result.setConfidenceScore(calculateOverallConfidence(responses));

        return result;
    }

    // Helper methods for building contextual queries
    private String buildComplianceQuery(String originalQuery, String contractAnalysis) {
        return String.format("Analyze compliance implications for: %s\n\nContract Context:\n%s",
                originalQuery, contractAnalysis != null ? contractAnalysis : "");
    }

    private String buildStrategyQuery(String originalQuery, String contractAnalysis, String complianceAnalysis) {
        StringBuilder strategyQuery = new StringBuilder();
        strategyQuery.append("Develop legal strategy for: ").append(originalQuery);

        if (contractAnalysis != null) {
            strategyQuery.append("\n\nContract Analysis Context:\n").append(contractAnalysis);
        }

        if (complianceAnalysis != null) {
            strategyQuery.append("\n\nCompliance Analysis Context:\n").append(complianceAnalysis);
        }

        return strategyQuery.toString();
    }

    private String buildResearchQuery(String originalQuery) {
        return "Research legal precedents and relevant case law for: " + originalQuery;
    }

    // Utility methods
    private boolean containsAny(String text, String... keywords) {
        return Arrays.stream(keywords).anyMatch(text::contains);
    }

    private List<String> extractKeyInsights(AgentResponse responses) {
        List<String> insights = new ArrayList<>();

        // Extract insights from each agent response
        if (responses.researchFindings != null) {
            insights.addAll(extractInsightsFromText(responses.researchFindings, "research"));
        }
        if (responses.contractAnalysis != null) {
            insights.addAll(extractInsightsFromText(responses.contractAnalysis, "contract"));
        }
        if (responses.complianceAnalysis != null) {
            insights.addAll(extractInsightsFromText(responses.complianceAnalysis, "compliance"));
        }
        if (responses.legalStrategy != null) {
            insights.addAll(extractInsightsFromText(responses.legalStrategy, "strategy"));
        }

        return insights.stream().distinct().limit(5).toList();
    }

    private List<String> extractInsightsFromText(String text, String category) {
        List<String> insights = new ArrayList<>();

        // Look for key patterns that indicate important insights
        Pattern insightPattern = Pattern.compile("(?:Key finding|Important|Critical|Notable):\\s*([^\\n.]+)");
        var matcher = insightPattern.matcher(text);

        while (matcher.find() && insights.size() < 3) {
            insights.add("[" + category.toUpperCase() + "] " + matcher.group(1).trim());
        }

        return insights;
    }

    private String extractRiskAssessment(AgentResponse responses) {
        // Aggregate risk assessments from all agents
        StringBuilder riskAssessment = new StringBuilder("Overall Risk Assessment:\n");

        if (responses.contractAnalysis != null && responses.contractAnalysis.contains("Risk")) {
            riskAssessment.append("• Contract Risk: Identified in analysis\n");
        }

        if (responses.complianceAnalysis != null && responses.complianceAnalysis.contains("violation")) {
            riskAssessment.append("• Compliance Risk: Potential violations detected\n");
        }

        return riskAssessment.toString();
    }

    private List<String> extractActionItems(String coordinatedResponse) {
        List<String> actionItems = new ArrayList<>();

        // Look for numbered action items or bullet points
        Pattern actionPattern = Pattern.compile("(?:Action|Step|Priority)\\s*\\d*:?\\s*([^\\n]+)");
        var matcher = actionPattern.matcher(coordinatedResponse);

        while (matcher.find() && actionItems.size() < 5) {
            actionItems.add(matcher.group(1).trim());
        }

        // If no structured action items found, extract from key recommendations
        if (actionItems.isEmpty()) {
            Pattern recPattern = Pattern.compile("(?:Recommend|Suggest|Should)\\s*([^\\n.]+)");
            matcher = recPattern.matcher(coordinatedResponse);

            while (matcher.find() && actionItems.size() < 3) {
                actionItems.add(matcher.group(1).trim());
            }
        }

        return actionItems;
    }

    private double calculateOverallConfidence(AgentResponse responses) {
        double totalConfidence = 0.0;
        int agentCount = 0;

        // Extract confidence scores from agent responses
        if (responses.researchFindings != null) {
            totalConfidence += extractConfidenceScore(responses.researchFindings);
            agentCount++;
        }
        if (responses.contractAnalysis != null) {
            totalConfidence += extractConfidenceScore(responses.contractAnalysis);
            agentCount++;
        }
        if (responses.complianceAnalysis != null) {
            totalConfidence += extractConfidenceScore(responses.complianceAnalysis);
            agentCount++;
        }

        return agentCount > 0 ? totalConfidence / agentCount : 75.0; // Default confidence
    }

    private double extractConfidenceScore(String text) {
        // Look for confidence percentages in the text
        Pattern confidencePattern = Pattern.compile("(\\d+)%");
        var matcher = confidencePattern.matcher(text);

        double maxConfidence = 75.0; // Default
        while (matcher.find()) {
            try {
                double score = Double.parseDouble(matcher.group(1));
                if (score > maxConfidence) {
                    maxConfidence = score;
                }
            } catch (NumberFormatException e) {
                // Ignore invalid numbers
            }
        }

        return maxConfidence;
    }

    /**
     * Get conversation history for a session
     */
    public List<String> getConversationHistory(String sessionId) {
        return conversationHistory.getOrDefault(sessionId, new ArrayList<>());
    }

    /**
     * Clear conversation history for a session
     */
    public void clearConversationHistory(String sessionId) {
        conversationHistory.remove(sessionId);
        agentResponses.remove(sessionId);
        logger.info("Cleared conversation history for session: {}", sessionId);
    }

    // Inner classes
    private static class AgentResponse {
        String researchFindings;
        String contractAnalysis;
        String complianceAnalysis;
        String legalStrategy;
    }

    public enum QueryType {
        CONTRACT_ANALYSIS,
        COMPLIANCE,
        LEGAL_RESEARCH,
        LEGAL_STRATEGY,
        COMPREHENSIVE
    }
}