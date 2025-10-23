package com.ppg.iicsdoc.generator;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for generating Markdown formatted text.
 * 
 * <p>
 * This class provides static methods to create various Markdown elements
 * such as headers, links, code blocks, tables, lists, blockquotes,
 * horizontal rules, bold and italic text, and badges.
 * </p>
 * 
 * <p>
 * Example usage:
 * </P>
 * 
 * <pre>{@code
 * 
 * String header = MarkdownUtil.header(2, "My Header");
 * String link = MarkdownUtil.link("Google", "https://www.google.com");
 * 
 * String codeBlock = MarkdownUtil.codeBlock("System.out.println(\"Hello, World!\");", "java");
 * List<String> headers = List.of("Name", "Age");
 * List<List<String>> rows = List.of(
 *         List.of("Alice", "30"),
 *         List.of("Bob", "25"));
 * 
 * String table = MarkdownUtil.table(headers, rows);
 * String badge = MarkdownUtil.badge("build", "passing", "green");
 * }</pre>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-23
 */
public class MarkdownUtil {

    /**
     * Escapes special Markdown characters in the given text.
     * 
     * @param text The input text to escape.
     * @return The escaped text.
     */
    public static String escape(String text) {
        if (text == null) {
            return null;
        }

        return text
                .replace("\\", "\\\\")
                .replace("`", "\\`")
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }

    /**
     * Creates a Markdown header.
     * 
     * @param level The header level (1-6).
     * @param text  The header text.
     * @return The formatted Markdown header.
     */
    public static String header(int level, String text) {
        int safeLevel = Math.max(1, Math.min(level, 6));
        return "#".repeat(safeLevel) + " " + text + "\n\n";
    }

    /**
     * Creates a Markdown link.
     * 
     * @param text The link text.
     * @param url  The link URL.
     * @return The formatted Markdown link.
     */
    public static String link(String text, String url) {
        return "[" + text + "](" + url + ")";
    }

    /**
     * Creates a Markdown code block.
     * 
     * @param code     The code content.
     * @param language The programming language for syntax highlighting.
     * @return The formatted Markdown code block.
     */
    public static String codeBlock(String code, String language) {
        return String.format("```%s\n%s\n```\n\n", language, code);
    }

    /**
     * Creates inline code in Markdown.
     * 
     * @param code The code content.
     * @return The formatted inline code.
     */
    public static String inlineCode(String code) {
        return "`" + code + "`";
    }

    /**
     * Creates a Markdown table.
     * 
     * @param headers The table headers.
     * @param rows    The table rows.
     * @return The formatted Markdown table.
     */
    public static String table(List<String> headers, List<List<String>> rows) {
        if (headers == null || headers.isEmpty() || rows == null || rows.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("| ").append(String.join(" | ", headers)).append(" |\n");
        sb.append("|");

        for (int i = 0; i < headers.size(); i++) {
            sb.append("---|");
        }

        sb.append("\n");
        for (List<String> row : rows) {
            sb.append("| ").append(String.join(" | ", row)).append(" |\n");
        }

        return sb.toString();
    }

    /**
     * Creates a Markdown list.
     * 
     * @param items   The list items.
     * @param ordered Whether the list is ordered (numbered) or unordered
     *                (bulleted).
     * @return The formatted Markdown list.
     */
    public static String list(List<String> items, boolean ordered) {
        if (items == null || items.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (ordered) {
                sb.append(i + 1).append(". ").append(items.get(i)).append("\n");
            } else {
                sb.append("- ").append(items.get(i)).append("\n");
            }
        }

        sb.append("\n");
        return sb.toString();
    }

    /**
     * Creates a Markdown blockquote.
     * 
     * @param text The blockquote text.
     * @return The formatted Markdown blockquote.
     */
    public static String blockquote(String text) {
        return text.lines()
                .map(line -> "> " + line)
                .collect(Collectors.joining("\n")) + "\n\n";
    }

    /**
     * Creates a horizontal rule in Markdown.
     * 
     * @return The formatted horizontal rule.
     */
    public static String horizontalRule() {
        return "---\n\n";
    }

    /**
     * Creates bold text in Markdown.
     * 
     * @param text The text to be bolded.
     * @return The formatted bold text.
     */
    public static String bold(String text) {
        return "**" + text + "**";
    }

    /**
     * Creates italic text in Markdown.
     * 
     * @param text The text to be italicized.
     * @return The formatted italic text.
     */
    public static String italic(String text) {
        return "*" + text + "*";
    }

    /**
     * Creates a badge using Shields.io in Markdown.
     * 
     * @param label The badge label.
     * @param value The badge value.
     * @param color The badge color.
     * @return The formatted Markdown badge.
     */
    public static String badge(String label, String value, String color) {
        String url = String.format("https://img.shields.io/badge/%s-%s-%s",
                label.replace(" ", "%20"),
                value.replace(" ", "%20"),
                color);

        return String.format("![%s](%s)", label + ": " + value, url);
    }

    /**
     * URL-encodes special characters for Shields.io badges.
     * 
     * @param text The text to encode.
     * @return The URL-encoded text.
     */
    private static String urlEncode(String text) {
        return text.replace(" ", "%20")
                .replace("-", "--")
                .replace("_", "__")
                .replace("=", "==")
                .replace("&", "%26");
    }
}
