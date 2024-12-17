package com.example.springiapromptdemo.repositories;

import com.example.springiapromptdemo.entities.DataSet;
import com.example.springiapromptdemo.entities.GraphDatasetElement;
import com.example.springiapromptdemo.services.DataSetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GraphDatasetElementsRepository extends JpaRepository<GraphDatasetElement,Long> {
    Page<GraphDatasetElement> findGraphDatasetElementsByDataSet(DataSet dataset, Pageable pageable);
    List<GraphDatasetElement> findGraphDatasetElementsByDataSet(DataSet dataset);
}
