export interface LegalQueryRequest {
  query: string
  documentType?: "CONTRACT" | "EMPLOYMENT_CONTRACT" | "LEASE_AGREEMENT" | "COMPLIANCE_REVIEW" | "COMPREHENSIVE"
  jurisdiction?: string
  urgency?: "LOW" | "MEDIUM" | "HIGH"
  sessionId?: string
}

export interface LegalAnalysisResult {
  sessionId: string
  query: string
  queryType: "CONTRACT_ANALYSIS" | "COMPLIANCE" | "LEGAL_RESEARCH" | "LEGAL_STRATEGY" | "COMPREHENSIVE"
  timestamp: string
  executiveSummary: string
  contractAnalysis: string
  complianceAnalysis: string
  strategicRecommendations: string
  keyInsights: string[]
  riskAssessment: string
  actionItems: string[]
  confidenceScore: number
  requiresHumanReview: boolean
  status: "SUCCESS" | "ERROR" | "PARTIAL"
}

export interface DocumentAnalysisRequest {
  file: File
  sessionId?: string
  analysisType?: "CONTRACT_REVIEW" | "COMPREHENSIVE"
}

export type ConversationHistory = string[]
