"use client"

import type React from "react"

import { useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Loader2, FileText } from "lucide-react"
import { useLegalAnalysis } from "@/hooks/use-legal-analysis"
import { useSession } from "@/hooks/use-session"
import type { LegalQueryRequest } from "@/lib/types"

interface FullAnalysisFormProps {
  onAnalysisComplete: (result: any) => void
}

export function FullAnalysisForm({ onAnalysisComplete }: FullAnalysisFormProps) {
  const [formData, setFormData] = useState<LegalQueryRequest>({
    query: "",
    documentType: undefined,
    jurisdiction: "",
    urgency: "MEDIUM",
  })

  const { analyze, loading, error } = useLegalAnalysis()
  const { sessionId, addToHistory } = useSession()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!formData.query.trim()) return

    try {
      const request = { ...formData, sessionId }
      const result = await analyze(request)
      onAnalysisComplete(result)
      addToHistory(`Full Analysis: ${formData.query}`)
    } catch (err) {
      console.error("Full analysis failed:", err)
    }
  }

  const updateFormData = (field: keyof LegalQueryRequest, value: any) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center space-x-2">
          <FileText className="h-5 w-5 text-blue-500" />
          <span>Full Legal Analysis</span>
        </CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="query">Legal Query</Label>
            <Textarea
              id="query"
              placeholder="Describe your legal question or situation in detail..."
              value={formData.query}
              onChange={(e) => updateFormData("query", e.target.value)}
              rows={4}
              disabled={loading}
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="documentType">Document Type</Label>
              <Select
                value={formData.documentType || ""}
                onValueChange={(value) => updateFormData("documentType", value || undefined)}
                disabled={loading}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select document type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="CONTRACT">Contract</SelectItem>
                  <SelectItem value="EMPLOYMENT_CONTRACT">Employment Contract</SelectItem>
                  <SelectItem value="LEASE_AGREEMENT">Lease Agreement</SelectItem>
                  <SelectItem value="COMPLIANCE_REVIEW">Compliance Review</SelectItem>
                  <SelectItem value="COMPREHENSIVE">Comprehensive</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="urgency">Urgency Level</Label>
              <Select
                value={formData.urgency || "MEDIUM"}
                onValueChange={(value) => updateFormData("urgency", value)}
                disabled={loading}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="LOW">Low</SelectItem>
                  <SelectItem value="MEDIUM">Medium</SelectItem>
                  <SelectItem value="HIGH">High</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="jurisdiction">Jurisdiction (Optional)</Label>
            <Input
              id="jurisdiction"
              placeholder="e.g., New York, California, Federal"
              value={formData.jurisdiction}
              onChange={(e) => updateFormData("jurisdiction", e.target.value)}
              disabled={loading}
            />
          </div>

          <Button type="submit" disabled={loading || !formData.query.trim()} className="w-full">
            {loading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Analyzing...
              </>
            ) : (
              "Start Full Analysis"
            )}
          </Button>
        </form>

        {error && (
          <Alert variant="destructive" className="mt-4">
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}
      </CardContent>
    </Card>
  )
}
