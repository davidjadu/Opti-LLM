package com.example.springiapromptdemo.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.example.springiapromptdemo.entities.GraphDatasetElement;
import com.example.springiapromptdemo.entities.PathResult;

public final class OllamaUtils {
	
    // Expression régulière pour détecter les crochets et remplacer les virgules à l'intérieur
    private static final Pattern BRACKET_PATTERN = Pattern.compile("\\[(.*?)\\]");
	
	private  OllamaUtils() {
		throw new AssertionError("Cannot instantiate the Utils class");
	}
	
    public static List<PathResult> readMetadata(){
    	
        List<PathResult> pathResults = new ArrayList<>();
        
        try (InputStream inputStream = OllamaUtils.class.getClassLoader().getResourceAsStream("data/metadata.csv");
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

        InputStream inputStream = OllamaUtils.class.getClassLoader().getResourceAsStream("data/graphs/".concat(fileName).concat(".csv"));
        
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
                graphDatasetElement.setDistance(Float.valueOf(distance));
                graphDatasetElementList.add(graphDatasetElement);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return graphDatasetElementList;
    }
}
