package com.vsti.quarkusai;

import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class ChatResource {

    @Inject
    Template chat;

    @Inject
    Template message;

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
}
