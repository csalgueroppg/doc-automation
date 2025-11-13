package com.ppg.iicsdoc.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ppg.iicsdoc.model.metadata.MetadataTag;
import com.ppg.iicsdoc.parser.MetadataTagParser;
import com.ppg.iicsdoc.service.MetadataService;
import com.ppg.iicsdoc.service.TagSuggestionService;
import com.ppg.iicsdoc.service.TagSuggestionService.TagSuggestion;
import com.ppg.iicsdoc.validation.MetadataTagValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/metadata-tags")
public class MetadataTagController {

    private final MetadataTagParser tagParser;
    private final MetadataTagValidator tagValidator;
    private final TagSuggestionService suggestionService;
    private final MetadataService metadataEnhancementService;

    @Autowired
    public MetadataTagController(
            MetadataTagParser tagParser,
            MetadataTagValidator tagValidator,
            TagSuggestionService suggestionService,
            MetadataService metadataEnhancementService) {
        this.tagParser = tagParser;
        this.tagValidator = tagValidator;
        this.suggestionService = suggestionService;
        this.metadataEnhancementService = metadataEnhancementService;
    }

    @PostMapping("/parse")
    public ResponseEntity<List<MetadataTag>> parseTags(@RequestBody String text) {
        log.info("Parsing metadata tags from text");

        List<MetadataTag> tags = tagParser.parseTags(text);
        return ResponseEntity.ok(tags);
    }

    @PostMapping("/validate")
    public ResponseEntity<MetadataTagValidator.TagValidationResult> validateTags(
        @RequestBody String text
    ) {
        log.info("Validating metadata tags");

        List<MetadataTag> tags = tagParser.parseTags(text);
        MetadataTagValidator.TagValidationResult result = tagValidator.validate(tags);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<TagSuggestionService.TagSuggestion>> getSuggestions(
        @RequestParam(required = false) String prefix
    ) {
        log.info("Getting tag suggestions for prefix: {}", prefix);
        List<TagSuggestionService.TagSuggestion> suggestions = 
            suggestionService.getSuggestionsByCategory(prefix);

        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/documentation")
    public ResponseEntity<String> getTagDocumentation() {
        log.info("Generating tag documentation");

        String documentation = suggestionService.generateTagDocumentation();
        return ResponseEntity.ok(documentation);
    }

    @PostMapping("/extract-text")
    public ResponseEntity<String> extractPlainText(@RequestBody String text) {
        log.info("Extracting plain text from tagged content");

        String plainText = tagParser.extractPlainText(text);
        return ResponseEntity.ok(plainText);
    }
    
    
}
