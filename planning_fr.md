# Planning LLM-opti

- Nom ?
- ## BD
    - Génération de graphes
        - Choisir les distributions de probabilité - méthodes - densité.
            - Mélange des liens ? - Comment implémenter cela dans le LLM ?
        - Comparer les performances entre elles.
    - Peut-être réduire le nombre de points.

- ## V1
    - ### Demande unique :
        - Zero shot prompting :
            - "This is a graph of 200 nodes, labeled from 0 to 199. Each line represents an edge in the format: initial_vertex final_vertex weight. Your task is to find the shortest path from node 0 to node 199. Provide your result as a sequence of node numbers that represent this path.
            [Graph data]
            "
            - Prendre le résultat et le comparer au résultat réel (donné dans la base de données).
                - Si le résultat obtenu n’est pas celui attendu, le chemin trouvé par le LLM est-il un chemin valide dans le graphe entre 0 et 199 ?
                - Si oui, quelle est la longueur de ce chemin par rapport au chemin optimal ?

        - Few shot prompting : Ajouter des exemples (cela améliore généralement les performances).
            - Mettre le même prompt + quelques exemples de graphes et les solutions attendues.

    - ### Optimisation du prompt :
        - Meta prompting (Il pourrait être intéressant de jouer avec le paramètre température ici) :
            - Question based prompting
                -  ```
                    """You are a prompt engineer tasked with optimizing a prompt for solving a version of the Traveling Salesman Problem. The objective is to find the shortest path (minimum weight sum) between node 0 and node 199 in a weighted graph.

                    The graph is represented in CSV format with edges in the form of: initial_vertex, final_vertex, weight where:

                        initial_vertex and final_vertex are integers between 0 and 199 (inclusive).
                        weight is a positive number representing the cost of traversal.

                    Your job is to iteratively improve the provided prompt to maximize its success rate, as evaluated on a reference database of graphs.

                    Query Structure:

                    {prompt to optimize}
                    [Graph Data]

                    Success Criteria:
                    The Success Rate measures the percentage of correct shortest-path solutions produced by the LLM using the prompt and provided graph data.

                    Example Prompts with Success Rates:

                    "This is a graph of 200 nodes, labeled from 0 to 199. Each line represents an edge in the format: initial_vertex final_vertex weight. Your task is to find the shortest path from node 0 to node 199. Provide your result as a sequence of node numbers that represent this path."
                    Success Rate: ##

                    "This is a graph with 200 nodes labeled from 0 to 199. The edges of the graph are given in the format: initial_vertex final_vertex weight. Your task is to calculate the shortest path from node 0 to node 199, minimizing the total weight. Please return the path as a list of node labels separated by spaces, for example: 0 3 15 199."
                    Succes Rate = ##

                    "You are solving a shortest-path problem in a graph with 200 nodes labeled 0 to 199. Each edge is described as initial_vertex final_vertex weight. For example, given the edges:
                    0 1 3
                    1 2 2
                    2 199 5
                    the shortest path from 0 to 199 is 0 1 2 199 with a total weight of 10. Now, analyze the graph data below to compute the shortest path from node 0 to node 199. Return the path as a space-separated sequence of nodes."
                    Success Rate = ##

                    Your goal is to improve this Success Rate by refining the prompt. Consider the clarity, specificity, and structure of the prompt when making improvements. Propose three new possible prompts"""


                    ```

        - Answer Based prompting (Chaîne de prompts)
            - ...

    - ### Optimisation itérative
        - Nous devrions probablement prendre l'un des meilleurs prompts issus de l'analyse.
            - ```
                """Your job is to solve a version of the Traveling Salesman Problem. The objective is to find the shortest path (minimum weight sum) between node 0 and node 199 in a weighted graph.

                The graph is represented in CSV format with edges in the form of: initial_vertex, final_vertex, weight where:

                [Graph Data]

                We are giving you some examples of paths on the graph with their respecting distance:

                [path1] - distance = ##
                [path2] - distance = ##
                [path3] - distance = ##

                Your job is to find a path on the graph that has lower distance that all of the path's given.
                """
                ```

        - Une fois que le programme retourne un chemin, nous devons calculer sa longueur (et vérifier s’il s’agit d’un chemin valide dans le graphe). Si la distance est plus courte, nous l’ajoutons comme un exemple supplémentaire et renvoyons le prompt au programme.
        - Cela devra être fait pour chaque graphe dans la base de données.
        - Nous voulons observer l'évolution au cours de l'optimisation : quelle est la distance minimale à chaque itération ? Si la distance minimale est atteinte (fournie dans la base de données), combien d’itérations ont été nécessaires ? Probablement environ 1000 itérations devraient suffire.

- ## V2
    - Entraînement du LLM.
