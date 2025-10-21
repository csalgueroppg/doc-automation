package com.ppg.iicsdoc.model.domain;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataFlow {
    private DataSource source;
    private List<String> transformationRefs;
    private DataTarget target;
}
