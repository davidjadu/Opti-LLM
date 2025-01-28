import requests
import csv
from tqdm import tqdm
import random

def chat_with_llama(prompt: str, system: str = ""):

    url = "http://localhost:8000/api/generate"
    headers = {
        "Content-Type": "application/json"
    }
    data = {
        "system": system,
        "model": "llama3.2:3b",
        "prompt": prompt,
        "stream": False,
        "context": [0],
        "options": {"seed": 42, "temperature": 0.0, "num_ctx": 5000}
    }

    response = requests.post(url, json=data, headers=headers)

    if response.status_code == 200:
        ans = response.json()['response']
        if response.json().get('prompt_eval_count', 0) >= 5000:
            print(f"Prompt evaluation count is bigger than context: {response.json()['prompt_eval_count']}")
        return ans
    else:
        return f"Error: {response.status_code}, {response.text}"

prompt = """Question: Find a path from node 0 to node 4 in the following graph:
0 1 74
2 3 28
Solution: From node 0, we can visit nodes 1 because the edges 0,1,74 are in the graph.
Updating distance for node 1: path 0 -> 1, distance = 0 + 74 = 74.
From node 1, the node with the smallest length in the queue, we can visit nodes 0 because the edges 1,0,74 are in the graph.
The distance to nodes 0 is already lower through another path, so we don't update it.
We ran out of nodes in the queue; hence, no path exists to the target node.


Question: Find a path from node 0 to node 4 in the following graph:
0 4 26
Solution: From node 0, we can visit nodes 4 because the edges 0,4,26 are in the graph.
Updating distance for node 4: path 0 -> 4, distance = 0 + 26 = 26.
Every path that has been traveled has bigger distance than the one to 4, therefore we reached the target node 4 with a total distance of 26.
FINAL ANSWER: Path: 0 -> 4.


Question: Find a path from node 0 to node 4 in the following graph:
2 3 52"""

response = chat_with_llama(prompt)
print(response)