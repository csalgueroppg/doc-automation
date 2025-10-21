package com.ppg.iicsdoc.model.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataSource {
    private String connectionRef;
    private String entity;
}
