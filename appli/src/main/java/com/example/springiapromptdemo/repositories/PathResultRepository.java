package com.example.springiapromptdemo.repositories;

import com.example.springiapromptdemo.entities.PathResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PathResultRepository extends JpaRepository<PathResult, Long> {

    @Query("SELECT pr FROM PathResult pr WHERE pr.dataSet.id = :dataSetId AND :start MEMBER OF pr.shortestPath AND :end MEMBER OF pr.shortestPath")
    Optional<PathResult> findByDataSetAndPath(Long dataSetId, String start, String end);

}
