package com.example.springiapromptdemo.services;

import com.example.springiapromptdemo.entities.DataSet;
import com.example.springiapromptdemo.entities.LLMPrompt;
import com.example.springiapromptdemo.entities.LLMResponse;
import com.example.springiapromptdemo.entities.PathResult;
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

    @Autowired
    OllamaChatModel ollamaChatModel;

    @Autowired PromptService promptService;

    @Autowired DataSetService dataSetService;

    @Autowired
    LLMResponseService llmResponseService;

    @Autowired
    GraphService graphService;


    /**
     * Cette methode permet d'appeler Ollama
     * @param promptId
     * @param datasetId
     * @return
     */
    public String callOllama(Long promptId,Long datasetId){

        log.trace("Recuperation des prompts et du dataset...");
        LLMPrompt savedPrompts = promptService.getOnePrompt(promptId);
        DataSet dataSet = dataSetService.getOneDataset(datasetId);

        log.trace("Augmentation du prompt systeme...");
        String finalSystemPrompt = augmentSystemPrompt(datasetId, savedPrompts);

        if (savedPrompts.getUserData() == null || finalSystemPrompt == null) {
            throw new IllegalArgumentException("Data for prompts cannot be null");
        }
        log.trace("Construction des prompts systeme et utilisateur...");

        PromptTemplate promptTemplate = new PromptTemplate(savedPrompts.getUserData());
        Message message = promptTemplate.createMessage(Map.of("request", promptTemplate));

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(finalSystemPrompt);
        Message systemMessage = new SystemMessage(finalSystemPrompt);

        log.trace("Contruction du prompt principal...");
        Prompt prompt = new Prompt(List.of(message, systemMessage));

        log.trace("Appel de Ollama...");
        ChatResponse chatResponse = ollamaChatModel.call(prompt);
        String response = chatResponse.getResult().getOutput().getContent();

        log.trace("Sauvegarde de la reponse...");
        saveResponse(response,dataSet,savedPrompts);

        return response;
    }

    /**
     * Cette methode permet d'ajouter des informations contextuelles au prompt systeme
     *  - Les informations contextuelles sont les elements du dataset
     *  - ET des indications sur la tache à effectuer
     * @param datasetId
     * @param savedPrompts
     * @return
     */
    private String augmentSystemPrompt(Long datasetId, LLMPrompt savedPrompts) {
        StringBuffer contextText=new StringBuffer();
        contextText.append("""
        Tu effectuera la tache demandé en tenant compte uniquement des informations fournis ci-dessous:
        """);
        dataSetService.getDatasetElements(datasetId).forEach(graphDatasetElement -> {
            contextText.append(graphDatasetElement.toString());
        });

        String finalSystemPrompt= savedPrompts.getSystemPrompt().concat(contextText.toString());
        return finalSystemPrompt;
    }

    /**
     * Cette methode permet de sauvegarder la reponse de l'utilisateur
     * @param response
     * @param dataSet
     * @param savedPrompts
     */
    private void saveResponse(String response, DataSet dataSet, LLMPrompt savedPrompts) {
        Double distance = getDataFromResponse(response);

        LLMResponse llmResponse = new LLMResponse();
        llmResponse.setDataSet(dataSet);
        llmResponse.setSystemPromt(savedPrompts.getSystemPrompt());
        llmResponse.setUserData(savedPrompts.getUserData());
        llmResponse.setProvidedDistance(distance);
        //llmResponse.setExpectedDistance(getExpectedDistance(dataSet, savedPrompts));
        llmResponse.setExecutionDate(new Date());
        //     llmResponseService.addLLMResponse(llmResponse);
    }

    /**
     * Cette methode permet de calculer la distance attendue
     * @param dataSet
     * @param savedPrompts
     * @return
     */
    private Double getExpectedDistance(DataSet dataSet, LLMPrompt savedPrompts) {
        PathResult shortestPath = graphService.findShortestPath(dataSet, savedPrompts.getStart().toString(), savedPrompts.getEnd().toString());
        return shortestPath.getTotalDistance();
    }
    

    /**
     * Cette methode permet de recuperer la distance fournie par Ollama
     * @param response
     * @return
     */
    private Double getDataFromResponse(String response){
        String jsonPath="$.total_distance";
        Double val = 0.0;
        try {
             val = JsonPath.read(response, jsonPath);
        } catch (Exception e) {
            log.error("Invalid response from Ollama");
        }
        return val;
    }
}
