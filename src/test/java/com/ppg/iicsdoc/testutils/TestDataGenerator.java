package com.ppg.iicsdoc.testutils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

public class TestDataGenerator {
   
    public static void main(String[] args) throws IOException {
        Path testResourcesDir = Paths.get("src/test/resources/sample-xml-generated");
        Files.createDirectories(testResourcesDir);

        generateMinimalProcess(testResourcesDir);
        generateLargeProcess(testResourcesDir);
        generateNestedTransformations(testResourcesDir);
    }

    private static void generateMinimalProcess(Path outputDir) throws IOException {
        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<process name=\"MinimalProcess\" type=\"CAI\" xmlns=\"http://informatica.com/iics\">\n");
        xml.append("<metadata>\n");
        xml.append("<description>Minimal valid process</description>\n");
        xml.append("<author>Test Generator</author>\n");
        xml.append("<created>").append(LocalDate.now()).append("</created>\n");
        xml.append("</metadata>\n");
        xml.append("</process>\n");

        Files.writeString(outputDir.resolve("minimal-process.xml"), xml.toString());
    }

    private static void generateLargeProcess(Path outputDir) throws IOException {
        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<process name=\"LargeProcess\" type=\"CDI\" version=\"1.0.0\">\n");
        xml.append("<metadata>\n");
        xml.append("<description>Large process for performance testing</description>\n");
        xml.append("<author>Test Generator</author>\n");
        xml.append("<created>").append(LocalDate.now()).append("</created>\n");
        xml.append("</metadata>\n");

        xml.append("<connections>\n");
        for (int i = 1; i <= 50; i++) {
            xml.append(String.format("<connection id=\"conn%d\" type=\"Database\">\n", i));
            xml.append(String.format("<name>Connection %d</name>\n", i));
            xml.append(String.format("<host>db%d.example.com</host>\n", i));
            xml.append(String.format("<port>5432</port>\n"));
            xml.append(String.format("<database>db_%d</database>", i));
            xml.append("<authentication type=\"Password\" />\n");
            xml.append("</connection>\n");
        }

        xml.append("</connections>\n").append("<transformations>\n");
        for (int i = 1; i <= 100; i++) {
            xml.append(String.format("<transformation id=\"trans%d\" type=\"Expression\">\n", i));
            xml.append(String.format("<name>Transformation %d</name>", i));
            xml.append(String.format("<description>Generated transformation %d</description>\n", i));
            xml.append(String.format("<expression>FIELD_%d * 2</expression>\n", i));
            xml.append("<inputFields>");
            xml.append(String.format("<field name=\"FIELD_%d\" type=\"Integer\" required=\"true\" />\n", i));
            xml.append("</inputFields>");
            xml.append("<outputFields>");
            xml.append(String.format("<field name=\"RESULT_%d\" type=\"Integer\" required=\"true\" />\n", i));
            xml.append("</outputFields>");
            xml.append("</transformation>");
        }

        xml.append("</transformations>");
        xml.append("</process>\n");

        Files.writeString(outputDir.resolve("large-process.xml"), xml.toString());
    }

    private static void generateNestedTransformations(Path outputDir) throws IOException {
        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<process name=\"LargeProcess\" type=\"CDI\" xmlns=\"http://informatica.com/iics\">\n");
        xml.append("<metadata>\n");
        xml.append("<description>Process with complex nested transformations</description>\n");
        xml.append("<author>Test Generator</author>\n");
        xml.append("<created>").append(LocalDate.now()).append("</created>\n");
        xml.append("</metadata>\n");
        
        xml.append("<transformations>\n");
        for (int i = 1; i <= 20; i++) {
            xml.append(String.format("<transformation id=\"trans%d\" type=\"Expression\">\n", i));
            xml.append(String.format("<name>Step %d</name>\n", i));
            xml.append(String.format("<description>Processing step %d in the chain</description>\n", i));

            if (i == 1) {
                xml.append("<expression>INPUT_VALUE</expression>");
            } else {
                xml.append(String.format("<expression>PREV_VALUE_%d + %d</expression>", i, i));
            }

            xml.append("<inputFields>\n");
            if (i == 1) {
                xml.append("<field name=\"INPUT_VALUE\" type=\"Integer\" required=\"true\"/>\n");
            } else {
                xml.append(String.format("<field name=\"PREV_VALUE_%d\" type=\"Integer\" required=\"true\"/>\n", i));
            }

            xml.append("</inputFields>\n");
            xml.append("<outputFields>\n");
            xml.append(String.format("<field name=\"PREV_VALUE_%d\" type=\"Integer\" required=\"true\"/>\n", i));
            xml.append("</outputFields>\n");
            xml.append("</transformation>");
        }

        xml.append("</transformations>\n");
        xml.append("</process>");

        Files.writeString(outputDir.resolve("nested-transformations.xml"), xml.toString());
    }
}
