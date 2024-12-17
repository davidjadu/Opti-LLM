package com.example.springiapromptdemo.web;

import com.example.springiapromptdemo.services.OllamaService;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/ollama")
public class OpenIARestController {

    @Autowired
    OllamaService ollamaService;

    @GetMapping
    public String callOllama(){
        return ollamaService.callOllama();

    }

    @GetMapping("/{promptId}")
    public String callOllama(@PathVariable Long promptId){
        return ollamaService.callOllama(promptId);

    }

    @GetMapping("/{promptId}/{datasetId}")
    public String callOllama(@PathVariable Long promptId,@PathVariable Long datasetId){
        return ollamaService.callOllama(promptId,datasetId);

    }
}
