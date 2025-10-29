package com.vsti.quarkusai;

import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface StreamingChatAiService {

    TokenStream chatStream(@UserMessage String message);
}
