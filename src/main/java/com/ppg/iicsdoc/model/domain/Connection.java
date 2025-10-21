package com.ppg.iicsdoc.model.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Connection {
    private String id;
    private String name;
    private ConnectionType type;
    private String url;
    private String host;
    private String database;
    private AuthenticationType authenticationType;
}
