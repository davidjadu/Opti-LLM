package com.example.springiapromptdemo.services;

import com.example.springiapromptdemo.entities.LLMPrompt;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OllamaService {

    @Autowired
    OllamaChatModel ollamaChatModel;

    @Autowired PromptService promptService;

    @Autowired DataSetService dataSetService;

    public String callOllama(){
        SystemPromptTemplate systemPromptTemplate= new SystemPromptTemplate(
                """
                        I need you to give me the best movie on the given category: action
                        on the given year : 2020.
                        the outpout shourld be in json format including the following fields :
                         - category <The given category>
                         - year <The year of the movie>
                         - title <The title of the movie>
                         - producer <The producer of the movie>
                         - actors < A list of the main actors of the movie>
                         - summary <a very small summary of the movie>
                        """
        );



        Prompt prompt= systemPromptTemplate.create(Map.of("category","action","year",2020));
        ChatResponse chatResponse = ollamaChatModel.call(prompt);
        System.out.println("chatResponse = " + chatResponse);
        return chatResponse.getResult().getOutput().getContent();
    }


    public String callOllama(Long promptId){
        LLMPrompt savedPrompts = promptService.getOnePrompt(promptId);
        Prompt prompt= new Prompt(savedPrompts.getSystemPrompt());
        ChatResponse chatResponse = ollamaChatModel.call(prompt);
        System.out.println("chatResponse = " + chatResponse);
        return chatResponse.getResult().getOutput().getContent();
    }


    public String callOllama(Long promptId,Long datasetId){
        LLMPrompt savedPrompts = promptService.getOnePrompt(promptId);
        StringBuffer contextText=new StringBuffer();
        contextText.append("Tu effectuera la tache demandé en tenant compte uniquement des informations fournis ci-dessous:");
        dataSetService.getDatasetElements(datasetId).forEach(graphDatasetElement -> {
            contextText.append(graphDatasetElement.toString());
        });

       String finalSystemPrompt=savedPrompts.getSystemPrompt().concat(contextText.toString());


        Prompt prompt= new Prompt(finalSystemPrompt);
        ChatResponse chatResponse = ollamaChatModel.call(prompt);
        System.out.println("chatResponse = " + chatResponse);
        return chatResponse.getResult().getOutput().getContent();
    }
}
