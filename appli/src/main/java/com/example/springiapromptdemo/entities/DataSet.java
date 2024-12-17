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
    @JsonIgnore
    @OneToMany(mappedBy = "dataSet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<GraphDatasetElement> graphDatasetElementList;
    @OneToMany(mappedBy = "dataSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PathResult> pathResults; // Liste des résultats associés
}
