package com.ppg.iicsdoc.model.metadata;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Metadata {
    
    private String description;
    private String author;
    private String owner;
    private List<MetadataTag> descriptionTags;
    private List<MetadataTag> allTags;
    private String purpose;
    private String summary;
    private List<String> systemRequirements;
    private List<String> prerequisites;
    private List<String> dependencies;

    private PerformanceMetadata performance;
    private SecurityMetadata security;
    private BusinessMetadata business;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PerformanceMetadata {
        private String sla;
        private String scaling;
        private String capacity;
        private String expectedLoad;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SecurityMetadata {
        private String classification;
        private String compliance;
        private List<String> requirements;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BusinessMetadata {
        private String owner;
        private List<String> stakeholders;
        private String impact;
    }
}
