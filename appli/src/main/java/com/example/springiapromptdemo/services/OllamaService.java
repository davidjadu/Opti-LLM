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
    public String callOllama(String promptString, List<GraphDatasetElement> graph){

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
     //   saveResponse(response,dataSet,savedPrompts);

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
        Tu effectuera la tache demandé en tenant compte uniquement des informations fournis ci-dessous:
        """);
        graph.forEach(graphDatasetElement -> {
            contextText.append(graphDatasetElement.toString());
        });

        String finalSystemPrompt= prompt.concat(contextText.toString());
        return finalSystemPrompt;
    }

    /**
     * Cette methode permet de sauvegarder la reponse de Ollama
     * @param response
     * @param dataSet
     * @param savedPrompts
     */
    //Todo: sauvegarder la réponse dans un fichier csv .
    private void saveResponse(String response, DataSet dataSet, LLMPrompt savedPrompts) {
        Double distance = getDataFromResponse(response);

        LLMResponse llmResponse = new LLMResponse();
        llmResponse.setDataSet(dataSet);
        llmResponse.setSystemPromt(savedPrompts.getSystemPrompt());
        llmResponse.setUserData(savedPrompts.getUserData());
        llmResponse.setProvidedDistance(distance);
      //  llmResponse.setExpectedDistance(getExpectedDistance(dataSet, savedPrompts));
        llmResponse.setScore(llmResponse.getExpectedDistance() - llmResponse.getProvidedDistance());
        llmResponse.setExecutionDate(new Date());
    }

    /**
     * Cette methode permet de calculer la distance attendue
     * @param dataSet
     * @param savedPrompts
     * @return
     */
    //Todo: changer cette methode pour lire la réponse du fichier response.csv
    private Double getExpectedDistance(DataSet dataSet, LLMPrompt savedPrompts) {
     //   PathResult shortestPath = graphService.findShortestPath(dataSet, savedPrompts.getStart().toString(), savedPrompts.getEnd().toString());
        return null;
    }
    

    /**
     * Cette methode permet de recuperer la distance fournie par Ollama
     * @param response
     * @return
     */
    private Double getDataFromResponse(String response) {
        String jsonPath="$.total_distance";
     // List<String>  pathresult= JsonPath.read(response, "$.shortest_path");

        Double val = 0.0;
        try {
             val = JsonPath.read(response, jsonPath);
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
        return val;
    }
}
