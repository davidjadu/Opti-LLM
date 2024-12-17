package com.example.springiapromptdemo.services;

import com.example.springiapromptdemo.entities.DataSet;
import com.example.springiapromptdemo.entities.GraphDatasetElement;
import com.example.springiapromptdemo.repositories.GraphDatasetElementsRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GraphDatasetElementsService {
    @Autowired
    GraphDatasetElementsRepository graphDatasetElementsRepository;
    public void loadDataSet(DataSet dataset, File file){
        List<GraphDatasetElement> graphDatasetElementList = new ArrayList<>();

       try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()))) {


            CSVFormat csvFormat = CSVFormat.newFormat(',').builder()
                    .setSkipHeaderRecord(true)
                    .build();

            Iterable<CSVRecord> records = csvFormat.parse(br);
            for (CSVRecord record : records) {
                String pointA = record.get(0);
                if(pointA.equals("Node1")) {continue;}
                String pointB = record.get(1);
                String distance = record.get(2);
                GraphDatasetElement graphDatasetElement=new GraphDatasetElement();
                graphDatasetElement.setDataSet(dataset);
                graphDatasetElement.setPointA(pointA);
                graphDatasetElement.setPointB(pointB);
                graphDatasetElement.setDistance(Float.valueOf(distance));
                graphDatasetElementsRepository.save(graphDatasetElement);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        graphDatasetElementsRepository.saveAll(graphDatasetElementList);
    }

    public Page<GraphDatasetElement> getDatasetsFromDataset(DataSet dataSet,Integer page,Integer size){
        return graphDatasetElementsRepository.findGraphDatasetElementsByDataSet(dataSet, PageRequest.of(page,size));
    }

    public List<GraphDatasetElement> getDatasetsFromDataset(DataSet dataSet){
        return graphDatasetElementsRepository.findGraphDatasetElementsByDataSet(dataSet);
    }

}
