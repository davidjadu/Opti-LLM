from datetime import datetime
import requests
import csv
from tqdm import tqdm
import os

CONTEXT_NUMBER = 100_000

def chat_with_llama(prompt: str, system: str):

    url = F"{os.getenv("OLLAMA_URL")}/api/generate"
    headers = {
        "Content-Type": "application/json"
    }
    data = {
        "system": system,
        "model": "llama3.2:3b",
        "prompt": prompt,
        "stream": False,
	"context": [],
        "options": {"seed":42, "temperature": 0.0, "num_ctx": CONTEXT_NUMBER}
    }

    response = requests.post(url, json=data, headers=headers)

    if response.status_code == 200:
        response_json= response.json()
        ans = response_json['response']
        tokens = response_json['prompt_eval_count']

        return ans, tokens
    else:
        return f"Error: {response.status_code}, {response.text}"

def get_metadata():
    metadata_file_path = f"{os.getenv('PATH_TO_DATA')}/metadata.csv"
    metadata = []
    with open(metadata_file_path, mode='r', newline='', encoding='utf-8') as file:
        csv_reader = csv.DictReader(file)
        for row in csv_reader:
            metadata.append(row)
    return metadata

def get_graph_data(graph_id: int):
    graph_file_path = f"{os.getenv('PATH_TO_DATA')}/graphs/graph_{graph_id}.csv"
    graph_data = ""
    with open(graph_file_path, mode='r', newline='', encoding='utf-8') as file:
        csv_reader = csv.reader(file)
        next(csv_reader)
        for row in csv_reader:
            graph_data += " ".join(row) + "\n"
    return graph_data

def main():
    metadata = get_metadata()
    results = []

    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")

    results_file_path = f"{os.getenv('PATH_TO_DATA')}/results/results_zero_shot_{timestamp}.csv"
    existing_results = []

    try:
        with open(results_file_path, mode='r', newline='', encoding='utf-8') as file:
            csv_reader = csv.DictReader(file)
            for row in csv_reader:
                existing_results.append(int(row['graph_id']))
    except FileNotFoundError:
        with open(results_file_path, mode='w', newline='', encoding='utf-8') as file:
            fieldnames = ['graph_id', 'response', 'tokens']
            csv_writer = csv.DictWriter(file, fieldnames=fieldnames)
            csv_writer.writeheader()

    error_counter = 0

    for row in tqdm(metadata, desc="Processing graphs"):
        graph_id = int(row['graph_id'])
        num_nodes = int(row['num_nodes'])

        if graph_id in existing_results:
            continue

        graph_data = get_graph_data(graph_id)

        final = num_nodes - 1

        prompt = f"""
        Forget any previous instruction.

        This is a graph of {num_nodes} nodes, labeled from 0 to {final}. Each line represents an edge in the format: initial_node final_node weight. Your task is to find the path with the minimum total weight from node 0 to node {final}.

        Return as your answer only a sequence of node numbers, in the format: 0 -> x -> y -> ... -> {final}. If you find no path, return 'No path found'.

        Do not return anything else. Do not explain. Do not include Python code.

        {graph_data}
        """
        system = f"You are tasked with solving a shortest-path problem. Your response must be strictly a sequence of numbers representing the path or 'No path found', nothing else. For example: '0 -> 1 -> 3 -> {final}' or 'No path found' are valid responses. Do not include code, explanations, or comments."

        response, tokens = chat_with_llama(prompt, system)

        results.append({'graph_id': graph_id, 'response': response, 'tokens': tokens})
        if tokens >=CONTEXT_NUMBER:
            error_file_path = "errors.txt"
            with open(error_file_path, 'a') as file:
                file.write(f"{graph_id=} answer might not be useful. The number of tokens in the prompt was {tokens=}")
            error_counter +=1
            if error_counter>=10:
                break

        with open(results_file_path, mode='a', newline='', encoding='utf-8') as file:
            fieldnames = ['graph_id', 'response', 'tokens']
            csv_writer = csv.DictWriter(file, fieldnames=fieldnames, quoting = csv.QUOTE_ALL)
            csv_writer.writerow(results[-1])

    print(f"Results saved to {results_file_path}")

if __name__ == "__main__":
    main()


