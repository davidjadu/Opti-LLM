package com.example.springiapromptdemo.services;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Service;

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
    
    public List<LLMResponse> initChat(String userMeassage) {
    	List<PathResult> pathResults = OllamaUtils.readMetadata();
    	List<LLMResponse> resp = new ArrayList<>();
        pathResults.stream().forEach(
                pathResult -> {
                    List<GraphDatasetElement> graph;
					try {					
						graph = OllamaUtils.loadDataSet(pathResult.getGraph_name());
						LLMResponse res = this.callOllama(userMeassage, graph);
						resp.add(res);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
                }
        );
        return resp;
    }

    /**
     * Cette methode permet d'appeler Ollama
     * @param promptString
     * @param graph
     * @return
     */
    private LLMResponse callOllama(String userMeassage, List<GraphDatasetElement> graph){

        log.trace("Construction des prompts systeme et utilisateur...");
        Prompt prompt = augmentSystemPrompt(graph, userMeassage);

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
    private Prompt augmentSystemPrompt(List<GraphDatasetElement> graph, String userMeassage) {
    	
    	SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(

    		"""
				Considering only information provided by the graph: {graph} 
				You will complete the task : {userMeassage}
				Return the result of the shortest path with format: [0, 1, 2, 3] into shortestPath variable. 
				The numbers 0, 1, 2, 3 represent the node numbers of the short path. Also calculate the total distance.	
			"""
		);
		
		return systemPromptTemplate.create(Map.of( "graph", graph, "userMeassage", userMeassage));
		
    }
    
    

    /**
     * Cette methode permet de sauvegarder la reponse de Ollama
     * @param response
     * @param dataSet
     * @param savedPrompts
     */
    //Todo: sauvegarder la réponse dans un fichier csv .
    /*private void saveResponse(String response, DataSet dataSet, LLMPrompt savedPrompts) {
        Double distance = getDataFromResponse(response);

        LLMResponse llmResponse = new LLMResponse();
        llmResponse.setDataSet(dataSet);
        llmResponse.setSystemPromt(savedPrompts.getSystemPrompt());
        llmResponse.setUserData(savedPrompts.getUserData());
        llmResponse.setProvidedDistance(distance);
      //llmResponse.setExpectedDistance(getExpectedDistance(dataSet, savedPrompts));
        llmResponse.setScore(llmResponse.getExpectedDistance() - llmResponse.getProvidedDistance());
        llmResponse.setExecutionDate(new Date());
    }*/

    /**
     * Cette methode permet de calculer la distance attendue
     * @param dataSet
     * @param savedPrompts
     * @return
     */
    //Todo: changer cette methode pour lire la réponse du fichier response.csv
    /*private Double getExpectedDistance(DataSet dataSet, LLMPrompt savedPrompts) {
     //   PathResult shortestPath = graphService.findShortestPath(dataSet, savedPrompts.getStart().toString(), savedPrompts.getEnd().toString());
        return null;
    }*/
    
}
