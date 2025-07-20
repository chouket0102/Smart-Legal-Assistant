import type { LegalQueryRequest, LegalAnalysisResult, ConversationHistory } from "./types"

const BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api/legal-assistant"

export class LegalAssistantAPI {
  static async healthCheck(): Promise<string> {
    const response = await fetch(`${BASE_URL}/health`)
    if (!response.ok) throw new Error("Health check failed")
    return response.text()
  }

  static async quickAnalysis(query: string): Promise<string> {
    const response = await fetch(`${BASE_URL}/quick-analysis?query=${encodeURIComponent(query)}`, { method: "POST" })
    if (!response.ok) throw new Error("Quick analysis failed")
    return response.text()
  }

  static async fullAnalysis(request: LegalQueryRequest): Promise<LegalAnalysisResult> {
    const response = await fetch(`${BASE_URL}/analyze`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(request),
    })
    if (!response.ok) throw new Error("Full analysis failed")
    return response.json()
  }

  static async analyzeDocument(file: File, analysisType = "COMPREHENSIVE"): Promise<LegalAnalysisResult> {
    const formData = new FormData()
    formData.append("file", file)
    formData.append("analysisType", analysisType)

    const response = await fetch(`${BASE_URL}/analyze-document`, {
      method: "POST",
      body: formData,
    })
    if (!response.ok) throw new Error("Document analysis failed")
    return response.json()
  }

  static async getConversationHistory(sessionId: string): Promise<ConversationHistory> {
    const response = await fetch(`${BASE_URL}/conversation/${sessionId}`)
    if (!response.ok) throw new Error("Failed to get conversation history")
    return response.json()
  }

  static async clearConversationHistory(sessionId: string): Promise<void> {
    const response = await fetch(`${BASE_URL}/conversation/${sessionId}`, {
      method: "DELETE",
    })
    if (!response.ok) throw new Error("Failed to clear conversation history")
  }
}
