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


    @PostMapping(value = "/{datasetId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void loadDataset(@PathVariable Long datasetId, @RequestParam(value = "files") List<MultipartFile> files) {
        System.out.println("testme");
        try {
            if (files.isEmpty()) {
                System.out.println("No files uploaded");
            } else {
                files.stream()
                        .filter(file -> !file.isEmpty()) // Filtrer les fichiers non vides
                        .forEach(file -> processFile(datasetId, file)); // Appliquer le traitement à chaque fichier
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Error uploading files", e);
        }
    }

    // Méthode séparée pour traiter chaque fichier
    private void processFile(Long datasetId, MultipartFile file) {
        try {
            // Définir le chemin de destination pour chaque fichier
            Path destination = Paths.get("C:\\workdir\\SpringAIDemo-master\\src\\main\\resources\\" + file.getOriginalFilename());

            // Copier le fichier dans le répertoire de destination
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            // Charger le fichier dans votre service
            dataSetService.loadDataset(datasetId, destination.toFile());

            System.out.println("File uploaded and processed: " + file.getOriginalFilename());

        } catch (IOException e) {
            // Vous pouvez aussi gérer des erreurs spécifiques à chaque fichier
            System.err.println("Error processing file " + file.getOriginalFilename() + ": " + e.getMessage());
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
