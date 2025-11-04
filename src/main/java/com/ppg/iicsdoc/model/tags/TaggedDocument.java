package com.ppg.iicsdoc.model.tags;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaggedDocument {
    
    private String documentId;
    private String content;
    private List<Tag> tags;
    private TagVerificationResult verificationResult;

    public void addTag(Tag tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }

        tags.add(tag);
    }

    public List<Tag> getProblematicTags() {
        if (tags == null) {
            return List.of();
        }

        return tags.stream()
            .filter(Tag::needsAttention)
            .toList();
    }

    public boolean allTagsValid() {
        if (tags == null || tags.isEmpty()) {
            return true;
        }

        return tags.stream().allMatch(Tag::isValid);
    }
}
