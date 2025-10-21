package com.ppg.iicsdoc.model.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Response {
   private String code;
   private String description;
   private String schemaType;
   private String schemaItems; 
}
