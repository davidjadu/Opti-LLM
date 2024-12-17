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
    @ManyToOne
    private LLMPrompt prompt;
    private Float score;
    @Temporal(TemporalType.TIMESTAMP)
    private Date executionDate;
}
