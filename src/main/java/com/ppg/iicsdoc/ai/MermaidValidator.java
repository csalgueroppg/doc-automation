package com.ppg.iicsdoc.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.ppg.iicsdoc.model.ai.MermaidDiagram;
import com.ppg.iicsdoc.model.common.ValidationResult;

import lombok.extern.slf4j.Slf4j;

/**
 * Validates Mermaid Diagram source code across all supported diagram types.
 * 
 * <p>
 * This component provides structural and semantic validation for Mermaid
 * diagrams,
 * including flowcharts, sequence diagrams, class diagrams, state diagrams, ER
 * diagrams, and Gantt charts. It detects the diagram type, applies
 * type-specific
 * validation rules, and performs common structural checks to ensure Mermaid
 * syntax correctness.
 * </p>
 * 
 * <p>
 * Supported diagram types:
 * </p>
 * 
 * <ul>
 * <li>Flowchart</li>
 * <li>Sequence Diagram</li>
 * <li>Class Diagram</li>
 * <li>State Diagram</li>
 * <li>Entity-Relationship (ER) Diagram</li>
 * <li>Gantt Chart</li>
 * </ul>
 * 
 * <p>
 * Validation results include:
 * </p>
 * 
 * <ul>
 * <li>Critical errors that prevent rendering</li>
 * <li>Non-critical warnings for best practices or potential issues</li>
 * </ul>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-22
 */
@Slf4j
@Component
public class MermaidValidator {

    /**
     * Matches the start of a Mermaid flowchart declaration.
     * Example: {@code flowchart TD}, {@code flowchart LR}
     */
    private static final Pattern FLOWCHART_PATTERN = Pattern.compile("^flowchart\\s+(TD|LR|TB|RL|BT)");

    /**
     * Matches the start of a Mermaid sequence diagram.
     * Example: {@code sequenceDiagram}
     */
    private static final Pattern SEQUENCE_PATTERN = Pattern.compile("^sequenceDiagram");

    /**
     * Matches the start of a Mermaid class diagram.
     * Example: {@code classDiagram}
     */
    private static final Pattern CLASS_PATTERN = Pattern.compile("^classDiagram");

    /**
     * Matches the start of a Mermaid state diagram.
     * Example: {@code stateDiagram}
     */
    private static final Pattern STATE_PATTERN = Pattern.compile("^stateDiagram");

    /**
     * Matches the start of a Mermaid ER diagram.
     * Example: {@code erDiagram}
     */
    private static final Pattern ER_PATTERN = Pattern.compile("^erDiagram");

    /**
     * Matches the start of a Mermaid Gantt chart.
     * Example: {@code gantt}
     */
    private static final Pattern GANTT_PATTERN = Pattern.compile("^gantt");

    /*
     * Matches task lines with an ID, start date, and duration.
     * Example: {@code Task Name :task1, 2025-01-01, 10d}
     */

