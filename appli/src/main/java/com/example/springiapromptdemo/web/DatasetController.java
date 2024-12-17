package com.example.springiapromptdemo.web;

import com.example.springiapromptdemo.entities.DataSet;
import com.example.springiapromptdemo.entities.GraphDatasetElement;
import com.example.springiapromptdemo.services.DataSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@RestController
@RequestMapping("/dataset")
public class DatasetController {
    @Autowired
    DataSetService dataSetService;
    @PostMapping
    public void createDataset(@RequestBody DataSet dataSet){
        dataSetService.createDataSet(dataSet);
    }

    @PostMapping(value = "/{datasetId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void loadDataset(@PathVariable Long datasetId,@RequestParam(value = "file") MultipartFile file){
        System.out.println("testme");
        try {
            if(file.isEmpty()) {
                System.out.println("File empty");
            }
            //Path destination = Paths.get("rootDir").resolve(file.getOriginalFilename()).normalize().toAbsolutePath();
            Path destination = Paths.get("C:\\workdir\\"+file.getOriginalFilename());
          Files.copy(file.getInputStream(), destination,StandardCopyOption.REPLACE_EXISTING);
            dataSetService.loadDataset(datasetId,destination.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @GetMapping("/{datasetId}/{page}/{size}")
    public Page<GraphDatasetElement> getDatasetElementsFromDataset(@PathVariable Long datasetId,@PathVariable Integer page,@PathVariable Integer size){
        return dataSetService.getDatasetElements(datasetId,page,size);
    }

    @GetMapping("/{page}/{size}")
    public Page<DataSet> getAllDatasets(@PathVariable Integer page,@PathVariable Integer size){
        return dataSetService.getAllDatasets(page,size);
    }

    @GetMapping("/{datasetId}")
    public ResponseEntity<DataSet>  getOneDataset(@PathVariable Long datasetId){
        DataSet oneDataset = dataSetService.getOneDataset(datasetId);
        return new ResponseEntity<DataSet>(oneDataset, HttpStatus.OK);
    }
}
