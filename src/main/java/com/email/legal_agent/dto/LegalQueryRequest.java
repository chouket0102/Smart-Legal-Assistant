package com.email.legal_agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class LegalQueryRequest {

    @NotBlank(message = "Query cannot be blank")
    @Size(min = 10, max = 10000, message = "Query must be between 10 and 10000 characters")

    private String query;


    private String sessionId;


    private String priority = "MEDIUM";


    private String jurisdiction;


    private String analysisType = "COMPREHENSIVE";


    private String context;


    private boolean includeCitations = true;


    private String responseLength = "DETAILED";

    // Constructors
    public LegalQueryRequest() {}

    public LegalQueryRequest(String query) {
        this.query = query;
    }

    public LegalQueryRequest(String query, String sessionId) {
        this.query = query;
        this.sessionId = sessionId;
    }

    // Getters and Setters
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public String getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public boolean isIncludeCitations() {
        return includeCitations;
    }

    public void setIncludeCitations(boolean includeCitations) {
        this.includeCitations = includeCitations;
    }

    public String getResponseLength() {
        return responseLength;
    }

    public void setResponseLength(String responseLength) {
        this.responseLength = responseLength;
    }

    @Override
    public String toString() {
        return "LegalQueryRequest{" +
                "query='" + (query != null ? query.substring(0, Math.min(query.length(), 100)) + "..." : null) + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", priority='" + priority + '\'' +
                ", jurisdiction='" + jurisdiction + '\'' +
                ", analysisType='" + analysisType + '\'' +
                ", includeCitations=" + includeCitations +
                ", responseLength='" + responseLength + '\'' +
                '}';
    }
}