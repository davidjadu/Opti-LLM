package com.example.springiapromptdemo.web;

import com.example.springiapromptdemo.entities.DataSet;
import com.example.springiapromptdemo.entities.PathResult;
import com.example.springiapromptdemo.repositories.DataSetRepository;
import com.example.springiapromptdemo.services.GraphService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/graph")
public class GraphController {

    private final GraphService graphService;
    private final DataSetRepository dataSetRepository;

    public GraphController(GraphService graphService, DataSetRepository dataSetRepository) {
        this.graphService = graphService;
        this.dataSetRepository = dataSetRepository;
    }

    @PostMapping("/save-shortest-path")
    public ResponseEntity<?> saveShortestPath(
            @RequestParam Long dataSetId,
            @RequestParam String start,
            @RequestParam String end) {
        try {
            PathResult pathResult = graphService.saveShortestPath(dataSetId, start, end);
            return ResponseEntity.ok(pathResult);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erreur de validation des paramètres : " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur : " + ex.getMessage());
        }
    }

    @GetMapping("/path-results")
    public ResponseEntity<List<PathResult>> getPathResults(@RequestParam Long dataSetId) {
        try {
            DataSet dataSet = dataSetRepository.findById(dataSetId)
                    .orElseThrow(() -> new RuntimeException("DataSet introuvable"));
            return ResponseEntity.ok(dataSet.getPathResults());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Null ou message personnalisé
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
