package com.example.springiapromptdemo.entities;

import lombok.Data;

@Data
public class LLMResponse {
	private String shortestPath;
    private double totalDistance;
}
