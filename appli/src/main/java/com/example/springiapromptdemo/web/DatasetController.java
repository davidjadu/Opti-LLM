package com.example.springiapromptdemo.web;

import com.example.springiapromptdemo.entities.DataSet;
import com.example.springiapromptdemo.entities.GraphDatasetElement;
import com.example.springiapromptdemo.services.DataSetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dataset")
public class DatasetController {

    @Autowired
    DataSetService dataSetService;

    @Value("${directory.path}")
    private  String directoryPath;
    @PostMapping
    public void createDataset(@RequestBody DataSet dataSet){
        dataSetService.createDataSet(dataSet);
    }

    @PostMapping(value = "/{datasetId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void loadDatasetFromFiles(@PathVariable Long datasetId) throws IOException {
        Path dirPath = Paths.get(directoryPath);
        List<File> fichiers = null;
        // Check if the directory exists
        if (Files.isDirectory(dirPath)) {
            // Get all files in the directory
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
                for (Path entry : stream) {
                    if (Files.isRegularFile(entry)) {
                        fichiers.add(new File(entry.toString()));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("The provided path is not a valid directory.");
        }
        fichiers.stream()
                .forEach(file -> processFile(datasetId, file));
    }

    @PostMapping(value = "/{datasetId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void loadDataset(@PathVariable Long datasetId, @RequestParam(value = "files") List<MultipartFile> files) {

        if (files.isEmpty()) {
            log.warn("No files uploaded");
        } else {
            files.stream()
                    .filter(file -> !file.isEmpty())
                    .forEach(file -> {
                        try {
                         File fichier= convertMultipartToFile(file);
                            processFile(datasetId, fichier);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    });
        }
    }

    /**
     * Convert MultipartFile to File
     */
    //Todo : A déplacer dans le service
    private File convertMultipartToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(file.getOriginalFilename());
        Files.copy(file.getInputStream(), convertedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return convertedFile;
    }

    // Méthode séparée pour traiter chaque fichier
    private void processFile(Long datasetId, File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            Files.copy(fis, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            dataSetService.loadDataset(datasetId, file);

            log.info("File uploaded and processed: {}", file.getName());

        } catch (IOException e) {
            log.error("Error processing file: {}", file.getAbsoluteFile(), e);
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
