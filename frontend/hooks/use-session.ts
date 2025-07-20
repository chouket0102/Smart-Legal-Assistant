"use client"

import { useState, useEffect, useCallback } from "react"
import { LegalAssistantAPI } from "@/lib/api"
import type { ConversationHistory } from "@/lib/types"

export const useSession = () => {
  const [sessionId, setSessionId] = useState<string>("")
  const [conversationHistory, setConversationHistory] = useState<ConversationHistory>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    // Generate or retrieve session ID
    const storedSessionId = localStorage.getItem("legal-assistant-session-id")
    if (storedSessionId) {
      setSessionId(storedSessionId)
    } else {
      const newSessionId = `session-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
      setSessionId(newSessionId)
      localStorage.setItem("legal-assistant-session-id", newSessionId)
    }
  }, [])

  const loadConversationHistory = useCallback(async () => {
    if (!sessionId) return
    setLoading(true)
    try {
      const history = await LegalAssistantAPI.getConversationHistory(sessionId)
      setConversationHistory(history)
    } catch (error) {
      console.error("Failed to load conversation history:", error)
    } finally {
      setLoading(false)
    }
  }, [sessionId])

  const clearConversation = useCallback(async () => {
    if (!sessionId) return
    setLoading(true)
    try {
      await LegalAssistantAPI.clearConversationHistory(sessionId)
      setConversationHistory([])
    } catch (error) {
      console.error("Failed to clear conversation:", error)
    } finally {
      setLoading(false)
    }
  }, [sessionId])

  const addToHistory = useCallback((message: string) => {
    setConversationHistory((prev) => [...prev, message])
  }, [])

  useEffect(() => {
    if (sessionId) {
      loadConversationHistory()
    }
  }, [sessionId, loadConversationHistory])

  return {
    sessionId,
    conversationHistory,
    loading,
    clearConversation,
    addToHistory,
    refreshHistory: loadConversationHistory,
  }
}