    private static final Pattern TASK_WITH_ID_AND_DATE = Pattern.compile(
            "^[^:]+\\s*:\\s*(?:(done|active|crit|milestone)\\s*,\\s*)?([a-zA-Z0-9_-]+)\\s*,\\s*(\\d{4}-\\d{2}-\\d{2})\\s*,\\s*(\\d+d)$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Matches task lines that depend on another task.
     * Example: {@code Another Task: after task1, 5d}
     */

    private static final Pattern TASK_AFTER_ID = Pattern.compile(
            "^[^:]+\\s*:\\s*(?:(done|active|crit|milestone)\\s*,\\s*)?after\\s+([a-zA-Z0-9_-]+)\\s*,\\s*(\\d+d)$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Matches task lines with a start date and duration, but no ID.
     * Example: {@code Task: 2025-01-01, 10d}
     */
    private static final Pattern TASK_WITH_DATE_ONLY = Pattern
            .compile("^[^:]+\\s*:\\s*(\\d{4}-\\d{2}-\\d{2})\\s*,\\s*(\\d+d)$");

    /**
     * Matches task lines with only a duration.
     * Example: {@code Task : 5d}
     */
    private static final Pattern TASK_WITH_DURATION_ONLY = Pattern.compile("^[^:]+\\s*:\\s*(\\d+d)$");

    /**
     * Matches task lines with an ID, a dependency, and a duration.
     * Example: {@code Task Name :task1, after task0, 10d}
     */

    private static final Pattern TASK_WITH_ID_AND_AFTER = Pattern.compile(
            "^[^:]+\\s*:\\s*(?:(done|active|crit|milestone)\\s*,\\s*)?([a-zA-Z0-9_-]+)\\s*,\\s*after\\s+([a-zA-Z0-9_-]+)\\s*,\\s*(\\d+d)$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Validates a Mermaid diagram by detecting its type and applying the
     * appropriate syntax checks.
     * 
     * <p>
     * This method performs the following steps:
     * </p>
     * 
     * <ul>
     * <li>Checks if the diagram is null or empty</li>
     * <li>Attempts to detect the diagram type from its content</li>
     * <li>Applies type-specific validation logic</li>
     * <li>Performs common validation applicable to all diagram types</li>
     * </ul>
     * 
     * <p>
     * It returns a {@link ValidationResult} indicating whether the diagram
     * is valid, and includes any errors or warnings encountered during
     * validation.
     * </p>
     * 
     * @param diagram the Mermaid diagram to validate
     * @return a {@link ValidationResult} representing the outcome of the validation
     */
    public ValidationResult validate(MermaidDiagram diagram) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (diagram == null || !diagram.hasContent()) {
            errors.add("Diagram is empty or null");
            return ValidationResult.invalid(errors);
        }

        String code = diagram.getDiagramCode().trim();
        MermaidDiagram.DiagramType detectedType = detectDiagramType(code);

        if (detectedType == null) {
            validateStructuralContent(code, errors, warnings);

            errors.add("Unable to detect valid Mermaid diagram type");
            return ValidationResult.invalidWithWarnings(errors, warnings);
        }

        if (diagram.getType() == null) {
            diagram.setType(detectedType);
        }

        switch (detectedType) {
            case FLOWCHART -> validateFlowChart(code, errors, warnings);
            case SEQUENCE -> validateSequenceDiagram(code, errors, warnings);
            case CLASS -> validateClassDiagram(code, errors, warnings);
            case STATE -> validateStateDiagram(code, errors, warnings);
            case ER -> validateERDiagram(code, errors, warnings);
            case GANTT -> validateGanttDiagram(code, errors, warnings);
        }

        if (errors.isEmpty()) {
            log.info("Mermaid diagram validation passed with {} warnings", warnings.size());
            log.info(String.join(", ", warnings));

            return ValidationResult.valid();
        } else {
            log.warn("Mermaid diagram validation failed with {} errors", errors.size());
            log.warn(String.join(", ", errors));

            return ValidationResult.invalidWithWarnings(errors, warnings);
        }
    }

    /**
     * Detects the type of Mermaid diagram based on the first meaningful line
     * of the source code.
     * 
     * <p>
     * If the diagram starts with a frontmatter black (delimited by {@code ---}),
     * the method skips it and analyzer the first non-formatter line. It then
     * matches the line against known patterns to determine the diagram type.
     * </p>
     * 
     * @param code the Mermaid diagram source code
     * @return the detected {@link MermaidDiagram.DiagramType}, or {@code null}
     *         if no match is found.
     */
    private MermaidDiagram.DiagramType detectDiagramType(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        String[] lines = code.split("\\r?\\n");
        String firstLine = lines[0].trim();

        if (firstLine.startsWith("---")) {
            for (int i = 1; i < lines.length; i++) {
                if (lines[i].trim().equals("---")) {
                    if (i + 1 < lines.length) {
                        firstLine = lines[i + 1].trim();
                    }

                    break;
                }
            }
        }

        if (FLOWCHART_PATTERN.matcher(firstLine).find()) {
            log.debug("Identified diagram type: flowchart");
            return MermaidDiagram.DiagramType.FLOWCHART;
        } else if (SEQUENCE_PATTERN.matcher(firstLine).find()) {
            log.debug("Identified diagram type: sequence");
            return MermaidDiagram.DiagramType.SEQUENCE;
        } else if (CLASS_PATTERN.matcher(firstLine).find()) {
            log.debug("Identified diagram type: class");
            return MermaidDiagram.DiagramType.CLASS;
        } else if (STATE_PATTERN.matcher(firstLine).find()) {
            log.debug("Identified diagram type: state");
            return MermaidDiagram.DiagramType.STATE;
        } else if (ER_PATTERN.matcher(firstLine).find()) {
            log.debug("Identified diagram type: er");
            return MermaidDiagram.DiagramType.ER;
        } else if (GANTT_PATTERN.matcher(firstLine).find()) {
            log.debug("Identified diagram type: gantt");
            return MermaidDiagram.DiagramType.GANTT;
        }

        return null;
    }

    /**
     * Validates the syntax of a Mermaid flowchart diagram.
     * 
     * <p>
     * This method checks for the presence of nodes and connections, ensures
     * balanced brackets, and performs basic structural validation of Mermaid
     * flowchart syntax. It collects critical errors and non-critical warnings
     * into the provided lists.
     * </p>
     * 
     * @param code     the Mermaid flowchart source code to validate
     * @param errors   a list to collect critical validation errors (e.g., missing
     *                 nodes)
     * @param warnings a list to collect non-critical issues (e.g., unbalanced
     *                 brackets)
     * @throws NullPointerException if {@code errors} or {@code warnings} is
     *                              {@code null}
     */
    private void validateFlowChart(
            String code,
            List<String> errors,
            List<String> warnings) {
        if (code == null || code.isEmpty()) {
            errors.add("Diagram code is empty");
            return;
        }

        if (!containsAnyNode(code)) {
            errors.add("Flowchart must contain at least one node" +
                    "(e.g., [text], (text), or {text})");
        }

        if (!containsAnyConnection(code)) {
            warnings.add("Flowchart has no connections between nodes.");
        }

        validateBracketBalance(code, "[", "]", "square", errors);
        validateBracketBalance(code, "(", ")", "round", errors);
        validateBracketBalance(code, "{", "}", "curly", errors);

        validateFlowchartDirection(code, warnings);
    }

    /**
     * Validates syntax of a Mermaid Sequence diagram.
     * 
     * <p>
     * This method checks for the presence of participants, message arrows,
     * and balanced control structures such as loops and alternatives. It
     * collects critical errors and non-critical warnings into the provided
     * lists.
     * </p>
     * 
     * @param code     the Mermaid sequence diagram source code
     * @param errors   a list to collect critical validation errors (e.g., missing
     *                 arrows)
     * @param warnings a list to collect non-critical issues (e.g., no participants)
     * @throws NullPointerException if {@code errors} or {@code warnings} is
     *                              {@code null}
     */
    private void validateSequenceDiagram(
            String code,
            List<String> errors,
            List<String> warnings) {
        if (code == null || code.isEmpty()) {
            errors.add("Diagram code is empty");
            return;
        }

        if (!containsParticipants(code)) {
            warnings.add("Sequence diagram has no explicit participant or actors");
        }

        if (!containsMessageArrows(code)) {
            errors.add("Sequence diagram must contain at least one message arrow" +
                    " (e.g., '->', '-->').");
        }

        validateBlockBalance(code, "loop", "end", "loop", errors);
        validateBlockBalance(code, "alt", "end", "alt", errors);
        validateBlockBalance(code, "opt", "end", "opt", errors);
        validateBlockBalance(code, "par", "end", "parallel", errors);
        validateBlockBalance(code, "rect", "end", "rect", errors);

        validateNoteSyntax(code, warnings);
    }

    /**
     * Validates the syntax of a Mermaid Class Diagram.
     * 
     * <p>
     * This method checks for class declarations, relationships, and structural
     * correctness. It collects critical errors and non-critical warnings into
     * the provided lists.
     * </p>
     * 
     * @param code     the Mermaid class diagram source code to validate
     * @param errors   a list to collect critical validation errors (e.g., missing
     *                 class definitions)
     * @param warnings a list to collect non-critical issues (e.g., unbalanced
     *                 brackets)
     * @throws NullPointerException if {@code errors} or {@code warnings} is
     *                              {@code null}
     */
    private void validateClassDiagram(
            String code,
            List<String> errors,
            List<String> warnings) {
        if (code == null || code.isEmpty()) {
            errors.add("Diagram source code is empty");
            return;
        }

        if (!containsClassDefinition(code)) {
            errors.add("Class diagram must contain at least one class definition" +
                    " (e.g., 'class className').");
        }

        if (!containsAnyRelationship(code)) {
            warnings.add("Class diagram has no relationships between classes.");
        }

        validateBracketBalance(code, "{", "}", "curly", errors);
    }

    /**
     * Validates the syntax of a Mermaid State Diagram.
     * 
     * <p>
     * This method checks for state declarations, transitions, and structural
     * correctness.
     * It collects critical errors and non-critical warnings into the provided
     * lists.
     * </p>
     * 
     * @param code     the Mermaid state diagram source code to validate
     * @param errors   a list to collect critical validation errors (e.g., missing
     *                 nodes)
     * @param warnings a list to collect non-critical issues (.e.g, unbalanced
     *                 brackets)
     * @throws NullPointerException if {@code errors} or {@code warnings} is
     *                              {@code null}
     */
    private void validateStateDiagram(String code, List<String> errors, List<String> warnings) {
        if (code == null || code.isEmpty()) {
            errors.add("Diagram source code is empty");
            return;
        }

        if (!containsAnyState(code)) {
            errors.add("State diagram must contain at least one state declaration");
        }

        if (!containsAnyTransition(code)) {
            warnings.add("State diagram has no transitions between states.");
        }

        if (!containsStartOrEndState(code)) {
            warnings.add("State diagram does not include a start or end state" +
                    " (e.g., '[*] --> State').");
        }

        validateBracketBalance(code, "{", "}", "curly", errors);
    }

    /**
     * Validates the syntax of a Mermaid ER Diagram.
     * 
     * <p>
     * This method checks for entity definitions, relationships, and structural
     * correctness. It collects critical errors and non-critical warnings into
     * the provided lists.
     * </p>
     * 
     * @param code     the Mermaid ER diagram source code to validate
     * @param errors   a list to collect critical validation errors (e.g., missing
     *                 entities)
     * @param warnings a list to collect non-critical issues (e.g., unbalanced
     *                 brackets)
     * @throws NullPointerException if {@code errors} or {@code warnings} is
     *                              {@code null}
     */
    private void validateERDiagram(
            String code,
            List<String> errors,
            List<String> warnings) {
        if (code == null || code.isEmpty()) {
            errors.add("Diagram code is empty");
            return;
        }

        if (!containsEntityDefinition(code)) {
            errors.add("ER diagram must contain at least one entity definition" +
                    " enclosed in '{...}'.");
        }

        if (!containsAnyERRelationship(code)) {
            warnings.add("ER diagram has no relationships between entities");
        }

        validateEREntityBlock(code, errors);
    }

    /**
     * Validates the syntax of a Mermaid Gantt Diagram.
     * 
     * <p>
     * This method checks for the presence of required structural elements
     * such as {@code gantt} and {@code dateFormat} declarations, and validates
     * each task line against supported Mermaid Gantt formats. It collects any
     * structural or syntactic issues into the provided {@code errors} and
     * {@code warnings} list.
     * </p>
     * 
     * @param code     the Mermaid Gantt diagram source code to validate
     * @param errors   a list to collect critical validation errors (e.g., missing
     *                 declarations)
     * @param warnings a list to collect non-critical issues (e.g., malformed tasks,
     *                 duplicates)
     * @throws NullPointerException if {@code errors} or {@code warnings} is
     *                              {@code null}.
     */
    private void validateGanttDiagram(
            String code,
            List<String> errors,
            List<String> warnings) {
        if (code == null || code.isBlank()) {
            errors.add("Diagram code is empty");
            return;
        }

        String[] lines = code.split("\\r?\\n");
        Set<String> taskIds = new HashSet<>();
        boolean hasGantt = false;
        boolean hasDateFormat = false;

        Predicate<String> isGantt = line -> line.equalsIgnoreCase("gantt");
        Predicate<String> isDateFormat = line -> line.toLowerCase().startsWith("dateformat");
        Predicate<String> isSectionOrTitle = line -> {
            String lower = line.toLowerCase();
            return lower.startsWith("section") || lower.startsWith("title");
        };

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty())
                continue;

            if (isGantt.test(line)) {
                hasGantt = true;
            } else if (isDateFormat.test(line)) {
                hasDateFormat = true;
            } else if (isSectionOrTitle.test(line)) {
                continue;
            } else {
                validateTaskLine(line, i + 1, taskIds, warnings);
            }
        }

        if (!hasGantt)
            errors.add("Missing required 'gant' declaration");

        if (!hasDateFormat)
            errors.add("Missing required 'dateFormat' declaration");
    }

