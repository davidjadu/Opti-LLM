package com.example.springiapromptdemo.repositories;

import com.example.springiapromptdemo.entities.LLMPrompt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavePromptsRepository extends JpaRepository<LLMPrompt,Long> {
}
