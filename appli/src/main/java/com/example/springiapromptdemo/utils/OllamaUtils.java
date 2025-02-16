package com.example.springiapromptdemo.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import com.example.springiapromptdemo.entities.FinalResult;
import com.example.springiapromptdemo.entities.GraphDatasetElement;
import com.example.springiapromptdemo.entities.PathResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public final class OllamaUtils {
	
    // Expression régulière pour détecter les crochets et remplacer les virgules à l'intérieur
    private static final Pattern BRACKET_PATTERN = Pattern.compile("\\[(.*?)\\]");

    private static String OUTPUT_DIRECTORY_PATH;
    private static String GRAPHS_INPUT_DIRECTORY_PATH;
    private static String INPUT_FILE_PATH;

    @Autowired
    private Environment env;

	private  OllamaUtils() { }

    @PostConstruct
    private void init() {
        OUTPUT_DIRECTORY_PATH = env.getProperty("output.file.directory");
        GRAPHS_INPUT_DIRECTORY_PATH = env.getProperty("input.graph.directory");
        INPUT_FILE_PATH = env.getProperty("input.file.path");

        log.info("=====> Output directory path: {}", OUTPUT_DIRECTORY_PATH);
        log.info("=====> Graph directory path: {}", GRAPHS_INPUT_DIRECTORY_PATH);
        log.info("=====> Metadata file path: {}", INPUT_FILE_PATH);
    }
	
    public static List<PathResult> readMetadata(){
    	
        List<PathResult> pathResults = new ArrayList<>();
        
        try (InputStream inputStream = OllamaUtils.class.getClassLoader().getResourceAsStream(INPUT_FILE_PATH);
        	BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
        	
        	// Lire tout le contenu du fichier et modifier les virgules dans les crochets
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                content.append(replaceCommasInsideBrackets(line)).append("\n");
            }

            // Maintenant, nous pouvons traiter le fichier modifié
            BufferedReader modifiedReader = new BufferedReader(new StringReader(content.toString()));


            CSVFormat csvFormat = CSVFormat.newFormat(',').builder()
            		.setHeader()
                    .setSkipHeaderRecord(true)
                    .build();

            Iterable<CSVRecord> records = csvFormat.parse(modifiedReader);
            
            for (CSVRecord record : records) {
                String graphId = record.get(0).replace("\"", "");
                
                // Si c'est un header ignorer la ligne
                if(graphId.equals("graph_id")) {
                	continue;
                }  
                
                String digistrapahString = record.get(4);
                String distance = record.get(5);
                String nbNode = record.get(1);

                digistrapahString = digistrapahString.substring(1, digistrapahString.length());
                String[] trim = digistrapahString.split(",");
                
                //convert digistrapahString to List of Strings
                List<String> digistrapah = new ArrayList<>();
                for (String s:trim) {
                   digistrapah.add(s);
                }

                PathResult pathResult = new PathResult();   
                pathResult.setGraph_name("graph_"+graphId);
                pathResult.setShortestPath(digistrapah);
                if(!"-".equals(distance)) {
                	pathResult.setTotalDistance(Double.parseDouble(distance));
                }
                pathResult.setNbNodes(Integer.parseInt(nbNode));
               pathResults.add(pathResult);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pathResults;
    }
    
    
    private static String replaceCommasInsideBrackets(String input) {
        // Vérifie si la chaîne contient des crochets et si elle est valide
        if (input == null || !input.contains("[")) {
            return input;
        }

        // Utilisation de la méthode Matcher pour trouver les parties avec des crochets
        Matcher matcher = BRACKET_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            // Remplacer les virgules par des tirets uniquement dans les crochets
            String modifiedContent = matcher.group(1).replace(",", "-");
            matcher.appendReplacement(result, "[" + modifiedContent + "]");
        }

        // Ajoute le reste de la chaîne après la dernière correspondance
        matcher.appendTail(result);
        return result.toString();
    }

    public static List<GraphDatasetElement> loadDataSet(String fileName) throws FileNotFoundException{
        List<GraphDatasetElement> graphDatasetElementList = new ArrayList<>();

        InputStream inputStream = OllamaUtils.class.getClassLoader().getResourceAsStream(GRAPHS_INPUT_DIRECTORY_PATH.concat(fileName).concat(".csv"));
        
        if (Objects.isNull(inputStream)) {
            throw new FileNotFoundException(String.format("Le fichier %s est introuvable.", fileName.concat(".csv")));
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

            CSVFormat csvFormat = CSVFormat.newFormat(',').builder()
                    .setSkipHeaderRecord(true)
                    .build();

            Iterable<CSVRecord> records = csvFormat.parse(br);
            for (CSVRecord record : records) {
                String pointA = record.get(0);
                if(pointA.equals("Node1")) {continue;}
                String pointB = record.get(1);
                String distance = record.get(2);
                GraphDatasetElement graphDatasetElement=new GraphDatasetElement();
                graphDatasetElement.setPointA(pointA);
                graphDatasetElement.setPointB(pointB);
                graphDatasetElement.setDistance(Double.valueOf(distance));
                graphDatasetElementList.add(graphDatasetElement);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return graphDatasetElementList;
    }
    
    
    public static void saveResponse(List<FinalResult> resp){

        String CSV_OUTPUT_FILE_PATH = OUTPUT_DIRECTORY_PATH + "finalResult.csv";

        File directory = new File(OUTPUT_DIRECTORY_PATH);
        if (!directory.exists()) {
            directory.mkdirs(); 
        }
        
    	 // Création d'un format CSV avec en-têtes
        CSVFormat csvFormat = CSVFormat.Builder.create()
                .setHeader("graph_id", "shortestPath", "hallucinations", "totalDistance", "score") // Définition des en-têtes
                .setSkipHeaderRecord(false) // Ne pas ignorer l'en-tête
                .build();
        
        try (BufferedWriter writer = new BufferedWriter (new FileWriter(CSV_OUTPUT_FILE_PATH));
             CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {

               // Écriture des données
        	   for(FinalResult finalResult : resp) {
        		   csvPrinter.printRecord(finalResult.getGraph_name(), 
        				   				  finalResult.getShortestPath(),
                                          finalResult.getHallucinationPaths(),
        				   				  finalResult.getTotalDistance(), 
        				   				  finalResult.getScore());
        	   }
        	   
               csvPrinter.flush(); // Force l'écriture des données dans le fichier

           } catch (IOException e) {
               e.printStackTrace();
           }
       
    }

    public static Pair<Boolean, List<String>> isHallucination(String path, List<GraphDatasetElement> graphDatasetElements) {
        List<String> faultPaths = new ArrayList<>();
        // Stocker les connexions dans un Set pour une recherche rapide
        Set<String> edges = new HashSet<>();
        for (GraphDatasetElement element : graphDatasetElements) {
            edges.add(element.getPointA() + "->" + element.getPointB());
        }

        // Vérifier chaque transition dans le chemin
        String[] nodes = (path != null && !path.isBlank())
                ? path.trim().split("\\s*->\\s*") : new String[0];
        for (int i = 1; i < nodes.length; i++) {
            if (!edges.contains(nodes[i - 1] + "->" + nodes[i])) {
                faultPaths.add(nodes[i - 1] + "->" + nodes[i]); // Si une connexion est absente, ce n'est pas valide
            }
        }
        return faultPaths.isEmpty() ?  Pair.of(false,faultPaths) : Pair.of(true,faultPaths);
    }

    public static Double calculTotalDistance(List<GraphDatasetElement> graphDatasetElements, String path) {
        // Stocker les distances dans une Map pour une recherche rapide
        Map<String, Double> distances = new HashMap<>();
        for (GraphDatasetElement g : graphDatasetElements) {
            distances.put(g.getPointA() + "->" + g.getPointB(), g.getDistance());
        }

        // Calcul de la distance totale
        String[] nodes = (path != null && !path.isBlank())
                ? path.trim().split("\\s*->\\s*") : new String[0];
        double totalDistance = 0;

        for (int i = 1; i < nodes.length; i++) {
            String edgeKey = nodes[i - 1] + "->" + nodes[i];
            totalDistance += distances.getOrDefault(edgeKey, 0.0); // Ajoute 0 si l'arête n'existe pas
        }

        return totalDistance;
    }

    public static boolean isExpectedPath(String path, String expectedPath){
        if(path == null || expectedPath == null) {
            return false;
        }
        expectedPath = formatPath(expectedPath);
        return path.trim().equals(expectedPath);
    }

    private static String formatPath(String rawPath) {
        return rawPath.replaceAll("[\\[\\]\"]", "")  // Supprime les crochets et guillemets
                .replaceAll("\\s*-\\s*", " -> ") // Remplace les tirets entourés d'espaces par " -> "
                .trim();
    }

}
