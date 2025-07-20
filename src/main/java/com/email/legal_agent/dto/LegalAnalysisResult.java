package com.email.legal_agent.dto;

import com.email.legal_agent.service.LegalTeamService;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LegalAnalysisResult {
    private String sessionId;
    private String query;
    private LegalTeamService.QueryType queryType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date timestamp;

    private String executiveSummary;
    private String researchFindings;
    private String contractAnalysis;
    private String complianceAnalysis;
    private String strategicRecommendations;

    private List<String> keyInsights;
    private String riskAssessment;
    private List<String> actionItems;
    private double confidenceScore;

    private boolean requiresHumanReview;
    private String errorMessage;
    private String status;

    // Constructors
    public LegalAnalysisResult() {
        this.timestamp = new Date();
        this.status = "SUCCESS";
        this.requiresHumanReview = false;
    }

    // Static factory method for error results
    public static LegalAnalysisResult error(String errorMessage) {
        LegalAnalysisResult result = new LegalAnalysisResult();
        result.setErrorMessage(errorMessage);
        result.setStatus("ERROR");
        result.setExecutiveSummary("Analysis failed: " + errorMessage);
        return result;
    }

    // Static factory method for human review required
    public static LegalAnalysisResult requiresReview(String reason) {
        LegalAnalysisResult result = new LegalAnalysisResult();
        result.setRequiresHumanReview(true);
        result.setStatus("REQUIRES_REVIEW");
        result.setExecutiveSummary("Human attorney review required: " + reason);
        return result;
    }

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public LegalTeamService.QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(LegalTeamService.QueryType queryType) {
        this.queryType = queryType;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getExecutiveSummary() {
        return executiveSummary;
    }

    public void setExecutiveSummary(String executiveSummary) {
        this.executiveSummary = executiveSummary;
    }

    public String getResearchFindings() {
        return researchFindings;
    }

    public void setResearchFindings(String researchFindings) {
        this.researchFindings = researchFindings;
    }

    public String getContractAnalysis() {
        return contractAnalysis;
    }

    public void setContractAnalysis(String contractAnalysis) {
        this.contractAnalysis = contractAnalysis;
    }

    public String getComplianceAnalysis() {
        return complianceAnalysis;
    }

    public void setComplianceAnalysis(String complianceAnalysis) {
        this.complianceAnalysis = complianceAnalysis;
    }

    public String getStrategicRecommendations() {
        return strategicRecommendations;
    }

    public void setStrategicRecommendations(String strategicRecommendations) {
        this.strategicRecommendations = strategicRecommendations;
    }

    public List<String> getKeyInsights() {
        return keyInsights;
    }

    public void setKeyInsights(List<String> keyInsights) {
        this.keyInsights = keyInsights;
    }

    public String getRiskAssessment() {
        return riskAssessment;
    }

    public void setRiskAssessment(String riskAssessment) {
        this.riskAssessment = riskAssessment;
    }

    public List<String> getActionItems() {
        return actionItems;
    }

    public void setActionItems(List<String> actionItems) {
        this.actionItems = actionItems;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public boolean isRequiresHumanReview() {
        return requiresHumanReview;
    }

    public void setRequiresHumanReview(boolean requiresHumanReview) {
        this.requiresHumanReview = requiresHumanReview;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "LegalAnalysisResult{" +
                "sessionId='" + sessionId + '\'' +
                ", query='" + query + '\'' +
                ", queryType=" + queryType +
                ", timestamp=" + timestamp +
                ", confidenceScore=" + confidenceScore +
                ", status='" + status + '\'' +
                ", requiresHumanReview=" + requiresHumanReview +
                '}';
    }
}