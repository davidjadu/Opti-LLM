package com.example.springiapromptdemo.services;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
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

    public OllamaService(ChatClient.Builder chatClient)
    {
        this.chatClient = chatClient.build();
    }
    
    public List<FinalResult> initChat(String userMessage) {
		
    	List<PathResult> pathResults = OllamaUtils.readMetadata();
    	List<FinalResult> resp = new ArrayList<>();
		
        pathResults.stream().forEach(
                pathResult -> {
					
                    List<GraphDatasetElement> graph;
					FinalResult finalResult = new FinalResult();
					Double totalDistance = null;
					Double score = null;
					
					try {
						
						log.info("=========== > TRAITEMENT DU "+pathResult.getGraph_name());
						graph = OllamaUtils.loadDataSet(pathResult.getGraph_name());
						LLMResponse res = this.callOllama(userMessage, graph, pathResult.getNbNodes());

						String strExpectedShortestPath = getExpectedShortestPath(pathResult);

						Pair<Boolean, List<String>> hallucination = OllamaUtils.isHallucination(res.getShortestPath(), graph);

						if((!hallucination.getFirst())
								&& (OllamaUtils.isExpectedPath(res.getShortestPath(), strExpectedShortestPath))) {
							totalDistance = OllamaUtils.calculTotalDistance(graph, res.getShortestPath());
							score = pathResult.getTotalDistance() - totalDistance;
						} else if ((!hallucination.getFirst())
								&& (!OllamaUtils.isExpectedPath(res.getShortestPath(), strExpectedShortestPath))) {
							totalDistance = OllamaUtils.calculTotalDistance(graph, res.getShortestPath());
							score = !Objects.isNull(pathResult.getTotalDistance())  ? (pathResult.getTotalDistance() - totalDistance) : null;
						} else if (hallucination.getFirst()) {
							finalResult.setHallucinationPaths(hallucination.getSecond());
						}

						finalResult.setGraph_name(pathResult.getGraph_name());
        				finalResult.setExpectedShortestPath(strExpectedShortestPath);
        				finalResult.setShortestPath(res.getShortestPath());
        				finalResult.setExpectedTotalDistance(pathResult.getTotalDistance());
        				finalResult.setTotalDistance(totalDistance);
        				finalResult.setScore(score);
        				
						resp.add(finalResult);

					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
                }
        );

		OllamaUtils.saveResponse(resp);
        return resp;
    }


	/**
     * Cette methode permet d'appeler Ollama
     * @param userMessage
     * @param graph
	 * @param nbNode
     * @return
     */
    private LLMResponse callOllama(String userMessage, List<GraphDatasetElement> graph, int nbNode){

        log.trace("Construction des prompts systeme et utilisateur...");
        Prompt prompt = augmentSystemPrompt(graph, userMessage, nbNode);

        log.trace("Appel de Ollama...");
        return this.chatClient.prompt(prompt)
				.call()
				.entity(LLMResponse.class);
    }

    /**
     * Cette methode permet d'ajouter des informations contextuelles au prompt systeme
     *  - Les informations contextuelles sont les elements du dataset
     *  - ET des indications sur la tache à effectuer
     * @param graph
     * @param userMessage
	 * @param nbNode
     * @return
     */
    private Prompt augmentSystemPrompt(List<GraphDatasetElement> graph, String userMessage, int nbNode) {
    	
    	SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(
    			
    		"""
				Forget any previous instruction.
				This is a graph of {num_nodes} nodes, labeled from 0 to {final}.Each line represents an edge in the format: initial_node final_node weight. 
				Considering {userMessage}, your task is to find the path with the minimum total weight from node 0 to node {final}.
				Return as your answer only a sequence of node numbers, in the format: 0 -> x -> y -> ... -> {final} into shortestPath variable. 
				If you find no path, return 'No path found'.
			    Do not return anything else. Do not explain. Do not include code.
				{graph_data}	
			"""
		);
		
		return systemPromptTemplate.create(Map.of( "graph_data", graph, "userMessage", userMessage, "num_nodes", nbNode, "final", (nbNode-1) ));
		
    }

	private String getExpectedShortestPath(PathResult pathResult) {
		StringBuilder expectedShortestPath = new StringBuilder();
		for(String elem : pathResult.getShortestPath()) {
			expectedShortestPath.append(elem);
		}
		return expectedShortestPath.toString().strip();
	}
    
}
