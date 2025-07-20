"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Separator } from "@/components/ui/separator"
import { MessageCircle, Trash2, RefreshCw } from "lucide-react"
import { useSession } from "@/hooks/use-session"

export function ConversationSidebar() {
  const { conversationHistory, loading, clearConversation, refreshHistory } = useSession()

  return (
    <Card className="h-full">
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <MessageCircle className="h-5 w-5 text-blue-500" />
            <span>Conversation History</span>
          </div>
          <div className="flex items-center space-x-1">
            <Button variant="ghost" size="sm" onClick={refreshHistory} disabled={loading}>
              <RefreshCw className={`h-4 w-4 ${loading ? "animate-spin" : ""}`} />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              onClick={clearConversation}
              disabled={loading || conversationHistory.length === 0}
            >
              <Trash2 className="h-4 w-4" />
            </Button>
          </div>
        </CardTitle>
      </CardHeader>
      <CardContent className="p-0">
        <ScrollArea className="h-[400px] px-4">
          {conversationHistory.length === 0 ? (
            <div className="text-center text-muted-foreground py-8">
              <MessageCircle className="h-12 w-12 mx-auto mb-4 opacity-50" />
              <p>No conversation history yet</p>
              <p className="text-sm">Start an analysis to see your history here</p>
            </div>
          ) : (
            <div className="space-y-3 pb-4">
              {conversationHistory.map((message, index) => (
                <div key={index}>
                  <div className="text-sm p-3 bg-muted rounded-md">
                    <p className="whitespace-pre-wrap">{message}</p>
                  </div>
                  {index < conversationHistory.length - 1 && <Separator className="my-2" />}
                </div>
              ))}
            </div>
          )}
        </ScrollArea>
      </CardContent>
    </Card>
  )
}
