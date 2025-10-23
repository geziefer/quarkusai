package com.vsti.quarkusai;

import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

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
        // Temporarily bypass RAG to test basic chat
        String botResponse = aiService.chat(userMessage);
        return message.data("userMessage", userMessage)
                     .data("botResponse", botResponse)
                     .data("sources", List.of()) // Empty sources for now
                     .render();
    }
}
