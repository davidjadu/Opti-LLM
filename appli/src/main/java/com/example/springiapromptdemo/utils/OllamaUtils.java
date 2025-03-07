package com.example.springiapromptdemo.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import com.example.springiapromptdemo.entities.FinalResult;
import com.example.springiapromptdemo.entities.GraphDatasetElement;
import com.example.springiapromptdemo.entities.PathResult;
import org.springframework.core.env.Environment;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public final class OllamaUtils {
	
    // Expression régulière pour détecter les crochets et remplacer les virgules à l'intérieur
    private static final Pattern BRACKET_PATTERN = Pattern.compile("\\[(.*?)\\]");

    private static String OUTPUT_DIRECTORY_PATH;
    private static String GRAPHS_INPUT_DIRECTORY_PATH;
    private static String INPUT_FILE_PATH;

    private final Environment env;

	private  OllamaUtils(Environment env) {
        this.env = env;
    }

    @PostConstruct
    private void init() {
        OUTPUT_DIRECTORY_PATH = env.getProperty("output.file.directory");
        GRAPHS_INPUT_DIRECTORY_PATH = env.getProperty("input.graph.directory");
        INPUT_FILE_PATH = env.getProperty("input.file.path");
        String URL = env.getProperty("spring.ai.ollama.base-url");

        log.info("=====> Output directory path: {}", OUTPUT_DIRECTORY_PATH);
        log.info("=====> Graph directory path: {}", GRAPHS_INPUT_DIRECTORY_PATH);
        log.info("=====> Metadata file path: {}", INPUT_FILE_PATH);
        log.info("=====> URL: {}", URL);
    }


    /**
     * Lire le fichier MetaData et extraire les données pour envoyer
     * au LLM par la suite
     * @return List<PathResult>
     * @throws IOException **
     */
    public static List<PathResult> readMetadata() throws IOException {
    	
        List<PathResult> pathResults = new ArrayList<>();

        InputStream is = getInputStream(INPUT_FILE_PATH);
        InputStream inputStream = is == null
                        ? OllamaUtils.class.getClassLoader().getResourceAsStream(INPUT_FILE_PATH)
                        : is;

        try ( BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
        	
        	// Lire tout le contenu du fichier et modifier les virgules dans les crochets
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                content.append(replaceCommasInsideBrackets(line)).append("\n");
            }

            // Maintenant, nous pouvons traiter le fichier modifié
            BufferedReader modifiedReader = new BufferedReader(new StringReader(content.toString()));


            CSVFormat csvFormat = CSVFormat.newFormat(',').builder()
            		.setHeader()
                    .setSkipHeaderRecord(true)
                    .build();

            Iterable<CSVRecord> records = csvFormat.parse(modifiedReader);
            
            for (CSVRecord record : records) {

                String graphId = record.get(0).replace("\"", "");
                
                // Si c'est un header ignorer la ligne
                if(graphId.equals("graph_id")) {
                	continue;
                }

                String digistrapahString = record.get(4);
                String distance = record.get(5);
                String nbNode = record.get(1);
                String approximateTSP = record.get(6);
                String approximateTSPLength = record.get(7).replaceAll("\"$", "");

                digistrapahString = digistrapahString.substring(1);
                String[] trim = digistrapahString.split(",");
                List<String> digistraPath = new ArrayList<>();
                Collections.addAll(digistraPath, trim);

                approximateTSP = approximateTSP.substring(1);
                String[] split = approximateTSP.split(",");
                List<String> appTSP = new ArrayList<>();
                Collections.addAll(appTSP, split);

                PathResult pathResult = new PathResult();   
                pathResult.setGraph_name("graph_"+graphId);
                pathResult.setShortestPath(digistraPath);
                pathResult.setApproximateTSP(appTSP);

                if(distance != null && !distance.trim().isEmpty() && !distance.trim().equals("_") && !distance.trim().equals("-")) {
                	pathResult.setTotalDistance(Double.parseDouble(distance));
                }
                if(approximateTSPLength != null && !approximateTSPLength.trim().isEmpty()
                        && !approximateTSPLength.trim().equals("_") && !approximateTSPLength.trim().equals("-")) {
                    pathResult.setApproximateTSPLength(Double.parseDouble(approximateTSPLength));
                }
                pathResult.setNbNodes(Integer.parseInt(nbNode));
                pathResults.add(pathResult);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pathResults;
    }

    /**
     * Modifier les données pour la suite du traitement notamment
     * les virgules dans les [] qui interviennent dans les split
     * @param input String
     * @return String
     */
    private static String replaceCommasInsideBrackets(String input) {
        // Vérifie si la chaîne contient des crochets et si elle est valide
        if (input == null || !input.contains("[")) {
            return input;
        }

        // Utilisation de la méthode Matcher pour trouver les parties avec des crochets
        Matcher matcher = BRACKET_PATTERN.matcher(input);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            // Remplacer les virgules par des tirets uniquement dans les crochets
            String modifiedContent = matcher.group(1).replace(",", "-");
            matcher.appendReplacement(result, "[" + modifiedContent + "]");
        }

        // Ajoute le reste de la chaîne après la dernière correspondance
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Lecture des fichiers contenant les graphs
     *
     * @param fileName représentant le nom du fichier contenant les graphs
     * @return List<GraphDatasetElement>
     * @throws IOException **
     */
    public static List<GraphDatasetElement> loadDataSet(String fileName) throws IOException {
        List<GraphDatasetElement> graphDatasetElementList = new ArrayList<>();

        InputStream is = getInputStream(GRAPHS_INPUT_DIRECTORY_PATH.concat(fileName).concat(".csv"));
        InputStream inputStream = is == null
                ? OllamaUtils.class.getClassLoader().getResourceAsStream(INPUT_FILE_PATH)
                : is;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

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
                graphDatasetElement.setPointA(pointA);
                graphDatasetElement.setPointB(pointB);
                graphDatasetElement.setDistance(Double.valueOf(distance));
                graphDatasetElementList.add(graphDatasetElement);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return graphDatasetElementList;
    }


    /**
     * Sauvegarde le résultat finale dans un fichier csv
     * @param resp : représentant le résultat à sauvegarder dans le fichier de sortie
     */
    public static void saveResponse(List<FinalResult> resp) throws FileNotFoundException {

        String CSV_OUTPUT_FILE_PATH = OUTPUT_DIRECTORY_PATH + "finalResult.csv";

        File directory = new File(OUTPUT_DIRECTORY_PATH);
        if (!directory.exists()) {
            throw new FileNotFoundException("Directory does not found");
        }
        
    	 // Création d'un format CSV avec en-têtes
        CSVFormat csvFormat = CSVFormat.Builder.create()
                .setHeader("graph_id", "shortestPath", "totalDistance", "score",
                           "approximateTSP", "totalDistanceForAppTSP", "scoreForAppTSP", "hallucinations") // Définition des en-têtes
                .setSkipHeaderRecord(false) // Ne pas ignorer l'en-tête
                .build();
        
        try (BufferedWriter writer = new BufferedWriter (new FileWriter(CSV_OUTPUT_FILE_PATH));
             CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {

               // Écriture des données
        	   for(FinalResult finalResult : resp) {
        		   csvPrinter.printRecord(finalResult.getGraph_name(), 
        				   				  finalResult.getShortestPath(),
        				   				  finalResult.getTotalDistance(), 
        				   				  finalResult.getScore(),
                                          finalResult.getApproximateTSP(),
                                          finalResult.getTotalDistanceForAppTSP(),
                                          finalResult.getScoreForAppTSP(),
                                          finalResult.getHallucinationPaths());
        	   }
        	   
               csvPrinter.flush(); // Force l'écriture des données dans le fichier

           } catch (IOException e) {
               log.error(e.getMessage());
           }
       
    }


    /**
     * Permet de déterminer si le LLM hallucine dans ses réponses ou pas
     *
     * @param path : représente le path optimum retourné par le LLM
     * @param graphDatasetElements : représente les graphs lus initialement
     * @return Pair<Boolean, List<String>> : contient deux objets : un booléen qui précise si
     * le llm hallucine et une liste contenant les paths qui sont considérés comme une hallucination
     */
    public static Pair<Boolean, List<String>> isHallucination(String path, List<GraphDatasetElement> graphDatasetElements) {
        List<String> faultPaths = new ArrayList<>();
        // Stocker les connexions dans un Set pour une recherche rapide
        Set<String> edges = new HashSet<>();
        for (GraphDatasetElement element : graphDatasetElements) {
            edges.add(element.getPointA() + "->" + element.getPointB());
            edges.add(element.getPointB() + "->" + element.getPointA());
        }

        // Vérifier chaque transition dans le chemin
        String[] nodes = (path != null && !path.isBlank())
                ? path.trim().split("\\s*->\\s*") : new String[0];
        for (int i = 1; i < nodes.length; i++) {
            if (!edges.contains(nodes[i - 1] + "->" + nodes[i])) {
                faultPaths.add(nodes[i - 1] + "->" + nodes[i]); // Si une connexion est absente, ce n'est pas valide
            }
        }
        return faultPaths.isEmpty() ?  Pair.of(false,faultPaths) : Pair.of(true,faultPaths);
    }

    /**
     * Permet d'obtenir la distance totale d'un chemin donné
     *
     * @param graphDatasetElements : représente les graphs lus initialement
     * @param path : Le chemin pour lequel on aimerait obtenir la distance totale
     * @return Double : représentant la distance totale
     */
    public static Double calculTotalDistance(List<GraphDatasetElement> graphDatasetElements, String path) {
        // Stocker les distances dans une Map pour une recherche rapide
        Map<String, Double> distances = new HashMap<>();
        for (GraphDatasetElement g : graphDatasetElements) {
            distances.put(g.getPointA() + "->" + g.getPointB(), g.getDistance());
            distances.put(g.getPointB() + "->" + g.getPointA(), g.getDistance());
        }

        // Calcul de la distance totale
        String[] nodes = (path != null && !path.isBlank())
                ? path.trim().split("\\s*->\\s*") : new String[0];
        double totalDistance = 0.0;

        for (int i = 1; i < nodes.length; i++) {
            String edgeKey = nodes[i - 1] + "->" + nodes[i];
            totalDistance += distances.getOrDefault(edgeKey, 0.0); // Ajoute 0 si l'arête n'existe pas
        }

        return totalDistance;
    }

    /**
     * Permet de savoir si c'est le path attendu ou pas
     * @param path : représentant le path à vérifier (celui retourné par le LLM)
     * @param expectedPath : path attendu et qui est donné par le fichier metadata
     * @return boolean: true si c'est le path attendu et false sinon
     */
    public static boolean isExpectedPath(String path, String expectedPath){
        if(path == null || expectedPath == null) {
            return false;
        }
        expectedPath = formatPath(expectedPath);
        return path.trim().equals(expectedPath);
    }

    /**
     * Formater le path
     * @param rawPath path à formatter
     * @return String : représentant le path formaté
     */
    private static String formatPath(String rawPath) {
        return rawPath.replaceAll("[\\[\\]\"]", "")  // Supprime les crochets et guillemets
                .replaceAll("\\s*-\\s*", " -> ") // Remplace les tirets entourés d'espaces par une flêche
                .trim();
    }

    /**
     * getInputStream File
     * @param filePath : chemin du fichier
     * @return InputStream
     * @throws IOException **
     */
    private static InputStream getInputStream(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            return Files.newInputStream(path);
        }
        throw new FileNotFoundException("Fichier introuvable : " + filePath);
    }

}
