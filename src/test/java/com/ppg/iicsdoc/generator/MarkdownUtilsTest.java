package com.ppg.iicsdoc.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class MarkdownUtilsTest {
    
    @Test
    void shouldEscapeSpecialCharacters() {
        String text = "Test * with _ special # characters";
        String escaped = MarkdownUtil.escape(text);

        assertTrue(escaped.contains("\\*"));
        assertTrue(escaped.contains("\\_"));
        assertTrue(escaped.contains("\\#"));
    }

    @Test 
    void shouldCreateHeader() {
        String header = MarkdownUtil.header(2, "Test Header");
        
        assertEquals("## Test Header\n\n", header);
    }

    @Test 
    void shouldCreateLink() {
        String link = MarkdownUtil.link("Google", "https://google.com");

        assertEquals("[Google](https://google.com)", link);
    }

    @Test 
    void shouldCreateCodeBlock() {
        String codeBlock = MarkdownUtil.codeBlock("const x = 1;", "javascript");

        assertTrue(codeBlock.startsWith("```javascript"));
        assertTrue(codeBlock.contains("const x = 1;"));
        assertTrue(codeBlock.contains("```\n"));
    }

    @Test 
    void shouldCreateInlineCode() {
        String inlineCode = MarkdownUtil.inlineCode("variable");

        assertEquals("`variable`", inlineCode);
    }

    @Test 
    void shouldCreateTable() {
        List<String> headers = List.of("Name", "Type", "Required");
        List<List<String>> rows = List.of(
            List.of("field1", "string", "Yes"),
            List.of("field2", "number", "No")
        );

        String table = MarkdownUtil.table(headers, rows);

        assertTrue(table.contains("| Name | Type | Required |"));
        assertTrue(table.contains("|---|---|---|"));
        assertTrue(table.contains("| field1 | string | Yes |"));
        assertTrue(table.contains("| field2 | number | No |"));
    }

    @Test 
    void shouldCreateUnorderedList() {
        List<String> items = List.of("Item 1", "Item 2", "Item 3");
        String list = MarkdownUtil.list(items, false);

        assertTrue(list.contains("- Item 1"));
        assertTrue(list.contains("- Item 2"));
        assertTrue(list.contains("- Item 3"));
    }

    @Test 
    void shouldCreateOrderedList() {
        List<String> items = List.of("First", "Second", "Third");
        String list = MarkdownUtil.list(items, true);

        assertTrue(list.contains("1. First"));
        assertTrue(list.contains("2. Second"));
        assertTrue(list.contains("3. Third"));
    }

    @Test 
    void shouldCreateBlockquote() {
        String text = "This is a quote\nWith multiple lines";
        String blockquote = MarkdownUtil.blockquote(text);

        assertTrue(blockquote.contains("> This is a quote"));
        assertTrue(blockquote.contains("> With multiple lines"));
    }

    @Test
    void shouldCreateHorizontalRule() {
        String hr = MarkdownUtil.horizontalRule();

        assertEquals("---\n\n", hr);
    }

    @Test
    void shouldCreateBoldText() {
        String bold = MarkdownUtil.bold("Important");

        assertEquals("**Important**", bold);
    }

    @Test 
    void shouldCreateItalicText() {
        String italic = MarkdownUtil.italic("Emphasis");

        assertEquals("*Emphasis*", italic);
    }

    @Test 
    void shouldHandleNullInEscape() {
        String escaped = MarkdownUtil.escape(null);
        assertEquals(null, escaped);
    }

    @Test 
    void shouldClampHeaderLevel() {
        String tooLow = MarkdownUtil.header(0, "Test");
        String tooHigh = MarkdownUtil.header(10, "Test Too!");

        assertTrue(tooLow.startsWith("# Test"));
        assertTrue(tooHigh.contains("###### Test Too!"));
    }
}
