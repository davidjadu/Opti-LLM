# Planning LLM-opti

- Name?
- ## BD
    - Graph Generation
        - Chose probability distributions - methods - density.
            - Edge shuffling? - How to implement this in the LLM
        - Compare performance among them.
    - maybe less points.
- ## V1
    - ### One time prompting:
        - Zero shot prompting:
            - "This is a graph of 200 nodes, labeled from 0 to 199. Each line represents an edge in the format: initial_vertex final_vertex weight. Your task is to find the shortest path from node 0 to node 199. Provide your result as a sequence of node numbers that represent this path.
            [Graph data]
            "
            - Take the result and compare it to the actual result (given in the database).
                - If we don't get the expected result, is the path obtained by the LLM an actual path on the graph between 0 to 199?
                - If it is how long is the path compare to the optimal one?
        - Few shot prompting: Add examples (This usually improves performance)
            - Put the same prompt + This are some examples of graphs and the expected solutions.
    - ### Prompt optimization:
        - Meta prompting (Here it might be interesting to play with temperature) :
            - Question based prompting
                -   ```
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
            - Answer Based prompting (Chain of prompt)
                - ...
    - ### Iterative optimization
        - We should probably take one of the best prompt from the prompt analysis.
            -  ```
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
        - Once the program returns a path. We need to compute the length (and whether is an actual path on the graph) if the distance is shorter we add it as one of the example paths and send the prompt to the program again.
        - This would have to be done with every graph in the db.
        - We want to see the evolution along the optimization: what's the minimal distance at every iteration ? If it reaches the minimum (provided in the db) how many steps did it took. Probably around a 1000 iterations should be ok.
- ## V2
    - LLM training.
