package com.example.springiapromptdemo.web;


import com.example.springiapromptdemo.entities.LLMPrompt;
import com.example.springiapromptdemo.services.PromptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/prompt")
public class PromptController {
    @Autowired
    PromptService promptService;

    @PostMapping
    public void addPrompt(@RequestBody LLMPrompt llmPrompt){
        promptService.addPrompt(llmPrompt);
    }

    @PutMapping
    public void updatePrompt(@RequestBody LLMPrompt llmPrompt){
        promptService.updatePrompt(llmPrompt);
    }

    @GetMapping("/{page}/{size}")
    public Page<LLMPrompt> getAllPrompts(@PathVariable Integer page,@PathVariable Integer size){
        return promptService.getAllSavedPrompts(page,size);
    }

    @GetMapping("/{promptId}")
    public LLMPrompt getOnePrompte(@PathVariable Long promptId){
        return promptService.getOnePrompt(promptId);
    }
}
