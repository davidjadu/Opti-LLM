package com.example.springiapromptdemo.entities;

import lombok.Data;

@Data
public class FinalResult {
	
   String graph_name;
   String ExpectedShortestPath;
   String shortestPath;
   double expectedTotalDistance;
   double totalDistance;
   double score;
}
