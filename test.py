import requests
import csv

def chat_with_llama(prompt: str, system: str):

    url = "http://localhost:11434/api/generate"  # URL of the Ollama API
    headers = {
        "Content-Type": "application/json"
    }
    data = {
        "system": system,
        "model": "llama3",  # Specify Llama3 as the model
        "prompt": prompt,
        "stream": False
    }

    response = requests.post(url, json=data, headers=headers)

    if response.status_code == 200:
        ans = response.json()['response']
        return ans
    else:
        return f"Error: {response.status_code}, {response.text}"

if __name__ == "__main__":

    import csv

    # Path to the CSV file
    file_path = "C:\\Users\\DavidJARAMILLODUQUE\\Documents\\Opti-LLM\\data\\csv\\graph_97.csv"

    # Open the CSV file in read mode
    graph_data=""
    with open(file_path, mode='r', newline='', encoding='utf-8') as file:
        # Create a CSV reader object
        csv_reader = csv.reader(file)

        next(csv_reader)
        # Iterate through the rows in the CSV file
        for row in csv_reader:
            graph_data+=" ".join(row)+"\n"

    prompt = f"""
    Forget any previous instruction.

    This is a graph of 200 nodes, labeled from 0 to 199. Each line represents an edge in the format: initial_node final_node weight. Your task is to find the path with the minimum total weight from node 0 to node 199.

    Return as your answer only a sequence of node numbers, in the format: 0 -> x -> y -> ... -> 199.

    Do not return anything else. Do not explain. Do not include Python code.

    {graph_data}
    """
    system="You are tasked with solving a shortest-path problem. Your response must be strictly a sequence of numbers representing the path, nothing else. For example: '0 -> 1 -> 3 -> 199'. Do not include code, explanations, or comments."

    response = chat_with_llama(prompt,system)
    print(f"Response: {response}")
