package com.example.springiapromptdemo.entities;

import lombok.Data;

import java.util.List;

@Data
public class FinalResult {
	
   String graph_name;
   String ExpectedShortestPath;
   String shortestPath;
   List<String> hallucinationPaths;
   Double expectedTotalDistance;
   Double totalDistance;
   Double score;
}
