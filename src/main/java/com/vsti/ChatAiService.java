package com.vsti;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface ChatAiService {

    String chat(@UserMessage String message);
}
