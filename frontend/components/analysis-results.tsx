"use client"

import { useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Progress } from "@/components/ui/progress"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { FileText, Shield, TrendingUp, AlertTriangle, CheckCircle, Copy, Download, Clock, User } from "lucide-react"
import type { LegalAnalysisResult } from "@/lib/types"

interface AnalysisResultsProps {
  result: LegalAnalysisResult
}

export function AnalysisResults({ result }: AnalysisResultsProps) {
  const [copied, setCopied] = useState<string | null>(null)

  const copyToClipboard = async (text: string, section: string) => {
    await navigator.clipboard.writeText(text)
    setCopied(section)
    setTimeout(() => setCopied(null), 2000)
  }

  const exportToPDF = () => {
    // This would integrate with a PDF generation library
    console.log("Exporting to PDF...")
    // For now, we'll create a simple text export
    const content = `
Legal Analysis Report
Generated: ${new Date(result.timestamp).toLocaleString()}
Session ID: ${result.sessionId}

EXECUTIVE SUMMARY
${result.executiveSummary}

CONTRACT ANALYSIS
${result.contractAnalysis}

COMPLIANCE ANALYSIS
${result.complianceAnalysis}

STRATEGIC RECOMMENDATIONS
${result.strategicRecommendations}

RISK ASSESSMENT
${result.riskAssessment}

KEY INSIGHTS
${result.keyInsights.map((insight) => `• ${insight}`).join("\n")}

ACTION ITEMS
${result.actionItems.map((item) => `• ${item}`).join("\n")}
    `

    const blob = new Blob([content], { type: "text/plain" })
    const url = URL.createObjectURL(blob)
    const a = document.createElement("a")
    a.href = url
    a.download = `legal-analysis-${result.sessionId}.txt`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  }

  const getConfidenceColor = (score: number) => {
    if (score >= 80) return "text-green-600"
    if (score >= 60) return "text-yellow-600"
    return "text-red-600"
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case "SUCCESS":
        return "bg-green-100 text-green-800 border-green-200"
      case "PARTIAL":
        return "bg-yellow-100 text-yellow-800 border-yellow-200"
      case "ERROR":
        return "bg-red-100 text-red-800 border-red-200"
      default:
        return "bg-gray-100 text-gray-800 border-gray-200"
    }
  }

  const CopyButton = ({ text, section }: { text: string; section: string }) => (
    <Button variant="ghost" size="sm" onClick={() => copyToClipboard(text, section)} className="h-8">
      {copied === section ? <CheckCircle className="h-4 w-4 text-green-500" /> : <Copy className="h-4 w-4" />}
    </Button>
  )

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">Legal Analysis Results</h2>
          <p className="text-muted-foreground">Generated on {new Date(result.timestamp).toLocaleString()}</p>
        </div>
        <div className="flex items-center space-x-2">
          <Badge className={getStatusColor(result.status)}>{result.status}</Badge>
          <Button variant="outline" size="sm" onClick={exportToPDF}>
            <Download className="h-4 w-4 mr-2" />
            Export PDF
          </Button>
        </div>
      </div>

      {/* Executive Summary */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <FileText className="h-5 w-5 text-blue-500" />
              <span>Executive Summary</span>
            </div>
            <CopyButton text={result.executiveSummary} section="summary" />
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="whitespace-pre-wrap">{result.executiveSummary}</p>
        </CardContent>
      </Card>

      {/* Compliance Scorecard */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <Shield className="h-5 w-5 text-green-500" />
            <span>Compliance Scorecard</span>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <span className="font-medium">Confidence Score</span>
            <span className={`font-bold ${getConfidenceColor(result.confidenceScore)}`}>{result.confidenceScore}%</span>
          </div>
          <Progress value={result.confidenceScore} className="w-full" />

          {result.requiresHumanReview && (
            <Alert>
              <User className="h-4 w-4" />
              <AlertDescription>
                This analysis requires human review due to complexity or potential risks.
              </AlertDescription>
            </Alert>
          )}
        </CardContent>
      </Card>

      {/* Contract Analysis */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <FileText className="h-5 w-5 text-purple-500" />
              <span>Contract Analysis</span>
            </div>
            <CopyButton text={result.contractAnalysis} section="contract" />
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="whitespace-pre-wrap">{result.contractAnalysis}</p>
        </CardContent>
      </Card>

      {/* Strategic Recommendations */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <TrendingUp className="h-5 w-5 text-blue-500" />
              <span>Strategic Recommendations</span>
            </div>
            <CopyButton text={result.strategicRecommendations} section="recommendations" />
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="whitespace-pre-wrap">{result.strategicRecommendations}</p>
        </CardContent>
      </Card>

      {/* Risk Assessment */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <AlertTriangle className="h-5 w-5 text-red-500" />
              <span>Risk Assessment</span>
            </div>
            <CopyButton text={result.riskAssessment} section="risk" />
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="whitespace-pre-wrap">{result.riskAssessment}</p>
        </CardContent>
      </Card>

      {/* Key Insights */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <CheckCircle className="h-5 w-5 text-green-500" />
            <span>Key Insights</span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <ul className="space-y-2">
            {result.keyInsights.map((insight, index) => (
              <li key={index} className="flex items-start space-x-2">
                <CheckCircle className="h-4 w-4 text-green-500 mt-0.5 flex-shrink-0" />
                <span>{insight}</span>
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>

      {/* Action Items */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <Clock className="h-5 w-5 text-orange-500" />
            <span>Action Items</span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <ul className="space-y-2">
            {result.actionItems.map((item, index) => (
              <li key={index} className="flex items-start space-x-2">
                <div className="h-4 w-4 border-2 border-orange-500 rounded mt-0.5 flex-shrink-0" />
                <span>{item}</span>
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>
    </div>
  )
}
