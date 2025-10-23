package com.ppg.iicsdoc.fixtures;

public class CopilotStreamChunkFixtures {

    public static String validChunkJson(String content) {
        return String.format("""
                {
                  "choices": [
                    {
                      "delta": {
                        "content": "%s"
                      }
                    }
                  ]
                }
                """, content);

    }

    public static String invalidChunkJson() {
        return "Not a valid JSON";
    }
}
