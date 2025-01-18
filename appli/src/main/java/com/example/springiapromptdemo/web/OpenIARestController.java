package com.example.springiapromptdemo.web;

import com.example.springiapromptdemo.entities.DataSet;
import com.example.springiapromptdemo.entities.GraphDatasetElement;
import com.example.springiapromptdemo.entities.PathResult;
import com.example.springiapromptdemo.services.OllamaService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ollama")
public class OpenIARestController {

    @Autowired
    OllamaService ollamaService;

    @PostMapping
    public void callOllama(@RequestBody String prompt){
        List<PathResult> pathResults = readMetadata();
        pathResults.stream().forEach(
                pathResult -> {
                    List<GraphDatasetElement> graph = loadDataSet(pathResult.getGraph_name());
                    ollamaService.callOllama(prompt, graph);
                }
        );

    }

    //Todo: deplacer dans le service
    private List<PathResult> readMetadata(){
        List<PathResult> pathResults = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("metadata.csv"))) {


            CSVFormat csvFormat = CSVFormat.newFormat(',').builder()
                    .setSkipHeaderRecord(true)
                    .build();

            Iterable<CSVRecord> records = csvFormat.parse(br);
            for (CSVRecord record : records) {
                String graphId = record.get(0);
                if(graphId.equals("graph_id")) {continue;}  // Si c'est un header ignorer la ligne
                String digistrapahString = record.get(4);
                String distance = record.get(1);

                digistrapahString=digistrapahString.substring(1, digistrapahString.length());
               String[] trim=digistrapahString.split(",");
               //convert digistrapahString to List of Strings
               List<String> digistrapah = new ArrayList<>();
               for (String s:trim) {
                   digistrapah.add(s);
               }

                PathResult pathResult = new PathResult();
                pathResult.setGraph_name("graph_"+graphId);
                pathResult.setShortestPath(digistrapah);

               pathResults.add(pathResult);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pathResults;
    }


    public List<GraphDatasetElement> loadDataSet(String fileName){
       File  file= new File(fileName);
        List<GraphDatasetElement> graphDatasetElementList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()))) {


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
