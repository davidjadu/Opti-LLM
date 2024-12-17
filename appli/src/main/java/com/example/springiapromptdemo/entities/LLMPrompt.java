package com.example.springiapromptdemo.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.context.annotation.Lazy;

import java.util.Date;
import java.util.List;

@Entity
@Data
public class LLMPrompt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length=1500)
    private String systemPrompt;
    private String userData;
    @OneToMany
    @Lazy
    private List<LLMResponse> llmResponseList;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;

}
