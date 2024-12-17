package com.example.springiapromptdemo.repositories;

import com.example.springiapromptdemo.entities.DataSet;
import com.example.springiapromptdemo.entities.GraphDatasetElement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataSetRepository extends JpaRepository<DataSet,Long> {
}
