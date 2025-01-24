package com.example.springiapromptdemo.services;

import com.example.springiapromptdemo.entities.*;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OllamaService {

    private final OllamaChatModel ollamaChatModel;




    public OllamaService(OllamaChatModel ollamaChatModel)
    {
        this.ollamaChatModel = ollamaChatModel;
    }

    /**
     * Cette methode permet d'appeler Ollama
     * @param promptString
     * @param graph
     * @return
     */
    public String callOllama(String promptString, List<GraphDatasetElement> graph ,PathResult pathResult){

        log.trace("Augmentation du prompt systeme...");
        String finalSystemPrompt = augmentSystemPrompt(graph, promptString);

        log.trace("Construction des prompts systeme et utilisateur...");

        /*
        PromptTemplate promptTemplate = new PromptTemplate(savedPrompts.getUserData());
        Message message = promptTemplate.createMessage(Map.of("request", promptTemplate));
*/
        Message systemMessage = new SystemMessage(finalSystemPrompt);

        log.trace("Contruction du prompt principal...");
        Prompt prompt = new Prompt(List.of(systemMessage));

        log.trace("Appel de Ollama...");
        ChatResponse chatResponse = ollamaChatModel.call(prompt);
        String response = chatResponse.getResult().getOutput().getContent();

        log.trace("Sauvegarde de la reponse...");
        saveResponse(response, pathResult, prompt);

        return response;
    }

    /**
     * Cette methode permet d'ajouter des informations contextuelles au prompt systeme
     *  - Les informations contextuelles sont les elements du dataset
     *  - ET des indications sur la tache à effectuer
     * @param graph
     * @param prompt
     * @return
     */
    private String augmentSystemPrompt(List<GraphDatasetElement> graph, String prompt) {
        StringBuffer contextText=new StringBuffer();
        contextText.append("""
        You will carry out the requested task taking into account only the information provided below:
        You are a system designed to calculate the shortest path and total distance between two nodes in a graph. The graph is represented as a series of connections between nodes, where each connection has a specific distance.\s
        """);
        graph.forEach(graphDatasetElement -> {
            contextText.append(graphDatasetElement.toString());
        });
        contextText.append("""
                 Task:
                 Find the shortest path and the total distance between the starting node "A" and the destination node "E". Provide your response in the following JSON format:
          """);

        String finalSystemPrompt= prompt.concat(contextText.toString());
        return finalSystemPrompt;
    }

    /**
     * Cette methode permet de sauvegarder la reponse de Ollama
     * @param response
     * @param resultPath
     * @param savedPrompts
     */
    //sauvegarder la réponse dans un fichier csv .
    private void saveResponse(String response, PathResult resultPath, Prompt savedPrompts) {

        PathResult pathResultLLM=getDataFromResponse(response);
        Double expectedDistance = resultPath.getTotalDistance();
        Double providedDistance = pathResultLLM.getTotalDistance();
        double scorePercentage = 0.0;
        if (expectedDistance != null && expectedDistance > 0) {
            double difference = Math.abs(pathResultLLM.getTotalDistance() - expectedDistance);
            scorePercentage = (1 - (difference / expectedDistance)) * 100;
        }

        LLMResponse llmResponse =  LLMResponse.builder()
                .providedDistance(providedDistance)
                .expectedDistance(expectedDistance)
                .graph_name(resultPath.getGraph_name())
                .shortestPath(pathResultLLM.getShortestPath())
                .score(scorePercentage)
                .systemPromt(savedPrompts.toString())
                .executionDate(new Date()).build();

        saveResponseToCSV(llmResponse);

    }
    private void saveResponseToCSV(LLMResponse llmResponse) {
        boolean isFileNew = !new File("ollama_responses.csv").exists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("ollama_responses.csv", true))) {
            if (isFileNew) {
                writer.write("GraphName,Prompt,ProvidedDistance,ShortestPath,Score,ExecutionDate\n");
            }
            StringBuilder sb = new StringBuilder();
            sb.append(llmResponse.getGraph_name()).append(",");
            sb.append(llmResponse.getSystemPromt()).append(",");
            sb.append(llmResponse.getProvidedDistance()).append(",");
            sb.append(llmResponse.getShortestPath().toString()).append(",");
            sb.append(llmResponse.getScore()).append(",");
            sb.append(llmResponse.getExecutionDate()).append("\n");

            writer.write(sb.toString());
            log.info("Réponse sauvegardée dans le fichier CSV.");
        } catch (IOException e) {
            log.error("Erreur lors de la sauvegarde dans le fichier CSV : ", e);
        }
    }


    /**
     * Cette methode permet de recuperer la distance fournie par Ollama
     * @param response
     * @return
     */
    private PathResult getDataFromResponse(String response) {

        String jsonPathDistance = "$.total_distance";
        String jsonPathPath = "$.shortest_path";
        Double val = 0.0;
        List<String> cheminParcouru = new ArrayList<>();
        Double distanceTotale = 0.0;
        try {
            // Extraction de la distance totale
            distanceTotale = JsonPath.read(response, jsonPathDistance);

            // Extraction du chemin parcouru
            cheminParcouru = JsonPath.read(response, jsonPathPath);
        } catch (Exception e) {
            log.error("""
        *********************************************************
        ERROR Invalid response from Ollama ....
        *********************************************************
        """);
            log.error(response);
            log.error("""
        *********************************************************
        """);
        }
        return  PathResult.builder().totalDistance(distanceTotale).shortestPath(cheminParcouru).build();
    }
}
