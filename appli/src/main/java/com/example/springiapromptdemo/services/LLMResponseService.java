package com.example.springiapromptdemo.services;

import com.example.springiapromptdemo.entities.LLMResponse;
import com.example.springiapromptdemo.repositories.LLMResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class LLMResponseService {
    @Autowired
    LLMResponseRepository llmResponseRepository;
    public void addLLMResponse(LLMResponse llmResponse){
        llmResponseRepository.save(llmResponse);
    }

    public LLMResponse getOne(Long id){
        return llmResponseRepository.getReferenceById(id);
    }

    public Page<LLMResponse> getLLMResponseByPrompt(Long promptId,Integer page,Integer size){
       return llmResponseRepository.getLLMResponsesByPrompt(promptId, PageRequest.of(page,size));
    }
}
