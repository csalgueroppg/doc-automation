package com.ppg.iicsdoc.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ppg.iicsdoc.exception.ParsingException;
import com.ppg.iicsdoc.model.domain.AuthenticationType;
import com.ppg.iicsdoc.model.domain.Connection;
import com.ppg.iicsdoc.model.domain.ConnectionType;
import com.ppg.iicsdoc.model.domain.DataFlow;
import com.ppg.iicsdoc.model.domain.Field;
import com.ppg.iicsdoc.model.domain.HttpMethod;
import com.ppg.iicsdoc.model.domain.OpenAPIEndpoint;
import com.ppg.iicsdoc.model.domain.Parameter;
import com.ppg.iicsdoc.model.domain.ParameterLocation;
import com.ppg.iicsdoc.model.domain.ParsedMetadata;
import com.ppg.iicsdoc.model.domain.ProcessType;
import com.ppg.iicsdoc.model.domain.Transformation;
import com.ppg.iicsdoc.model.domain.TransformationType;

class XMLParserServiceTest {
    private XMLParserService parserService;

    @BeforeEach
    void setUp() {
        parserService = new XMLParserService();
    }

    @Test
    void shouldParseSimpleCAIProcess() throws Exception {
        Path xmlFile = Paths.get("src/test/resources/sample-xml/simple-cai-process.xml");
        ParsedMetadata result = parserService.parse(xmlFile);

        assertNotNull(result);
        assertEquals("CustomerDataSync", result.getProcessName());
        assertEquals(ProcessType.CAI, result.getProcessType());
        assertEquals("1.0", result.getVersion());
        assertEquals("Synchronizes customer data between systems", result.getDescription());
        assertEquals("John Doe", result.getAuthor());
    }

    @Test
    void shouldParseConnection() throws Exception {
        Path xmlFile = Paths.get("src/test/resources/sample-xml/simple-cai-process.xml");
        ParsedMetadata result = parserService.parse(xmlFile);

        assertEquals(2, result.getConnections().size());

        Connection restConn = result.getConnections().get(0);
        assertEquals("conn1", restConn.getId());
        assertEquals("Customer API", restConn.getName());
        assertEquals(ConnectionType.REST, restConn.getType());
        assertEquals("https://api.example.com/customers", restConn.getUrl());
        assertEquals(AuthenticationType.OAUTH2, restConn.getAuthenticationType());
    }

    @Test
    void shouldParseTransformations() throws Exception {
        Path xmlFile = Paths.get("src/test/resources/sample-xml/simple-cai-process.xml");
        ParsedMetadata result = parserService.parse(xmlFile);

        assertEquals(2, result.getTransformations().size());

        Transformation exprTrans = result.getTransformations().get(0);
        assertEquals("trans1", exprTrans.getId());
        assertEquals("FormatCustomerData", exprTrans.getName());
        assertEquals(TransformationType.EXPRESSION, exprTrans.getType());
        assertEquals("CONCAT(firstName, ' ', lastName)", exprTrans.getExpression());
        assertEquals(2, exprTrans.getInputFields().size());
        assertEquals(1, exprTrans.getOutputFields().size());
    }

    @Test
    void shouldParseOpenAPIEndpoints() throws Exception {
        Path xmlFile = Paths.get("src/test/resources/sample-xml/simple-cai-process.xml");
        ParsedMetadata result = parserService.parse(xmlFile);

        assertEquals(1, result.getOpenApiEndpoints().size());

        OpenAPIEndpoint endpoint = result.getOpenApiEndpoints().get(0);
        assertEquals("/customers", endpoint.getPath());
        assertEquals(HttpMethod.GET, endpoint.getMethod());
        assertEquals("getCustomers", endpoint.getOperationId());
        assertEquals("Retrieve customer list", endpoint.getSummary());
        assertEquals(2, endpoint.getParameters().size());
        assertTrue(endpoint.getResponses().containsKey("200"));
        assertEquals(2, endpoint.getTags().size());
        assertTrue(endpoint.getTags().contains("customers"));
        assertTrue(endpoint.getTags().contains("data-sync"));
    }

    @Test
    void shouldParseDataFlow() throws Exception {
        Path xmlFile = Paths.get("src/test/resources/sample-xml/simple-cai-process.xml");
        ParsedMetadata result = parserService.parse(xmlFile);

        assertNotNull(result.getDataFlow());

        DataFlow flow = result.getDataFlow();
        assertNotNull(flow.getSource());
        assertEquals("conn1", flow.getSource().getConnectionRef());
        assertEquals("Customer", flow.getSource().getEntity());
        assertEquals(2, flow.getTransformationRefs().size());
        assertEquals("trans1", flow.getTransformationRefs().get(0));
        assertEquals("trans2", flow.getTransformationRefs().get(1));
        assertNotNull(flow.getTarget());
        assertEquals("conn2", flow.getTarget().getConnectionRef());
        assertEquals("CustomerTable", flow.getTarget().getEntity());
    }

    @Test 
    void shouldThrowExceptionForNonExistentFile() {
        Path xmlFile = Paths.get("src/test/resources/sample-xml/non-existent.xml");
        ParsingException exception = assertThrows(ParsingException.class, () -> {
            parserService.parse(xmlFile);
        });

        assertTrue(exception.getMessage().contains("Failed to parse XML file"));
    }

    @Test 
    void shouldHandleTransformations() throws Exception {
        Path xmlFile = Paths.get("src/test/resources/sample-xml/simple-cai-process.xml");
        ParsedMetadata result = parserService.parse(xmlFile);

        assertNotNull(result.getTransformations());
        assertFalse(result.getTransformations().isEmpty());
    }

    @Test
    void shouldParseFieldsCorrectly() throws Exception {
        Path xmlFile = Paths.get("src/test/resources/sample-xml/simple-cai-process.xml");
        ParsedMetadata result = parserService.parse(xmlFile);
        Transformation trans = result.getTransformations().get(0);
        Field firstNameField = trans.getInputFields().get(0);

        assertEquals("firstName", firstNameField.getName());
        assertEquals("string", firstNameField.getType());

        Field fullNameField = trans.getOutputFields().get(0);
        assertEquals("fullName", fullNameField.getName());
        assertEquals("string", fullNameField.getType());
    }

    @Test 
    void shouldParseParametersCorrectly() throws Exception {
        Path xmlFile = Paths.get("src/test/resources/sample-xml/simple-cai-process.xml");
        ParsedMetadata result = parserService.parse(xmlFile);
        OpenAPIEndpoint endpoint = result.getOpenApiEndpoints().get(0);
        Parameter statusParam = endpoint.getParameters().get(0);

        assertEquals("status", statusParam.getName());
        assertEquals(ParameterLocation.QUERY, statusParam.getIn());
        assertEquals("string", statusParam.getType());
        
        Parameter limitParam = endpoint.getParameters().get(1);
        assertEquals("limit", limitParam.getName());
        assertEquals(ParameterLocation.QUERY, statusParam.getIn());
        assertEquals("integer", limitParam.getType());
    }

    @Test 
    void shouldHandleMissingOptionalFields() throws Exception {
        Path xmlFile = Paths.get("src/test/resources/sample-xml/simple-cai-process.xml");
        ParsedMetadata result = parserService.parse(xmlFile);

        assertNotNull(result);
        assertNotNull(result.getProcessName());
    }
}