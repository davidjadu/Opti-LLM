# SpringIAPromptDemo

## Description
SpringIAPromptDemo est une application Maven permettant de résoudre le problème du voyageur de commerce sur des graphes de nœuds en utilisant Ollama. L'application prend en entrée des fichiers CSV contenant les graphes et leurs métadonnées, envoie les données au LLM, et sauvegarde les résultats dans un fichier CSV. On compare ensuite les résultats du LLM à ceux du fichiers métadata pour déterminer le score.

## Fonctionnalités
- Lecture et traitement de fichiers CSV représentant des graphes.
- Lecture des métadonnées des graphes à partir d'un fichier spécifique.
- Communication avec Ollama pour trouver les chemins optimaux.
- Sauvegarde des résultats dans un fichier CSV.
- Exposition d'une API REST pour interagir avec l'application.

## Prérequis
- Java 17+
- Maven 3+

## Installation
### 1. Cloner le dépôt
```sh
git clone https://github.com/votre-utilisateur/SpringIAPromptDemo.git
cd SpringIAPromptDemo
```

### 2. Configurer les variables d'environnement
L'application utilise des variables d'environnement pour définir les répertoires et fichiers de travail. Toutefois, des répertoires par défaut sont fournis dans le projet. Si vous ne souhaitez pas définir de nouveaux chemins, vous pouvez simplement placer vos fichiers dans ces répertoires par défaut.

Exemple de configuration manuelle :
```sh linux
export SERVER_PORT=server_port
export OLLAMA_HOST_URL=url_ollama
export OLLAMA_PORT=ollama_port
export OLLAMA_MODEL=ollama_model
export GRAPH_DIRECTORY=/chemin/vers/graphs/
export METADATA_FILE=/chemin/vers/metadata.csv
export OUTPUT_DIRECTORY=/chemin/vers/output/
```

```cmd
set SERVER_PORT=server_port
set OLLAMA_HOST_URL=url_ollama
set OLLAMA_PORT=ollama_port
set OLLAMA_MODEL=ollama_model
set GRAPH_DIRECTORY=/chemin/vers/graphs/
set METADATA_FILE=/chemin/vers/metadata.csv
set OUTPUT_DIRECTORY=/chemin/vers/output/
```

```powershell
$env:SERVER_PORT=server_port
$env:OLLAMA_HOST_URL=url_ollama
$env:OLLAMA_PORT=ollama_port
$env:OLLAMA_MODEL=ollama_model
$env:GRAPH_DIRECTORY=/chemin/vers/graphs/
$env:METADATA_FILE=/chemin/vers/metadata.csv
$env:OUTPUT_DIRECTORY=/chemin/vers/output/
```

### 3. Compilation et exécution
```sh
mvn clean install
java -jar target/nom_application.jar
```

## Utilisation
1. Placez vos fichiers CSV de graphes dans le répertoire spécifié par `INPUT_DIR` ou utilisez le répertoire par défaut.
2. Placez le fichier de métadonnées dans `METADATA_FILE` ou utilisez celui par défaut.
3. Exécutez l'application.
4. Les résultats seront enregistrés dans le fichier spécifié par `OUTPUT_FILE`.

## API REST
L'application expose un endpoint défini dans `OpenIARestController.java` pour interagir avec Ollama :

### 1. Appeler Ollama avec un message utilisateur
**GET** `/ollama/prompt`
- **Description** : Envoie un message utilisateur à Ollama et récupère la réponse.
- **Paramètres** :
  - `userMessage` (query param) : Le message à envoyer à Ollama.
- **Réponse** : Une liste d'objets `FinalResult` contenant la réponse de l'IA.
