package com.example.springiapromptdemo.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
public class PathResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    private List<String> shortestPath; // Liste des nœuds dans l'ordre du chemin

    private double totalDistance; // Distance totale

    private String graph_name;
}
