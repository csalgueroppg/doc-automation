package com.ppg.iicsdoc.parser;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

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
import com.ppg.iicsdoc.util.FileUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for parsing IICS XML metadata files
 */
@Slf4j
@Service
public class XMLParserService {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;

    /**
     * Parse XML file and extract metadata
     */
    public ParsedMetadata parse(Path xmlFile) throws ParsingException {
        log.info("Starting to parse XML file: {}", xmlFile);
        long startTime = System.currentTimeMillis();

        try {
            FileUtil.validateFileReadable(xmlFile);

            Document doc = loadXMLDocument(xmlFile);
            Element root = doc.getDocumentElement();
            ParsedMetadata metadata = buildMetadata(root);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Successfully parsed XML file in {} ms", duration);

            return metadata;
        } catch (Exception e) {
            log.error("Failed to parse XML files: {}", xmlFile, e);
            throw new ParsingException("Failed to parse XML file", e);
        }
    }

    /**
     * Load XML document using DOM parser
     */
    private Document loadXMLDocument(Path xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlFile.toFile());
    }

    /**
     * Build ParsedMetadata from root element
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
     * Parse connections
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
     * Parse single connection
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
     * Parse transformations
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
     * Parse single transformation
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
     * Parse fields
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
     * Parse single field
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
     * Parse OpenAPI endpoints
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
     * Parse single endpoint
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
     * Parse parameters
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
     * Parse single parameter
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
     * Parse response
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
     * Parse single response
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
     * Parse tags
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
     * Parse data flow
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
     * Parse data source
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
     * Parse transformation references
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
     * Parse data target
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

    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }

        return nodes.item(0).getTextContent().trim();
    }

    private Element getFirstChildElement(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? (Element) nodes.item(0) : null;
    }

    private ProcessType parseProcessType(String type) {
        try {
            return ProcessType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            log.warn("Unknown process type: {}, defaulting to CAI", type);
            return ProcessType.CAI;
        }
    }

    private ConnectionType parseConnectionType(String type) {
        try {
            return ConnectionType.valueOf(type.toUpperCase().replace(" ", "_"));
        } catch (Exception e) {
            log.warn("Unknown connection type: {}, defaulting to OTHER", type);
            return ConnectionType.OTHER;
        }
    }

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

    private TransformationType parseTransformationType(String type) {
        try {
            return TransformationType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            log.warn("Unknown transformation type: {}, defaulting to OTHER", type);
            return TransformationType.OTHER;
        }
    }

    private HttpMethod parseHttpMethod(String method) {
        try {
            return HttpMethod.valueOf(method.toUpperCase());
        } catch (Exception e) {
            log.warn("Unknown HTTP method: {}, defaulting to GET", method);
            return HttpMethod.GET;
        }
    }

    private ParameterLocation parseParameterLocation(String location) {
        try {
            return ParameterLocation.valueOf(location.toUpperCase());
        } catch (Exception e) {
            log.warn("Unknown parameter location: {}, defaulting to QUERY", location);
            return ParameterLocation.QUERY;
        }
    }

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
