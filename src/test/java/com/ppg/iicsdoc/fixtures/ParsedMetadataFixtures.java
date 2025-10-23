package com.ppg.iicsdoc.fixtures;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.ppg.iicsdoc.model.domain.Connection;
import com.ppg.iicsdoc.model.domain.ConnectionType;
import com.ppg.iicsdoc.model.domain.DataFlow;
import com.ppg.iicsdoc.model.domain.DataSource;
import com.ppg.iicsdoc.model.domain.DataTarget;
import com.ppg.iicsdoc.model.domain.HttpMethod;
import com.ppg.iicsdoc.model.domain.OpenAPIEndpoint;
import com.ppg.iicsdoc.model.domain.Parameter;
import com.ppg.iicsdoc.model.domain.ParsedMetadata;
import com.ppg.iicsdoc.model.domain.ProcessType;
import com.ppg.iicsdoc.model.domain.Response;
import com.ppg.iicsdoc.model.domain.Transformation;
import com.ppg.iicsdoc.model.domain.TransformationType;

public class ParsedMetadataFixtures {
    public static ParsedMetadata withConnectionsAndTransformations() {
        ParsedMetadata metadata = ParsedMetadata.builder()
                .processName("CustomerSync")
                .processType(ProcessType.CAI)
                .version("1.2.3")
                .description("Synchronizes customer data between systems")
                .build();

        Connection conn1 = Connection.builder()
                .id("conn1")
                .name("CRM_DB")
                .type(ConnectionType.DATABASE)
                .url("jdbc:mysql://crm")
                .database("CRM_DB")
                .build();

        Connection conn2 = Connection.builder()
                .id("conn2")
                .name("Salesforce")
                .type(ConnectionType.REST)
                .url("https://api.salesforce.com")
                .build();

        metadata.setConnections(List.of(conn1, conn2));

        Transformation trans1 = Transformation.builder()
                .id("trans1")
                .name("FilterActive")
                .type(TransformationType.FILTER)
                .expression("status = 'active'")
                .build();

        Transformation trans2 = Transformation.builder()
                .id("trans2")
                .name("MapFields")
                .type(TransformationType.EXPRESSION)
                .expression("UPPER(name)")
                .build();

        metadata.setTransformations(List.of(trans1, trans2));

        DataFlow flow = DataFlow.builder()
                .source(DataSource.builder().connectionRef("conn1").build())
                .target(DataTarget.builder().connectionRef("conn2").build())
                .build();

        metadata.setDataFlow(flow);

        return metadata;
    }

    public static ParsedMetadata withOpenAPIEndpoints() {
        ParsedMetadata metadata = ParsedMetadata.builder()
                .processName("CustomerSync")
                .processType(ProcessType.CAI)
                .version("1.2.3")
                .description("Synchronizes customer data between systems")
                .build();

        OpenAPIEndpoint endpoint = OpenAPIEndpoint.builder()
                .method(HttpMethod.GET)
                .path("/customers")
                .summary("Retrieve customer list")
                .operationId("getCustomers")
                .parameters(List.of(
                        Parameter.builder().name("limit").type("integer").build(),
                        Parameter.builder().name("offset").type("integer").build()))
                .responses(Map.of(
                        "200",
                        Response.builder()
                                .code("200")
                                .description("OK").build()))
                .build();

        metadata.setOpenApiEndpoints(List.of(endpoint));
        return metadata;
    }

    public static ParsedMetadata withProcessFlow() {
        ParsedMetadata metadata = ParsedMetadata.builder()
                .processName("CustomerSync")
                .processType(ProcessType.CAI)
                .version("1.2.3")
                .description("Synchronizes customer data between systems")
                .build();

        Connection conn1 = Connection.builder()
                .id("conn1")
                .name("ERP_DB")
                .type(ConnectionType.DATABASE)
                .build();

        Connection conn2 = Connection.builder()
                .id("conn2")
                .name("CRM_API")
                .type(ConnectionType.REST)
                .build();

        metadata.setConnections(List.of(conn1, conn2));
        Transformation trans1 = Transformation.builder()
            .id("trans1")
            .name("FilterValidOrders")
            .type(TransformationType.FILTER)
            .expression("status = 'active'")
            .build();

        metadata.setTransformations(List.of(trans1));
        DataFlow flow = DataFlow.builder()
                .source(DataSource.builder()
                        .connectionRef("conn1")
                        .entity("ERP_DB")
                        .build())
                .target(DataTarget.builder()
                    .connectionRef("conn2")
                    .entity("CRM_API")
                    .build())
                .transformationRefs(List.of("trans1"))
                .build();

        metadata.setDataFlow(flow);

        return metadata;
    }

    public static ParsedMetadata empty() {
        return ParsedMetadata.builder().build();
    }

    public static ParsedMetadata createSimpleMetadata() {
        return ParsedMetadata.builder()
                .processName("CustomerDataSync")
                .processType(ProcessType.CAI)
                .version("1.0.0")
                .description("Test process")
                .author("Test author")
                .created(LocalDate.now())
                .connections(List.of(
                        Connection.builder()
                                .id("conn1")
                                .name("CustomerAPI")
                                .type(ConnectionType.REST)
                                .url("https://api.example.com/customers")
                                .build(),
                        Connection.builder()
                                .id("conn2")
                                .name("CustomerDB")
                                .type(ConnectionType.DATABASE)
                                .url("jdbc:mysql://customerdb")
                                .database("CustomerDB")
                                .build()
                ))
                .transformations(List.of(
                        Transformation.builder()
                                .id("trans1")
                                .name("FormatCustomerData")
                                .type(TransformationType.EXPRESSION)
                                .expression("CONCAT(firstName, ' ', lastName)")
                                .build()
                ))
                .openApiEndpoints(List.of(
                        OpenAPIEndpoint.builder()
                                .path("/customers")
                                .method(HttpMethod.GET)
                                .operationId("getCustomers")
                                .summary("Get customers")
                                .build()
                ))
                .dataFlow(DataFlow.builder()
                        .source(DataSource.builder()
                                .connectionRef("conn1")
                                .entity("Customer")
                                .build())
                        .target(DataTarget.builder()
                                .connectionRef("conn2")
                                .entity("CustomerTable")
                                .build())
                        .build())
                .build();
    }
}
