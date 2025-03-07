package com.example.springiapromptdemo.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.example.springiapromptdemo.entities.FinalResult;
import com.example.springiapromptdemo.entities.GraphDatasetElement;
import com.example.springiapromptdemo.entities.LLMResponse;
import com.example.springiapromptdemo.entities.PathResult;
import com.example.springiapromptdemo.utils.OllamaUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OllamaService {
    
    private final ChatClient chatClient;

	private static final String SYSTEM_MESSAGE_RESOURCE_ZSP = "src/main/resources/zero-shot-prompt/systemMessage.txt";
	private static final String USER_MESSAGE_RESOURCE_ZSP = "src/main/resources/zero-shot-prompt/userMessage.txt";

	private static final String SYSTEM_MESSAGE_RESOURCE_FSP = "src/main/resources/few-shot-prompt/systemMessage.txt";
	private static final String USER_MESSAGE_RESOURCE_FSP = "src/main/resources/few-shot-prompt/userMessage.txt";

    public OllamaService(ChatClient.Builder chatClient)
    {
        this.chatClient = chatClient.build();
    }

	/**
	 * Initialisation du chat avec le LLM
	 * @return List<FinalResult> :
	 * @throws IOException **
	 */
	public List<FinalResult> initChat() throws IOException {

    	List<PathResult> pathResults = OllamaUtils.readMetadata();
    	List<FinalResult> resp = new ArrayList<>();

		for (PathResult pathResult : pathResults) {

			List<GraphDatasetElement> graph;

			try {

				log.info("================== TRAITEMENT DU {} ===============", pathResult.getGraph_name());

				graph = OllamaUtils.loadDataSet(pathResult.getGraph_name());

				Map<String, LLMResponse> llmResponse = this.callOllama(graph, pathResult.getNbNodes());

				FinalResult finalResult = computeDataWithLLMResponse(pathResult, llmResponse, graph);

				resp.add(finalResult);

				log.info("=============== FIN DU TRAITEMENT DU {} ============", pathResult.getGraph_name());

			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}

		OllamaUtils.saveResponse(resp);
        return resp;
    }


	/**
	 * Récupérer les données à sauvegarder en fonction aussi du retour des LLM
	 * @param pathResult : représente la ligne lue dans le fichier metadata
	 * @param llmResponse : Réponse du LLM
	 * @param graph : représente les données du graphe
	 * @return FinalResult
	 */
	private FinalResult computeDataWithLLMResponse(PathResult pathResult, Map<String, LLMResponse> llmResponse,
												   List<GraphDatasetElement> graph) {

		FinalResult finalResult = new FinalResult();

		String strExpectedShortestPath = getExpectedShortestPath(pathResult, true);
		LLMResponse zeroShotPromptResp = llmResponse.get("zeroShotPrompt");
		this.computeScoreAndDistance(zeroShotPromptResp, graph, pathResult, strExpectedShortestPath, finalResult, true);

		finalResult.setGraph_name(pathResult.getGraph_name());
		finalResult.setExpectedShortestPath(strExpectedShortestPath);
		finalResult.setShortestPath(zeroShotPromptResp.getShortestPath());
		finalResult.setExpectedTotalDistance(pathResult.getTotalDistance());


		String strExpectedApproximateTSP = getExpectedShortestPath(pathResult, false);
		LLMResponse fewShotPromptResp = llmResponse.get("fewShotPrompt");
		this.computeScoreAndDistance(fewShotPromptResp, graph, pathResult, strExpectedApproximateTSP, finalResult, false);

		finalResult.setExpectedApproximateTSP(strExpectedApproximateTSP);
		finalResult.setApproximateTSP(fewShotPromptResp.getShortestPath());
		finalResult.setExpectedTotalDistanceForAppTSP(pathResult.getApproximateTSPLength());
		
		return finalResult;
	}

	/**
	 * Calcul le score final et la distance totale en tenant compte des éventuelles hallucinations du LLM
	 * @param llmResponse : Réponse du LLM
	 * @param graph : représente les données du graphe
	 * @param pathResult : représente la ligne lue dans le fichier metadata
	 * @param strExpectedShortestPath : chemin attendu
	 * @param finalResult : Résultat final à sauvegarder
	 * @param isZeroShotPromptResponse : booléen permettant de savoir si c'est zeroShotPrompt ou fewShotPrompt
	 */
	private void computeScoreAndDistance(
			LLMResponse llmResponse, List<GraphDatasetElement> graph, PathResult pathResult,
			String strExpectedShortestPath, FinalResult finalResult, Boolean isZeroShotPromptResponse) {

		Double totalDistance = 0.0;
		Double score;

		Pair<Boolean, List<String>> hallucination = OllamaUtils.isHallucination(llmResponse.getShortestPath(), graph);

		if((!hallucination.getFirst())
				&& (OllamaUtils.isExpectedPath(llmResponse.getShortestPath(), strExpectedShortestPath))) {
			totalDistance = OllamaUtils.calculTotalDistance(graph, llmResponse.getShortestPath());
		} else if ((!hallucination.getFirst())
				&& (!OllamaUtils.isExpectedPath(llmResponse.getShortestPath(), strExpectedShortestPath))) {
			totalDistance = OllamaUtils.calculTotalDistance(graph, llmResponse.getShortestPath());
		} else if (hallucination.getFirst()) {
			finalResult.setTotalDistance(null);
			finalResult.setScore(null);
			finalResult.setTotalDistanceForAppTSP(null);
			finalResult.setScoreForAppTSP(null);
			finalResult.setHallucinationPaths(hallucination.getSecond());
		}


		if(isZeroShotPromptResponse && !hallucination.getFirst()) {
			finalResult.setTotalDistance(totalDistance);
			score = !Objects.isNull(pathResult.getTotalDistance())  ? (pathResult.getTotalDistance() - totalDistance) : null;
			finalResult.setScore(score);
		}else if(!isZeroShotPromptResponse && !hallucination.getFirst()) {
			finalResult.setTotalDistanceForAppTSP(totalDistance);
			score =  !Objects.isNull(pathResult.getApproximateTSPLength())
						? (pathResult.getApproximateTSPLength() - totalDistance) : null;
			finalResult.setScoreForAppTSP(score);
		}

	}

	/**
     * Cette methode permet d'appeler le LLM pour la tâche
     * @param graph : représente les données du graphe
	 * @param nbNode : nombre de noeuds totals à atteindre
     * @return Map<String, LLMResponse> : réponse du LLM en fonction du type de prompt
     */
    private Map<String, LLMResponse> callOllama(List<GraphDatasetElement> graph, int nbNode) throws IOException {
		Map<String, LLMResponse> responses = new HashMap<>();

        log.trace("Construction des prompts système et utilisateur...");
        Prompt zeroShotPrompt = zeroShotPrompt(graph, nbNode);
		Prompt fewShotPrompt = fewShotPrompt(graph, nbNode);

        log.trace("Appel de ollama avec ChatClient...");
		LLMResponse zeroShotPromptResponse = chatClient.prompt(zeroShotPrompt)
													   .advisors(new SimpleLoggerAdvisor())
													   .call()
													   .entity(LLMResponse.class);

		LLMResponse fewShotPromptResponse = chatClient.prompt(fewShotPrompt)
													  .advisors(new SimpleLoggerAdvisor())
													  .call()
													  .entity(LLMResponse.class);
		responses.put("zeroShotPrompt", zeroShotPromptResponse);
		responses.put("fewShotPrompt", fewShotPromptResponse);

		return responses;
	}

    /**
     * Cette methode permet d'ajouter des informations contextuelles au prompt système
     *  - Les informations contextuelles sont les elements du dataset
     *  - ET des indications sur la tâche à effectuer.
	 *  -
	 *  Obtenir un prompt de type fewShotPrompt
	 *  -
     * @param graph : représente les données du graphe
	 * @param nbNode : nombre de noeuds totals à atteindre
     * @return Prompt : zeroShotPrompt
     */
    private Prompt zeroShotPrompt(List<GraphDatasetElement> graph, int nbNode) throws IOException {

		String graphStr = transformListToString(graph);

		String systemTemplate = Files.readString(Paths.get(SYSTEM_MESSAGE_RESOURCE_ZSP));
		String systemMessageTxt = String.format(systemTemplate, (nbNode - 1));

		String userTemplate = Files.readString(Paths.get(USER_MESSAGE_RESOURCE_ZSP));
		String userMessageTxt = String.format(userTemplate, nbNode, (nbNode - 1), graphStr);

		SystemMessage systemMessage = new SystemMessage(systemMessageTxt);
		UserMessage userMessage = new UserMessage(userMessageTxt);

		return new Prompt(List.of(systemMessage, userMessage));
	}

	/**
	 * Cette methode permet d'ajouter des informations contextuelles au prompt système
	 * Les informations contextuelles sont les elements du dataset
	 * ET des indications sur la tâche à effectuer.
	 * -
	 * Obtenir un prompt de type fewShotPrompt
	 * -
	 * @param graph : représente les données du graphe
	 * @param nbNode : nombre de noeuds totals à atteindre
	 * @return Prompt : fewShotPrompt
	 * @throws IOException -
	 */
	private Prompt fewShotPrompt(List<GraphDatasetElement> graph, int nbNode) throws IOException {

		String ex1FilePath = "src/main/resources/few-shot-prompt/examples/example1.txt";
		String ex2FilePath = "src/main/resources/few-shot-prompt/examples/example2.txt";

		String graphStr = transformListToString(graph);

		String systemMessageTxt = Files.readString(Paths.get(SYSTEM_MESSAGE_RESOURCE_FSP));
		SystemMessage systemMessage = new SystemMessage(systemMessageTxt);

		String userTemplate = Files.readString(Paths.get(USER_MESSAGE_RESOURCE_FSP));
		String userMessageTxt = String.format(userTemplate, nbNode, (nbNode - 1), graphStr);
		UserMessage userMessage = new UserMessage(userMessageTxt);

		UserMessage um1 = new UserMessage(Files.readString(Paths.get(ex1FilePath)));
		SystemMessage sm1 = new SystemMessage("No path found");

		UserMessage um2 = new UserMessage(Files.readString(Paths.get(ex2FilePath)));
		SystemMessage sm2 = new SystemMessage("0 -> 1 -> 2 -> 4 -> 3 -> 4 -> 2 -> 1 -> 0");

		return new Prompt(List.of(systemMessage,um1,sm1,um2,sm2,userMessage));
	}


	/**
	 * Transformer une Liste de List<GraphDatasetElement> en string
	 * @param graph : représente les données du graphe
	 * @return String : représentant la liste de List<GraphDatasetElement>
	 */
	private String transformListToString(List<GraphDatasetElement> graph) {
		return graph.stream()
				.map(g -> String.format("Node1 : %s => Node2 : %s => weight => %f",
						g.getPointA(), g.getPointB(), g.getDistance()))
				.collect(Collectors.joining(" "));
	}

	/**
	 * Permet d'obtenir le path attend
	 * @param pathResult : représente la ligne lue dans le fichier metadata
	 * @param isZeroShotPrompt : booléen permettant de savoir si c'est un zeroShotPrompt ou pas
	 * @return String : représentant le path attendu
	 */
	private String getExpectedShortestPath(PathResult pathResult, Boolean isZeroShotPrompt) {
		List<String> pathElem = isZeroShotPrompt ? pathResult.getShortestPath() : pathResult.getApproximateTSP();
		StringBuilder expectedShortestPath = new StringBuilder();
		for(String elem : pathElem) {
			expectedShortestPath.append(elem);
		}
		return expectedShortestPath.toString().strip();
	}
    
}
