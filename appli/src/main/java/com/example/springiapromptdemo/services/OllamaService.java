package com.example.springiapromptdemo.services;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
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
    
    public List<FinalResult> initChat(String userMeassage) {
    	List<PathResult> pathResults = OllamaUtils.readMetadata();
    	List<FinalResult> resp = new ArrayList<>();
        pathResults.stream().forEach(
                pathResult -> {
                    List<GraphDatasetElement> graph;
					try {					
						graph = OllamaUtils.loadDataSet(pathResult.getGraph_name());
						LLMResponse res = this.callOllama(userMeassage, graph, pathResult.getNbNodes());
						
						//Double score = res.getTotalDistance() - pathResult.getTotalDistance();
						
						StringBuilder sortestPath = new StringBuilder();
						for(String elem : pathResult.getShortestPath()) {
							sortestPath.append(elem);
						}
						sortestPath = new StringBuilder(sortestPath.toString().strip());
						
						FinalResult finalResult = new FinalResult();
        				finalResult.setGraph_name(pathResult.getGraph_name());
        				finalResult.setExpectedShortestPath(sortestPath.toString());
        				finalResult.setShortestPath(res.getShortestPath());
        				finalResult.setExpectedTotalDistance(pathResult.getTotalDistance());
        				//finalResult.setTotalDistance(res.getTotalDistance());
        				//finalResult.setScore(score);
        				
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
     * @param promptString
     * @param graph
     * @return
     */
    private LLMResponse callOllama(String userMeassage, List<GraphDatasetElement> graph, int nbNode){

        log.trace("Construction des prompts systeme et utilisateur...");
        Prompt prompt = augmentSystemPrompt(graph, userMeassage, nbNode);

        log.trace("Appel de Ollama...");

        //log.trace("Sauvegarde de la reponse...");
        //saveResponse(response,dataSet,savedPrompts);

        return this.chatClient.prompt(prompt)
				.call()
				.entity(LLMResponse.class);
    }

    /**
     * Cette methode permet d'ajouter des informations contextuelles au prompt systeme
     *  - Les informations contextuelles sont les elements du dataset
     *  - ET des indications sur la tache à effectuer
     * @param graph
     * @param prompt
     * @return
     */
    private Prompt augmentSystemPrompt(List<GraphDatasetElement> graph, String userMeassage, int nbNode) {
    	
    	SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(
    			
    		"""
				Forget any previous instruction.
				This is a graph of {num_nodes} nodes, labeled from 0 to {final}.Each line represents an edge in the format: initial_node final_node weight. 
				Considering {userMeassage}, your task is to find the path with the minimum total weight from node 0 to node {final}.
				Return as your answer only a sequence of node numbers, in the format: 0 -> x -> y -> ... -> {final} into shortestPath variable. 
				If you find no path, return 'No path found'.
				Also returns the total distance by adding the distance between each node of the shortest path.
			    Do not return anything else. Do not explain. Do not include code.
				{graph_data}	
			"""
		);
		
		return systemPromptTemplate.create(Map.of( "graph_data", graph, "userMeassage", userMeassage, "num_nodes", nbNode, "final", (nbNode-1) ));
		
    }
    
}
