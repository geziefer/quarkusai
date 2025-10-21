package com.vsti;

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
    ChatAiService aiService;

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
        String botResponse = aiService.chat(userMessage);
        return message.data("userMessage", userMessage).data("botResponse", botResponse).render();
    }
}
