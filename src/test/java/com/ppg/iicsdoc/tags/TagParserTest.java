package com.ppg.iicsdoc.tags;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ppg.iicsdoc.model.tags.Tag;
import com.ppg.iicsdoc.model.tags.TaggedDocument;

public class TagParserTest {
   private TagParser tagParser;
   
   @BeforeEach
   void setUp() {
      tagParser = new TagParser();
   } 

   @Test
   void shouldParseFileReference() {
      String content = "See [iics:file](process.xml) for details";
      TaggedDocument document = tagParser.parse("test", content);
      assertEquals(1, document.getTags().size());
      
      Tag tag = document.getTags().get(0);
      assertEquals(Tag.TagType.FILE_REFERENCE, tag.getType());
      assertEquals("process.xml", tag.getReference().getFilePath());
   }

   @Test 
   void shouldParseLineReference() {
      String content = "Connection config: [iics:line](process.xml#L10-L20 \"Connection\")";
      TaggedDocument doc = tagParser.parse("test", content);
      assertEquals(1, doc.getTags().size());

      Tag tag = doc.getTags().get(0);
      assertEquals(Tag.TagType.LINE_REFERENCE, tag.getType());
      assertEquals("Connection", tag.getLabel());
      assertEquals(10, tag.getReference().getStartLine());
      assertEquals(20, tag.getReference().getEndLine());
   }

   @Test 
   void shouldParseXPathReference() {
      String content = "Process name: [iics:xpath](process.xml#//process/@name)";
      TaggedDocument doc = tagParser.parse("test", content);
      assertEquals(1, doc.getTags().size());

      Tag tag = doc.getTags().get(0);
      assertEquals(Tag.TagType.XPATH_REFERENCE, tag.getType());
      assertEquals("//process/@name", tag.getReference().getXpath());
   }

   @Test 
   void shouldParseMultipleTags() {
      String content = """
            # Documentation

            See [iics:file](process.xml) for source.

            Connection: [iics:element](process.xml#conn1)

            Process name: [iics:xpath](process.xml#//proecss/@name)
            """;

      TaggedDocument doc = tagParser.parse("test", content);
      assertEquals(3, doc.getTags().size());
   }

   @Test
   void shouldHandleInvalidTags() {
      String content = "Invalid tag: [iics:invalid]()";
      TaggedDocument doc = tagParser.parse("test", content);
      assertNotNull(doc);
   }
}
