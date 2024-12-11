import networkx as nx
from networkx import NetworkXNoPath
import numpy as np
import os
import random

from itertools import product

from tqdm import tqdm
import csv

def generate_graphs():

    weight_range = (0,100)
    node_list = [5,10,15,20,50,100,150,200]
    sigma_list = [1,2,3,5,8,13,21,34]
    average_edges_list = [1,2,3,5,8,13,21]
    number_of_graphs = 10

    output_dir = f"data/graphs"
    os.makedirs(output_dir, exist_ok=True)

    graph_id = 0

    metadata=[]

    for num_nodes, sigma, average_edges, _ in tqdm(product(node_list,sigma_list,average_edges_list, range(number_of_graphs))):
        G = nx.Graph()
        G.add_nodes_from(range(num_nodes))

        for n in range(num_nodes):
            for m in range(n + 1, num_nodes):  # Avoid self-loops and duplicate edges
                prob = average_edges*1/np.sqrt(np.pi*sigma**2) * np.exp(-((m - n) ** 2) / (2 * sigma ** 2))
                if np.random.rand() < prob:  # Add edge with probability 'prob'
                    G.add_edge(n, m)

        for u, v in G.edges():
            G[u][v]['weight'] = random.randint(weight_range[0], weight_range[1])

        try:
            path_length, shortest_path = nx.single_source_dijkstra(G,source=0,target=num_nodes-1,weight='weight')

        except NetworkXNoPath:
            path_length,shortest_path = '-','-'

        try:
            tsp_path = nx.approximation.traveling_salesman_problem(G)
            tsp_length = nx.path_weight(G, tsp_path, weight='weight')
        except:
            tsp_path,tsp_length = '-', '-'

        randomize_edges_list=list(G.edges)
        random.shuffle(randomize_edges_list)

        with open(f"{output_dir}/graph_{graph_id}.csv", "w", newline="") as file:
            writer = csv.writer(file)
            writer.writerow(["Node1", "Node2", "Weight"])  # Optional: Write header
            for u, v in randomize_edges_list:
                weight = G[u][v]['weight']  # Access weight from graph
                writer.writerow([u, v, weight])

        metadata.append([graph_id, num_nodes, sigma, average_edges, shortest_path, path_length, tsp_path, tsp_length])

        graph_id +=1


    with open(f"{output_dir}/min_path", "w", newline="") as file:
        writer = csv.writer(file)
        writer.writerow(["graph_id","num_nodes","sigma","average edges", "dijkstra path", "dijkstra length", "approximate_tsp", "approximate_tsp_length"])
        writer.writerows(metadata)

if __name__=='__main__':
    # Parameters
    generate_graphs()

