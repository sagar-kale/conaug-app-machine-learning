package com.conaug.conaugApp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.Map;

@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Response {
    private String rating;
    private String status;
    private Integer statusCode;
    double score;
    Map<String, Double> rating_probability;
}
