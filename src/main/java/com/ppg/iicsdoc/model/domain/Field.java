package com.ppg.iicsdoc.model.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Field {
    private String name;
    private String type;
    private String description;
    private boolean required;
}
