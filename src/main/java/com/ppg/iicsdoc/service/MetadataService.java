package com.ppg.iicsdoc.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ppg.iicsdoc.model.metadata.Metadata;
import com.ppg.iicsdoc.model.metadata.MetadataTag;
import com.ppg.iicsdoc.model.metadata.MetadataTag.MetadataTagType;
import com.ppg.iicsdoc.parser.MetadataTagParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MetadataService {

    private final MetadataTagParser tagParser;

    public MetadataService(MetadataTagParser parser) {
        this.tagParser = parser;
    }

    public Metadata enhance(String description, String author, String owner) {
        log.info("Enhancing metadata with tag parsing");

        List<MetadataTag> descriptionTags = tagParser.parseTags(description);
        Metadata.MetadataBuilder builder = Metadata.builder()
                .description(description)
                .author(author)
                .owner(owner)
                .descriptionTags(descriptionTags)
                .allTags(descriptionTags);

        String purpose = tagParser.getTagValue(descriptionTags, MetadataTagType.PURPOSE);
        if (purpose != null) {
            builder.purpose(purpose);
        }

        String summary = tagParser.getTagValue(descriptionTags, MetadataTagType.SUMMARY);
        if (summary != null) {
            builder.summary(summary);
        }

        List<String> systemReqs = extractListValues(descriptionTags, MetadataTagType.SYSTEM_REQUIREMENTS);
        if (!systemReqs.isEmpty()) {
            builder.systemRequirements(systemReqs);
        }

        List<String> dependencies = extractListValues(descriptionTags, MetadataTagType.DEPENDENCIES);
        if (!dependencies.isEmpty()) {
            builder.dependencies(dependencies);
        }

        Metadata.PerformanceMetadata performance = buildPerformanceMetadata(descriptionTags);
        if (performance != null) {
            builder.performance(performance);
        }

        Metadata.SecurityMetadata security = buildSecurityMetadata(descriptionTags);
        if (security != null) {
            builder.security(security);
        }

        Metadata.BusinessMetadata business = buildBusinessMetadata(descriptionTags);
        if (business != null) {
            builder.business(business);
        }

        Metadata enhanced = builder.build();

        log.info("Enhanced metadata with {} tags", enhanced.getAllTags().size());
        return enhanced;
    }

    public String generateMetadataDocumentation(Metadata metadata) {
        StringBuilder doc = new StringBuilder();

        doc.append("## Metadata\n\n");
        if (metadata.getPurpose() != null) {
            doc.append("### Purpose");
            doc.append(metadata.getPurpose()).append("\n\n");
        }

        if (metadata.getSummary() != null) {
            doc.append("### Summary\n\n");
            doc.append(metadata.getSummary()).append("\n\n");
        }

        if (metadata.getDescription() != null && !metadata.getDescription().isEmpty()) {
            doc.append("### Description");
            doc.append(metadata.getDescription()).append("\n\n");
        }

        if (metadata.getSystemRequirements() != null &&
                !metadata.getSystemRequirements().isEmpty()) {
            doc.append("### System Requirements");
            for (String req : metadata.getSystemRequirements()) {
                doc.append("- ").append(req).append("\n");
            }

            doc.append("\n");
        }

        if (metadata.getPrerequisites() != null &&
                !metadata.getPrerequisites().isEmpty()) {
            doc.append("### Prerequisites\n\n");
            for (String prereq : metadata.getPrerequisites()) {
                doc.append("- ").append(prereq).append("\n");
            }

            doc.append("\n");
        }

        if (metadata.getDependencies() != null &&
                !metadata.getDependencies().isEmpty()) {
            doc.append("### Dependencies");
            for (String dep : metadata.getDependencies()) {
                doc.append("- ").append(dep).append("\n");
            }

            doc.append("\n");
        }

        if (metadata.getPerformance() != null) {
            doc.append("### Performance\n\n");
            Metadata.PerformanceMetadata perf = metadata.getPerformance();

            if (perf.getSla() != null) {
                doc.append("- **SLA**: ").append(perf.getSla())
                        .append("\n");
            }

            if (perf.getScaling() != null) {
                doc.append("- **Scaling**: ").append(perf.getScaling())
                        .append("\n");
            }

            if (perf.getCapacity() != null) {
                doc.append("- **Capacity**: ").append(perf.getCapacity())
                        .append("\n");
            }

            if (perf.getExpectedLoad() != null) {
                doc.append("- **Expected Load**: ").append(perf.getExpectedLoad())
                        .append("\n");
            }

            doc.append("\n");
        }

        if (metadata.getSecurity() != null) {
            doc.append("### Security\n\n");
            Metadata.SecurityMetadata sec = metadata.getSecurity();

            if (sec.getClassification() != null) {
                doc.append("- **Data Classification**: ")
                        .append(sec.getClassification()).append("\n");
            }

            if (sec.getCompliance() != null) {
                doc.append("- **Compliance**: ")
                        .append(sec.getCompliance()).append("\n");
            }

            if (sec.getRequirements() != null && !sec.getRequirements().isEmpty()) {
                doc.append("- **Security Requirements**:\n");
                for (String req : sec.getRequirements()) {
                    doc.append("- ").append(req).append("\n");
                }
            }

            doc.append("\n");
        }

        if (metadata.getBusiness() != null) {
            doc.append("### Business Information\n\n");
            Metadata.BusinessMetadata biz = metadata.getBusiness();

            if (biz.getOwner() != null) {
                doc.append("- **Business Owner**: ")
                        .append(biz.getOwner()).append("\n");
            }

            if (biz.getStakeholders() != null &&
                    !biz.getStakeholders().isEmpty()) {
                doc.append("- **Stakeholders**: ")
                        .append(biz.getStakeholders()).append("\n");
            }

            if (biz.getImpact() != null) {
                doc.append("- **Business Impact**")
                    .append(biz.getImpact()).append("\n");
            }

            doc.append("\n");
        }

        return doc.toString();
    }

    private List<String> extractListValues(List<MetadataTag> tags, MetadataTagType type) {
        String value = tagParser.getTagValue(tags, type);
        if (value == null || value.trim().isEmpty()) {
            return List.of();
        }

        return Arrays.stream(value.split("[,;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private Metadata.PerformanceMetadata buildPerformanceMetadata(List<MetadataTag> tags) {
        String sla = tagParser.getTagValue(tags, MetadataTagType.SLA);
        String scaling = tagParser.getTagValue(tags, MetadataTagType.SCALING);
        String capacity = tagParser.getTagValue(tags, MetadataTagType.CAPACITY);
        String performance = tagParser.getTagValue(tags, MetadataTagType.PERFORMANCE);

        if (sla == null && scaling == null && capacity == null && performance == null) {
            return null;
        }

        return Metadata.PerformanceMetadata.builder()
                .sla(sla)
                .scaling(scaling)
                .capacity(capacity)
                .expectedLoad(performance)
                .build();
    }

    private Metadata.SecurityMetadata buildSecurityMetadata(List<MetadataTag> tags) {
        String classification = tagParser.getTagValue(tags, MetadataTagType.DATA_CLASSIFICATION);
        String compliance = tagParser.getTagValue(tags, MetadataTagType.COMPLIANCE);
        List<String> requirements = extractListValues(tags, MetadataTagType.SECURITY);

        if (classification == null && compliance == null && requirements.isEmpty()) {
            return null;
        }

        return Metadata.SecurityMetadata.builder()
                .classification(classification)
                .compliance(compliance)
                .requirements(requirements)
                .build();
    }

    private Metadata.BusinessMetadata buildBusinessMetadata(List<MetadataTag> tags) {
        String owner = tagParser.getTagValue(tags, MetadataTagType.BUSINESS_OWNER);
        List<String> stakeholders = extractListValues(tags, MetadataTagType.STAKEHOLDERS);
        String impact = tagParser.getTagValue(tags, MetadataTagType.BUSINESS_IMPACT);

        if (owner == null && stakeholders.isEmpty() && impact == null) {
            return null;
        }

        return Metadata.BusinessMetadata.builder()
                .owner(owner)
                .stakeholders(stakeholders)
                .impact(impact)
                .build();
    }
}
