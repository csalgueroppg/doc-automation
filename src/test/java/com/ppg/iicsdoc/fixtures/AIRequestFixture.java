package com.ppg.iicsdoc.fixtures;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.ppg.iicsdoc.model.ai.AIRequest;

public class AIRequestFixture {

    public static AIRequest simpleRequest(String content) {
        AIRequest.Message message = new AIRequest.Message("user", content);
        AIRequest request = new AIRequest();

        request.setMessages(List.of(message));
        return request;
    }

    public static AIRequest multiMessageRequest(List<String> contents) {
        List<AIRequest.Message> messages = Arrays.stream(contents.toArray(new String[0]))
                .map(content -> {
                    AIRequest.Message msg = new AIRequest.Message("user", content);
                    return msg;
                })
                .collect(Collectors.toList());

        AIRequest request = new AIRequest();
        request.setMessages(messages);

        return request;
    }
}
