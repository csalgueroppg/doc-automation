package com.ppg.iicsdoc.model.domain;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Transformation {
    private String id;
    private String name;
    private TransformationType type;
    private String expression;
    private String condition;
    private List<Field> inputFields;
    private List<Field> outputFields;
}
