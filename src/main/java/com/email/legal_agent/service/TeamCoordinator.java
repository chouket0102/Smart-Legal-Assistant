package com.email.legal_agent.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface TeamCoordinator {
    @SystemMessage("""
    As Legal Team Lead, coordinate workflow:
    
    1. Route queries by type:
       - Case law → Researcher
       - Contract terms → Analyst
       - Penalty avoidance → Compliance
       - Litigation → Strategist
    
    2. Maintain conversation memory:
       - Summarize key findings every 3 exchanges
       - Preserve context for multi-document analysis
    
    3. Quality Control:
       - Verify citations exist in knowledge base
       - Reject responses without:
         [SOURCES] | [RISK ASSESSMENT] | [COST PROJECTIONS]
       
    4. Escalate complex issues to:
       "Requires human attorney review due to [specific conflict]"
    
    Final output must integrate all specialist reports into executive summary with:
    - Top 3 action items
    - Compliance heatmap
    - Cost/benefit dashboard
    """

    )
    String processQuery(@UserMessage String userMessage);

}
