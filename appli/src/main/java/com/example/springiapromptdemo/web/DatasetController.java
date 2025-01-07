package com.example.springiapromptdemo.web;

import com.example.springiapromptdemo.entities.DataSet;
import com.example.springiapromptdemo.entities.GraphDatasetElement;
import com.example.springiapromptdemo.services.DataSetService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RestController
@RequestMapping("/dataset")
public class DatasetController {

    @Autowired
    DataSetService dataSetService;

    private static final String DIRECTORY_PATH = "C:\\path\\to\\your\\folder";  // Chemin du répertoire contenant les fichiers

    @PostMapping
    public void createDataset(@RequestBody DataSet dataSet){
        dataSetService.createDataSet(dataSet);
    }

    public void loadDataset(@PathVariable Long datasetId) {
        // Créer un objet File pour le répertoire contenant les fichiers
        File folder = new File(DIRECTORY_PATH);

        // Vérifiez si le dossier existe et est valide
        if (!folder.exists() || !folder.isDirectory()) {
            log.warn("Le répertoire spécifié n'existe pas ou ce n'est pas un dossier valide");
            return;
        }

        // Liste des fichiers dans le répertoire
        File[] files = folder.listFiles();

        if (files == null || files.length == 0) {
            log.warn("Aucun fichier trouvé dans le répertoire");
        } else {
            // Parcourez chaque fichier et traitez-le
            for (File file : files) {
                if (file.isFile()) {  // Vérifiez qu'il s'agit bien d'un fichier, pas d'un répertoire
                    try {
                        // Traitez le fichier directement, pas besoin de MultipartFile
                        dataSetService.loadDataset(datasetId, file);  // Appel de la méthode existante
                    } catch (Exception e) {
                        log.error("Erreur lors du traitement du fichier: {}", file.getName(), e);
                    }
                }
            }
        }
    }

    @PostMapping(value = "/{datasetId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void loadDataset(@PathVariable Long datasetId, @RequestParam(value = "files") List<MultipartFile> files) {

        if (files.isEmpty()) {
            log.warn("No files uploaded");
        } else {
            files.stream()
                    .filter(file -> !file.isEmpty())
                    .forEach(file -> processFile(datasetId, file));     
        }
    }

    // Méthode séparée pour traiter chaque fichier
    private void processFile(Long datasetId, MultipartFile file) {
        try {
            Path destination = Paths.get("C:\\workdir\\SpringAIDemo-master\\src\\main\\resources\\" + file.getOriginalFilename());
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            dataSetService.loadDataset(datasetId, destination.toFile());

            log.info("File uploaded and processed: {}", file.getOriginalFilename());

        } catch (IOException e) {
            log.error("Error processing file: {}", file.getOriginalFilename(), e);
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
