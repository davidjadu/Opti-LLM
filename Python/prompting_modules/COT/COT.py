import os
from dotenv import load_dotenv
import pandas as pd
import requests
from COT_genertaor import Question, Solution
import random
import csv
from tqdm import tqdm

CONTEXT_NUMBER = 100_000
def chat_with_llama(prompt: str, system: str = ""):

    url = f"{os.getenv("OLLAMA_URL")}/api/generate"
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
        print(f"Error: {response.status_code}, {response.text}")

def get_metadata():
    metadata_file_path = f"{os.getenv('PATH_TO_DATA')}/metadata.csv"
    metadata_df = pd.read_csv(metadata_file_path)
    return metadata_df

def get_similar_graphs(graph_data, df):
    matching_graphs = df[
        (df['num_nodes'] == graph_data['num_nodes']) &
        (df['sigma'] == graph_data['sigma']) &
        (df['average edges'] == graph_data['average edges']) &
        (df['graph_id'] != graph_data['graph_id'])
    ]
    return matching_graphs['graph_id'].tolist()

if __name__=="__main__":
    load_dotenv()
    df = get_metadata()[:10]
    results = []
    error_counter = 0

    results_file_path = f"{os.getenv('PATH_TO_DATA')}/results/results_COT.csv"
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

    for index, row in tqdm(df.iterrows(),total=10):
        if row['graph_id'] in existing_results:
            continue

        similar_graphs = random.sample(get_similar_graphs(row, df),2)
        prompt = ""
        for graph_id in similar_graphs:
            prompt += f"Question: {Question(graph_id).get()}Solution: {Solution(graph_id).get()}\n\n"
        prompt += f"Question: {Question(row['graph_id']).get()}"

        response, tokens = chat_with_llama(prompt)
        results.append({'graph_id': row['graph_id'], 'response': response, 'tokens': tokens})

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
