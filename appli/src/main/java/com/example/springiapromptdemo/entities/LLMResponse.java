package com.example.springiapromptdemo.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
@Builder

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LLMResponse {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String systemPromt;
    private DataSet dataSet;
    private String graph_name;
    @ElementCollection
    private List<String> shortestPath;
    private Double expectedDistance;
    private Double providedDistance;
    private Double score;
    @Temporal(TemporalType.TIMESTAMP)
    private Date executionDate;
}
