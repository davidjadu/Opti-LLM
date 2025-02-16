# SpringIAPromptDemo


Changements à faire:
    + Lire les fichiers de résultats en meme temps que les fichiers de graphes
    + Recuperer le prompte à partire de l'api
    + sauvegarder le resultat dans un fichier csv 


callapi(String prompt){
List<ResultPath> resultpath=readMetadata();
List<ResultPath> resultat=new ArrayList<ResultPath>();

    for(ResultPath result:resultpath){
        List<GraphDataSetElement> graph=  readGraph(resultpath.getGraphId());
     Result result=   callOllama(graph,prompt); 
    Double score= resultPath.TotalDistance-result.TotalDistance;
    reslutpath.setScore(score);
    resultat.setPrompt(finalPrompt);
    resultat.add(resultpath);
    }
save result(resultat);
}
