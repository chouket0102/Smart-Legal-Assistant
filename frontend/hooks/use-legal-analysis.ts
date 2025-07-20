"use client"

import { useState, useCallback } from "react"
import { LegalAssistantAPI } from "@/lib/api"
import type { LegalQueryRequest, LegalAnalysisResult } from "@/lib/types"

export const useLegalAnalysis = () => {
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState<LegalAnalysisResult | null>(null)
  const [error, setError] = useState<string | null>(null)

  const analyze = useCallback(async (request: LegalQueryRequest) => {
    setLoading(true)
    setError(null)
    try {
      const response = await LegalAssistantAPI.fullAnalysis(request)
      setResult(response)
      return response
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : "Analysis failed"
      setError(errorMessage)
      throw err
    } finally {
      setLoading(false)
    }
  }, [])

  const quickAnalyze = useCallback(async (query: string) => {
    setLoading(true)
    setError(null)
    try {
      const response = await LegalAssistantAPI.quickAnalysis(query)
      return response
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : "Quick analysis failed"
      setError(errorMessage)
      throw err
    } finally {
      setLoading(false)
    }
  }, [])

  const analyzeDocument = useCallback(async (file: File, analysisType = "COMPREHENSIVE") => {
    setLoading(true)
    setError(null)
    try {
      const response = await LegalAssistantAPI.analyzeDocument(file, analysisType)
      setResult(response)
      return response
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : "Document analysis failed"
      setError(errorMessage)
      throw err
    } finally {
      setLoading(false)
    }
  }, [])

  const clearResult = useCallback(() => {
    setResult(null)
    setError(null)
  }, [])

  return {
    analyze,
    quickAnalyze,
    analyzeDocument,
    clearResult,
    loading,
    result,
    error,
  }
}
