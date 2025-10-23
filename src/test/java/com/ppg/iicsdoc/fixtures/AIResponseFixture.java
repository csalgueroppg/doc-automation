package com.ppg.iicsdoc.fixtures;

import java.util.List;

import com.ppg.iicsdoc.model.ai.AIResponse;

public class AIResponseFixture {
   
    public static AIResponse simpleResponse(String text) {
        AIResponse.Content content = new AIResponse.Content();
        content.setType("text");
        content.setText(text);

        AIResponse response = new AIResponse();
        response.setContent(List.of(content));

        return response;
    }
}
