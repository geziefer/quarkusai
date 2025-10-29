package com.vsti.quarkusai;

import dev.langchain4j.service.TokenStream;
import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

import java.util.List;

@Path("/")
public class ChatResource {

    @Inject
    Template chat;

    @Inject
    Template message;

    @Inject
    ChatAiService aiService;

    @Inject
    RagChatService ragChatService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String index() {
        return chat.instance().render();
    }

    @POST
    @Path("/chat")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public String sendMessage(@FormParam("message") String userMessage) {
        RagChatService.ChatResponse response = ragChatService.chat(userMessage);
        return message.data("userMessage", userMessage)
                     .data("botResponse", response.response())
                     .data("sources", response.sources())
                     .render();
    }

    @GET
    @Path("/chat/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void streamMessage(@QueryParam("message") String userMessage, @Context SseEventSink eventSink, @Context Sse sse) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            eventSink.send(sse.newEvent("error", "Message is required"));
            eventSink.close();
            return;
        }
        
        try {
            // For now, simulate streaming by sending the regular response word by word
            String response = aiService.chat(userMessage);
            String[] words = response.split(" ");
            
            for (String word : words) {
                eventSink.send(sse.newEvent("token", word + " "));
                Thread.sleep(100); // Simulate streaming delay
            }
            
            eventSink.send(sse.newEvent("complete", ""));
            eventSink.close();
        } catch (Exception e) {
            eventSink.send(sse.newEvent("error", "Failed to stream: " + e.getMessage()));
            eventSink.close();
        }
    }
}
