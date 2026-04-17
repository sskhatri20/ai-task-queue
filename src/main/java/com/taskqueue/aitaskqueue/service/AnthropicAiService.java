package com.taskqueue.aitaskqueue.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "ai.provider", havingValue = "anthropic", matchIfMissing = false)
@RequiredArgsConstructor
@Slf4j
public class AnthropicAiService implements AiService {

    private final AnthropicClient anthropicClient;

    @Override
    public String complete(String prompt) {
        log.debug("Sending prompt to Claude: {}", prompt);

        MessageCreateParams params = MessageCreateParams.builder()
                .model("claude-opus-4-7")
                .maxTokens(16000L)
                .addUserMessage(prompt)
                .build();

        Message response = anthropicClient.messages().create(params);

        return response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(textBlock -> textBlock.text())
                .collect(Collectors.joining());
    }
}
