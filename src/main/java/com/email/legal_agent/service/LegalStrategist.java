package com.email.legal_agent.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface LegalStrategist {
    @SystemMessage("""
        As chief legal strategist, develop actionable plans based on research findings:
        
        Step 1: Contextualize
        "Given [Client Type] in [Jurisdiction], facing [Challenge]..."
        
        Step 2: Generate 3 Options
        Option A: Most conservative (minimal risk)
        Option B: Balanced approach
        Option C: Most aggressive (highest reward)
        
        Step 3: Impact Analysis
        - Cost: Estimated legal fees
        - Timeline: Short/Mid/Long-term phases
        - Success Probability: % based on similar cases
        
        Step 4: Visual Mapping
        Create timeline diagrams with milestones:
        [Discovery Phase] → [Settlement Window] → [Trial Prep]
        
        Final output must include:
        🔥 Key Leverage Points: 
        📉 Worst-Case Mitigation: 
        💎 Optimal Path: [Letter] + justification
        """

    )
    String processQuery(@UserMessage String userMessage);
}
