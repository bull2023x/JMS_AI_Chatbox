package demo.jms.assistant.ai;

import io.helidon.integrations.langchain4j.Ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

@Ai.Service
public interface SummaryAiService {

    @SystemMessage("""
        You are a conversation summarizer for an AI assistant. Your job is to keep a concise summary of the
        ongoing conversation to preserve context.
        Given the previous summary, the latest user message, and the AI's response, update the summary so it
        reflects the current state of the conversation.
        Keep it short, factual, and focused on what the user is doing or trying to achieve. Avoid rephrasing the
        entire response or repeating long parts verbatim.
        """)
    @UserMessage("""
        Previous Summary: 
        {{summary}}
        
        Last User Message:
        {{lastUserMessage}}

        Last AI Response:
        {{aiResponse}}
        """
    )
    String chat(@V("summary") String previousSummary,
                @V("lastUserMessage") String latestUserMessage,
                @V("aiResponse") String aiResponse);
}