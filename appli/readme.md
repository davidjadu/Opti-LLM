# SpringIAPromptDemo

## Description
SpringIAPromptDemo est une application Maven permettant de résoudre le problème du voyageur sur des graphes de nœuds en utilisant l'API Ollama. L'application prend en entrée des fichiers CSV contenant les graphes et leurs métadonnées, envoie les données à l'API, et sauvegarde les résultats dans un fichier CSV.

## Fonctionnalités
- Lecture et traitement de fichiers CSV représentant des graphes.
- Lecture des métadonnées des graphes à partir d'un fichier spécifique.
- Communication avec l'API Ollama pour résoudre les chemins optimaux.
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
```sh
export INPUT_DIR=/chemin/vers/fichiers_graphes
export METADATA_FILE=/chemin/vers/metadata.csv
export OUTPUT_FILE=/chemin/vers/resultats.csv
```

### 3. Compilation et exécution
```sh
mvn clean install
java -jar target/SpringIAPromptDemo.jar
```

#### Alternative : Lancer avec les variables d'environnement en une seule commande
```sh
INPUT_DIR=/chemin/vers/fichiers_graphes METADATA_FILE=/chemin/vers/metadata.csv OUTPUT_FILE=/chemin/vers/resultats.csv mvn spring-boot:run
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
