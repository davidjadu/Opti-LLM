package com.example.springiapromptdemo.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
    @ManyToOne
    @JsonIgnore
    private DataSet dataSet;

    public String toString(){
        return "  + Le noeud "+pointA+"est lié au noeud "+pointB+"la distance entre eux est de "+distance+".";
    }
}
