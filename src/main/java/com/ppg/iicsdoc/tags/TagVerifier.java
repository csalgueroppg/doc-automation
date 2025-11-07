package com.ppg.iicsdoc.tags;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

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
import com.ppg.iicsdoc.util.FileHasher;

import lombok.extern.slf4j.Slf4j;

/**
 * Verifies the integrity and accuracy of {@link Tag} instances
 * within a {@link TaggedDocument}.
 * 
 * <p>
 * The {@code TagVerifier} performs validation checks based on tag
 * type and reference, including file existence, content matching,
 * XPath elevation, and hash comparison. It produces a
 * {@link TagVerificationResult} summarizing the outcome of the
 * verification process.
 * </p>
 * 
 * <h2>Supported Verification Types</h2>
 * <ul>
 * <li><b>File Reference</b>: compares file hash against stored value</li>
 * <li><b>Line Reference / Code Snippet</b>: compares extracted lines to
 * expected content</li>
 * <li><b>XPath Reference</b>: evaluate XPath and compares result</li>
 * <li><b>Element Reference</b>: checks for existence of element by ID</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * TagVerifier verifier = new TagVerifier();
 * TaggedDocument doc = ...; // loaded or parsed document
 *
 * TagVerificationResult result = verifier.verify(doc);
 *
 * if (!result.isAllValid()) {
 *     result.getProblematicTags().forEach(tag ->
 *         System.out.println("Tag " + tag.getId() + " needs review: " + tag.getDescription()));
 * }
 * }</pre>
 * 
 * @see Tag
 * @see TagReference
 * @see TaggedDocument
 * @see TagVerificationResult
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-11-04
 */
@Slf4j
@Component
public class TagVerifier {

    private final Map<Path, String> hashCache = new HashMap<>();

    /**
     * Verifies all tags in the given document and returns a summary
     * result.
     * 
     * @param document the tagged document to verify
     * @return a {@link TagVerificationResult} containing verification statistics
     */
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

                    default -> {
                    }
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

    /**
     * Verifies a single tag based on its type and reference.
     * 
     * @param tag the tag to verify
     * @throws Exception if verification fails unexpectedly
     */
    public void verifyTag(Tag tag) throws Exception {
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

    /**
     * 
     * 
     * @param file
     * @return
     * @throws Exception
     */
    public String getFileHashCached(Path file) throws Exception {
        return hashCache.computeIfAbsent(file, f -> {
            try {
                return FileHasher.calculateFileHash(file);
            } catch (Exception e) {
                log.error("Error calculating hash", e);
                return "";
            }
        });
    }

    /**
     * Verifies a file reference by comparing its hash.
     * 
     * @param tag      the tag to verify
     * @param filePath the path to the referenced file
     * @throws Exception if hash calculation fails
     */
    private void verifyFileReference(Tag tag, Path filePath) throws Exception {
        String currentHash = FileHasher.calculateFileHash(filePath);
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

    /**
     * Verifies a line reference or code snippet by comparing extracted lines.
     * 
     * @param tag      the tag to verify
     * @param filePath the path to the referenced file
     * @throws Exception if line extraction fails
     */
    private void verifyLineReference(Tag tag, Path filePath) throws Exception {
        var ref = tag.getReference();
        if (ref.getStartLine() == null || ref.getEndLine() == null) {
            tag.setStatus(Tag.TagStatus.ERROR);
            return;
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                if (lineNumber >= ref.getStartLine() && lineNumber <= ref.getEndLine()) {
                    content.append(line).append("\n");
                }

                if (lineNumber > ref.getEndLine()) {
                    break;
                }

                lineNumber++;
            }
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
        }
    }

    /**
     * Verifies an XPath reference by evaluating the expression against the XML
     * file.
     *
     * @param tag      the tag to verify
     * @param filePath the path to the XML file
     * @throws Exception if XPath evaluation fails
     */
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

    /**
     * Verifies an element reference by checking for an element with the given ID.
     *
     * @param tag      the tag to verify
     * @param filePath the path to the XML file
     * @throws Exception if element lookup fails
     */
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
}