    // Helper methods
    /**
     * Performs structural validation on a Mermaid diagram, regardless of its
     * specific type.
     * 
     * <p>
     * This method checks for:
     * </p>
     * 
     * <ul>
     * <li>Unbalanced brackets: (), {}, []</li>
     * <li>Presence of meaningful content (non-empty, non-comment lines)</li>
     * <li>Suspicious or unknown lines that may not match known Mermaid syntax</li>
     * </ul>
     * 
     * <p>
     * It is intended to be used as a fallback or supplement when the diagram type
     * is unknown or when additional structural checks are needed.
     * </p>
     * 
     * @param code     the Mermaid diagram soure code
     * @param errors   a list to collecto critical validation errors
     * @param warnings a list to collect non-critical issues
     */
    private void validateStructuralContent(
            String code,
            List<String> errors,
            List<String> warnings) {
        if (code == null || code.isBlank()) {
            errors.add("Diagram code is empty");
            return;
        }

        validateBracketBalance(code, "(", ")", "round", errors);
        validateBracketBalance(code, "[", "]", "square", errors);
        validateBracketBalance(code, "{", "}", "curly", errors);

        String[] lines = code.split("\\r?\\n");
        boolean hasContent = Arrays.stream(lines)
                .map(String::trim)
                .anyMatch(line -> !line.isEmpty() && !line.startsWith("%%"));

        if (!hasContent) {
            errors.add("Diagram contains no meaningful content");
        }

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("%%"))
                continue;

