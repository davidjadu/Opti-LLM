package com.example.springiapromptdemo.entities;

public record FinalResult (String graph_name,
						   String shortestPath, 
						   double totalDistance,
						   double expectedTotalDistance,
						   double score) {}
