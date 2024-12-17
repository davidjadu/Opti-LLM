package com.example.springiapromptdemo.repositories;

import com.example.springiapromptdemo.entities.LLMResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LLMResponseRepository extends JpaRepository<LLMResponse,Long> {

    @Query("SELECT L from LLMResponse L where L.prompt.id = ?1")
    Page<LLMResponse> getLLMResponsesByPrompt(Long promptId, Pageable pageable);
}
