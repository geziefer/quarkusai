package com.vsti.quarkusai;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class RagChatService {

    @Inject
    ChatAiService aiService;

    @Inject
    EmbeddingModel embeddingModel;

    @Inject
    EmbeddingStore<TextSegment> embeddingStore;

    public ChatResponse chat(String userMessage) {
        // Generate embedding for user query
        Embedding queryEmbedding = embeddingModel.embed(userMessage).content();
        
        // Search for relevant documents with higher threshold
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(3)
                .minScore(0.75)
                .build()).matches();
        
        // Only use matches that actually meet our threshold
        List<EmbeddingMatch<TextSegment>> relevantMatches = matches.stream()
            .filter(match -> match.score() >= 0.75)
            .toList();
        
        // Build context from relevant documents
        String context = relevantMatches.stream()
            .map(match -> match.embedded().text())
            .collect(Collectors.joining("\n\n"));
        
        // Create enhanced prompt with context
        String enhancedPrompt = buildPromptWithContext(userMessage, context);
        
        // Get AI response
        String aiResponse = aiService.chat(enhancedPrompt);
        
        // Only extract sources if we have relevant matches
        List<String> sources = relevantMatches.isEmpty() ? List.of() : relevantMatches.stream()
            .map(match -> match.embedded().metadata().getString("filename"))
            .distinct()
            .collect(Collectors.toList());
        
        return new ChatResponse(aiResponse, sources);
    }

    private String buildPromptWithContext(String userMessage, String context) {
        if (context.isEmpty()) {
            return userMessage;
        }
        
        return String.format("""
            Based on the following context information, please answer the user's question.
            If the context doesn't contain relevant information, say so and provide a general answer.
            
            Context:
            %s
            
            Question: %s
            
            Answer:""", context, userMessage);
    }

    public record ChatResponse(String response, List<String> sources) {}
}
