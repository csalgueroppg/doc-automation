package com.ppg.iicsdoc.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.ppg.iicsdoc.model.metadata.MetadataTag;
import com.ppg.iicsdoc.model.metadata.MetadataTag.MetadataTagType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MetadataTagValidator {

    @Data
    @AllArgsConstructor
    public static class TagValidationResult {
        private boolean valid;
        private List<String> errors;
        private List<String> warnings;
        private List<String> suggestions;
    }

    public TagValidationResult validate(List<MetadataTag> tags) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        validateRequiredTags(tags, errors, suggestions);
        validateDeprecatedTags(tags, warnings);
        validateConflicts(tags, errors);
        validateTagValues(tags, errors);
        validateRecommendedTags(tags, suggestions);

        boolean valid = errors.isEmpty();
        log.info("Tag validation: {} errors, {} warnings, {} suggestions",
                errors.size(), warnings.size(), suggestions.size());

        return new TagValidationResult(valid, errors, warnings, suggestions);
    }

    public String generateValidationReport(TagValidationResult result) {
        StringBuilder report = new StringBuilder();
        report.append("# Metadata Tag Validation Report\n\n");

        if (result.isValid()) {
            report.append("**Validation Passed**\n\n");
        } else {
            report.append("**Validation Failed**\n\n");
        }

        if (!result.getErrors().isEmpty()) {
            report.append("## Errors\n\n");
            for (String error : result.getErrors()) {
                report.append("- ").append(error).append("\n");
            }

            report.append("\n");
        }

        if (!result.getWarnings().isEmpty()) {
            report.append("## Warnings\n\n");
            for (String warning : result.getWarnings()) {
                report.append("- ").append(warning).append("\n");
            }

            report.append("\n");
        }

        if (!result.getSuggestions().isEmpty()) {
            report.append("## Suggestions\n\n");
            for (String suggestion : result.getSuggestions()) {
                report.append("- ").append(suggestion).append("\n");
            }

            report.append("\n");
        }

        return report.toString();
    }

    private void validateRequiredTags(
            List<MetadataTag> tags,
            List<String> errors,
            List<String> suggestions) {
        if (!hasTag(tags, MetadataTagType.PURPOSE)) {
            suggestions.add("Consider adding @purpose tag to describe the main goal of the development");
        }

        if (!hasTag(tags, MetadataTagType.DESCRIPTION) && !hasTag(tags, MetadataTagType.SUMMARY)) {
            suggestions.add("Add @description or @summary to provide a general overview");
        }
    }

    private void validateDeprecatedTags(List<MetadataTag> tags, List<String> warnings) {
        for (MetadataTag tag : tags) {
            if (tag.isDeprecated()) {
                warnings.add(String.format("Tag @%s is deprecated. %s",
                        tag.getTagName(), tag.getValue()));
            }
        }
    }

    private void validateConflicts(List<MetadataTag> tags, List<String> errors) {
        boolean hasDeprecated = hasTag(tags, MetadataTagType.DEPRECATED);
        boolean hasVersion = hasTag(tags, MetadataTagType.VERSION);

        if (hasDeprecated && hasVersion) {
            errors.add("Conflictig tags: @deprecated and @version should not both be present");
        }
    }

    private void validateTagValues(List<MetadataTag> tags, List<String> errors) {
        for (MetadataTag tag : tags) {
            String value = tag.getValue();
            if (value == null || value.trim().isEmpty()) {
                errors.add(String.format("Tag @%s has empty value", tag.getTagName()));
                continue;
            }

            switch (tag.getType()) {
                case SLA -> validateSLA(tag, errors);
                case DATA_CLASSIFICATION -> validateDataClassification(tag, errors);
                case VERSION -> validateVersion(tag, errors);
            }
        }
    }

    private void validateSLA(MetadataTag tag, List<String> errors) {
        String value = tag.getValue().toLowerCase();
        if (!value.matches(".*\\d+.*%.*")
                && !value.matches(".*\\d+.*seconds?.*")
                && !value.matches(".*\\d+.*minutes?.*")) {
            errors.add("@sla should specify uptime percentage or response time" +
                    " (e.g., '99.9% uptime', or 'max 5 seconds per record')");
        }
    }

    private void validateDataClassification(MetadataTag tag, List<String> errors) {
        String value = tag.getValue().toLowerCase();
        List<String> validClassifications = List.of(
                "public",
                "internal",
                "confidential",
                "restricted",
                "pii");

        boolean valid = validClassifications.stream()
            .anyMatch(value::contains);

        if (!valid) {
            errors.add("@data-classification should be one of: " + 
                String.join(", ", validClassifications));
        }
    }

    private void validateVersion(MetadataTag tag, List<String> errors) {
        String value = tag.getValue();
        if (!value.matches("\\d+\\.\\d+(\\.\\d+)?")) {
            errors.add("@version should follow semantic versioning format" +
                    "(e.g., '1.0.0')");
        }
    }

    private void validateRecommendedTags(List<MetadataTag> tags, List<String> suggestions) {
        if (!hasTag(tags, MetadataTagType.SECURITY) &&
                !hasTag(tags, MetadataTagType.DATA_CLASSIFICATION)) {
            suggestions.add("Consider adding @security or @data-classification for security " +
                    "documentation");
        }

        if (!hasTag(tags, MetadataTagType.PERFORMANCE) &&
                !hasTag(tags, MetadataTagType.SLA)) {
            suggestions.add("Consider adding @performnace or @sla for" +
                    " operational documentation");
        }

        if (!hasTag(tags, MetadataTagType.BUSINESS_OWNER) &&
                !hasTag(tags, MetadataTagType.STAKEHOLDERS)) {
            suggestions.add("Consider adding @business-owner or " +
                    "@stakeholders for accountability");
        }
    }

    private boolean hasTag(List<MetadataTag> tags, MetadataTagType type) {
        return tags.stream().anyMatch(tag -> tag.getType() == type);
    }

}
