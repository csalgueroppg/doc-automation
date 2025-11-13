package com.ppg.iicsdoc.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ppg.iicsdoc.model.metadata.MetadataTag.MetadataTagType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Service
public class TagSuggestionService {
    
    @Data
    @AllArgsConstructor
    public static class TagSuggestion {
        private String tagName;
        private MetadataTagType type;
        private String description;
        private String example;
        private boolean required;
    }

    private final List<TagSuggestion> allSuggestions;

    public TagSuggestionService() {
        this.allSuggestions = initializeSuggestions();
    }

    public List<TagSuggestion> getAllSuggestions() {
        return new ArrayList<>(allSuggestions);
    }

    public List<TagSuggestion> getSuggestionsByCategory(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return getAllSuggestions();
        }

        String lowerPrefix = prefix.toLowerCase();
        return allSuggestions.stream()
            .filter(s -> s.getTagName().toLowerCase().startsWith(lowerPrefix))
            .collect(Collectors.toList());
    }

    public List<TagSuggestion> getRequiredTags() {
        return allSuggestions.stream()
            .filter(TagSuggestion::isRequired)
            .collect(Collectors.toList());
    }

    public String generateTagDocumentation() {
        StringBuilder doc = new StringBuilder();

        doc.append("# Available Metadata Tags\n\n");
        doc.append("This document lists all supported metadata tags");

        List<String> categories = List.of(
            "General", "Requirements", "Performance", "Security",
            "Operations", "Business", "Documentation", "Data"
        );

        for (String category : categories) {
            doc.append("## ").append(category).append("\n");
            List<TagSuggestion> categoryTags = getSuggestionsByCategory(category);

            for (TagSuggestion suggestion : categoryTags) {
                doc.append("### @").append(suggestion.getTagName());
                if (suggestion.isRequired()) {
                    doc.append(" *Required*");
                }

                doc.append("\n\n");
                doc.append(suggestion.getDescription()).append("\n\n");
                doc.append("**Example:**\n```\n");
                doc.append(suggestion.getExample());
                doc.append("\n```\n\n");
            }
        }

        return doc.toString();
    }

    private List<TagSuggestion> initializeSuggestions() {
        List<TagSuggestion> suggestions = new ArrayList<>();

        suggestions.add(new TagSuggestion(
            "purpose", 
            MetadataTagType.PURPOSE,
            "Main purpose or goal of the development",  
            "@purpose Synchronize customer data with Oracle ORM",
            true
        ));

        return suggestions;
    }

    private String getCategoryForType(MetadataTagType type) {
        return switch (type) {
            case PURPOSE, DESCRIPTION, SUMMARY -> "General";
            case SYSTEM_REQUIREMENTS, PREREQUISITES, DEPENDENCIES -> "Requirements";
            case PERFORMANCE, SLA, SCALING, CAPACITY -> "Performance";
            case SECURITY, DATA_CLASSIFICATION, COMPLIANCE -> "Security";
            case MONITORING, ALERTING, ERROR_HANDLING, RETRY_POLICY -> "Operations";
            case BUSINESS_OWNER, STAKEHOLDERS, BUSINESS_IMPACT -> "Business";
            case EXAMPLE, SEE_ALSO, DEPRECATED, SINCE, VERSION -> "Documentation";
            case INPUT_FORMAT, OUTPUT_FORMAT, DATA_SOURCE, DATA_TARGET -> "Data";
            default -> "other";
        };
    }
}
