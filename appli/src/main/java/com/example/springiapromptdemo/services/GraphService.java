package com.example.springiapromptdemo.services;

import com.example.springiapromptdemo.entities.DataSet;
import com.example.springiapromptdemo.entities.GraphDatasetElement;
import com.example.springiapromptdemo.entities.PathResult;
import com.example.springiapromptdemo.repositories.DataSetRepository;
import com.example.springiapromptdemo.repositories.PathResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class GraphService {

    private final DataSetRepository dataSetRepository;
    private final PathResultRepository pathResultRepository;

    public GraphService(DataSetRepository dataSetRepository, PathResultRepository pathResultRepository) {
        this.dataSetRepository = dataSetRepository;
        this.pathResultRepository = pathResultRepository;
    }

    @Transactional
    public PathResult findShortestPath(DataSet dataSet, String start, String end) {
        // Étape 1: Vérifier si le chemin existe déjà
        Optional<PathResult> existingPath = pathResultRepository.findByDataSetAndPath(dataSet.getId(), start, end);
        if (existingPath.isPresent()) {
            return existingPath.get(); // Retourner le chemin existant
        }

        // Étape 2: Construire une représentation en mémoire du graphe
        Map<String, Map<String, Float>> graph = buildGraph(dataSet.getGraphDatasetElementList());

        // Étape 3: Appliquer l'algorithme de Dijkstra
        PathResult newPathResult = dijkstra(graph, start, end);

        // Étape 4: Associer le DataSet au nouveau PathResult
        newPathResult.setDataSet(dataSet);

        // Étape 5: Sauvegarder le résultat
        return pathResultRepository.save(newPathResult);
    }

    private Map<String, Map<String, Float>> buildGraph(List<GraphDatasetElement> elements) {
        Map<String, Map<String, Float>> graph = new HashMap<>();
        for (GraphDatasetElement element : elements) {
            String pointA = element.getPointA();
            String pointB = element.getPointB();
            Float distance = element.getDistance();

            // Log pour vérifier les valeurs

            graph.putIfAbsent(pointA, new HashMap<>());
            graph.get(pointA).put(pointB, distance);

            graph.putIfAbsent(pointB, new HashMap<>());
            graph.get(pointB).put(pointA, distance);
        }


        return graph;
    }

   /* private PathResult dijkstra(Map<String, Map<String, Float>> graph, String start, String end) {
        if (!graph.containsKey(start) || !graph.containsKey(end)) {
            throw new IllegalArgumentException("Points de départ ou de destination inexistants dans le graphe.");
        }
        Map<String, Float> distances = new HashMap<>();
        Map<String, String> previousNodes = new HashMap<>();
        PriorityQueue<String> priorityQueue = new PriorityQueue<>(Comparator.comparing(distances::get));
        Set<String> visited = new HashSet<>();

        // Initialisation
        for (String node : graph.keySet()) {
            distances.put(node, Float.MAX_VALUE);
        }
        distances.putIfAbsent(start, Float.MAX_VALUE);
        priorityQueue.add(start);

        // Algorithme principal
        while (!priorityQueue.isEmpty()) {
            String currentNode = priorityQueue.poll();
            if (visited.contains(currentNode)) continue;
            visited.add(currentNode);

            if (currentNode.equals(end)) break;

            Map<String, Float> neighbors = graph.getOrDefault(currentNode, new HashMap<>());
            for (Map.Entry<String, Float> neighbor : neighbors.entrySet()) {
                String neighborNode = neighbor.getKey();
                float newDist = distances.get(currentNode) + neighbor.getValue();

                if (newDist < distances.get(neighborNode)) {
                    distances.put(neighborNode, newDist);
                    previousNodes.put(neighborNode, currentNode);
                    priorityQueue.add(neighborNode);
                }
            }
        }

        // Reconstruction du chemin
        List<String> path = new ArrayList<>();
        String current = end;
        while (current != null) {
            path.add(0, current);
            current = previousNodes.get(current);
        }

        PathResult result = new PathResult();
        result.setShortestPath(path);
        result.setTotalDistance(distances.get(end));
        return result;
    }*/
   private PathResult dijkstra(Map<String, Map<String, Float>> graph, String start, String end) {
       if (!graph.containsKey(start) || !graph.containsKey(end)) {
           throw new IllegalArgumentException("Points de départ ou de destination inexistants dans le graphe.");
       }

       // Initialisation des structures de données
       Map<String, Double> distances = new HashMap<>();
       Map<String, String> previousNodes = new HashMap<>();
       PriorityQueue<String> priorityQueue = new PriorityQueue<>(Comparator.comparing(distances::get));
       Set<String> visited = new HashSet<>();

       // Initialisation des distances à l'infini
       for (String node : graph.keySet()) {
           distances.put(node, Double.MAX_VALUE);
       }

       // Initialiser la distance du nœud de départ
       distances.put(start, 0.0);
       priorityQueue.add(start);

       // Algorithme principal
       while (!priorityQueue.isEmpty()) {
           String currentNode = priorityQueue.poll();

           // Ignorer les nœuds déjà visités
           if (visited.contains(currentNode)) continue;
           visited.add(currentNode);

           // Si on atteint le nœud final, on peut arrêter l'exploration
           if (currentNode.equals(end)) break;

           Map<String, Float> neighbors = graph.getOrDefault(currentNode, new HashMap<>());

           for (Map.Entry<String, Float> neighbor : neighbors.entrySet()) {
               String neighborNode = neighbor.getKey();
               Float edgeDistance = neighbor.getValue();

               // Vérifier les distances nulles ou invalides
               if (edgeDistance == null || edgeDistance < 0) continue;

               Double newDist = distances.get(currentNode) + edgeDistance;

               // Relaxation des distances
               if (newDist < distances.get(neighborNode)) {
                   distances.put(neighborNode, newDist);
                   previousNodes.put(neighborNode, currentNode);
                   priorityQueue.add(neighborNode);
               }
           }
       }

       // Reconstruction du chemin le plus court
       List<String> path = new ArrayList<>();
       String current = end;

       while (current != null) {
           path.add(0, current);
           current = previousNodes.get(current);
       }

       // Si le chemin reconstruit ne contient que le nœud final, aucun chemin valide n'a été trouvé
       if (path.size() == 1 && !path.contains(start)) {
           path.clear(); // Aucun chemin valide
       }

       // Préparer le résultat final
       PathResult result = new PathResult();
       result.setShortestPath(path);
       result.setTotalDistance(path.isEmpty() ? Double.MAX_VALUE : distances.getOrDefault(end, Double.MAX_VALUE));
       return result;
   }

    @Transactional
    public PathResult saveShortestPath(Long dataSetId, String start, String end) {
        if (dataSetId == null || start == null || end == null) {
            throw new RuntimeException("Paramètres invalides");
        }

        // Récupérer le DataSet par ID
        DataSet dataSet = dataSetRepository.findById(dataSetId).orElseThrow(() -> new RuntimeException("DataSet introuvable"));

        // Calculer le chemin le plus court
        PathResult pathResult = findShortestPath(dataSet, start, end);

        // Associer le résultat au DataSet
        pathResult.setDataSet(dataSet);

        // Sauvegarder le résultat
        return pathResultRepository.save(pathResult);
    }

}
