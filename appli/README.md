# SpringIAPromptDemo


Changements à faire:
    + Lire les fichiers de résultats en meme temps que les fichiers de graphes
    + Recuperer le prompte à partire de l'api
    + sauvegarder le resultat dans un fichier csv 
Introduction

Ce programme utilise une API externe nommée Ollama pour traiter des graphes représentant des chemins entre des noeuds. Le programme lit les métadonnées et les données de graphes à partir de fichiers CSV, envoie ces données à l’API, et sauvegarde les résultats obtenus.

La structure est divisée en plusieurs parties principales :

Lecture des métadonnées.

Lecture des données de graphes.

Traitement des résultats à l’aide de l’API Ollama.

Sauvegarde des résultats dans un fichier.

Architecture du programme

Contrôleur principal :

@PostMapping
public void callOllama(@RequestBody String prompt) {
List<PathResult> pathResults = readMetadata(); // Lecture des métadonnées.

    pathResults.stream().forEach(pathResult -> {
        List<GraphDatasetElement> graph = loadDataSet(pathResult.getGraph_name()); // Lecture des données du graphe.
        ollamaService.callOllama(prompt, graph, pathResult); // Appel de l'API avec les données.
    });
}

Cette méthode principale réalise les étapes suivantes :

Lit les métadonnées à partir d’un fichier CSV via la méthode readMetadata.

Charge les graphes individuels à partir des fichiers correspondants via la méthode loadDataSet.

Appelle le service Ollama pour traiter les données des graphes et obtient les résultats.

Lecture des métadonnées : readMetadata()

Cette méthode charge un fichier CSV contenant des informations sur les graphes et leur chemin le plus court précalculé.

Fonctionnement :

Ouvre le fichier metadata.csv en mode lecture.

Parcourt chaque ligne pour extraire les colonnes suivantes :

graphId : Identifiant du graphe.

digistrapathString : Représentation du chemin le plus court.

digistraLenth : Représentation la distance totale parcourue.

Transforme ces informations en objets PathResult et les ajoute à une liste.

Code :

private List<PathResult> readMetadata() {
List<PathResult> pathResults = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader("metadata.csv"))) {
        CSVFormat csvFormat = CSVFormat.newFormat(',').builder()
                .setSkipHeaderRecord(true)
                .build();

        Iterable<CSVRecord> records = csvFormat.parse(br);
        for (CSVRecord record : records) {
            String graphId = record.get(0);
            if (graphId.equals("graph_id")) continue; // Ignorer les en-têtes

            String digistrapahString = record.get(4);
            String distance = record.get(5);
            digistrapahString = digistrapahString.substring(1, digistrapahString.length());
            String[] trim = digistrapahString.split(",");
            List<String> digistrapah = Arrays.asList(trim);

                PathResult pathResult = PathResult.builder()
                    .graph_name("graph_" + graphId)
                    .shortestPath(digistrapah)
                    .build();

            pathResults.add(pathResult);
        }
    } catch (IOException e) {
        throw new RuntimeException(e);
    }

    return pathResults;
}

Lecture des données du graphe : loadDataSet()

Cette méthode charge un fichier CSV spécifique à chaque graphe contenant des informations sur les noeuds connectés et leurs distances.

Fonctionnement :

Ouvre le fichier CSV correspondant à un graphe.

Parcourt chaque ligne pour extraire :

Node1 : Premier noeud.

Node2 : Deuxième noeud.

Distance : Distance entre les deux noeuds.

Transforme ces informations en objets GraphDatasetElement et les ajoute à une liste.

Code :

public List<GraphDatasetElement> loadDataSet(String fileName) {
File file = new File(fileName);
List<GraphDatasetElement> graphDatasetElementList = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
        CSVFormat csvFormat = CSVFormat.newFormat(',').builder()
                .setSkipHeaderRecord(true)
                .build();

        Iterable<CSVRecord> records = csvFormat.parse(br);
        for (CSVRecord record : records) {
            String pointA = record.get(0);
            if (pointA.equals("Node1")) continue; // Ignorer les en-têtes

            String pointB = record.get(1);
            String distance = record.get(2);

            GraphDatasetElement graphDatasetElement = new GraphDatasetElement();
            graphDatasetElement.setPointA(pointA);
            graphDatasetElement.setPointB(pointB);
            graphDatasetElement.setDistance(Float.valueOf(distance));

            graphDatasetElementList.add(graphDatasetElement);
        }
    } catch (IOException e) {
        throw new RuntimeException(e);
    }

    return graphDatasetElementList;
}

Appel de l’API Ollama : callOllama()

Cette méthode envoie les données du graphe et le prompt utilisateur à l’API Ollama, puis sauvegarde la réponse obtenue.

Fonctionnement :

Augmente le prompt utilisateur avec les données du graphe.

Envoie le prompt à l’API via le modèle de chat.

Parse la réponse de l’API pour extraire les informations utiles (e.g., distance totale).

Sauvegarde la réponse sous forme d’objet LLMResponse.

Code :

public String callOllama(String promptString, List<GraphDatasetElement> graph, PathResult pathResult) {
String finalSystemPrompt = augmentSystemPrompt(graph, promptString);

    Message systemMessage = new SystemMessage(finalSystemPrompt);
    Prompt prompt = new Prompt(List.of(systemMessage));

    ChatResponse chatResponse = ollamaChatModel.call(prompt);
    String response = chatResponse.getResult().getOutput().getContent();

    saveResponse(response, pathResult.getGraph_name(), promptString);

    return response;
}

Sauvegarde des réponses : saveResponse()

Cette méthode crée un objet LLMResponse contenant :

Le nom du graphe.

Le prompt utilisé.

La distance totale fournie par l’API.

Le score calculé (en pourcentage).

La date d’exécution.

Code :