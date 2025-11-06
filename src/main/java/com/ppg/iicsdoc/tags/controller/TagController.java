package com.ppg.iicsdoc.tags.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ppg.iicsdoc.model.tags.TagVerificationResult;
import com.ppg.iicsdoc.model.tags.TaggedDocument;
import com.ppg.iicsdoc.tags.TagManagementService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagManagementService tagManagementService;

    public TagController(TagManagementService tagManagementService) {
        this.tagManagementService = tagManagementService;
    }

    @PostMapping("/verify")
    public ResponseEntity<TagVerificationResult> verifyTags(
            @RequestBody Map<String, String> request) {
        String content = request.get("content");
        String documentId = request.getOrDefault("documentId", "temp");

        log.info("Verifying tags in document: {}", documentId);

        TaggedDocument document = tagManagementService.processDocument(documentId, content);
        return ResponseEntity.ok(document.getVerificationResult());
    }

    @PostMapping("/report")
    public ResponseEntity<String> getTagReport(
        @RequestBody Map<String, String> request) {
        String content = request.get("content");
        String documentId = request.getOrDefault("documentId", "temp");
        TaggedDocument document = tagManagementService.processDocument(documentId, content);
        String report = tagManagementService.generateReport(document.getVerificationResult());

        return ResponseEntity.ok(report);
    }

    @PostMapping("/render")
    public ResponseEntity<String> renderTags(
        @RequestBody Map<String, String> request) {
        String content = request.get("content");
        String rendered = tagManagementService.verifyAndUpdateDocument(content);

        return ResponseEntity.ok(rendered);
    }

}
