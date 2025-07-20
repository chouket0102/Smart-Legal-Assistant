"use client"

import { useHealthCheck } from "@/hooks/use-health-check"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { RefreshCw, CheckCircle, XCircle, Clock } from "lucide-react"

export function HealthIndicator() {
  const { status, message, checkHealth } = useHealthCheck()

  const getStatusIcon = () => {
    switch (status) {
      case "healthy":
        return <CheckCircle className="h-4 w-4 text-green-500" />
      case "unhealthy":
        return <XCircle className="h-4 w-4 text-red-500" />
      default:
        return <Clock className="h-4 w-4 text-yellow-500" />
    }
  }

  const getStatusColor = () => {
    switch (status) {
      case "healthy":
        return "bg-green-100 text-green-800 border-green-200"
      case "unhealthy":
        return "bg-red-100 text-red-800 border-red-200"
      default:
        return "bg-yellow-100 text-yellow-800 border-yellow-200"
    }
  }

  return (
    <Card>
      <CardContent className="p-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            {getStatusIcon()}
            <span className="font-medium">Service Status</span>
          </div>
          <div className="flex items-center space-x-2">
            <Badge className={getStatusColor()}>{status === "checking" ? "Checking..." : status}</Badge>
            <Button variant="ghost" size="sm" onClick={checkHealth} disabled={status === "checking"}>
              <RefreshCw className={`h-4 w-4 ${status === "checking" ? "animate-spin" : ""}`} />
            </Button>
          </div>
        </div>
        <p className="text-sm text-muted-foreground mt-2">{message}</p>
      </CardContent>
    </Card>
  )
}
