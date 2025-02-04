package com.example.springiapromptdemo.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springiapromptdemo.entities.FinalResult;
import com.example.springiapromptdemo.entities.LLMResponse;
import com.example.springiapromptdemo.services.OllamaService;

@RestController
@RequestMapping("/ollama")
public class OpenIARestController {

    @Autowired
    OllamaService ollamaService;

    @GetMapping("/prompt")
    public ResponseEntity<List<FinalResult>> callOllama(@RequestParam String userMeassage){
    	List<FinalResult> res = this.ollamaService.initChat(userMeassage);
        return ResponseEntity.ok(res);
    }

}
