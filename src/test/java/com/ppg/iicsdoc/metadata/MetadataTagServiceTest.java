package com.ppg.iicsdoc.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ppg.iicsdoc.model.metadata.MetadataTag;
import com.ppg.iicsdoc.model.metadata.MetadataTag.MetadataTagType;
import com.ppg.iicsdoc.parser.MetadataTagParser;

class MetadataTagServiceTest {
    private MetadataTagParser parser;

    @BeforeEach
    void setUp() {
        parser = new MetadataTagParser();
    }

    @Test
    void shouldParseSimpleTag() {
        String text = "@purpose Synchronize customer data";

        List<MetadataTag> tags = parser.parseTags(text);
        assertEquals(1, tags.size());
        
        MetadataTag tag = tags.get(0);
        assertEquals("purpose", tag.getTagName());
        assertEquals("Synchronize customer data", tag.getValue());
        assertEquals(MetadataTagType.PURPOSE, tag.getType());
    }

    @Test
    void shouldParseMultipleTags() {
        String text = """
                @purpose Main goal of the process
                @sla 99.9% uptime
                @security OAuth 2.0 authentication
                """;

        List<MetadataTag> tags = parser.parseTags(text);
        assertEquals(3, tags.size());
        assertEquals(MetadataTagType.PURPOSE, tags.get(0).getType());
        assertEquals(MetadataTagType.SLA, tags.get(1).getType());
        assertEquals(MetadataTagType.SECURITY, tags.get(2).getType());
    }

    @Test 
    void shouldParseTagsWithColonSeparation() {
        String text = "@purpose: Sync customer data\n@sla: 99.9%";
        List<MetadataTag> tags = parser.parseTags(text);

        assertEquals(2, tags.size());
        assertEquals("Sync customer data", tags.get(0).getValue());
        assertEquals("99.9%", tags.get(1).getValue());
    }

    @Test 
    void shouldParseTagsWithAttributes() {
        String text = "@example(language=java) System.out.println(\"Hello\")";
        List<MetadataTag> tags = parser.parseTags(text);
        assertEquals(1, tags.size());

        MetadataTag tag = tags.get(0);
        assertNotNull(tag.getAttributes());
        assertEquals("java", tag.getAttributes().get("language"));
    }
}
