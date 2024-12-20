package com.example.springiapromptdemo.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class LLMResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String systemPromt;
    private String userData;
    @ManyToOne(cascade = CascadeType.REFRESH)
    private DataSet dataSet;
    private Double expectedDistance;
    private Double providedDistance;
    private Double score;
    @Temporal(TemporalType.TIMESTAMP)
    private Date executionDate;
}
