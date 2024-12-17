package com.example.springiapromptdemo.services;

import com.example.springiapromptdemo.entities.DataSet;
import com.example.springiapromptdemo.entities.GraphDatasetElement;
import com.example.springiapromptdemo.repositories.DataSetRepository;
import com.example.springiapromptdemo.repositories.GraphDatasetElementsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DataSetService {
    @Autowired
    DataSetRepository dataSetRepository;

    @Autowired
    GraphDatasetElementsService elementsService;

    public void createDataSet(DataSet dataSet){
         dataSetRepository.save(dataSet);
    }

    public void loadDataset(Long datasetId,File file){
        DataSet dataSet = dataSetRepository.getReferenceById(datasetId);
        elementsService.loadDataSet(dataSet,file);
    }

    public Page<GraphDatasetElement> getDatasetElements(Long datasetId,Integer page,Integer size){
        DataSet dataSet = dataSetRepository.getReferenceById(datasetId);
        return elementsService.getDatasetsFromDataset(dataSet,page,size);
    }
    public List<GraphDatasetElement> getDatasetElements(Long datasetId){
        DataSet dataSet = dataSetRepository.getReferenceById(datasetId);
        return elementsService.getDatasetsFromDataset(dataSet);
    }
    public Page<DataSet> getAllDatasets(Integer page,Integer size){
        return dataSetRepository.findAll(PageRequest.of(page,size));
    }



    public DataSet getOneDataset(Long id){
        return dataSetRepository.getReferenceById(id);

    }
}
