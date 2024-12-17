package com.example.springiapromptdemo.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.context.annotation.Lazy;

import java.util.List;

@Data
@Entity
public class DataSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @OneToMany
    @Lazy
    @JsonIgnore
    List<GraphDatasetElement> graphDatasetElementList;
}