            if (!isKnownMermaidSyntax(line)) {
                warnings.add("Line " + (i + 1) + " may not match known " +
                        "Mermaid syntax: \"" + line + "\"");
            }
        }
    }

    /**
     * Checks whether a line appears to follow Mermaid syntax patterns.
     * 
     * @param line the line to check
     * @return {@code true} if the line matches known Mermaid syntax;
     *         {@code false} otherwise
     */
    private boolean isKnownMermaidSyntax(String line) {
        return line.matches("(?i)^graph\\s+(TD|LR|RL|BT)\\b") ||
                line.matches("(?i)^sequenceDiagram\\b") ||
                line.matches("(?i)^classDiagram\\b") ||
                line.matches("(?i)^stateDiagram\\b") ||
                line.matches("(?i)^erDiagram\\b") ||
                line.matches("(?i)^gantt\\b") ||
                line.matches("(?i)^timeline\\b") ||
                line.matches("(?i)^mindmap\\b") ||
                line.matches("(?i)^pie\\b") ||
                line.matches(".*--.*") ||
                line.matches(".*:.*") ||
                line.matches(".*\\{.*\\}");
    }

    /**
     * Checks the diagram to verify it contains any node
     * 
     * @param code Source code to be validated
     * @return {@code true} if the diagram contains any node, {@code false}
     *         otherwise
     */
    private boolean containsAnyNode(String code) {
        return code.contains("[") || code.contains("{") || code.contains("(");
    }

    /**
     * Checks the diagram to verify it contains at least one connection
     * 
     * @param code Source code to be validated
     * @return {@code true} if the diagram contains any connection, {@code false}
     *         otherwise
     */
    private boolean containsAnyConnection(String code) {
        return code.contains("-->") ||
                code.contains("---") ||
                code.contains("-.->") ||
                code.contains("==>");
    }

    /**
     * Validates the number of opening and closing brackets is balanced.
     * 
     * @param code   the diagram source code
     * @param open   the opening bracket character
     * @param close  the closing bracket character
     * @param type   a human-readable name for the bracket type (e.g., "square")
     * @param errors the list to collect errors if unbalanced
     */
    private void validateBracketBalance(
            String code,
            String open,
            String close,
            String type,
            List<String> errors) {
        int openCount = countOccurrences(code, open.charAt(0));
        int closeCount = countOccurrences(code, close.charAt(0));

        if (openCount != closeCount) {
            errors.add("Unbalanced " + type + " brackets found: " +
                    openCount + " '" + open + "' and " + closeCount + "' " +
                    close + "'.");
        }
    }

    /**
     * Counts the number of occurrences of a substring in a string.
     * 
     * @param text      the text to search
     * @param substring the substring to count
     * @return the number of times the substring appears in the text
     */
    private int countOccurrences(String text, char character) {
        int count = 0;
        boolean inString = false;
        boolean inComment = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"' && (i == 0 || text.charAt(i - 1) != '\\')) {
                inString = !inString;
                continue;
            }

            if (!inString &&
                    c == '%' &&
                    i + 1 < text.length() &&
                    text.charAt(i + 1) == '%') {
                inComment = true;
                i++;

                continue;
            }

            if (inComment && (c == '\n' || c == 'r')) {
                inComment = false;
                continue;
            }

            if (!inString && !inComment && c == character) {
                count++;
            }
        }

        return count;
    }

    /**
     * Checks for the presence of a valid flowchart direction declaration.
     * 
     * <p>
     * Mermaid flowcharts typically start with a direction like "graph TD" or
     * "graph LR"
     * </p>
     * 
     * @param code     the diagram source code
     * @param warnings the list to collect warnings if direction is missing.
     */
    private void validateFlowchartDirection(String code, List<String> warnings) {
        Pattern directionPattern = Pattern.compile(
                "(?i)^\\s*flowchart\\s+(TD|LR|RL|BT)\\b",
                Pattern.MULTILINE);

        if (!directionPattern.matcher(code).find()) {
            warnings.add("Flowchart does no specify a direction " +
                    "(e.g., 'graph TD', 'graph LR')");
        }
    }

    /**
     * Checks whether the sequence diagram source contains any participant
     * or actor.
     * 
     * @param code source code to be checked
     * @return {@code true} if the code contains any participant, {@code false}
     *         otherwise
     */
    private boolean containsParticipants(String code) {
        return code.contains("participant") || code.contains("actor");
    }

    /**
     * Validates the source code to find any message arrow connections
     * 
     * @param code source code to be checked
     * @return {@code true} if the code contains message arrows, {@code false}
     *         otherwise
     */
    private boolean containsMessageArrows(String code) {
        return code.contains("->") ||
                code.contains("-->") ||
                code.contains("->>") ||
                code.contains("-->>");
    }

    /**
     * Validates that a block structure (e.g., loop/end, alt/end) is balanced.
     * 
     * @param code   the diagram source code
     * @param open   the opening keyword (e.g., "loop")
     * @param close  the closing keyword (e.g., "end")
     * @param label  a human-readable label for the block type
     * @param errors the list to collect errors if unbalanced
     */
    private void validateBlockBalance(
            String code,
            String open,
            String close,
            String label,
            List<String> errors) {
        int openCount = countKeywordOccurrences(code, open);
        int closeCount = countKeywordOccurrences(code, close);

        if (openCount != closeCount) {
            errors.add("Unbalanced '" + label + "' blocks: found " +
                    openCount + " '" + open + "' and " + closeCount + " '"
                    + close + "'.");
        }
    }

    /**
     * Counts the keyword occurrence inside a Mermaid sequence diagram
     * 
     * @param code    source code to search from
     * @param keyword keyword to search for
     * @return an {@code int} with the number of occurrences
     */
    private int countKeywordOccurrences(String code, String keyword) {
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b");
        Matcher matcher = pattern.matcher(code);

        int count = 0;
        while (matcher.find()) {
            count++;
        }

        return count;
    }

    /**
     * Validates the syntax of note declarations in the sequence diagram.
     * 
     * @param code     the diagram source code
     * @param warnings the list to collect warnings for malformed notes
     */
    private void validateNoteSyntax(String code, List<String> warnings) {
        Pattern notePattern = Pattern.compile(
                "(?i)^\\s*note\\s+(left|right|over)\\b.*",
                Pattern.MULTILINE);

        Matcher matcher = notePattern.matcher(code);
        while (matcher.find()) {
            String line = matcher.group();
            if (!line.contains(":") && !line.contains("\n")) {
                warnings.add("Note may be missing content or colon: \"" +
                        line.trim() + "\"");
            }
        }
    }

    /**
     * Validates whether a class diagram contains a class definition.
     * 
     * @param code source code to be checked
     * @return {@code true} if the diagram contains a class definition,
     *         {@code false} otherwise.
     */
    private boolean containsClassDefinition(String code) {
        Pattern classPattern = Pattern.compile("(?m)^\\s*class\\s+\\w+");
        return classPattern.matcher(code).find();
    }

    /**
     * Validates whether a class diagram contains any relationship.
     * 
     * @param code source code to be checked
     * @return {@code true} if the diagram contains at least one relationship,
     *         {@code false} otherwise
     */
    private boolean containsAnyRelationship(String code) {
        return code.contains("--") ||
                code.contains("<|--") ||
                code.contains("*--") ||
                code.contains("o--");
    }

    /**
     * Validates whether the state diagram contains any valid state.
     * 
     * @param code source code to validate
     * @return {@code true} if the state diagram contains any state, {@code false}
     *         otherwise
     */
    private boolean containsAnyState(String code) {
        Pattern transitionPattern = Pattern.compile(
                "\\b([\\w\\[\\]\\*]+)\\s*-->",
                Pattern.MULTILINE);

        Matcher matcher = transitionPattern.matcher(code);
        Set<String> states = new HashSet<>();

        while (matcher.find()) {
            String state = matcher.group(1);
            if (!state.equals("[*]")) {
                states.add(state);
            }
        }

        return !states.isEmpty();
    }

    /**
     * Validates whether the state diagram contains any transition
     * 
     * @param code source code to validate
     * @return {@code true} if the state diagram contains any transition,
     *         {@code false}
     *         otherwise
     */
    private boolean containsAnyTransition(String code) {
        Pattern transitionPattern = Pattern.compile("(?m)^\\s*\\S+\\s+--?>\\s+\\S+");
        return transitionPattern.matcher(code).find();
    }

    /**
     * Validates whether the state diagram contains any transition
     * 
     * @param code source code to validate
     * @return {@code true} if the state diagram contains any start or end state,
     *         {@code false} otherwise
     */
    private boolean containsStartOrEndState(String code) {
        return code.contains("[*] -->") || code.contains("--> [*]");
    }

    /**
     * Validates whether the ER diagram contains any entity definition
     * 
     * @param code source code to validate
     * @return {@code true} if the ER diagram contains any entity, {@code false}
     *         otherwise
     */
    private boolean containsEntityDefinition(String code) {
        Pattern entityPattern = Pattern.compile("(?m)^\\s*\\w+\\s*\\{");
        return entityPattern.matcher(code).find();
    }

    /**
     * Validates whether the ER diagram contains at least one relationship.
     * 
     * @param code source code to validate
     * @return {@code true} if the ER diagram contains any relationship,
     *         {@code false} otherwise.
     */
    private boolean containsAnyERRelationship(String code) {
        Pattern relationPattern = Pattern.compile(
                "(?m)^\\s*\\w+\\s+([|}o]{1,2}--[|{o]{1,2})\\s+\\w+");

        return relationPattern.matcher(code).find();
    }

    /**
     * Validates an entity block
     * 
     * @param code   source code to be validated
     * @param errors a list to collect critical errors
     */
    private void validateEREntityBlock(String code, List<String> errors) {
        Pattern entityBlockPattern = Pattern.compile(
                "\\b\\w+\\s*\\{[^}]*\\}",
                Pattern.MULTILINE | Pattern.DOTALL);

        Matcher matcher = entityBlockPattern.matcher(code);
        int validBlocks = 0;

        while (matcher.find()) {
            validBlocks++;
        }

        int openCount = countKeywordOccurrences(code, "{");
        int closeCount = countKeywordOccurrences(code, "}");

        if (openCount != closeCount) {
            errors.add("Unbalanced or malformed ER entity block: found " +
                    openCount + "'{' and " + closeCount + " '}', with " +
                    validBlocks + " valid entity blocks");
        }
    }

    /**
     * Validates a single task line within a Mermaid Gantt Diagram.
     * 
     * <p>
     * This method checks the line against supported task formats, including:
     * <ul>
     * <li>Task with ID, start date, and duration</li>
     * <li>Task with dependency (after another task ID)</li>
     * <li>Task with start date and duration</li>
     * <li>Task with duration analysis</li>
     * </ul>
     * 
     * <p>
     * It also tracks task IDs to detect duplicates and unresolved dependencies
     * </p>
     * 
     * @param line       the task line to validate
     * @param lineNumber the line number in the source code (1-based)
     * @param taskIds    a set of known task IDs for tracking duplicates and
     *                   dependencies
     * @param warnings   a list to collect warnings related to the task line
     */
    private void validateTaskLine(
            String line, int lineNumber, Set<String> taskIds, List<String> warnings) {
        Matcher m1 = TASK_WITH_ID_AND_DATE.matcher(line);
        Matcher m2 = TASK_AFTER_ID.matcher(line);
        Matcher m3 = TASK_WITH_DATE_ONLY.matcher(line);
        Matcher m4 = TASK_WITH_DURATION_ONLY.matcher(line);
        Matcher m5 = TASK_WITH_ID_AND_AFTER.matcher(line);

        if (m1.matches()) {
            String id = m1.group(2);
            if (!taskIds.add(id)) {
                warnings.add("Duplicate task ID '" + id + "' on line " + lineNumber);
            }
        } else if (m5.matches()) {
            String id = m5.group(2);
            String refId = m5.group(3);

            if (!taskIds.add(id)) {
                warnings.add("Duplicate task ID '" + id + "' on line " + lineNumber);
            }

            if (!taskIds.contains(refId)) {
                warnings.add("Referenced task ID '" + refId + "' on line " + 
                    lineNumber + " not found before use");
            }
        } else if (m2.matches()) {
            String refId = m2.group(2);
            if (!taskIds.contains(refId)) {
                warnings.add("Referenced task ID '" + refId + "' on line " + lineNumber +
                        " not found before use.");
            }
        } else if (!m3.matches() && !m4.matches()) {
            warnings.add("Unrecognized or malformed task line at " + lineNumber +
                    ": \"" + line + "\"");
        }
    }
}