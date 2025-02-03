package com.example.springiapromptdemo.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class GraphDatasetElement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String pointA;
    private String pointB;
    private Float distance;

    private String grapheName;

    public String toString(){
        return "  + Le noeud "+pointA+"est lié au noeud "+pointB+"la distance entre eux est de "+distance+".";
    }
}
