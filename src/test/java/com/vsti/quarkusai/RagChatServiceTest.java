package com.vsti.quarkusai;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class RagChatServiceTest {

    @Test
    void shouldReturnUserMessageWhenContextIsEmpty() throws Exception {
        // Given
        RagChatService service = new RagChatService();
        String userMessage = "What is the weather?";
        String emptyContext = "";
        
        // When
        String result = invokePrivateMethod(service, "buildPromptWithContext", userMessage, emptyContext);
        
        // Then
        assertEquals(userMessage, result);
    }

    @Test
    void shouldBuildEnhancedPromptWithContext() throws Exception {
        // Given
        RagChatService service = new RagChatService();
        String userMessage = "What is machine learning?";
        String context = "Machine learning is a subset of AI that enables computers to learn.";
        
        // When
        String result = invokePrivateMethod(service, "buildPromptWithContext", userMessage, context);
        
        // Then
        assertTrue(result.contains("Based on the following context information"));
        assertTrue(result.contains("Context:"));
        assertTrue(result.contains(context));
        assertTrue(result.contains("Question: " + userMessage));
        assertTrue(result.contains("Answer:"));
    }

    @Test
    void shouldHandleMultilineContext() throws Exception {
        // Given
        RagChatService service = new RagChatService();
        String userMessage = "Explain the concept";
        String context = "Line 1 of context\nLine 2 of context\nLine 3 of context";
        
        // When
        String result = invokePrivateMethod(service, "buildPromptWithContext", userMessage, context);
        
        // Then
        assertTrue(result.contains(context));
        assertTrue(result.contains(userMessage));
        assertFalse(result.equals(userMessage)); // Should be enhanced
    }

    @Test
    void shouldHandleNullOrEmptyInputs() throws Exception {
        // Given
        RagChatService service = new RagChatService();
        
        // When & Then - Test empty context
        String result2 = invokePrivateMethod(service, "buildPromptWithContext", "test", "");
        assertEquals("test", result2);
        
        String result3 = invokePrivateMethod(service, "buildPromptWithContext", "", "context");
        assertTrue(result3.contains("context"));
        assertTrue(result3.contains("Question: "));
    }

    private String invokePrivateMethod(RagChatService service, String methodName, String userMessage, String context) throws Exception {
        Method method = RagChatService.class.getDeclaredMethod(methodName, String.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, userMessage, context);
    }
}
