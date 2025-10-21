package com.ppg.iicsdoc.model.domain;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OpenAPIEndpoint {
    private String path;
    private HttpMethod method;
    private String operationId;
    private String summary;
    private String description;
    private List<Parameter> parameters;
    private Map<String, Response> responses;
    private List<String> tags;
}