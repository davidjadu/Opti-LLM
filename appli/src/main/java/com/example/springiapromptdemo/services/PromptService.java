package com.example.springiapromptdemo.services;

import com.example.springiapromptdemo.entities.LLMPrompt;
import com.example.springiapromptdemo.repositories.SavePromptsRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PromptService {
    @Autowired
    SavePromptsRepository savePromptsRepository;

    public void addPrompt(LLMPrompt prompt) {
        prompt.setDateCreation(new Date());
        savePromptsRepository.save(prompt);
    }
    public void updatePrompt(LLMPrompt prompt) {
        LLMPrompt referenceById = savePromptsRepository.getReferenceById(prompt.getId());
        BeanUtils.copyProperties(prompt,referenceById);
        savePromptsRepository.save(referenceById);
    }

    public void deletePrompt(Long id) {
        savePromptsRepository.deleteById(id);
    }

    public LLMPrompt getOnePrompt(Long id) {
        return savePromptsRepository.getReferenceById(id);
    }

    public Page<LLMPrompt> getAllSavedPrompts(Integer page, Integer size) {
        return savePromptsRepository.findAll(PageRequest.of(page, size));
    }
}


