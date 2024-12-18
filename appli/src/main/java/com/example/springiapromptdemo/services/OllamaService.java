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


    public String callOllama(Long promptId,Long datasetId){
        LLMPrompt savedPrompts = promptService.getOnePrompt(promptId);
        DataSet dataSet = dataSetService.getOneDataset(datasetId);
        PathResult shortestPath = graphService.findShortestPath(dataSet, savedPrompts.getStart().toString(), savedPrompts.getEnd().toString());
        StringBuffer contextText=new StringBuffer();
        contextText.append("""
        Tu effectuera la tache demandé en tenant compte uniquement des informations fournis ci-dessous:
        """);
        dataSetService.getDatasetElements(datasetId).forEach(graphDatasetElement -> {
            contextText.append(graphDatasetElement.toString());
        });

       String finalSystemPrompt=savedPrompts.getSystemPrompt().concat(contextText.toString());

// Ensure that data is not null
        if (savedPrompts.getUserData() == null || finalSystemPrompt == null) {
            throw new IllegalArgumentException("Data for prompts cannot be null");
        }

        PromptTemplate promptTemplate = new PromptTemplate(savedPrompts.getUserData());
        Message message = promptTemplate.createMessage(Map.of("request", promptTemplate));

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(finalSystemPrompt);
        Message systemMessage = new SystemMessage(finalSystemPrompt);

        Prompt prompt = new Prompt(List.of(message, systemMessage));

        ChatResponse chatResponse = ollamaChatModel.call(prompt);
        System.out.println("chatResponse = " + chatResponse);
        String response = chatResponse.getResult().getOutput().getContent();
        Double distance = getDataFromResponse(response);

        LLMResponse llmResponse = new LLMResponse();
        llmResponse.setDataSet(dataSet);
        llmResponse.setSystemPromt(savedPrompts.getSystemPrompt());
        llmResponse.setUserData(savedPrompts.getUserData());
        llmResponse.setProvidedDistance(distance);
        llmResponse.setExpectedDistance(shortestPath.getTotalDistance());
        llmResponse.setExecutionDate(new Date());
        llmResponseService.addLLMResponse(llmResponse);
        return response;
    }

    private Double getExpectedDistance(DataSet dataSet){
        return 100.0; //TODO: Implement this method
    }
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
