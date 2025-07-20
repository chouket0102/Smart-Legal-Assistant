"use client"

import { useState, useEffect } from "react"
import { LegalAssistantAPI } from "@/lib/api"

export const useHealthCheck = () => {
  const [status, setStatus] = useState<"checking" | "healthy" | "unhealthy">("checking")
  const [message, setMessage] = useState<string>("")

  const checkHealth = async () => {
    setStatus("checking")
    try {
      const response = await LegalAssistantAPI.healthCheck()
      setMessage(response)
      setStatus("healthy")
    } catch (error) {
      setMessage("Service unavailable")
      setStatus("unhealthy")
    }
  }

  useEffect(() => {
    checkHealth()
    const interval = setInterval(checkHealth, 30000) // Check every 30 seconds
    return () => clearInterval(interval)
  }, [])

  return { status, message, checkHealth }
}
