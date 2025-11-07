package com.ppg.iicsdoc.fixtures;

public class CopilotStreamChunkFixtures {

    public static String validChunkJson(String content) {
        return """
                {
                  "choices": [
                    {
                      "delta": {
                        "content": "%s"
                      }
                    }
                  ]
                }
                """.formatted(content);

    }

    public static String invalidChunkJson() {
        return "Not a valid JSON";
    }
}
