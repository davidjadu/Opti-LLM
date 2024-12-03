import networkx as nx
from networkx import NetworkXNoPath
import numpy as np
import os
import random
import torch
from tqdm import tqdm
import csv

def generate_graphs(num_graphs, num_nodes, average_edges, weight_range, output_dir, sigma):
    os.makedirs(output_dir, exist_ok=True)
    dijkstra_distance = []
    for i in tqdm(range(num_graphs)):
        # Create a random graph
        # G = nx.gnp_random_graph(n=num_nodes, p=edge_probability, directed=False)

        # # Add weights to the edges
        # for u, v in G.edges():
        #     G[u][v]['weight'] = random.randint(weight_range[0], weight_range[1])

        G, p = get_valid_graph(num_nodes, average_edges, weight_range, sigma)
        randomize_edges_list=list(G.edges)
        random.shuffle(randomize_edges_list)

        with open(f"{output_dir}/graph_{i}.csv", "w", newline="") as file:
            writer = csv.writer(file)
            writer.writerow(["Node1", "Node2", "Weight"])  # Optional: Write header
            for u, v in randomize_edges_list:
                weight = G[u][v]['weight']  # Access weight from graph
                writer.writerow([u, v, weight])
        di=None
        try:
            di=nx.dijkstra_path(G,source=0,target=num_nodes-1,weight='weight')
        except NetworkXNoPath:
            pass

        dijkstra_distance.append([i,di])

    with open(f"{output_dir}/min_path", "w", newline="") as file:
        writer = csv.writer(file)
        writer.writerows(dijkstra_distance)

def get_valid_graph(num_nodes, average_edges, weight_range, sigma):
    while True:
        G = nx.Graph()
        G.add_nodes_from(range(num_nodes))

        for n in range(num_nodes):
            for m in range(n + 1, num_nodes):  # Avoid self-loops and duplicate edges
                prob = average_edges* 1/np.sqrt(np.pi*sigma**2) * np.exp(-((m - n) ** 2) / (2 * sigma ** 2))
                if np.random.rand() < prob:  # Add edge with probability 'prob'
                    G.add_edge(n, m)

        for u, v in G.edges():
            G[u][v]['weight'] = random.randint(weight_range[0], weight_range[1])

        try:
            shortest_path = nx.dijkstra_path(G,source=0,target=num_nodes-1,weight='weight')
            return G, shortest_path
        except NetworkXNoPath:
            pass

if __name__=='__main__':
    # Parameters
    num_graphs = 100           # Number of graphs to generate
    num_nodes = 200         # Number of nodes in each graph
    average_edges = 2    # Probability of edge creation (use 1.0 for a complete graph)
    weight_range = (1, 100)        # Weight range (min, max)
    sigma = 10
    output_dir = f"data/csv"      # Directory to save the graphs

    # Generate and save graphs
    generate_graphs(num_graphs, num_nodes, average_edges, weight_range, output_dir, sigma)

