package com.ppg.iicsdoc.parser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ppg.iicsdoc.exception.ParsingException;
import com.ppg.iicsdoc.model.domain.AuthenticationType;
import com.ppg.iicsdoc.model.domain.Connection;
import com.ppg.iicsdoc.model.domain.ConnectionType;
import com.ppg.iicsdoc.model.domain.DataFlow;
import com.ppg.iicsdoc.model.domain.DataSource;
import com.ppg.iicsdoc.model.domain.DataTarget;
import com.ppg.iicsdoc.model.domain.Field;
import com.ppg.iicsdoc.model.domain.HttpMethod;
import com.ppg.iicsdoc.model.domain.OpenAPIEndpoint;
import com.ppg.iicsdoc.model.domain.Parameter;
import com.ppg.iicsdoc.model.domain.ParameterLocation;
import com.ppg.iicsdoc.model.domain.ParsedMetadata;
import com.ppg.iicsdoc.model.domain.ProcessType;
import com.ppg.iicsdoc.model.domain.Response;
import com.ppg.iicsdoc.model.domain.Transformation;
import com.ppg.iicsdoc.model.domain.TransformationType;
import com.ppg.iicsdoc.model.validation.SchemaValidationResult;
import com.ppg.iicsdoc.util.FileUtil;
import com.ppg.iicsdoc.validation.XMLValidationService;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides functionality for parsing IICS XML metadata files and converting
 * them into structured {@link ParsedMetadata}
 * 
 * <p>
 * This service handles validation, DOM parsing, and extraction of key metadata
 * such as process details, connections, transformations, OpenAPI endpoints, and
 * more.
 * </p>
 * 
 * <p>
 * Designed for use within a Spring context as a singleton service.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-21
 * 
 * @see ParsedMetadata
 */
@Slf4j
@Service
public class XMLParserService {

    /** Date formatter for consistent timestamp usage. */
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;

    /** XML schema validation instance */
    private final XMLValidationService validationService;

    /**
     * Creates a new instance of {@code XMLParserService} with an attached
     * {@link XMLValidationService} for schema validation.
     * 
     * @param validationService XML schema validation service instance
     */
    public XMLParserService(XMLValidationService validationService) {
        this.validationService = validationService;
    }

    /**
     * Parses the given XML file and extracts metadata into a
     * {@link ParsedMetadata} object.
     * 
     * <p>
     * Uses a custom schema validator as the first step ensuring the xml file
     * is valid and follows the expected pattern.
     * </p>
     * 
     * @param xmlFile the path to the XML file to parse
     * @return the parsed metadata
     * @throws ParsingException if the file cannot be read or parsed.
     */
    public ParsedMetadata parse(Path xmlFile) throws ParsingException {
        log.info("Starting to parse XML file: {}", xmlFile);
        long startTime = System.currentTimeMillis();

        try {
            if (!Files.exists(xmlFile) || !Files.isReadable(xmlFile)) {
                throw new ParsingException("Failed to parse XML file: File does" + 
                    " not exist or is not readable" + xmlFile);
            }

            log.info("Validating XML file");
            SchemaValidationResult validationResult = validationService.validateComplete(xmlFile);

            if (!validationResult.isValid()) {
                log.error("XML validation failed with {} errors", 
                    validationResult.getErrorCount());

                List<String> errorMessages = validationResult.getErrors().stream()  
                    .map(e -> String.format("[%s] Line %d: %s", 
                        e.getCode(), e.getLineNumber(), e.getMessage()))
                    .toList();

                throw new ParsingException(
                    "XML Validation failed",
                    xmlFile.toString(),
                    errorMessages);
            }

            if (validationResult.hasWarnings()) {
                log.warn("XML validation produced {} warnings", 
                    validationResult.getWarningCount());

                validationResult.getWarnings().forEach(w -> log.warn("  [{}] {}", 
                    w.getCode(), w.getMessage()));
            }

            FileUtil.validateFileReadable(xmlFile);

            Document doc = loadXMLDocument(xmlFile);
            Element root = doc.getDocumentElement();
            ParsedMetadata metadata = buildMetadata(root);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Successfully parsed XML file in {} ms", duration);

            return metadata;
        } catch (ParsingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse XML files: {}", xmlFile, e);
            throw new ParsingException("Failed to parse XML file", e);
        }
    }

