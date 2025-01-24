package com.example.springiapromptdemo.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.context.annotation.Lazy;

import java.util.Date;
import java.util.List;


@Data
public class LLMPrompt {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String systemPrompt;

    private String userData;
//    @OneToMany
    @Lazy
    private List<LLMResponse> llmResponseList;
    private Integer start;
    private Integer end;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;

}
