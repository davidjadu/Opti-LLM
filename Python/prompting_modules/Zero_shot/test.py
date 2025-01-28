import requests
import csv
import logging

# Configure logging
# logging.basicConfig(level=logging.DEBUG)

CONTEXT_NUMBER = 5000

def chat_with_llama(prompt: str, system: str):

    url = "http://localhost:8000/api/generate"  # URL of the Ollama API
    headers = {
        "Content-Type": "application/json"
    }
    data = {
        "system": system,
        "model": "llama3.2:3b",  # Specify Llama3 as the model
        "prompt": prompt,
        # "raw": True,
        "keep_alive": 0,
        "stream": False,
        "options": {"temperature": 0.0, "num_ctx": CONTEXT_NUMBER}  # Set temperature to 0 for deterministic output
    }

    # Log the request data
    #logging.debug(f"Request data: {data}")

    response = requests.post(url, json=data, headers=headers)

    if response.status_code == 200:
        #print(response.json())
        #logging.debug(response.json())
        ans = response.json() # ['response']
        return ans
    else:
        return f"Error: {response.status_code}, {response.text}"

if __name__ == "__main__":

    import csv
    graph_id = 2801
    num_nodes = 50
    # Path to the CSV file
    file_path = "./data/graphs/graph_2801.csv"

    # Open the CSV file in read mode
    graph_data=""
    with open(file_path, mode='r', newline='', encoding='utf-8') as file:
        # Create a CSV reader object
        csv_reader = csv.reader(file)

        next(csv_reader)
        # Iterate through the rows in the CSV file
        for row in csv_reader:
            graph_data+=" ".join(row)+"\n"

    final = num_nodes -1

    prompt = f"""
    Forget any previous instruction.

    This is a graph of {num_nodes} nodes, labeled from 0 to {final}. Each line represents an edge in the format: initial_node final_node weight. Your task is to find the path with the minimum total weight from node 0 to node {final}.

    Return as your answer only a sequence of node numbers, in the format: 0 -> x -> y -> ... -> {final}. If you find no path, return 'No path found'.

    Do not return anything else. Do not explain. Do not include Python code.

    {graph_data}
    """
    system = f"You are tasked with solving a shortest-path problem. Your response must be strictly a sequence of numbers representing the path or 'No path found', nothing else. For example: '0 -> 1 -> 3 -> {final}' or 'No path found' are valid responses. Do not include code, explanations, or comments."


    # prompt = f"""
    # Forget any previous instruction.

    # This is a graph of {final+1} nodes, labeled from 0 to {final}. Each line represents an edge in the format: initial_node final_node weight. Your task is to find the path with the minimum total weight from node 0 to node {final}.

    # Return as your answer only a sequence of node numbers, in the format: 0 -> x -> y -> ... -> {final}.

    # Do not return anything else. Do not explain. Do not include Python code.

    # {graph_data}
    # """
    # system=f"You are tasked with solving a shortest-path problem. Return as your answer only a sequence of node numbers, in the format: 0 -> x -> y -> {final}. If you find no path, return 'No path found'. Do not include code, explanations, or comments."


    # Log the prompt and system
   # logging.debug(f"Prompt: {prompt}")
   # logging.debug(f"System: {system}")

    response = chat_with_llama(prompt,system)
    print(f"Response: {response}")