    /**
     * Parses the given XML file without schema validation.
     * 
     * @param xmlFile the path to the XML file to parse
     * @return the parsed metadata
     * @throws ParsingException if the file cannot be read or parsed.
     */
    public ParsedMetadata parseWithoutValidation(Path xmlFile) throws Exception {
        log.warn("Parsing XML without validation (not recommended)");

        try {
            Document doc = loadXMLDocument(xmlFile);
            Element root = doc.getDocumentElement();

            return buildMetadata(root);
        } catch (Exception e) {
            log.error("Failed to parse XML file", e);
            throw new ParsingException("Failed to parse XML file", e);
        }
    }

    /**
     * Loads an XML {@link Document} from the specified file path using a DOM
     * parser.
     * 
     * <p>
     * The parser is configured to be namespace-aware and to disallow DOCTYPE
     * declarations.
     * </p>
     * 
     * @param xmlFile the path to the XML file
     * @return the parsed XML document
     * @throws Exception if an error occurs during parsing
     */
    private Document loadXMLDocument(Path xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlFile.toFile());
    }

    /**
     * Constructs a {@link ParsedMetadata} object from the root element of the XML
     * document.
     * 
     * <p>
     * Extracts attributes and nested metadata elements such as process name, type,
     * version, author, timestamps, connections, transformations, endpoints, and
     * data flow.
     * </p>
     * 
     * @param root the root element of the XML document
     * @return the constructed metadata object.
     */
    private ParsedMetadata buildMetadata(Element root) {
        log.debug("Building metadata from root element: {}", root.getTagName());

        return ParsedMetadata.builder()
                .processName(root.getAttribute("name"))
                .processType(parseProcessType(root.getAttribute("type")))
                .version(root.getAttribute("version"))
                .description(getTextContent(root, "metadata/description"))
                .author(getTextContent(root, "metadata/author"))
                .created(parseDate(getTextContent(root, "metadata/created")))
                .modified(parseDate(getTextContent(root, "metadata/modified")))
                .connections(parseConnections(root))
                .transformations(parseTransformations(root))
                .openApiEndpoints(parseOpenAPIEndpoints(root))
                .dataFlow(parseDataFlow(root))
                .additionalProperties(new HashMap<>())
                .build();
    }

    /**
     * Parses all {@link Connection} elements from the XML root and returns
     * a list of {@link Connection} objects.
     *
     * @param root the root element of the XML document
     * @return a list of parsed connections
     */
    private List<Connection> parseConnections(Element root) {
        List<Connection> connections = new ArrayList<>();
        NodeList connectionNodes = root.getElementsByTagName("connection");

        log.debug("Found {} connections", connectionNodes.getLength());
        for (int i = 0; i < connectionNodes.getLength(); i++) {
            Element connElement = (Element) connectionNodes.item(i);
            connections.add(parseConnection(connElement));
        }

        return connections;
    }

    /**
     * Parses a single {@code Connection} element and constructs a
     * {@link Connection} object.
     * 
     * <p>
     * Extracts attributes such as ID, type, name, URL, host, database, and
     * authentication type.
     * </p>
     * 
     * @param elem the connection element to parse
     * @return the parsed connection
     */
    private Connection parseConnection(Element elem) {
        String id = elem.getAttribute("id");
        String type = elem.getAttribute("type");

        return Connection.builder()
                .id(id)
                .name(getElementText(elem, "name"))
                .type(parseConnectionType(type))
                .url(getElementText(elem, "url"))
                .host(getElementText(elem, "host"))
                .database(getElementText(elem, "database"))
                .authenticationType(parseAuthType(elem))
                .build();
    }

    /**
     * Parses a single {@code Transformation} element and constructs a
     * {@link Transformation} object.
     * 
     * <p>
     * Extracts attributes such as ID, type, name, expression, condition,
     * input fields, and output fields.
     * </p>
     * 
     * @param elem the transformation element to parse
     * @return the parsed transformation
     */
    private List<Transformation> parseTransformations(Element root) {
        List<Transformation> transformations = new ArrayList<>();
        NodeList transNodes = root.getElementsByTagName("transformation");

        log.debug("Found {} transformations", transNodes.getLength());
        for (int i = 0; i < transNodes.getLength(); i++) {
            Element transElement = (Element) transNodes.item(i);
            transformations.add(parseTransformation(transElement));
        }

        return transformations;
    }

    /**
     * Parses a single {@code Transformation} element and constructs a
     * {@link Transformation} object.
     * 
     * <p>
     * Extracts attributes such as ID, type, name, expression, condition,
     * and associated input/output fields.
     * </p>
     * 
     * @param elem the transformation element to parse
     * @return the parsed transformation
     */
    private Transformation parseTransformation(Element elem) {
        String id = elem.getAttribute("id");
        String type = elem.getAttribute("type");

        return Transformation.builder()
                .id(id)
                .name(getElementText(elem, "name"))
                .type(parseTransformationType(type))
                .expression(getElementText(elem, "expression"))
                .condition(getElementText(elem, "condition"))
                .inputFields(parseFields(elem, "inputFields"))
                .outputFields(parseFields(elem, "outputFields"))
                .build();
    }

    /**
     * Parses a list of {@code Field} elements contained within a specified
     * container tag.
     * 
     * <p>
     * Used to extract input or output fields from transformation elements.
     * </p>
     * 
     * @param parent       the parent element containing the field container
     * @param containerTag the tag name of the container element (e.g.,
     *                     "inputField")
     * @return a list of parsed fields
     */
    private List<Field> parseFields(Element parent, String containerTag) {
        List<Field> fields = new ArrayList<>();
        NodeList containers = parent.getElementsByTagName(containerTag);

        if (containers.getLength() == 0) {
            return fields;
        }

        Element container = (Element) containers.item(0);
        NodeList fieldNodes = container.getElementsByTagName("field");

        for (int i = 0; i < fieldNodes.getLength(); i++) {
            Element fieldElem = (Element) fieldNodes.item(i);
            fields.add(parseField(fieldElem));
        }

        return fields;
    }

    /**
     * Parses a single {@code <field>} element and constructs a {@link Field}
     * object.
     * 
     * <p>
     * Extracts attributes such as name, type, description, and required flag.
     * </p>
     *
     * @param elem the field element to parse
     * @return the parsed field
     */
    private Field parseField(Element elem) {
        return Field.builder()
                .name(elem.getAttribute("name"))
                .type(elem.getAttribute("type"))
                .description(elem.getAttribute("description"))
                .required(Boolean.parseBoolean(elem.getAttribute("required")))
                .build();
    }

    /**
     * Parses the {@code OpenAPIEndpoint} section from the XML root and extracts
     * a list of {@link OpenAPIEndpoint} objects.
     * 
     * <p>
     * Each endpoint represents an API operation with associated metadata.
     * </p>
     * 
     * @param root the root element of the XML element
     * @return a list of parsed OpenAPI endpoints
     */
    private List<OpenAPIEndpoint> parseOpenAPIEndpoints(Element root) {
        List<OpenAPIEndpoint> endpoints = new ArrayList<>();
        NodeList openapiNodes = root.getElementsByTagName("openapi");

        if (openapiNodes.getLength() == 0) {
            log.debug("No OpenAPI section found");
            return endpoints;
        }

        Element openapiElem = (Element) openapiNodes.item(0);
        NodeList endpointNodes = openapiElem.getElementsByTagName("endpoint");

        log.debug("Found {} OpenAPI endpoints", endpointNodes.getLength());
        for (int i = 0; i < endpointNodes.getLength(); i++) {
            Element endpointElem = (Element) endpointNodes.item(i);
            endpoints.add(parseEndpoint(endpointElem));
        }

        return endpoints;
    }

    /**
     * Parses a single {@code OpenAPIEndpoint} element and constructs an
     * {@link OpenAPIEndpoint} object.
     * 
     * <p>
     * Extracts attributes such as path, method, operation ID, summary,
     * description, parameters, responses, and tags.
     * </p>
     * 
     * @param elem endpoint element to parse
     * @return parsed OpenAPI endpoint
     */
    private OpenAPIEndpoint parseEndpoint(Element elem) {
        String path = elem.getAttribute("path");
        String method = elem.getAttribute("method");

        return OpenAPIEndpoint.builder()
                .path(path)
                .method(parseHttpMethod(method))
                .operationId(getElementText(elem, "operationId"))
                .summary(getElementText(elem, "summary"))
                .description(getElementText(elem, "description"))
                .parameters(parseParameters(elem))
                .responses(parseResponses(elem))
                .tags(parseTags(elem))
                .build();
    }

    /**
     * Parses the {@code Parameter} section from an endpoint element and
     * returns a list of {@link Parameter} objects.
     * 
     * <p>
     * each parameter includes metadata such as name, location, type, and
     * description.
     * </p>
     * 
     * @param parent the parent element containing the parameters section
     * @return a list of parsed parameters
     */
    private List<Parameter> parseParameters(Element parent) {
        List<Parameter> parameters = new ArrayList<>();
        NodeList paramContainers = parent.getElementsByTagName("parameters");

        if (paramContainers.getLength() == 0) {
            return parameters;
        }

        Element container = (Element) paramContainers.item(0);
        NodeList paramNodes = container.getElementsByTagName("parameter");

        for (int i = 0; i < paramNodes.getLength(); i++) {
            Element paramElem = (Element) paramNodes.item(i);
            parameters.add(parseParameter(paramElem));
        }

        return parameters;
    }

    /**
     * Parses a single {@code Parameter} element and constructs a
     * {@link Parameter} object.
     * 
     * <p>
     * Extracts attributes such as name, location, type, required flags,
     * description, and default value.
     * </p>
     * 
     * @param elem the parameter element to parse
     * @return the parsed parameter
     */
    private Parameter parseParameter(Element elem) {
        return Parameter.builder()
                .name(elem.getAttribute("name"))
                .in(parseParameterLocation(elem.getAttribute("in")))
                .type(elem.getAttribute("type"))
                .required(Boolean.parseBoolean(elem.getAttribute("required")))
                .description(elem.getAttribute("description"))
                .defaultValue(elem.getAttribute("default"))
                .build();
    }

    /**
     * Parses the {@code Responses} section from an endpoint element and
     * returns a map of {@link Response} objects.
     * 
     * <p>
     * Each response is keyed by its HTTP status code.
     * </p>
     * 
     * @param parent the parent element containing the responses section
     * @return a map of parsed responses keyed by status code
     */
    private Map<String, Response> parseResponses(Element parent) {
        Map<String, Response> responses = new HashMap<>();
        NodeList respContainers = parent.getElementsByTagName("responses");

        if (respContainers.getLength() == 0) {
            return responses;
        }

        Element container = (Element) respContainers.item(0);
        NodeList respNodes = container.getElementsByTagName("response");

        for (int i = 0; i < respNodes.getLength(); i++) {
            Element respElem = (Element) respNodes.item(i);
            String code = respElem.getAttribute("code");

            responses.put(code, parseResponse(respElem));
        }

        return responses;
    }

    /**
     * Parses a single {@code Response} element and constructs a
     * {@link Response} object.
     * 
     * <p>
     * Extract attributes such as code, description, and optional schema
     * details.
     * </p>
     * 
     * @param elem the parent element containing the responses section
     * @return the parsed response.
     */
    private Response parseResponse(Element elem) {
        Element schemaElem = getFirstChildElement(elem, "schema");

        return Response.builder()
                .code(elem.getAttribute("code"))
                .description(getElementText(elem, "description"))
                .schemaType(schemaElem != null ? schemaElem.getAttribute("type") : null)
                .schemaItems(schemaElem != null ? schemaElem.getAttribute("items") : null)
                .build();
    }

    /**
     * Parses the {@code <tags>} section from an endpoint element and returns a list
     * of tag strings.
     *
     * @param parent the parent element containing the tags section
     * @return a list of parsed tags
     */
    private List<String> parseTags(Element parent) {
        List<String> tags = new ArrayList<>();
        NodeList tagContainers = parent.getElementsByTagName("tags");

        if (tagContainers.getLength() == 0) {
            return tags;
        }

        Element container = (Element) tagContainers.item(0);
        NodeList tagNodes = container.getElementsByTagName("tag");

        for (int i = 0; i < tagNodes.getLength(); i++) {
            Element tagElem = (Element) tagNodes.item(i);
            tags.add(tagElem.getTextContent().trim());
        }

        return tags;
    }

    /**
     * Parses the {@code <dataFlow>} section from the XML root and constructs a
     * {@link DataFlow} object.
     * 
     * <p>
     * Includes source, transformation references, and target components.
     * </p>
     * 
     * @param root the root element of the XML document
     * @return the parsed data flow, or {@code null} if not present
     */
    private DataFlow parseDataFlow(Element root) {
        NodeList flowNodes = root.getElementsByTagName("dataFlow");
        if (flowNodes.getLength() == 0) {
            log.debug("No data flow found");
            return null;
        }

        Element flowElem = (Element) flowNodes.item(0);
        return DataFlow.builder()
                .source(parseDataSource(flowElem))
                .transformationRefs(parseTransformationRefs(flowElem))
                .target(parseDataTarget(flowElem))
                .build();
    }

    /**
     * Parses the {@code <source>} element from a data flow and constructs a
     * {@link DataSource} object.
     * 
     * <p>
     * Includes connection reference and entity name.
     * </p>
     * 
     * @param parent the parent element containing the source section
     * @return the parsed data source, or {@code null} if not present
     */
    private DataSource parseDataSource(Element parent) {
        Element sourceElem = getFirstChildElement(parent, "source");
        if (sourceElem == null) {
            return null;
        }

        return DataSource.builder()
                .connectionRef(getElementText(sourceElem, "connectionRef"))
                .entity(getElementText(sourceElem, "entity"))
                .build();
    }

    /**
     * Parses all {@code <transformationRef>} elements from a data flow and returns
     * a list of transformation reference IDs.
     *
     * @param parent the parent element containing transformation references
     * @return a list of transformation reference IDs
     */
    private List<String> parseTransformationRefs(Element parent) {
        List<String> refs = new ArrayList<>();
        NodeList refNodes = parent.getElementsByTagName("transformationRef");

        for (int i = 0; i < refNodes.getLength(); i++) {
            Element refElem = (Element) refNodes.item(i);
            refs.add(refElem.getTextContent().trim());
        }

        return refs;
    }

    /**
     * Parses the {@code <target>} element from a data flow and constructs a
     * {@link DataTarget} object.
     * 
     * <p>
     * Includes connection reference and entity name.
     * </p>
     *
     * @param parent the parent element containing the target section
     * @return the parsed data target, or {@code null} if not present
     */
    private DataTarget parseDataTarget(Element parent) {
        Element targetElem = getFirstChildElement(parent, "target");
        if (targetElem == null) {
            return null;
        }

        return DataTarget.builder()
                .connectionRef(getElementText(targetElem, "connectionRef"))
                .entity(getElementText(targetElem, "entity"))
                .build();
    }

    // Helper methods
    /**
     * Retrieves the text content from a nested XML path within the given parent
     * element.
     * 
     * <p>
     * The path is specified using forward slashes (e.g.,
     * {@code "metadata/author"}).
     * </p>
     *
     * @param parent the parent XML element
     * @param path   the slash-separated path to the target element
     * @return the trimmed text content of the target element, or {@code null} if
     *         not found
     */
    private String getTextContent(Element parent, String path) {
        String[] parts = path.split("/");
        Element current = parent;

        for (String part : parts) {
            NodeList nodes = current.getElementsByTagName(part);
            if (nodes.getLength() == 0) {
                return null;
            }

            current = (Element) nodes.item(0);
        }

        return current != null ? current.getTextContent().trim() : null;
    }

    /**
     * Retrieves the text content of the first child element with the specified tag
     * name.
     *
     * @param parent  the parent XML element
     * @param tagName the tag name of the child element
     * @return the trimmed text content of the child element, or {@code null} if not
     *         found
     */
    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }

        return nodes.item(0).getTextContent().trim();
    }

    /**
     * Returns the first child element with the specified tag name.
     *
     * @param parent  the parent XML element
     * @param tagName the tag name of the child element
     * @return the first matching child element, or {@code null} if none found
     */
    private Element getFirstChildElement(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? (Element) nodes.item(0) : null;
    }

    /**
     * Parses a string into a {@link ProcessType} enum value.
     * 
     * <p>
     * Defaults to {@code CAI} if the value is unrecognized.
     * </p>
     * 
     * @param type the string representation of the process type
     * @return the parsed process type
     */
    private ProcessType parseProcessType(String type) {
        try {
            return ProcessType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            log.warn("Unknown process type: {}, defaulting to CAI", type);
            return ProcessType.CAI;
        }
    }

    /**
     * Parses a string into a {@link ConnectionType} enum value.
     * 
     * <p>
     * Spaces are replaced with underscores before parsing.
     * Defaults to {@code OTHER} if the value is unrecognized.
     * </p>
     *
     * @param type the string representation of the connection type
     * @return the parsed connection type
     */
    private ConnectionType parseConnectionType(String type) {
        try {
            return ConnectionType.valueOf(type.toUpperCase().replace(" ", "_"));
        } catch (Exception e) {
            log.warn("Unknown connection type: {}, defaulting to OTHER", type);
            return ConnectionType.OTHER;
        }
    }

    /**
     * Parses the {@code <authentication>} element from a connection and returns its
     * {@link AuthenticationType}.
     * 
     * <p>
     * Hyphens are replaced with underscores before parsing.
     * Defaults to {@code NONE} if the element is missing or unrecognized.
     * </p>
     * 
     * @param elem the connection element con
     * @return the parsed authentication type
     */
    private AuthenticationType parseAuthType(Element elem) {
        NodeList authNodes = elem.getElementsByTagName("authentication");
        if (authNodes.getLength() == 0) {
            return AuthenticationType.NONE;
        }

        Element authElem = (Element) authNodes.item(0);
        String type = authElem.getAttribute("type");

        try {
            return AuthenticationType.valueOf(type.toUpperCase().replace("-", "_"));
        } catch (Exception e) {
            log.warn("Unknown authentication type: {}, defaulting to NONE", type);
            return AuthenticationType.NONE;
        }
    }

    /**
     * Parses a string into a {@link TransformationType} enum value.
     * 
     * <p>
     * Defaults to {@code OTHER} if the value is unrecognized.
     * </p>
     *
     * @param type the string representation of the transformation type
     * @return the parsed transformation type
     */
    private TransformationType parseTransformationType(String type) {
        try {
            return TransformationType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            log.warn("Unknown transformation type: {}, defaulting to OTHER", type);
            return TransformationType.OTHER;
        }
    }

    /**
     * Parses a string into an {@link HttpMethod} enum value.
     * 
     * <p>
     * Defaults to {@code GET} if the value is unrecognized.
     * </p>
     *
     * @param method the string representation of the HTTP method
     * @return the parsed HTTP method
     */
    private HttpMethod parseHttpMethod(String method) {
        try {
            return HttpMethod.valueOf(method.toUpperCase());
        } catch (Exception e) {
            log.warn("Unknown HTTP method: {}, defaulting to GET", method);
            return HttpMethod.GET;
        }
    }

    /**
     * Parses a string into a {@link ParameterLocation} enum value.
     * 
     * <p>
     * Defaults to {@code QUERY} if the value is unrecognized.
     * </p>
     *
     * @param location the string representation of the parameter location
     * @return the parsed parameter location
     */
    private ParameterLocation parseParameterLocation(String location) {
        try {
            return ParameterLocation.valueOf(location.toUpperCase());
        } catch (Exception e) {
            log.warn("Unknown parameter location: {}, defaulting to QUERY", location);
            return ParameterLocation.QUERY;
        }
    }

    /**
     * Parses a date string into a {@link LocalDate} using the ISO format.
     * 
     * <p>
     * Returns {@code null} if the input is empty or cannot be parsed.
     * </p>
     * 
     * @param dateString the date string to parse
     * @return the parsed date, or {@code null} if invalid
     */
    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dateString, dateFormatter);
        } catch (Exception e) {
            log.warn("Failed to parse data: {}", dateString, e);
            return null;
        }
    }
}
