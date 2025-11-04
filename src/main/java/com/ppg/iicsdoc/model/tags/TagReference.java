package com.ppg.iicsdoc.model.tags;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagReference {

    private String filePath;
    private Integer startLine;
    private Integer endLine;
    private String xpath;
    private String elementId;
    private String hash;

    public static TagReference file(String filePath) {
        return TagReference.builder()
                .filePath(filePath)
                .build();
    }

    public static TagReference lines(
            String filePath,
            int startLine,
            int endLine) {
        return TagReference.builder()
                .filePath(filePath)
                .startLine(startLine)
                .endLine(endLine)
                .build();
    }

    public static TagReference xpath(String filePath, String xpath) {
        return TagReference.builder()
                .filePath(filePath)
                .xpath(xpath)
                .build();
    }

    public static TagReference elementId(String filePath, String elementId) {
        return TagReference.builder()
                .filePath(filePath)
                .elementId(elementId)
                .build();
    }
}
