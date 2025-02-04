package com.example.springiapromptdemo.entities;

import java.util.List;

import lombok.Data;

@Data
public class PathResult {
    private List<String> shortestPath; // Liste des nœuds dans l'ordre du chemin
    private double totalDistance; // Distance totale
    private String graph_name;
    int nbNodes;
}
