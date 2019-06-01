package com.conaug.conaugApp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.List;
import java.util.Map;

@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Response {
    private String status;
    private Integer statusCode;
    double score;
    private String rating;
    private String organization;
    Map<String, Double> rating_probability;
}
