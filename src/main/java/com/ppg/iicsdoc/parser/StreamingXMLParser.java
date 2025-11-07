package com.ppg.iicsdoc.parser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.ppg.iicsdoc.model.domain.Connection;
import com.ppg.iicsdoc.model.domain.ConnectionType;
import com.ppg.iicsdoc.model.domain.ParsedMetadata;
import com.ppg.iicsdoc.model.domain.ProcessType;
import com.ppg.iicsdoc.model.domain.Transformation;
import com.ppg.iicsdoc.model.domain.TransformationType;

import lombok.extern.slf4j.Slf4j;

/**
 * Streaming XML parser for extracting metadata from Informatica IICS process
 * definition files.
 *
 * <p>
 * This parser uses the SAX (Simple API for XML) parsing model to process large
 * XML files efficiently without loading the entire document into memory. It is
 * particularly suited for parsing large Informatica Cloud Integration (IICS)
 * process metadata files that may contain thousands of nested elements such as
 * <code>connection</code> and <code>transformation</code>.
 * </p>
 *
 * <p>
 * The parser builds a {@link ParsedMetadata} object by incrementally processing
 * XML elements and attributes, extracting information such as:
 * </p>
 * 
 * <ul>
 * <li>Process name, type, version, and description</li>
 * <li>Connection definitions (ID, type, URL, name, etc.)</li>
 * <li>Transformation components and their associated metadata</li>
 * </ul>
 *
 * <p>
 * Since it uses the SAX API, the parser is highly memory-efficient and suitable
 * for batch or real-time validation pipelines where many process definitions
 * are analyzed.
 * </p>
 *
 * <h3>Example Usage</h3>
 * 
 * <pre>{@code
 * @Autowired
 * private StreamingXMLParser xmlParser;
 *
 * public void parseExampleFile(Path processFile) {
 *     try {
 *         ParsedMetadata metadata = xmlParser.parseStreaming(processFile);
 *         System.out.println("Process: " + metadata.getProcessName());
 *     } catch (Exception e) {
 *         log.error("Failed to parse XML file", e);
 *     }
 * }
 * }</pre>
 *
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-25
 */
@Slf4j
@Component
public class StreamingXMLParser {

    /**
     * Parses an XML file using a streaming (SAX-based) approach and extracts
     * process metadata.
     *
     * @param xmlFile the path to the XML file to parse
     * @return a fully populated {@link ParsedMetadata} instance
     * @throws Exception if an error occurs during SAX parsing or file reading
     */
    public ParsedMetadata parseStreaming(Path xmlFile) throws Exception {
        log.info("Starting streaming parse of: {}", xmlFile.getFileName());

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);

        SAXParser saxParser = factory.newSAXParser();
        IICSProcessHandler handler = new IICSProcessHandler();

        saxParser.parse(xmlFile.toFile(), handler);
        return handler.getMetadata();
    }

    /**
     * Internal SAX handler for parsing IICS process definitions.
     *
     * <p>
     * This class listens to SAX parsing events (element start, element end, and
     * text content) and dynamically builds a {@link ParsedMetadata} object with
     * nested {@link Connection} and {@link Transformation} structures.
     * </p>
     */
    private static class IICSProcessHandler extends DefaultHandler {

        private ParsedMetadata.ParsedMetadataBuilder metadataBuilder;
        private List<Connection> connections = new ArrayList<>();
        private List<Transformation> transformations = new ArrayList<>();

        private Stack<String> elementStack = new Stack<>();
        private StringBuilder textBuffer = new StringBuilder();

        private Connection.ConnectionBuilder currentConnection;
        private Transformation.TransformationBuilder currentTransformation;

        /**
         * Called by the SAX parser when a new XML element starts.
         * Initializes builders for metadata, connection, or transformation elements.
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            elementStack.push(qName);
            textBuffer.setLength(0);

            switch (qName) {
                case "process" -> {
                    metadataBuilder = ParsedMetadata.builder()
                            .processName(attributes.getValue("name"))
                            .processType(parseProcessType(attributes.getValue("type")))
                            .version(attributes.getValue("version"));
                }

                case "connection" -> {
                    currentConnection = Connection.builder()
                            .id(attributes.getValue("id"))
                            .type(parseConnectionType(attributes.getValue("type")));
                }

                case "transformation" -> {
                    currentTransformation = Transformation.builder()
                            .id(attributes.getValue("id"))
                            .type(parseTransformationType(attributes.getValue("type")));
                }
            }
        }

        /**
         * Called when an XML element ends.
         * Populates the corresponding builder or appends collected text to fields.
         */
        @Override
        public void endElement(String uri, String localName, String qName) {
            String currentPath = String.join("/", elementStack);
            String text = textBuffer.toString().trim();

            if (currentPath.endsWith("/metadata/description")) {
                metadataBuilder.description(text);
            } else if (currentPath.endsWith("/metadata/author")) {
                metadataBuilder.author(text);
            } else if (currentPath.endsWith("/connection/name")) {
                currentConnection.name(text);
            } else if (currentPath.endsWith("/connection/url")) {
                currentConnection.url(text);
            } else if (qName.equals("connection")) {
                connections.add(currentConnection.build());
                currentConnection = null;
            } else if (qName.equals("transformation")) {
                transformations.add(currentTransformation.build());
                currentTransformation = null;
            }

            elementStack.pop();
            textBuffer.setLength(0);
        }

        /** Collects text between XML tags. */
        @Override
        public void characters(char[] ch, int start, int length) {
            textBuffer.append(ch, start, length);
        }

        public ParsedMetadata getMetadata() {
            return metadataBuilder
                    .connections(connections)
                    .transformations(transformations)
                    .build();
        }

        private ProcessType parseProcessType(String type) {
            return switch (type) {
                case "CAI" -> ProcessType.CAI;
                case "CDI" -> ProcessType.CDI;
                default -> ProcessType.CAI;
            };
        }

        private ConnectionType parseConnectionType(String type) {
            return switch (type) {
                case "REST" -> ConnectionType.REST;
                case "SOAP" -> ConnectionType.SOAP;
                case "DATABASE", "ORACLE", "SQLSERVER" -> ConnectionType.DATABASE;
                case "FILE" -> ConnectionType.FILE;
                case "FTP" -> ConnectionType.FTP;
                case "SFTP" -> ConnectionType.SFTP;
                case "AMAZONS3", "S3" -> ConnectionType.S3;
                case "SNOWFLAKE" -> ConnectionType.SNOWFLAKE;
                case "REDSHIFT" -> ConnectionType.REDSHIFT;
                default -> ConnectionType.OTHER;
            };
        }

        private TransformationType parseTransformationType(String type) {
            return switch (type) {
                case "EXPRESSION" -> TransformationType.EXPRESSION;
                case "FILTER" -> TransformationType.FILTER;
                case "AGGREGATOR" -> TransformationType.AGGREGATOR;
                case "JOINER" -> TransformationType.JOINER;
                case "LOOKUP" -> TransformationType.LOOKUP;
                case "ROUTER" -> TransformationType.ROUTER;
                case "SORTER" -> TransformationType.SORTER;
                case "UNION" -> TransformationType.UNION;
                default -> TransformationType.OTHER;
            };
        }
    }
}
