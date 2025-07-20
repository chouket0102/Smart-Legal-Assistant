"use client"

import type React from "react"

import { useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Loader2, Zap, Copy, CheckCircle } from "lucide-react"
import { useLegalAnalysis } from "@/hooks/use-legal-analysis"
import { useSession } from "@/hooks/use-session"

export function QuickAnalysisForm() {
  const [query, setQuery] = useState("")
  const [result, setResult] = useState<string>("")
  const [copied, setCopied] = useState(false)
  const { quickAnalyze, loading, error } = useLegalAnalysis()
  const { addToHistory } = useSession()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!query.trim()) return

    try {
      const response = await quickAnalyze(query)
      setResult(response)
      addToHistory(`Quick Analysis: ${query}`)
      addToHistory(`Result: ${response}`)
    } catch (err) {
      console.error("Quick analysis failed:", err)
    }
  }

  const copyToClipboard = async () => {
    if (result) {
      await navigator.clipboard.writeText(result)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center space-x-2">
          <Zap className="h-5 w-5 text-yellow-500" />
          <span>Quick Analysis</span>
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <form onSubmit={handleSubmit} className="space-y-4">
          <Textarea
            placeholder="Ask a quick legal question..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            rows={3}
            disabled={loading}
          />
          <Button type="submit" disabled={loading || !query.trim()} className="w-full">
            {loading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Analyzing...
              </>
            ) : (
              "Get Quick Answer"
            )}
          </Button>
        </form>

        {error && (
          <Alert variant="destructive">
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {result && (
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <h4 className="font-medium">Quick Analysis Result</h4>
              <Button variant="ghost" size="sm" onClick={copyToClipboard} className="h-8">
                {copied ? <CheckCircle className="h-4 w-4 text-green-500" /> : <Copy className="h-4 w-4" />}
              </Button>
            </div>
            <div className="p-3 bg-muted rounded-md">
              <p className="text-sm whitespace-pre-wrap">{result}</p>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
