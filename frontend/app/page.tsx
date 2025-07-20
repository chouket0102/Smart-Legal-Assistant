"use client"

import { useState } from "react"
import { HealthIndicator } from "@/components/health-indicator"
import { QuickAnalysisForm } from "@/components/quick-analysis-form"
import { FullAnalysisForm } from "@/components/full-analysis-form"
import { DocumentUpload } from "@/components/document-upload"
import { AnalysisResults } from "@/components/analysis-results"
import { ConversationSidebar } from "@/components/conversation-sidebar"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Scale, Brain, X } from "lucide-react"
import type { LegalAnalysisResult } from "@/lib/types"

export default function Home() {
  const [analysisResult, setAnalysisResult] = useState<LegalAnalysisResult | null>(null)
  const [showSidebar, setShowSidebar] = useState(true)

  const handleAnalysisComplete = (result: LegalAnalysisResult) => {
    setAnalysisResult(result)
  }

  const clearResults = () => {
    setAnalysisResult(null)
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      <div className="container mx-auto p-4">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <div className="p-2 bg-blue-600 rounded-lg">
                <Scale className="h-8 w-8 text-white" />
              </div>
              <div>
                <h1 className="text-3xl font-bold text-gray-900">Smart Legal Assistant</h1>
                <p className="text-gray-600">AI-powered legal analysis with multiple specialized agents</p>
              </div>
            </div>
            <div className="flex items-center space-x-2">
              <Button variant="outline" onClick={() => setShowSidebar(!showSidebar)} className="lg:hidden">
                {showSidebar ? "Hide" : "Show"} History
              </Button>
              {analysisResult && (
                <Button variant="outline" onClick={clearResults}>
                  <X className="h-4 w-4 mr-2" />
                  Clear Results
                </Button>
              )}
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
          {/* Main Content */}
          <div className={`${showSidebar ? "lg:col-span-3" : "lg:col-span-4"} space-y-6`}>
            {/* Health Status */}
            <HealthIndicator />

            {!analysisResult ? (
              <>
                {/* Analysis Forms */}
                <Tabs defaultValue="quick" className="w-full">
                  <TabsList className="grid w-full grid-cols-3">
                    <TabsTrigger value="quick">Quick Analysis</TabsTrigger>
                    <TabsTrigger value="full">Full Analysis</TabsTrigger>
                    <TabsTrigger value="document">Document Upload</TabsTrigger>
                  </TabsList>

                  <TabsContent value="quick" className="mt-6">
                    <QuickAnalysisForm />
                  </TabsContent>

                  <TabsContent value="full" className="mt-6">
                    <FullAnalysisForm onAnalysisComplete={handleAnalysisComplete} />
                  </TabsContent>

                  <TabsContent value="document" className="mt-6">
                    <DocumentUpload onAnalysisComplete={handleAnalysisComplete} />
                  </TabsContent>
                </Tabs>

                {/* Features Overview */}
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center space-x-2">
                      <Brain className="h-5 w-5 text-purple-500" />
                      <span>AI Legal Agents</span>
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                      <div className="p-4 border rounded-lg">
                        <h3 className="font-semibold text-blue-600">Contract Analyzer</h3>
                        <p className="text-sm text-muted-foreground mt-1">
                          Reviews contracts for key terms, risks, and compliance issues
                        </p>
                      </div>
                      <div className="p-4 border rounded-lg">
                        <h3 className="font-semibold text-green-600">Compliance Checker</h3>
                        <p className="text-sm text-muted-foreground mt-1">
                          Ensures documents meet regulatory requirements
                        </p>
                      </div>
                      <div className="p-4 border rounded-lg">
                        <h3 className="font-semibold text-purple-600">Risk Assessor</h3>
                        <p className="text-sm text-muted-foreground mt-1">
                          Identifies potential legal risks and mitigation strategies
                        </p>
                      </div>
                      <div className="p-4 border rounded-lg">
                        <h3 className="font-semibold text-orange-600">Strategy Advisor</h3>
                        <p className="text-sm text-muted-foreground mt-1">
                          Provides strategic recommendations and action plans
                        </p>
                      </div>
                      <div className="p-4 border rounded-lg">
                        <h3 className="font-semibold text-red-600">Research Assistant</h3>
                        <p className="text-sm text-muted-foreground mt-1">
                          Conducts legal research and precedent analysis
                        </p>
                      </div>
                      <div className="p-4 border rounded-lg">
                        <h3 className="font-semibold text-indigo-600">Document Reviewer</h3>
                        <p className="text-sm text-muted-foreground mt-1">Comprehensive document analysis and review</p>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </>
            ) : (
              /* Analysis Results */
              <AnalysisResults result={analysisResult} />
            )}
          </div>

          {/* Conversation Sidebar */}
          {showSidebar && (
            <div className="lg:col-span-1">
              <ConversationSidebar />
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
