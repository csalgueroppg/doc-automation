package com.ppg.iicsdoc.tags;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.ppg.iicsdoc.model.tags.Tag;
import com.ppg.iicsdoc.model.tags.TagReference;
import com.ppg.iicsdoc.model.tags.TagVerificationResult;
import com.ppg.iicsdoc.model.tags.TaggedDocument;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TagVerifier {
    
    public TagVerificationResult verify(TaggedDocument document) {
        log.info("Verifying {} tags in document: {}", 
            document.getTags().size(), document.getDocumentId());

        List<Tag> problematicTags = new ArrayList<>();
        int valid = 0;
        int outdated = 0;
        int missing = 0;
        int error = 0;

        for (Tag tag : document.getTags()) {
            try {
                verifyTag(tag);
                switch (tag.getStatus()) {
                    case VALID -> valid++;
                    case OUTDATED -> {
                        outdated++;
                        problematicTags.add(tag);
                    }

                    case MISSING -> {
                        missing++;
                        problematicTags.add(tag);
                    }

                    case ERROR -> {
                        error++;
                        problematicTags.add(tag);
                    }

                    default -> {}
                }
            } catch (Exception e) {
                log.error("Error verifying tag: {}", tag.getId(), e);

                tag.setStatus(Tag.TagStatus.ERROR);
                error++;

                problematicTags.add(tag);
            }
        }

        TagVerificationResult result = TagVerificationResult.builder()
            .totalTags(document.getTags().size())
            .validTags(valid)
            .outdatedTags(outdated)
            .missingTags(missing)
            .errorTags(error)
            .problematicTags(problematicTags)
            .build();

        document.setVerificationResult(result);
        log.info("Verification complete: {}/{} valid", 
            valid, document.getTags().size());

        return result;
    }

    private void verifyTag(Tag tag) throws Exception {
        TagReference ref = tag.getReference();
        Path filePath = Paths.get(ref.getFilePath());

        if (!Files.exists(filePath)) {
            tag.setStatus(Tag.TagStatus.MISSING);
            tag.setDescription("Referenced file not found: " + ref.getFilePath());

            return;
        }

        switch (tag.getType()) {
            case FILE_REFERENCE -> verifyFileReference(tag, filePath);
            case LINE_REFERENCE, CODE_SNIPPET -> verifyLineReference(tag, filePath);
            case XPATH_REFERENCE -> verifyXPathReference(tag, filePath);
            case CONNECTION_REF, TRANSFORMATION_REF -> verifyElementReference(tag, filePath);
            default -> tag.setStatus(Tag.TagStatus.VALID);
        }

        tag.setLastVerified(LocalDateTime.now());
    }

    private void verifyFileReference(Tag tag, Path filePath) throws Exception {
        String currentHash = calculateFileHash(filePath);
        if (tag.getReference().getHash() == null) {
            tag.getReference().setHash(currentHash);
            tag.setStatus(Tag.TagStatus.VALID);
        } else if (currentHash.equals(tag.getReference().getHash())) {
            tag.setStatus(Tag.TagStatus.VALID);
        } else {
            tag.setStatus(Tag.TagStatus.OUTDATED);
            tag.setDescription("File content has changed since last verification");
        }
    }

    private void verifyLineReference(Tag tag, Path filePath) throws Exception {
        List<String> lines = Files.readAllLines(filePath);
        TagReference ref = tag.getReference();

        if (ref.getStartLine() == null || ref.getEndLine() == null) {
            tag.setStatus(Tag.TagStatus.ERROR);
            tag.setDescription("Invalid line range");

            return;
        }

        if (ref.getStartLine() > lines.size() || ref.getEndLine() > lines.size()) {
            tag.setStatus(Tag.TagStatus.ERROR);
            tag.setDescription("Line range exceeds file length");

            return;
        }

        StringBuilder content = new StringBuilder();
        for (int i = ref.getStartLine() - 1; i < ref.getEndLine(); i++) {
            content.append(lines.get(1)).append("\n");
        }

        String actualContent = content.toString().trim();
        tag.setActualContent(actualContent);

        if (tag.getExpectedContent() == null) {
            tag.setExpectedContent(actualContent);
            tag.setStatus(Tag.TagStatus.VALID);
        } else if (actualContent.equals(tag.getExpectedContent())) {
            tag.setStatus(Tag.TagStatus.VALID);
        } else {
            tag.setStatus(Tag.TagStatus.OUTDATED);
            tag.setDescription("Referenced lines have changed");
        }
    }

    private void verifyXPathReference(Tag tag, Path filePath) throws Exception {
        TagReference ref = tag.getReference();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(filePath.toFile());
        XPath xpath = XPathFactory.newInstance().newXPath();
        String result = (String) xpath.evaluate(ref.getXpath(), doc, XPathConstants.STRING);

        if (result == null || result.isEmpty()) {
            tag.setStatus(Tag.TagStatus.MISSING);
            tag.setDescription("XPath query returned no results");

            return;
        }

        tag.setActualContent(result);
        if (tag.getExpectedContent() == null) {
            tag.setExpectedContent(result);
            tag.setStatus(Tag.TagStatus.VALID);
        } else if (result.equals(tag.getExpectedContent())) {
            tag.setStatus(Tag.TagStatus.VALID);
        } else {
            tag.setStatus(Tag.TagStatus.OUTDATED);
            tag.setDescription("XPath query result has changed");
        }
    }

    private void verifyElementReference(Tag tag, Path filePath) throws Exception {
        TagReference ref = tag.getReference();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(filePath.toFile());

        XPath xpath = XPathFactory.newInstance().newXPath();
        String query = String.format("//*[@id='%s']", ref.getElementId());
        Node node = (Node) xpath.evaluate(query, doc, XPathConstants.NODE);

        if (node == null) {
            tag.setStatus(Tag.TagStatus.MISSING);
            tag.setDescription("Element not found: " + ref.getElementId());

            return;
        }

        tag.setStatus(Tag.TagStatus.VALID);
    }

    private String calculateFileHash(Path filePath) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] fileBytes = Files.readAllBytes(filePath);
            byte[] hash = digest.digest(fileBytes);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append("0");

                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            throw new IOException("Failed to calculate hash", e);
        }
    }
}
