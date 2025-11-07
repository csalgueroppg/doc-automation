package com.ppg.iicsdoc.model.domain;

import java.util.List;
import java.util.function.Supplier;

import lombok.Getter;

@Getter
public class LazyParsedMetadata {
    
    private final String processName;
    private final ProcessType processType;

    private Supplier<List<Connection>> connectionSupplier;
    private List<Connection> connections;

    private Supplier<List<Transformation>> transformationsSupplier;
    private List<Transformation> transformations;

    private Supplier<List<OpenAPIEndpoint>> openApiEndpointsSupplier;
    private List<OpenAPIEndpoint> openApiEndpoints;

    public LazyParsedMetadata(String processName, ProcessType processType) {
        this.processName = processName;
        this.processType = processType;
    }

    // Accessors
    public List<Connection> getConnections() {
        if (connections == null && connectionSupplier != null) {
            connections = connectionSupplier.get();
        }

        return connections;
    }

    public List<Transformation> getTransformations() {
        if (transformations == null && transformationsSupplier != null) {
            transformations = transformationsSupplier.get();
        }

        return transformations;
    }

    public List<OpenAPIEndpoint> getOpenApiEndpoints() {
        if (openApiEndpoints == null && openApiEndpointsSupplier != null) {
            openApiEndpoints = openApiEndpointsSupplier.get();
        }

        return openApiEndpoints;
    }

    // Modifiers
    public void setConnectionSupplier(Supplier<List<Connection>> supplier) {
        this.connectionSupplier = supplier;
    }

    public void setTransformationSupplier(Supplier<List<Transformation>> supplier) {
        this.transformationsSupplier = supplier;
    }

    public void setOpenApiEndpointsSupllier(Supplier<List<OpenAPIEndpoint>> supplier) {
        this.openApiEndpointsSupplier = supplier;
    }
}
